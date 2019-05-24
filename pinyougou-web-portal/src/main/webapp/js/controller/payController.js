app.controller('payController' ,function($scope ,$location,payService){
	
	//生成二维码
	// 订单ID (支付表的支付ID)  支付金额  二维码的Value值  code_url
	$scope.createNative=function(){
		payService.createNative().success(
			function(response){//Map
				
				//显示订单号和金额
				$scope.money= (response.total_fee/100).toFixed(2);
				$scope.out_trade_no=response.out_trade_no;
				
				//生成二维码
				 var qr=new QRious({
					    element:document.getElementById('qrious'),
						size:250,
						value:response.code_url,
						level:'H'
			     });
				 
				 queryPayStatus();//调用查询
				
			}	
		);	
	}
	
	//调用查询
	queryPayStatus=function(){
		//调用 查询时  微信支付系统  查询哪个订单的状态(未支付  已支付
		payService.queryPayStatus($scope.out_trade_no).success(
			function(response){
				if(response.flag){
					location.href="paysuccess.html#?money="+$scope.money;
				}else{

					if(response.message=='二维码超时' || response.message=='支付超时'){
						$scope.createNative();//重新生成二维码

					}else{
						alert(response.message);
						location.href="payfail.html";
					}

				}				
			}		
		);		
	}
	
	//获取金额
	$scope.getMoney=function(){
		return $location.search()['money'];
	}
	
});