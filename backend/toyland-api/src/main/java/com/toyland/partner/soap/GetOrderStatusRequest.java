package com.toyland.partner.soap;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "GetOrderStatusRequest", namespace = "http://toyland.vn/soap/partner")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"orderCode"})
public class GetOrderStatusRequest {

	private String orderCode;

	public String getOrderCode() { return orderCode; }
	public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
}
