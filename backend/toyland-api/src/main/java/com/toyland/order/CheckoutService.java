package com.toyland.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toyland.auth.EmailVerificationService;
import com.toyland.auth.CurrentUserService;
import com.toyland.cart.CartItem;
import com.toyland.cart.CartItemRepository;
import com.toyland.cart.CartService;
import com.toyland.cart.dto.CartDtos.CartLine;
import com.toyland.catalog.Product;
import com.toyland.catalog.ProductVariant;
import com.toyland.common.exception.ApiException;
import com.toyland.common.util.Money;
import com.toyland.common.util.TextUtils;
import com.toyland.inventory.InventoryService;
import com.toyland.inventory.MovementType;
import com.toyland.loyalty.LoyaltyRules;
import com.toyland.loyalty.LoyaltyService;
import com.toyland.order.dto.OrderDtos.PlaceOrderRequest;
import com.toyland.order.dto.OrderDtos.PlaceOrderResponse;
import com.toyland.order.dto.OrderDtos.Quote;
import com.toyland.order.dto.OrderDtos.QuoteRequest;
import com.toyland.payment.MomoService;
import com.toyland.promotion.Campaign;
import com.toyland.promotion.PricingService;
import com.toyland.promotion.VoucherService;
import com.toyland.promotion.VoucherService.VoucherResult;
import com.toyland.promotion.VoucherType;
import com.toyland.promotion.VoucherUsageApi;
import com.toyland.shipping.ShippingService;
import com.toyland.shipping.ShippingService.FeeResult;
import com.toyland.shipping.ShippingService.Parcel;
import com.toyland.user.Address;
import com.toyland.user.UserApi;
import com.toyland.user.User;

/**
 * Thanh toan: tinh tam (quote) va dat hang. Toan bo dat hang nam trong 1 transaction:
 * kiem tra ton -> tru kho -> ghi voucher -> tao don -> xoa gio. Loi o buoc nao se rollback het.
 */
@Service
public class CheckoutService {

	private static final DateTimeFormatter CODE_FMT = DateTimeFormatter.ofPattern("yyMMdd");

	private final CurrentUserService currentUser;
	private final CartItemRepository cartRepository;
	private final CartService cartService;
	private final UserApi users;
	private final ShippingService shippingService;
	private final VoucherService voucherService;
	private final VoucherUsageApi voucherUsages;
	private final PricingService pricingService;
	private final InventoryService inventoryService;
	private final OrderRepository orderRepository;
	private final MomoService momoService;
	private final OrderWorkflow workflow;
	private final LoyaltyService loyaltyService;

	@Value("${app.order.payment-timeout-minutes}")
	private int paymentTimeout;

	public CheckoutService(CurrentUserService currentUser, CartItemRepository cartRepository, CartService cartService,
			UserApi users, ShippingService shippingService, VoucherService voucherService,
			VoucherUsageApi voucherUsages, PricingService pricingService, InventoryService inventoryService,
			OrderRepository orderRepository, MomoService momoService, OrderWorkflow workflow,
			LoyaltyService loyaltyService) {
		this.currentUser = currentUser;
		this.cartRepository = cartRepository;
		this.cartService = cartService;
		this.users = users;
		this.shippingService = shippingService;
		this.voucherService = voucherService;
		this.voucherUsages = voucherUsages;
		this.pricingService = pricingService;
		this.inventoryService = inventoryService;
		this.orderRepository = orderRepository;
		this.momoService = momoService;
		this.workflow = workflow;
		this.loyaltyService = loyaltyService;
	}

	/** Ket qua tinh toan noi bo (giu lai entity de dat hang). */
	private record Calc(Quote quote, List<CartItem> items, Address address, VoucherResult voucher, int pointsUsed) {
	}

	@Transactional(readOnly = true)
	public Quote quote(QuoteRequest r) {
		return calculate(currentUser.require(), r.addressId(), r.voucherCode(), r.giftWrap(), r.usePoints(), false).quote();
	}

