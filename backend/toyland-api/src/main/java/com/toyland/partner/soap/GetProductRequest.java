package com.toyland.partner.soap;

import jakarta.xml.bind.annotation.*;
import java.math.BigDecimal;

@XmlRootElement(name = "GetProductRequest", namespace = "http://toyland.vn/soap/partner")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"sku"})
public class GetProductRequest {
	private String sku;

	public String getSku() { return sku; }
	public void setSku(String sku) { this.sku = sku; }
}
