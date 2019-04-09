package com.imooc.myo2o.web.shop;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.encoder.QRCode;
import com.imooc.myo2o.dto.ShopAuthMapExecution;
import com.imooc.myo2o.entity.PersonInfo;
import com.imooc.myo2o.entity.Shop;
import com.imooc.myo2o.entity.ShopAuthMap;
import com.imooc.myo2o.entity.WechatAuth;
import com.imooc.myo2o.enums.ShopAuthMapStateEnum;
import com.imooc.myo2o.service.PersonInfoService;
import com.imooc.myo2o.service.ShopAuthMapService;
import com.imooc.myo2o.service.WechatAuthService;
import com.imooc.myo2o.util.CodeUtil;
import com.imooc.myo2o.util.DESUtils;
import com.imooc.myo2o.util.HttpServletRequestUtil;
import com.imooc.myo2o.util.ShortNetAddressUtil;
import com.imooc.myo2o.util.weixin.WeiXinUserUtil;
import com.imooc.myo2o.util.weixin.WeixinUtil;
import com.imooc.myo2o.util.weixin.message.pojo.UserAccessToken;
import com.imooc.myo2o.util.weixin.message.pojo.WechatInfo;

@Controller
@RequestMapping("/shop")
public class ShopAuthManagementController {
	@Autowired
	private ShopAuthMapService shopAuthMapService;
	@Autowired
	private PersonInfoService personInfoService;
	@Autowired
	private WechatAuthService wechatAuthService;

	@RequestMapping(value = "/listshopauthmapsbyshop", method = RequestMethod.GET)
	@ResponseBody
	private Map<String, Object> listShopAuthMapsByShop(
			HttpServletRequest request) {
		Map<String, Object> modelMap = new HashMap<String, Object>();
		int pageIndex = HttpServletRequestUtil.getInt(request, "pageIndex");
		int pageSize = HttpServletRequestUtil.getInt(request, "pageSize");
		Shop currentShop = (Shop) request.getSession().getAttribute("currentShop");
		if ((pageIndex > -1) && (pageSize > -1) && (currentShop != null)&& (currentShop.getShopId() != null)) {
			ShopAuthMapExecution se = shopAuthMapService
					.listShopAuthMapByShopId(currentShop.getShopId(),
							pageIndex, pageSize);
			modelMap.put("shopAuthMapList", se.getShopAuthMapList());
			modelMap.put("count", se.getCount());
			modelMap.put("success", true);
		} else {
			modelMap.put("success", false);
			modelMap.put("errMsg", "empty pageSize or pageIndex or shopId");
		}
		return modelMap;
	}

	@RequestMapping(value = "/getshopauthmapbyid", method = RequestMethod.GET)
	@ResponseBody
	private Map<String, Object> getShopAuthMapById(@RequestParam Long shopAuthId) {
		Map<String, Object> modelMap = new HashMap<String, Object>();
		if (shopAuthId != null && shopAuthId > -1) {
			//查找授权信息
			ShopAuthMap shopAuthMap = shopAuthMapService.getShopAuthMapById(shopAuthId);
			modelMap.put("shopAuthMap", shopAuthMap);
			modelMap.put("success", true);
		} else {
			modelMap.put("success", false);
			modelMap.put("errMsg", "empty shopAuthId");
		}
		return modelMap;
	}

