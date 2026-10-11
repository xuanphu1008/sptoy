package com.toyland.partner.soap;

import java.math.BigDecimal;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "GetOrderStatusResponse", namespace = "http://toyland.vn/soap/partner")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"orderCode", "externalRef", "status", "statusLabel", "paymentStatus", "total"})
public class GetOrderStatusResponse {

	private String orderCode;
	private String externalRef;
	private String status;
	private String statusLabel;
	private String paymentStatus;
	private BigDecimal total;

	public String getOrderCode() { return orderCode; }
	public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
	public String getExternalRef() { return externalRef; }
	public void setExternalRef(String externalRef) { this.externalRef = externalRef; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public String getStatusLabel() { return statusLabel; }
	public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }
	public String getPaymentStatus() { return paymentStatus; }
	public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
	public BigDecimal getTotal() { return total; }
	public void setTotal(BigDecimal total) { this.total = total; }
}