	private Calc calculate(User user, Long addressId, String voucherCode, boolean giftWrap, boolean usePoints, boolean strictVoucher) {
		List<Campaign> campaigns = pricingService.runningCampaigns();
		List<CartItem> items = cartRepository.findAllOfUser(user.getId());
		List<CartLine> lines = new ArrayList<>();
		List<String> issues = new ArrayList<>();
		BigDecimal subtotal = BigDecimal.ZERO;
		int weight = 0;
		int maxL = 1;
		int maxW = 1;
		int sumH = 0;
		for (CartItem c : items) {
			CartLine l = cartService.line(c.getId(), c.getVariant(), c.getQuantity(), campaigns);
			lines.add(l);
			if (!l.available()) {
				issues.add(l.productName() + ": " + l.message());
				continue;
			}
			subtotal = subtotal.add(l.lineTotal());
			Product p = c.getVariant().getProduct();
			weight += c.getVariant().getWeightGram() * c.getQuantity();
			maxL = Math.max(maxL, p.getLengthCm());
			maxW = Math.max(maxW, p.getWidthCm());
			sumH += p.getHeightCm() * c.getQuantity();
		}
		if (items.isEmpty()) {
			issues.add("Giỏ hàng đang trống");
		}
		Address address = null;
		BigDecimal fee = BigDecimal.ZERO;
		String provider = null;
		String shipMsg = "Chọn địa chỉ để tính phí vận chuyển";
		if (addressId != null) {
			address = users.findOwnAddress(addressId, user.getId()).orElseThrow(() -> ApiException.notFound("địa chỉ", addressId));
			FeeResult fr = shippingService.fee(address.getDistrictId(), address.getWardCode(),
					new Parcel(Math.max(weight, 100), maxL, maxW, Math.min(Math.max(sumH, 1), 200), subtotal));
			fee = fr.fee();
			provider = fr.provider();
			shipMsg = fr.message();
		} else {
			issues.add("Chưa chọn địa chỉ giao hàng");
		}
		BigDecimal shipDiscount = BigDecimal.ZERO;
		BigDecimal threshold = shippingService.freeThreshold();
		if (threshold != null && threshold.signum() > 0 && subtotal.compareTo(threshold) >= 0) {
			shipDiscount = fee;
			shipMsg = "Miễn phí vận chuyển cho đơn từ " + threshold.toBigInteger() + "đ";
		}
		BigDecimal discount = BigDecimal.ZERO;
		String voucherError = null;
		String voucherName = null;
		VoucherResult vr = null;
		String code = TextUtils.trimToNull(voucherCode);
		if (code != null) {
			try {
				vr = voucherService.evaluate(code, user.getId(), subtotal, fee);
				if (vr.voucher().getType() == VoucherType.FREESHIP && fee.signum() > 0 && shipDiscount.compareTo(fee) >= 0) {
					vr = null;
					throw ApiException.badRequest("Đơn hàng đã được miễn phí vận chuyển, không cần dùng mã FREESHIP");
				}
				discount = vr.discount();
				shipDiscount = Money.min(fee, shipDiscount.max(vr.shippingDiscount()));
				voucherName = vr.voucher().getName();
				code = vr.voucher().getCode();
			} catch (ApiException e) {
				if (strictVoucher) {
					throw e;
				}
				voucherError = e.getMessage();
			}
		}
		BigDecimal wrapFee = giftWrap ? shippingService.giftWrapFee() : BigDecimal.ZERO;
		// diem thuong: toi da X% gia tri hang sau voucher
		LoyaltyRules rules = loyaltyService.rules();
		BigDecimal goods = subtotal.subtract(discount).max(BigDecimal.ZERO);
		int balance = users.pointsOf(user.getId());
		int pointsMax = rules.maxRedeemable(balance, goods);
		int pointsUsed = usePoints ? pointsMax : 0;
		BigDecimal pointsDiscount = rules.valueOf(pointsUsed);
		int pointsToEarn = rules.pointsToEarn(goods.subtract(pointsDiscount), user.getLifetimePoints());
		BigDecimal total = subtotal.subtract(discount).subtract(pointsDiscount).add(fee).subtract(shipDiscount).add(wrapFee)
				.max(BigDecimal.ZERO);
		boolean can = issues.isEmpty();
		Quote q = new Quote(lines, subtotal, discount, fee, shipDiscount, wrapFee, total, vr == null ? null : code, voucherName,
				voucherError, provider, shipMsg, threshold, can, issues, momoService.isEnabled(), balance, pointsMax, pointsUsed,
				pointsDiscount, pointsToEarn, rules.pointValue());
		return new Calc(q, items, address, vr, pointsUsed);
	}

