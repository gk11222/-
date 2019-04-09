package com.imooc.myo2o.web.shop;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.myo2o.dto.EchartSeries;
import com.imooc.myo2o.dto.EchartXAxis;
import com.imooc.myo2o.dto.ShopAuthMapExecution;
import com.imooc.myo2o.dto.UserProductMapExecution;
import com.imooc.myo2o.entity.PersonInfo;
import com.imooc.myo2o.entity.Product;
import com.imooc.myo2o.entity.ProductSellDaily;
import com.imooc.myo2o.entity.Shop;
import com.imooc.myo2o.entity.ShopAuthMap;
import com.imooc.myo2o.entity.UserProductMap;
import com.imooc.myo2o.entity.WechatAuth;
import com.imooc.myo2o.enums.ShopAuthMapStateEnum;
import com.imooc.myo2o.enums.UserProductMapStateEnum;
import com.imooc.myo2o.service.PersonInfoService;
import com.imooc.myo2o.service.ProductSellDailyService;
import com.imooc.myo2o.service.ProductService;
import com.imooc.myo2o.service.ShopAuthMapService;
import com.imooc.myo2o.service.UserProductMapService;
import com.imooc.myo2o.service.WechatAuthService;
import com.imooc.myo2o.util.HttpServletRequestUtil;
import com.imooc.myo2o.util.weixin.WeiXinUserUtil;
import com.imooc.myo2o.util.weixin.message.pojo.UserAccessToken;
import com.imooc.myo2o.util.weixin.message.pojo.WechatInfo;
import com.mysql.cj.x.protobuf.MysqlxExpr.Operator;

@Controller
@RequestMapping("/shop")
public class UserProductManagementController {
	@Autowired
	private UserProductMapService userProductMapService;
	@Autowired
	private PersonInfoService personInfoService;
	@Autowired
	private ProductService productService;
	@Autowired
	private ShopAuthMapService shopAuthMapService;
	@Autowired
	private ProductSellDailyService productSellDailyService;
	@Autowired
	private WechatAuthService wechatAuthService;
	
	
	@RequestMapping(value = "/listproductselldailyinfobyshop", method = RequestMethod.GET)
	@ResponseBody
	private Map<String, Object> listproductselldailyinfobyshop(HttpServletRequest request) {
		Map<String, Object> modelMap=new HashMap<String, Object>();
		Shop currentshop=(Shop) request.getSession().getAttribute("currentShop");
		if (currentshop!=null && currentshop.getShopId()!=null) {
			ProductSellDaily productSellDailyCondition=new ProductSellDaily();
			productSellDailyCondition.setShop(currentshop);
			Calendar calendar=Calendar.getInstance();
			//昨天日期
			calendar.add(calendar.DATE, -1);
			Date endTime=calendar.getTime();
			//七天前日期
			calendar.add(calendar.DATE, -6);
			Date beginTime=calendar.getTime();
			//查询店铺销售情况
			List<ProductSellDaily> productSellDailyList=productSellDailyService.listProductSellDaily(productSellDailyCondition, beginTime, endTime);
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
			HashSet<String> legendData=new HashSet<String>();
			//日期
			HashSet<String> xData=new HashSet<String>();
			HashSet<EchartSeries> series=new HashSet<EchartSeries>();
			List<Integer> totalList=new ArrayList<Integer>();
			//当前商品名
			String currentProductName="";
			for(int i=0;i<productSellDailyList.size();i++) {
				ProductSellDaily productSellDaily=productSellDailyList.get(i);
				legendData.add(productSellDaily.getProduct().getProductName());
				xData.add(sdf.format(productSellDaily.getCreateTime()));
				if (!currentProductName.equals(productSellDaily.getProduct().getProductName())&&
						!currentProductName.isEmpty()) {
					EchartSeries eSeries=new EchartSeries();
					eSeries.setName(currentProductName);
					eSeries.setData(totalList.subList(0, totalList.size()));
					series.add(eSeries);
					totalList=new ArrayList<Integer>();
					//变换下currentProductId为当前的productId
					currentProductName=productSellDaily.getProduct().getProductName();
					totalList.add(productSellDaily.getTotal());
				}else {
					//为productId则继续添加新值
					totalList.add(productSellDaily.getTotal());
					currentProductName=productSellDaily.getProduct().getProductName();
				}
				//队列之末，将最后一个商品销量添加上
				if (i==productSellDailyList.size()-1) {
					EchartSeries eSeries=new EchartSeries();
					eSeries.setName(currentProductName);
					eSeries.setData(totalList.subList(0, totalList.size()));
					series.add(eSeries);
				}
			}
			modelMap.put("series", series);
			modelMap.put("legendData", legendData);
			//拼接出xAxis
			List<EchartXAxis> xAxis=new ArrayList<EchartXAxis>();
			EchartXAxis exa=new EchartXAxis();
			exa.setData(xData);
			xAxis.add(exa);
			modelMap.put("xAxis", xAxis);
			modelMap.put("success", true);
		}else {
			modelMap.put("success", false);
			modelMap.put("errMsg", "empty shopId");
		}
		return modelMap;
	}
	
	
	@RequestMapping(value = "/listuserproductmapsbyshop", method = RequestMethod.GET)
	@ResponseBody
	private Map<String, Object> listUserProductMapsByShop(
			HttpServletRequest request) {
		Map<String, Object> modelMap = new HashMap<String, Object>();
		int pageIndex = HttpServletRequestUtil.getInt(request, "pageIndex");
		int pageSize = HttpServletRequestUtil.getInt(request, "pageSize");
		Shop currentShop = (Shop) request.getSession().getAttribute(
				"currentShop");
		if ((pageIndex > -1) && (pageSize > -1) && (currentShop != null)
				&& (currentShop.getShopId() != null)) {
			UserProductMap userProductMapCondition = new UserProductMap();
			userProductMapCondition.setShopId(currentShop.getShopId());
			String productName = HttpServletRequestUtil.getString(request,"productName");
			if (productName != null) {
				userProductMapCondition.setProductName(productName);
			}
			UserProductMapExecution ue = userProductMapService
					.listUserProductMap(userProductMapCondition, pageIndex,pageSize);
			modelMap.put("userProductMapList", ue.getUserProductMapList());
			modelMap.put("count", ue.getCount());
			modelMap.put("success", true);
		} else {
			modelMap.put("success", false);
			modelMap.put("errMsg", "empty pageSize or pageIndex or shopId");
		}
		return modelMap;
	}

