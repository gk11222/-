package com.imooc.myo2o.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.imooc.myo2o.entity.ProductSellDaily;

public interface ProductSellDailyDao {
	List<ProductSellDaily> queryProductSellDailyList(
			@Param("productSellDailyCondition") ProductSellDaily productSellDailyCondition,
			@Param("beginTime") Date beginTime,
			@Param("endTime") Date endTime);
	
	//平台商品所有销量
	int insertProductSellDaily();
	//统计没有销量的
	int insertDefaultProductSellDaily();
}
