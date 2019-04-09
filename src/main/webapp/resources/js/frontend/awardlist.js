$(function(){
	var loading=false;
	var maxItems=999;
	var pageSize=10;
	//获取奖品列表
	var listUrl='/myo2o/frontend/listawardsbyshop';
	//兑换奖品的URL
	var exchangeUrl='/myo2o/frontend/adduserawardmap';
	var pageNum=1;
	var shopId=getQueryString('shopId');
	var awardName='';
	var canProceed=false;
	var totalPoint=0;
	addItems(pageSize,pageNum);
	function addItems(pageSize,pageIndex){
		var url=listUrl+'?'+'pageIndex'+pageIndex+'&pageSize='
		       +pageSize+'&shopId='+shopId+'&awardName='+awardName
		loading=true;
		$.getJSON(url,function(data){
			if(data.success){
				maxItems=data.count;
				var html='';
				data.awardList.map(function(item,index){
					html+=''+'<div class="card" data-award-id="'
					+item.awardId+'"data-point="'+item.point
					+'">'+'<div class="card-header">'
					+item.awardName+'<span class="pull-right">需要积分'
					+item.point+'</span></div>'
					+'<div class="card-content">'
					+'<div class="list-block media-list">'+'</url>'
					+'<li class="item-content">'
					+'<div class="item-media">'+'<img src="'
					+getContextPath()+item.awardImg
					+'"width="44">'+'</div>'
					+'<div class="item-inner">'
					+'<div class="item-subtitle">'+item.awardDesc
					+'</div>'+'</div>'+'</li>'+'</ul>'
					+'</div>'+'</div>'+'</div class="card-footer">'
					+'<p class="color-gray">'
					+new Date(item.lastEditTime).format("yyyy-MM-dd")
					+'更新</p>';
				if(data.totalPoint!=undefined){
					html+='<span>点击领取</span></div></div>'
				}else{
					html+='</div></div>'
				}
				});
				$('.list-div').append(html);
				if(data.totalPoint!=undefined){
					canProceed=true;
					$("#title").text('当前积分'+data.totalPoint);
					totalPoint=data.totalPoint;
				}
				var total=$('.list-div .card').length;
				if(total>=maxItems){
					$.datechInfinteScroll($('.infinite-scroll'));
					$('.infinite-scroll-preloader').remove();
					return;
				}
				pageNum+=1;
				loading=false;
				$.refreshScroller();
			}
		});
	}
	$(document).on('infinite','infinite-scroll-bottom',function(){
		if(loading){
			return;
			addItems(pageSize,pageNum);
		}
	});
	
	$('.award-list').on('click','.card',function(e){
		if(canProceed&&(totalPoint>e.currentTarget.dataset.point)){
			$.confirm('需要消耗'+e.currentTarget.dataset.point+'积分，确定操作吗',function(){
				$.ajax({
					url:exchangeUrl,
					type:'POST',
					data:{
						awardId:e.currentTarget.dataset.awardId
					},
					dataType:'json',
					success:function(data){
						if(data.success){
							$.toast("操作成功");
							totalPoint=totalPoint-e.currentTarget.datatset.point
							$("#title").text('当前积分'+totalPoint);
						}else{
							$.toast("操作失败");
						}
					}
				});
			});
		}else{
			$.toast('积分不足或无权限操作！');
		}
	});
	$('#search').on('input', function(e) {
		productName = e.target.value;
		$('.list-div').empty();
		pageNum = 1;
		addItems(pageSize, pageNum);
	});

	$('#me').click(function() {
		$.openPanel('#panel-left-demo');
	});
	$.init();
});
