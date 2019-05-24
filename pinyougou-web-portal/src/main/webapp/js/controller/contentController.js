app.controller("contentController",function($scope,contentService){

	//数组  保存首页上所有广告集合
	$scope.contentList = [];


	// 根据分类ID查询广告的方法:
	$scope.findByCategoryId = function(categoryId){

		//根据广告分类的Id 查询此位置上的所有广告集合
		contentService.findByCategoryId(categoryId).success(function(response){
			$scope.contentList[categoryId] = response;
		});
	}


	
	//搜索  （传递参数）
	$scope.search=function(){
		location.href="http://localhost:9103/search.html#?keywords="+$scope.keywords;
	}
	
});