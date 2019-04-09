package com.imooc.myo2o.dao;


import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;

import com.imooc.myo2o.BaseTest;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProductSellDaoTest extends BaseTest{

	@Autowired
	private ProductSellDailyDao productSellDailyDao;
	@Test
	public void testAInsertProductSellDaily() throws IOException{
		int effectedNum=productSellDailyDao.insertProductSellDaily();
		assertEquals(4, effectedNum);
	}
	
		

	/*@Test
	public void testBInsertProductSellDaily() {
		int effectedNum=productSellDailyDao.insertDefaultProductSellDaily();
		assertEquals(0, effectedNum);
	}*/
}