	@RequestMapping(value = "/addshopauthmap", method = RequestMethod.GET)
	@ResponseBody
	private String addShopAuthMap(String shopAuthMapStr,HttpServletRequest request) throws IOException {
		WechatAuth auth=getEmployeeInfo(request);
		if (auth!=null) {
			PersonInfo user =personInfoService.getPersonInfoById(auth.getPersonInfo().getUserId());
			request.getSession().setAttribute("user", user);
			System.out.println("111111111");
			System.out.println("用户名"+user);
			//解析state
			String qrCodeinfo=new String(
					URLDecoder.decode(HttpServletRequestUtil.getString(request, "state"), "UTF-8"));
			System.out.println(qrCodeinfo);
			ObjectMapper mapper=new ObjectMapper();
			WechatInfo wechatInfo=null;
			try {
				wechatInfo=mapper.readValue(qrCodeinfo.replace("aaa", "\""), WechatInfo.class);
			} catch (Exception e) {
				return "Fail1";
			}
			//是否过期
			if (!checkQRCodeInfo(wechatInfo)) {
				return "Fail2";
			}
			//去重
			ShopAuthMapExecution allMapList=shopAuthMapService.listShopAuthMapByShopId(wechatInfo.getShopId(), 1, 99);
			List<ShopAuthMap> shopauthList=allMapList.getShopAuthMapList();
			for(ShopAuthMap sm:shopauthList) {
				if (sm.getEmployee().getUserId()==user.getUserId()) {
					return "Fail3";
				}
			}
			try {
				ShopAuthMap shopAuthMap=new ShopAuthMap();
				Shop shop=new Shop();
				shop.setShopId(wechatInfo.getShopId());
				shopAuthMap.setShop(shop);
				shopAuthMap.setEmployee(user);
				shopAuthMap.setTitle("员工");
				shopAuthMap.setTitleFlag(1);
				ShopAuthMapExecution se=shopAuthMapService.addShopAuthMap(shopAuthMap);
				if (se.getState()==ShopAuthMapStateEnum.SUCCESS.getState()) {
					System.out.println("222222");
					return "SUCCESS";
				}else {
					return "Fail4";
				}
			} catch (Exception e) {
				return "Fail5";
			} 
		}
		return "Fail6";
	}	
		
