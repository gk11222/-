package com.imooc.myo2o.web.frontend;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.imooc.myo2o.entity.PersonInfo;
import com.imooc.myo2o.entity.Product;
import com.imooc.myo2o.service.ProductService;
import com.imooc.myo2o.util.CodeUtil;
import com.imooc.myo2o.util.HttpServletRequestUtil;
import com.imooc.myo2o.util.ShortNetAddressUtil;

@Controller
@RequestMapping("/frontend")
public class ProductDetailController {
	@Autowired
	private ProductService productService;

	

	@RequestMapping(value = "/listproductdetailpageinfo", method = RequestMethod.GET)
	@ResponseBody
	private Map<String, Object> listProductDetailPageInfo(
			HttpServletRequest request) {
		Map<String, Object> modelMap = new HashMap<String, Object>();
		long productId = HttpServletRequestUtil.getLong(request, "productId");
		Product product = null;
		if (productId != -1) {
			product = productService.getProductById(productId);
			PersonInfo user=(PersonInfo) request.getSession().getAttribute("user");
			System.out.println("用户名"+user);
			if (user==null) {
				modelMap.put("needQRCode", false);
			}else {
				modelMap.put("needQRCode", true);
			}
			
			modelMap.put("product", product);
			modelMap.put("success", true);
		} else {
			modelMap.put("success", false);
			modelMap.put("errMsg", "empty productId");
		}
		return modelMap;
	}
	
	
	private static String urlPrefix;
	private static String urlMiddle;
	private static String urlSuffix;
	private static String productmapUrl;
	
	@Value("${weixinprefix}")
	public void setUrlPrefix(String urlPrefix) {
		ProductDetailController.urlPrefix=urlPrefix;
	}
	@Value("${weixinmiddle}")
	public void setUrlMiddle(String urlMiddle) {
		ProductDetailController.urlMiddle=urlMiddle;
	}
	@Value("${weixinsuffix}")
	public void setUrlSuffix(String urlSuffix) {
		ProductDetailController.urlSuffix=urlSuffix;
	}
	@Value("${wechatproductmapUrl}")
	public void setAuthUrl(String productmapUrl) {
		ProductDetailController.productmapUrl=productmapUrl;
	}
	/*
	 * 生成二维码
	 */
	@RequestMapping(value = "/generateqrcode4product",method = RequestMethod.GET)
	@ResponseBody
	private void generateQRCode4ShopAuth(HttpServletRequest request,HttpServletResponse response) {
		System.out.println("进入二维码");
		/*long productId = HttpServletRequestUtil.getLong(request,"productId");*/
		long productId = HttpServletRequestUtil.getLong(request, "productId");
		System.out.println(productId);
		/*PersonInfo user=(PersonInfo) request.getSession().getAttribute("user");
		System.out.println(user);*/
		/*if (productId!=-1 &&user!=null&& user.getUserId()!=null) {*/
		if (productId!=-1) {
			long timeStamp=System.currentTimeMillis();
			String content="{aaaproductIdaaa:"+productId+",aaacreateTimeaaa:"+timeStamp+"}";
			
			//https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx50f9bf2185f27ac1&redirect_uri=http://localhost:8080/myo2o/shop/addshopauthmap&response_type=code&scope=snsapi_userinfo&state=%7BaaashopIdaaa%3A16%2CaaacreateTimeaaa%3A1553702241544%7D#wechat_redirect
			try {
			String longUrl=urlPrefix+productmapUrl+urlMiddle+URLEncoder.encode(content, "utf-8")+urlSuffix;
			System.out.println(longUrl);
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

	

