package com.imooc.myo2o.entity;

import java.util.Date;

/*
 * 消费详情
 */
public class ProductSellDaily {
	private long productSellDailyId;
	private Date createTime;
	private Integer total;
	private Product product;
	private Shop shop;
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Integer getTotal() {
		return total;
	}
	public void setTotal(Integer total) {
		this.total = total;
	}
	public Product getProduct() {
		return product;
	}
	public void setProduct(Product product) {
		this.product = product;
	}
	public Shop getShop() {
		return shop;
	}
	public void setShop(Shop shop) {
		this.shop = shop;
	}
	public long getProductSellDailyId() {
		return productSellDailyId;
	}
	public void setProductSellDailyId(long productSellDailyId) {
		this.productSellDailyId = productSellDailyId;
	}
	
}
