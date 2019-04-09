$(function() {
	var productId = getQueryString('productId');
	var productUrl = '/myo2o/frontend/listproductdetailpageinfo?productId='
			+ productId;
			$.getJSON(productUrl,
					function(data) {
						if (data.success) {
							var product = data.product;
							$('#product-img').attr('src', product.imgAddr);
							$('#product-time').text(new Date(product.lastEditTime).Format("yyyy-MM-dd"));
							if(product.point!=undefined){
								$('#product-point').text('购买可得'+product.point+'积分');
							}
							$('#product-name').text(product.productName);
							$('#product-desc').text(product.productDesc);
							if(product.normalPrice!=undefined&&product.promotionPrice!=undefined){
								$('#price').show();
								$('#normalPrice').html('<div>'+'￥'+product.normalPrice+'</div>');
								$('#promotionPrice').text('￥'+product.promotionPrice);
							}
							var imgListHtml = '';
							product.productImgList.map(function(item, index) {
								imgListHtml += '<div> <img src="'
										+ item.imgAddr + '"/></div><br />';
							});
							$('#imgList').html(imgListHtml);
							// 二维码
							var imgcode='';
							if(data.success){
								imgcode += '<div> <img src="/myo2o/frontend/generateqrcode4product?productId='
									+ product.productId +'"/></div>';
							}
							$('#codeimg').html(imgcode);
						}
					});
	$('#me').click(function() {
		$.openPanel('#panel-left-demo');
	});
	$.init();
});
