package com.toyland.partner.soap;

import jakarta.xml.bind.annotation.*;
import java.math.BigDecimal;

@XmlRootElement(name = "GetStockResponse", namespace = "http://toyland.vn/soap/partner")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"sku", "available", "inStock"})
public class GetStockResponse {
	private String sku;
	private int available;
	private boolean inStock;

	public String getSku() { return sku; }
	public void setSku(String sku) { this.sku = sku; }
	public int getAvailable() { return available; }
	public void setAvailable(int available) { this.available = available; }
	public boolean isInStock() { return inStock; }
	public void setInStock(boolean inStock) { this.inStock = inStock; }
}
