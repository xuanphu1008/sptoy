package com.toyland.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toyland.catalog.CatalogApi;
import com.toyland.catalog.ProductVariant;
import com.toyland.common.exception.ApiException;
import com.toyland.common.util.TextUtils;
import com.toyland.inventory.InventoryService;
import com.toyland.inventory.MovementType;

/**
 * Dat hang do doi tac goi API. Khac don cua khach: khong co gio hang, khong voucher/diem thuong,
 * phi van chuyen co dinh, thanh toan do doi tac tu doi soat (PaymentMethod.PARTNER).
 */
@Service
public class PartnerOrderService implements PartnerOrderApi {

	private static final DateTimeFormatter CODE_FMT = DateTimeFormatter.ofPattern("yyMMdd");
	private static final int MAX_LINES = 50;
	private static final int MAX_QTY = 99;

	private final CatalogApi catalog;
	private final InventoryService inventory;
	private final OrderRepository orders;
	private final OrderWorkflow workflow;

	@Value("${app.partner.shipping-fee:30000}")
	private BigDecimal shippingFee;

	public PartnerOrderService(CatalogApi catalog, InventoryService inventory, OrderRepository orders, OrderWorkflow workflow) {
		this.catalog = catalog;
		this.inventory = inventory;
		this.orders = orders;
		this.workflow = workflow;
	}

	@Override
	@Transactional
	public PartnerOrder create(Long partnerId, String partnerName, Command c) {
		String ref = TextUtils.trimToNull(c.externalRef());
		if (ref == null || ref.length() > 64) {
			throw ApiException.badRequest("PARTNER_REF_INVALID", "externalRef là bắt buộc và tối đa 64 ký tự");
		}
		Optional<Order> dup = orders.findByPartnerIdAndPartnerRef(partnerId, ref);
		if (dup.isPresent()) {
			throw new ApiException(HttpStatus.CONFLICT, "PARTNER_REF_DUPLICATE",
					"externalRef \"" + ref + "\" đã có đơn " + dup.get().getOrderCode());
		}
		if (c.items() == null || c.items().isEmpty() || c.items().size() > MAX_LINES) {
			throw ApiException.badRequest("PARTNER_ITEMS_INVALID", "Đơn phải có từ 1 đến " + MAX_LINES + " dòng hàng");
		}
		// gop cac dong cung SKU, kiem tra so luong
		Map<String, Integer> qty = new LinkedHashMap<>();
		for (Line l : c.items()) {
			String sku = TextUtils.trimToNull(l.sku());
			if (sku == null || l.quantity() < 1 || l.quantity() > MAX_QTY) {
				throw ApiException.badRequest("PARTNER_ITEMS_INVALID", "Mỗi dòng cần sku và số lượng từ 1 đến " + MAX_QTY);
			}
			qty.merge(sku, l.quantity(), Integer::sum);
		}
		Order o = new Order();
		o.setOrderCode(newCode());
		o.setPartnerId(partnerId);
		o.setPartnerRef(ref);
		o.setReceiverName(c.receiverName().trim());
		o.setPhone(c.phone().trim());
		o.setProvinceName(c.province().trim());
		o.setDistrictName(c.district().trim());
		o.setWardName(c.ward().trim());
		o.setAddressDetail(c.addressDetail().trim());
		o.setNote(TextUtils.trimToNull(c.note()));
		o.setPaymentMethod(PaymentMethod.PARTNER);
		o.setPaymentStatus(PaymentStatus.UNPAID);
		o.setStatus(OrderStatus.PENDING);
		BigDecimal subtotal = BigDecimal.ZERO;
		for (Map.Entry<String, Integer> e : qty.entrySet()) {
			ProductVariant v = catalog.findSellableVariant(e.getKey())
					.orElseThrow(() -> new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "SKU_NOT_SELLABLE",
							"Mã hàng " + e.getKey() + " không tồn tại hoặc đã ngừng bán"));
			OrderItem oi = new OrderItem();
			oi.setProduct(v.getProduct());
			oi.setVariant(v);
			oi.setProductName(v.getProduct().getName());
			oi.setVariantName(v.getDisplayName());
			oi.setSku(v.getSku());
			oi.setImageUrl(v.getImageUrl());
			oi.setUnitPrice(v.getPrice());
			oi.setOriginalPrice(v.getPrice());
			oi.setCostPrice(v.getCostPrice());
			oi.setQuantity(e.getValue());
			o.addItem(oi);
			subtotal = subtotal.add(v.getPrice().multiply(BigDecimal.valueOf(e.getValue())));
		}
		o.setSubtotal(subtotal);
		o.setShippingFee(shippingFee);
		o.setTotalAmount(subtotal.add(shippingFee));
		o.addHistory(null, OrderStatus.PENDING, "Đặt hàng qua API đối tác " + partnerName, "partner:" + partnerName);
		o = orders.save(o);
		// tru kho: kiem tra lai ton, khoa lac quan tren bien the; thieu hang thi nem loi va rollback ca don
		for (OrderItem oi : o.getItems()) {
			inventory.change(oi.getVariant(), -oi.getQuantity(), MovementType.SALE, o.getOrderCode(), "Xuất bán cho đối tác " + partnerName);
		}
		workflow.notifyNewOrder(o);
		return view(o);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<PartnerOrder> find(Long partnerId, String orderCode) {
		return orders.findByOrderCodeAndPartnerId(orderCode, partnerId).map(PartnerOrderService::view);
	}

	@Override
	@Transactional
	public PartnerOrder cancel(Long partnerId, String partnerName, String orderCode, String reason) {
		Order o = orders.findByOrderCodeAndPartnerId(orderCode, partnerId)
				.orElseThrow(() -> ApiException.notFound("đơn hàng", orderCode));
		if (!o.getStatus().customerCancellable()) {
			throw new ApiException(HttpStatus.CONFLICT, "ORDER_NOT_CANCELLABLE",
					"Đơn " + orderCode + " đang ở trạng thái \"" + o.getStatus().getLabel() + "\", không huỷ được qua API");
		}
		o.setCancelReason(TextUtils.trimToNull(reason) != null ? reason.trim() : "Đối tác huỷ đơn");
		workflow.transition(o, OrderStatus.CANCELLED, o.getCancelReason(), "partner:" + partnerName);
		return view(o);
	}

	private static PartnerOrder view(Order o) {
		List<LineView> lines = o.getItems().stream().map(i -> new LineView(i.getSku(), i.getProductName(), i.getVariantName(),
				i.getUnitPrice(), i.getQuantity(), i.getLineTotal())).toList();
		return new PartnerOrder(o.getOrderCode(), o.getPartnerRef(), o.getStatus().name(), o.getStatus().getLabel(),
				o.getPaymentStatus().name(), o.getSubtotal(), o.getShippingFee(), o.getTotalAmount(), o.getCreatedAt(), lines);
	}

	private String newCode() {
		String code;
		do {
			code = "TL" + LocalDateTime.now().format(CODE_FMT) + ThreadLocalRandom.current().nextInt(100000, 1000000);
		} while (orders.existsByOrderCode(code));
		return code;
	}
}