	@RequestMapping(value = "/adduserproductmap", method = RequestMethod.GET)
	@ResponseBody
	private String addUserProductMap(HttpServletRequest request) throws IOException {
		System.out.println("111");
		WechatAuth wechatAuth = getOperationInfo(request);
		if (wechatAuth!=null) {
			
			//解析state
			String qrCodeinfo=new String(
					URLDecoder.decode(HttpServletRequestUtil.getString(request, "state"), "UTF-8"));
			System.out.println(qrCodeinfo);
			ObjectMapper mapper=new ObjectMapper();
			WechatInfo wechatInfo=null;
			try {
				wechatInfo=mapper.readValue(qrCodeinfo.replace("aaa", "\""), WechatInfo.class);
			} catch (Exception e) {
				return "Fail";
			}
			//是否过期
			if (!checkQRCodeInfo(wechatInfo)) {
				return "Fail";
			}
			
			Long productId=wechatInfo.getProductId();
			//Long customerId=wechatInfo.getCustomerId();
			Long customerId=12L;
			UserProductMap userProductMap=compactUserProductMap4Add(customerId, productId);
			if (userProductMap !=null) {
				try {
					//添加消费记录
					UserProductMapExecution se=userProductMapService.addUserProductMap(userProductMap);
					if (se.getState()==UserProductMapStateEnum.SUCCESS.getState()) {
						return "SUCCESS";
					}
				} catch (Exception e) {
					return "Fail";
				}
			}
		}
		return "Fail";
	}	
	private WechatAuth getOperationInfo(HttpServletRequest request) {
		String code=request.getParameter("code");
		WechatAuth auth=null;
		if (null!=code) {
			UserAccessToken token;
			try {
				token=WeiXinUserUtil.getUserAccessToken(code);
				String openId=token.getOpenId();
				request.getSession().setAttribute("openId", openId);
				System.out.println(openId);
				auth=wechatAuthService.getWechatAuthByOpenId(openId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return auth;
	}

	private boolean checkQRCodeInfo(WechatInfo wechatInfo) {
		if (wechatInfo != null && wechatInfo.getProductId() != null
				&& wechatInfo.getCustomerId() != null
				&& wechatInfo.getCreateTime() != null) {
			long nowTime = System.currentTimeMillis();
			if ((nowTime - wechatInfo.getCreateTime()) <= 5000) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private UserProductMap compactUserProductMap4Add(Long customerId,
			Long productId) {
		UserProductMap userProductMap = null;
		if (customerId != null && productId != null) {
			userProductMap = new UserProductMap();
			PersonInfo personInfo = personInfoService.getPersonInfoById(customerId);
			Product product = productService.getProductById(productId);
			userProductMap.setProductId(productId);
			userProductMap.setShopId(product.getShop().getShopId());
			userProductMap.setProductName(product.getProductName());
			userProductMap.setUserName(personInfo.getName());
			userProductMap.setPoint(product.getPoint());
			userProductMap.setCreateTime(new Date());
		}
		return userProductMap;
	}
	//操作权限
	private boolean checkShopAuth(long userId, UserProductMap userProductMap) {
		ShopAuthMapExecution shopAuthMapExecution = shopAuthMapService
				.listShopAuthMapByShopId(userProductMap.getShopId(), 1, 1000);
		for (ShopAuthMap shopAuthMap : shopAuthMapExecution
				.getShopAuthMapList()) {
			if (shopAuthMap.getEmployeeId() == userId) {
				return true;
			}
		}
		return false;
	}
}
