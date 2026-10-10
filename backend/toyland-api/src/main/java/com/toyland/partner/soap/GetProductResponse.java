package com.toyland.partner.soap;

import jakarta.xml.bind.annotation.*;
import java.math.BigDecimal;

@XmlRootElement(name = "GetProductResponse", namespace = "http://toyland.vn/soap/partner")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"sku", "productName", "category", "brand", "price", "stock", "imageUrl"})
public class GetProductResponse {
	private String sku;
	private String productName;
	private String category;
	private String brand;
	private BigDecimal price;
	private int stock;
	private String imageUrl;

	public String getSku() { return sku; }
	public void setSku(String sku) { this.sku = sku; }
	public String getProductName() { return productName; }
	public void setProductName(String productName) { this.productName = productName; }
	public String getCategory() { return category; }
	public void setCategory(String category) { this.category = category; }
	public String getBrand() { return brand; }
	public void setBrand(String brand) { this.brand = brand; }
	public BigDecimal getPrice() { return price; }
	public void setPrice(BigDecimal price) { this.price = price; }
	public int getStock() { return stock; }
	public void setStock(int stock) { this.stock = stock; }
	public String getImageUrl() { return imageUrl; }
	public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