	@Transactional
	public PlaceOrderResponse place(PlaceOrderRequest r, String clientIp) {
		User user = currentUser.require();
		EmailVerificationService.requireVerified(user, "đặt hàng");
		if (r.paymentMethod() == PaymentMethod.MOMO && !momoService.isEnabled()) {
			throw ApiException.badRequest("MOMO_DISABLED", "Thanh toán MoMo đang tắt, vui lòng chọn COD");
		}
		Calc calc = calculate(user, r.addressId(), r.voucherCode(), r.giftWrap(), r.usePoints(), true);
		Quote q = calc.quote();
		if (!q.canPlaceOrder()) {
			throw ApiException.badRequest("CART_INVALID", String.join("; ", q.issues()));
		}
		Address a = calc.address();
		Order o = new Order();
		o.setOrderCode(newCode());
		o.setUser(user);
		o.setReceiverName(a.getReceiverName());
		o.setPhone(a.getPhone());
		o.setProvinceName(a.getProvinceName());
		o.setDistrictName(a.getDistrictName());
		o.setWardName(a.getWardName());
		o.setAddressDetail(a.getDetail());
		o.setDistrictId(a.getDistrictId());
		o.setWardCode(a.getWardCode());
		o.setNote(TextUtils.trimToNull(r.note()));
		o.setGiftWrap(r.giftWrap());
		o.setGiftMessage(r.giftWrap() ? TextUtils.trimToNull(r.giftMessage()) : null);
		o.setSubtotal(q.subtotal());
		o.setDiscountAmount(q.discount());
		o.setShippingFee(q.shippingFee());
		o.setShippingDiscount(q.shippingDiscount());
		o.setGiftWrapFee(q.giftWrapFee());
		o.setTotalAmount(q.total());
		o.setPointsUsed(calc.pointsUsed());
		o.setPointsDiscount(q.pointsDiscount());
		o.setPaymentMethod(r.paymentMethod());
		o.setPaymentStatus(PaymentStatus.UNPAID);
		if (calc.voucher() != null) {
			o.setVoucher(calc.voucher().voucher());
			o.setVoucherCode(calc.voucher().voucher().getCode());
		}
		OrderStatus initial = r.paymentMethod() == PaymentMethod.MOMO ? OrderStatus.PENDING_PAYMENT : OrderStatus.PENDING;
		o.setStatus(initial);
		if (initial == OrderStatus.PENDING_PAYMENT) {
			o.setPaymentDeadline(LocalDateTime.now().plusMinutes(paymentTimeout));
		}
		for (int i = 0; i < calc.items().size(); i++) {
			CartItem c = calc.items().get(i);
			CartLine l = q.lines().get(i);
			ProductVariant v = c.getVariant();
			OrderItem oi = new OrderItem();
			oi.setProduct(v.getProduct());
			oi.setVariant(v);
			oi.setProductName(v.getProduct().getName());
			oi.setVariantName(v.getDisplayName());
			oi.setSku(v.getSku());
			oi.setImageUrl(l.imageUrl());
			oi.setUnitPrice(l.unitPrice());
			oi.setOriginalPrice(l.originalPrice());
			oi.setCostPrice(v.getCostPrice());
			oi.setQuantity(c.getQuantity());
			o.addItem(oi);
		}
		o.addHistory(null, initial, initial == OrderStatus.PENDING_PAYMENT ? "Đặt hàng - chờ thanh toán MoMo" : "Đặt hàng thành công (COD)",
				user.getUsername());
		o = orderRepository.save(o);
		// tru kho (kiem tra lai ton, co optimistic lock tren bien the)
		for (OrderItem oi : o.getItems()) {
			inventoryService.change(oi.getVariant(), -oi.getQuantity(), MovementType.SALE, o.getOrderCode(), "Xuất bán");
		}
		if (calc.voucher() != null) {
			var voucher = calc.voucher().voucher();
			voucherService.consume(voucher);
			// chi tinh phan giam do voucher (khong gom freeship theo nguong 500k)
			voucherUsages.recordUsage(voucher, user, o,
					calc.voucher().discount().add(Money.min(q.shippingFee(), calc.voucher().shippingDiscount())));
		}
		loyaltyService.redeem(user, o, calc.pointsUsed());
		cartRepository.deleteAll(calc.items());
		if (r.paymentMethod() == PaymentMethod.COD) {
			// don MoMo chi bao cho nhan vien khi da thanh toan xong (MomoService)
			workflow.notifyNewOrder(o);
		}
		String paymentUrl = null;
		if (r.paymentMethod() == PaymentMethod.MOMO) {
			// goi API MoMo; MoMo tu choi -> nem loi -> rollback toan bo don (hoan kho, voucher, diem)
			paymentUrl = momoService.createPayment(o);
		}
		workflow.publishOrderEvent(o, "order.placed", null, null);
		return new PlaceOrderResponse(o.getId(), o.getOrderCode(), o.getTotalAmount(), o.getPaymentMethod(), paymentUrl);
	}

	private String newCode() {
		String code;
		do {
			code = "TL" + LocalDateTime.now().format(CODE_FMT) + ThreadLocalRandom.current().nextInt(100000, 1000000);
		} while (orderRepository.existsByOrderCode(code));
		return code;
	}
}
