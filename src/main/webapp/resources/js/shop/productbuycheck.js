$(function() {
    var shopId = 1;
    var productName = '';
    getProductSellDailyList();
    function getList() {
        var listUrl = '/myo2o/shop/listuserproductmapsbyshop?pageIndex=1&pageSize=9999&shopId=' + shopId + '&productName=' + productName;
        $.getJSON(listUrl, function (data) {
            if (data.success) {
                var userProductMapList = data.userProductMapList;
                var tempHtml = '';
                userProductMapList.map(function (item, index) {
                    tempHtml += ''
                         +      '<div class="row row-productbuycheck">'
                         +          '<div class="col-20">'+ item.productName +'</div>'
                         +          '<div class="col-40 productbuycheck-time">'+ new Date(item.createTime).Format("yyyy-MM-dd") +'</div>'
                         +          '<div class="col-33">'+ item.userName +'</div>'
                         +          '<div class="col-33">'+ item.point +'</div>'
                         +      '</div>';
                });
                $('.productbuycheck-wrap').html(tempHtml);
            }
        });
    }

    $('#search').on('change', function (e) {
        productName = e.target.value;
        $('.productbuycheck-wrap').empty();
        getList();
    });
    
    
    function getProductSellDailyList(){
    	var listProductSellDailyUrl="/myo2o/shop/listproductselldailyinfobyshop";
    	$.getJSON(listProductSellDailyUrl,function(data){
    		if(data.success){
    			var myChart=echarts.init(document.getElementById('chart'));
    			var option =generateStaticEchartPart();
    			option.legend.data=data.legendData;
    			option.series=data.series;
    			option.xAxis=data.xAxis;
    			
    			myChart.setOption(option);
    		}
    	});
    }
    /*
     * echars
     */
   /* getList();
    var myChart = echarts.init(document.getElementById('chart'));*/
    function generateStaticEchartPart(){
        return option = {
            tooltip : {
                trigger: 'axis',
                axisPointer : {            // 坐标轴指示器，坐标轴触发有效
                    type : 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                }
            },
            //图例
            legend: {
            },
            //绘图网格
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                containLabel: true
            },
            xAxis : [
            ],
            yAxis : [
                {
                    type : 'value'
                }
            ],
            series : [
               /* {
                    name:'',
                    type:'bar',
                    data:[]
                },
                {
                    name:'',
                    type:'bar',
                    data:[]
                },
                {
                    name:'',
                    type:'bar',
                    data:[]
                }*/
            ]
        };

        //myChart.setOption(option);

        }
    });
    
    