		private boolean checkQRCodeInfo(WechatInfo wechatInfo) {
			if (wechatInfo!=null&& wechatInfo.getCreateTime()!=null) {
				long nowTime=System.currentTimeMillis();
				if (nowTime-wechatInfo.getCreateTime()<=600000) {
					return true;
				}else {
					return false;
				}
			}else {
				return false;
			}
		}

	
	private WechatAuth getEmployeeInfo(HttpServletRequest request) {
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

	@RequestMapping(value = "/modifyshopauthmap", method = RequestMethod.POST)
	@ResponseBody
	private Map<String, Object> modifyShopAuthMap(String shopAuthMapStr,HttpServletRequest request) {
		Map<String, Object> modelMap = new HashMap<String, Object>();
		boolean statusChange=HttpServletRequestUtil.getBoolean(request, "statusChange");
		if (statusChange&&!CodeUtil.checkVerifyCode(request)) {
			modelMap.put("success", false);
			modelMap.put("errMsg", "输入了错误的验证码");
			return modelMap;
		}
		ObjectMapper mapper = new ObjectMapper();
		ShopAuthMap shopAuthMap = null;
		try {
			//json转换成ShopAuthMap实例
			shopAuthMap = mapper.readValue(shopAuthMapStr, ShopAuthMap.class);
		} catch (Exception e) {
			modelMap.put("success", false);
			modelMap.put("errMsg", e.toString());
			return modelMap;
		}
		//判断非空
		if (shopAuthMap != null && shopAuthMap.getShopAuthId() != null) {
			try {
				//查看是否为店家本身，本身不支持修改
				if (!checkPermission(shopAuthMap.getShopAuthId())) {
					modelMap.put("success", false);
					modelMap.put("errMsg", "无法对店家本身权限做修改");
					return modelMap;
				}
				Shop currentShop = (Shop) request.getSession().getAttribute("currentShop");
				PersonInfo user = (PersonInfo) request.getSession().getAttribute("user");
				shopAuthMap.setShopId(currentShop.getShopId());
				shopAuthMap.setEmployeeId(user.getUserId());
				ShopAuthMapExecution se = shopAuthMapService.modifyShopAuthMap(shopAuthMap);
				if (se.getState() == ShopAuthMapStateEnum.SUCCESS.getState()) {
					modelMap.put("success", true);
				} else {
					modelMap.put("success", false);
					modelMap.put("errMsg", se.getStateInfo());
				}
			} catch (RuntimeException e) {
				modelMap.put("success", false);
				modelMap.put("errMsg", e.toString());
				return modelMap;
			}
		} else {
			modelMap.put("success", false);
			modelMap.put("errMsg", "请输入要修改的授权信息");
		}
		return modelMap;
	}
	//检查对象是否可修改
	private boolean checkPermission(Long shopAuthId) {
		ShopAuthMap grantedPerson=shopAuthMapService.getShopAuthMapById(shopAuthId);
		if (grantedPerson.getTitleFlag()==0) {
			//店家本身，不能修改
			return false;
		}else {
			return true;
		}
	}

	@RequestMapping(value = "/removeshopauthmap", method = RequestMethod.GET)
	@ResponseBody
	private Map<String, Object> removeShopAuthMap(Long shopAuthId) {
		Map<String, Object> modelMap = new HashMap<String, Object>();
		if (shopAuthId != null && shopAuthId > 0) {
			try {
				ShopAuthMapExecution se = shopAuthMapService
						.removeShopAuthMap(shopAuthId);
				if (se.getState() == ShopAuthMapStateEnum.SUCCESS.getState()) {
					modelMap.put("success", true);
				} else {
					modelMap.put("success", false);
					modelMap.put("errMsg", se.getStateInfo());
				}
			} catch (RuntimeException e) {
				modelMap.put("success", false);
				modelMap.put("errMsg", e.toString());
				return modelMap;
			}

		} else {
			modelMap.put("success", false);
			modelMap.put("errMsg", "请至少选择一个授权进行删除");
		}
		return modelMap;
	}
	
	
	
	private static String urlPrefix;
	private static String urlMiddle;
	private static String urlSuffix;
	private static String authUrl;
	
	@Value("${weixinprefix}")
	public void setUrlPrefix(String urlPrefix) {
		ShopAuthManagementController.urlPrefix=urlPrefix;
	}
	@Value("${weixinmiddle}")
	public void setUrlMiddle(String urlMiddle) {
		ShopAuthManagementController.urlMiddle=urlMiddle;
	}
	@Value("${weixinsuffix}")
	public void setUrlSuffix(String urlSuffix) {
		ShopAuthManagementController.urlSuffix=urlSuffix;
	}
	@Value("${wechatauthurl}")
	public void setAuthUrl(String authUrl) {
		ShopAuthManagementController.authUrl=authUrl;
	}
	/*
	 * 生成二维码
	 */
	@RequestMapping(value = "/generateqrcode4shopauth", method = RequestMethod.GET)
	@ResponseBody
	private void generateQRCode4ShopAuth(HttpServletRequest request,HttpServletResponse response) {
		Shop shop=(Shop) request.getSession().getAttribute("currentShop");
		if (shop!=null && shop.getShopId()!=null) {
			long timeStamp=System.currentTimeMillis();
			String content="{aaashopIdaaa:"+shop.getShopId()+",aaacreateTimeaaa:"+timeStamp+"}";
		   try {
			String longUrl=urlPrefix +authUrl+urlMiddle +URLEncoder.encode(content, "UTF-8")+urlSuffix;
			//https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx50f9bf2185f27ac1&redirect_uri=http://localhost:8080/myo2o/shop/addshopauthmap&response_type=code&scope=snsapi_userinfo&state=%7BaaashopIdaaa%3A16%2CaaacreateTimeaaa%3A1553702241544%7D#wechat_redirect
			String shortUrl=ShortNetAddressUtil.createShortUrl(longUrl);
			System.err.println(shortUrl);
			//调用二维码生成的工具类方法，传入短的URL，生成二维码
			BitMatrix qRcodeImg=CodeUtil.generteQRCodeStream(shortUrl,response);
			//二维码以图片流形式输出到前端
			MatrixToImageWriter.writeToStream(qRcodeImg, "png",response.getOutputStream());
			
		   } catch (Exception e) {
			   e.printStackTrace();
		   }
		}
	}
}
