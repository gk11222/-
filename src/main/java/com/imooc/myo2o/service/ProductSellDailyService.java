package com.imooc.myo2o.service;

import java.util.Date;
import java.util.List;
import com.imooc.myo2o.entity.ProductSellDaily;

public interface ProductSellDailyService {

	void dailyCalculate();
	//根据条件的日销量
	List<ProductSellDaily> listProductSellDaily(ProductSellDaily productSellDailyCondition,Date beginTime,
			Date endTime);
}
