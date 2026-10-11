package com.toyland.inventory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toyland.audit.AuditService;
import com.toyland.auth.CurrentUserService;
import com.toyland.catalog.Product;
import com.toyland.catalog.ProductVariant;
import com.toyland.catalog.ProductVariantRepository;
import com.toyland.common.dto.PageResponse;
import com.toyland.common.exception.ApiException;
import com.toyland.common.util.TextUtils;
import com.toyland.inventory.dto.InventoryDtos.AdjustRequest;
import com.toyland.inventory.dto.InventoryDtos.MovementDto;
import com.toyland.inventory.dto.InventoryDtos.ReceiptDto;
import com.toyland.inventory.dto.InventoryDtos.ReceiptItemDto;
import com.toyland.inventory.dto.InventoryDtos.ReceiptItemRequest;
import com.toyland.inventory.dto.InventoryDtos.ReceiptRequest;
import com.toyland.inventory.dto.InventoryDtos.StockRow;
import com.toyland.notification.NotificationService;
import com.toyland.supplier.Supplier;
import com.toyland.supplier.SupplierService;

/**
 * Moi thay doi ton kho deu di qua service nay de:
 *  (1) cap nhat ton bien the + tong ton san pham, (2) ghi nhat ky stock_movements.
 */
@Service
@Transactional
public class InventoryService {

	public static final int LOW_STOCK = 5;
	private static final DateTimeFormatter CODE_FMT = DateTimeFormatter.ofPattern("yyMMddHHmmss");

	private final ProductVariantRepository variantRepository;
	private final StockMovementRepository movementRepository;
	private final StockReceiptRepository receiptRepository;
	private final CurrentUserService currentUser;
	private final SupplierService supplierService;
	private final AuditService audit;
	private final NotificationService notificationService;

	public InventoryService(ProductVariantRepository variantRepository, StockMovementRepository movementRepository,
			StockReceiptRepository receiptRepository, CurrentUserService currentUser, SupplierService supplierService,
			AuditService audit, NotificationService notificationService) {
		this.variantRepository = variantRepository;
		this.movementRepository = movementRepository;
		this.receiptRepository = receiptRepository;
		this.currentUser = currentUser;
		this.supplierService = supplierService;
		this.audit = audit;
		this.notificationService = notificationService;
	}

	/**
	 * Thay doi ton kho (delta am = xuat). Nem loi neu ton khong du.
	 * Bien the co @Version -> 2 giao dich dong thoi cung sua se bi phat hien (optimistic lock).
	 */
	public void change(ProductVariant v, int delta, MovementType type, String refCode, String note) {
		int newQty = v.getStockQuantity() + delta;
		if (newQty < 0) {
			throw ApiException.badRequest("OUT_OF_STOCK", "Sản phẩm \"" + v.getProduct().getName()
					+ (v.getDisplayName() != null ? " - " + v.getDisplayName() : "") + "\" chỉ còn " + v.getStockQuantity() + " trong kho");
		}
		int oldQty = v.getStockQuantity();
		v.setStockQuantity(newQty);
		if (type == MovementType.SALE && v.isActive() && oldQty > LOW_STOCK && newQty <= LOW_STOCK) {
			notificationService.notifyStaff("LOW_STOCK", newQty == 0 ? "Hết hàng: " + v.getSku() : "Sắp hết hàng: " + v.getSku(),
					v.getProduct().getName() + (v.getDisplayName() != null ? " - " + v.getDisplayName() : "") + " chỉ còn " + newQty,
					"/admin/inventory?lowStock=true");
		}
		Product p = v.getProduct();
		if (v.isActive()) {
			p.setTotalStock(Math.max(0, p.getTotalStock() + delta));
		}
		StockMovement m = new StockMovement();
		m.setVariant(v);
		m.setSku(v.getSku());
		m.setType(type);
		m.setQuantityChange(delta);
		m.setBalanceAfter(newQty);
		m.setRefCode(refCode);
		m.setNote(note);
		m.setCreatedBy(currentUser.actorName());
		movementRepository.save(m);
	}

