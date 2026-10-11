package com.toyland.partner.soap;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.toyland.catalog.CatalogApi;
import com.toyland.order.PartnerOrderApi;
import com.toyland.partner.Partner;
import org.springframework.ws.context.MessageContext;
import com.toyland.catalog.CatalogApi.SellableItem;

/** Dich vu SOAP cua doi tac: cung nghiep vu voi REST /api/partner/v1 (dung chung CatalogApi), khac giao thuc. */
@Endpoint
public class PartnerSoapEndpoint {

	private static final String NS = "http://toyland.vn/soap/partner";

	private final CatalogApi catalog;
	private final PartnerOrderApi orders;

	public PartnerSoapEndpoint(CatalogApi catalog, PartnerOrderApi orders) {
		this.catalog = catalog;
		this.orders = orders;
	}

	@PayloadRoot(namespace = NS, localPart = "GetStockRequest")
	@ResponsePayload
	public GetStockResponse getStock(@RequestPayload GetStockRequest req) {
		SellableItem i = find(req.getSku());
		GetStockResponse r = new GetStockResponse();
		r.setSku(i.sku());
		r.setAvailable(i.stock());
		r.setInStock(i.stock() > 0);
		return r;
	}

	@PayloadRoot(namespace = NS, localPart = "GetProductRequest")
	@ResponsePayload
	public GetProductResponse getProduct(@RequestPayload GetProductRequest req) {
		SellableItem i = find(req.getSku());
		GetProductResponse r = new GetProductResponse();
		r.setSku(i.sku());
		r.setProductName(i.productName());
		r.setCategory(i.category());
		r.setBrand(i.brand());
		r.setPrice(i.price());
		r.setStock(i.stock());
		r.setImageUrl(i.imageUrl());
		return r;
	}

	@PayloadRoot(namespace = NS, localPart = "GetOrderStatusRequest")
	@ResponsePayload
	public GetOrderStatusResponse getOrderStatus(@RequestPayload GetOrderStatusRequest req, MessageContext ctx) {
		Partner partner = (Partner) ctx.getProperty(PartnerSoapAuthInterceptor.PROP_PARTNER);
		PartnerOrderApi.PartnerOrder o = orders.find(partner.getId(), req.getOrderCode().trim())
				.orElseThrow(() -> new PartnerSoapException("Không tìm thấy đơn " + req.getOrderCode().trim()));
		GetOrderStatusResponse r = new GetOrderStatusResponse();
		r.setOrderCode(o.orderCode());
		r.setExternalRef(o.externalRef());
		r.setStatus(o.status());
		r.setStatusLabel(o.statusLabel());
		r.setPaymentStatus(o.paymentStatus());
		r.setTotal(o.total());
		return r;
	}

	private SellableItem find(String sku) {
		if (sku == null || sku.isBlank()) {
			throw new PartnerSoapException("Thiếu mã hàng (sku)");
		}
		return catalog.sellableBySku(sku.trim()).orElseThrow(() -> new PartnerSoapException("Không tìm thấy mã hàng " + sku.trim()));
	}
}