	@Transactional(readOnly = true)
	public PageResponse<StockRow> list(String keyword, boolean lowOnly, int page, int size) {
		String kw = TextUtils.isBlank(keyword) ? null : TextUtils.normalize(keyword);
		return PageResponse.of(variantRepository.searchInventory(kw, lowOnly ? LOW_STOCK : null,
				PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(Sort.Direction.ASC, "stockQuantity").and(Sort.by("id")))), v -> {
					Product p = v.getProduct();
					String img = v.getImageUrl() != null ? v.getImageUrl() : (p.getImages().isEmpty() ? null : p.getImages().get(0).getUrl());
					return new StockRow(v.getId(), p.getId(), p.getName(), p.getSlug(), v.getSku(), v.getDisplayName(), img,
							v.getStockQuantity(), v.getPrice(), v.getCostPrice(), v.getStockQuantity() <= LOW_STOCK);
				});
	}

	public void adjust(AdjustRequest r) {
		ProductVariant v = variantRepository.findWithProduct(r.variantId()).orElseThrow(() -> ApiException.notFound("biến thể", r.variantId()));
		int delta = r.newQuantity() - v.getStockQuantity();
		if (delta == 0) {
			return;
		}
		String note = TextUtils.isBlank(r.note()) ? "Điều chỉnh kiểm kê" : r.note().trim();
		change(v, delta, MovementType.ADJUSTMENT, "DC-" + LocalDateTime.now().format(CODE_FMT), note);
		audit.log("ADJUST", "INVENTORY", v.getSku(), "Kiểm kê " + v.getSku() + ": " + (v.getStockQuantity() - delta) + " → "
				+ v.getStockQuantity() + " (" + note + ")");
	}

	@Transactional(readOnly = true)
	public PageResponse<MovementDto> movements(Long variantId, MovementType type, String keyword, int page, int size) {
		String kw = TextUtils.isBlank(keyword) ? null : keyword.trim().toLowerCase();
		return PageResponse.of(movementRepository.search(variantId, type, kw, PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100))), m -> new MovementDto(
				m.getId(), m.getVariant() == null ? null : m.getVariant().getId(), m.getSku(), m.getType(), m.getQuantityChange(),
				m.getBalanceAfter(), m.getRefCode(), m.getNote(), m.getCreatedBy(), m.getCreatedAt()));
	}

	public ReceiptDto createReceipt(ReceiptRequest r) {
		StockReceipt receipt = new StockReceipt();
		String code;
		do {
			code = "PN-" + LocalDateTime.now().format(CODE_FMT) + ThreadLocalRandom.current().nextInt(10, 100);
		} while (receiptRepository.existsByCode(code));
		receipt.setCode(code);
		if (r.supplierId() != null) {
			Supplier s = supplierService.requireActive(r.supplierId());
			receipt.setSupplierRef(s);
			receipt.setSupplier(s.getName());
		} else {
			receipt.setSupplier(TextUtils.trimToNull(r.supplier()));
		}
		receipt.setNote(TextUtils.trimToNull(r.note()));
		receipt.setCreatedBy(currentUser.actorName());
		int totalQty = 0;
		BigDecimal totalCost = BigDecimal.ZERO;
		for (ReceiptItemRequest it : r.items()) {
			ProductVariant v = variantRepository.findWithProduct(it.variantId())
					.orElseThrow(() -> ApiException.notFound("biến thể", it.variantId()));
			StockReceiptItem item = new StockReceiptItem();
			item.setReceipt(receipt);
			item.setVariant(v);
			item.setSku(v.getSku());
			String pname = v.getProduct().getName() + (v.getDisplayName() != null ? " - " + v.getDisplayName() : "");
			item.setProductName(pname.length() > 300 ? pname.substring(0, 300) : pname);
			item.setQuantity(it.quantity());
			item.setUnitCost(it.unitCost());
			receipt.getItems().add(item);
			totalQty += it.quantity();
			totalCost = totalCost.add(it.unitCost().multiply(BigDecimal.valueOf(it.quantity())));
			v.setCostPrice(it.unitCost()); // cap nhat gia von moi nhat
			change(v, it.quantity(), MovementType.IMPORT, code, receipt.getSupplier());
		}
		receipt.setTotalQuantity(totalQty);
		receipt.setTotalCost(totalCost);
		StockReceipt saved = receiptRepository.save(receipt);
		audit.log("CREATE", "STOCK_RECEIPT", code, "Tạo phiếu nhập " + code + " (" + totalQty + " sp"
				+ (receipt.getSupplier() != null ? ", NCC " + receipt.getSupplier() : "") + ")");
		return toDto(saved);
	}

	@Transactional(readOnly = true)
	public PageResponse<ReceiptDto> receipts(Long supplierId, int page, int size) {
		PageRequest pr = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
		return PageResponse.of(supplierId == null ? receiptRepository.findAllByOrderByIdDesc(pr)
				: receiptRepository.findBySupplierRefIdOrderByIdDesc(supplierId, pr), this::toDto);
	}

	@Transactional(readOnly = true)
	public ReceiptDto receipt(Long id) {
		return toDto(receiptRepository.findById(id).orElseThrow(() -> ApiException.notFound("phiếu nhập", id)));
	}

	private ReceiptDto toDto(StockReceipt r) {
		List<ReceiptItemDto> items = r.getItems().stream().map(i -> new ReceiptItemDto(
				i.getVariant() == null ? null : i.getVariant().getId(), i.getSku(), i.getProductName(), i.getQuantity(), i.getUnitCost())).toList();
		return new ReceiptDto(r.getId(), r.getCode(), r.getSupplierRef() == null ? null : r.getSupplierRef().getId(),
				r.getSupplier(), r.getNote(), r.getTotalQuantity(), r.getTotalCost(),
				r.getCreatedBy(), r.getCreatedAt(), items);
	}
}
