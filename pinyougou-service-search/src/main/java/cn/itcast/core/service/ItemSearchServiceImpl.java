package cn.itcast.core.service;

import cn.itcast.core.pojo.item.Item;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 搜索管理
 */
@Service
public class ItemSearchServiceImpl implements ItemsearchService {

    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;
    //定义搜索对象的结构  category:商品分类
    // $scope.searchMap={'keywords':'   三       星     手        机    ','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sort':'','sortField':''};

    //开始搜索
    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {

        //处理关键词

         searchMap.put("keywords",searchMap.get("keywords").replaceAll(" ",""));

        Map<String, Object> resultMap = new HashMap<>();
        //1:商品分类结果集
        List<String> categoryListByKeywords = findCategoryListByKeywords(searchMap);
        resultMap.put("categoryList", categoryListByKeywords);

        if (null != categoryListByKeywords && categoryListByKeywords.size() > 0) {

            Object typeId = redisTemplate.boundHashOps("itemCat").get(categoryListByKeywords.get(0));
            //2:品牌结果集
            List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);
            //3:规格结果集
            List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);

            resultMap.put("brandList", brandList);
            resultMap.put("specList", specList);
        }
        //4:查询结果集 总条数 总页数
        resultMap.putAll(search2(searchMap));
        //查询高亮结果集
        return resultMap;

    }

    //根据关键字查询商品分类结果集
    public List<String> findCategoryListByKeywords(Map<String, String> searchMap) {

        //关键词
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        Query query = new SimpleQuery(criteria);
        //根据商品分类进行分组查询
        GroupOptions groupOptions = new GroupOptions();
        groupOptions.addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //执行查询
        GroupPage<Item> page = solrTemplate.queryForGroupPage(query, Item.class);

        //查询分组结果
        GroupResult<Item> item_category = page.getGroupResult("item_category");

        Page<GroupEntry<Item>> groupEntries = item_category.getGroupEntries();

        List<String> categoryList = new ArrayList<>();
        List<GroupEntry<Item>> content = groupEntries.getContent();
        for (GroupEntry<Item> entry : content) {
            categoryList.add(entry.getGroupValue());
        }

        return categoryList;

    }

    //查询高亮结果集
    public Map<String, Object> search2(Map<String, String> searchMap) {
        Map<String, Object> resultMap = new HashMap<>();
        //关键词
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        HighlightQuery highlightQuery = new SimpleHighlightQuery(criteria);
        //过滤条件
        //判断过滤条件是有值
        //商品分类
        if (null != searchMap.get("category") && !"".equals(searchMap.get("category"))) {
            //商品分类添加到过滤条件中
            FilterQuery filterQuery = new SimpleFilterQuery();
            filterQuery.addCriteria(new Criteria("item_category").is(searchMap.get("category")));
            highlightQuery.addFilterQuery(filterQuery);
        }
        //品牌
        if (null != searchMap.get("brand") && !"".equals(searchMap.get("brand"))) {

            FilterQuery filterQuery = new SimpleFilterQuery();
            filterQuery.addCriteria(new Criteria("item_brand").is(searchMap.get("brand")));
            highlightQuery.addFilterQuery(filterQuery);

        }
        //价格区间
        if (null != searchMap.get("price") && !"".equals(searchMap.get("price"))) {

            String[] p = searchMap.get("price").split("-");

            FilterQuery filterQuery = new SimpleFilterQuery();//0-500  3000-*
            if ("*".equals(p[1])) {
                filterQuery.addCriteria(new Criteria("item_price").greaterThanEqual(p[0]));
            } else {
                filterQuery.addCriteria(new Criteria("item_price").between(p[0], p[1], true, false));
            }
            highlightQuery.addFilterQuery(filterQuery);
        }
        //定义搜索对象的结构  category:商品分类

        //$scope.searchMap={'sort':'','sortField':''};


//        "item_spec_网络": "联通3G",
//        "item_spec_机身内存": "16G",
        //规格
        if(null != searchMap.get("spec") && !"".equals(searchMap.get("spec"))){
            Map<String,String> specMap = JSON.parseObject(searchMap.get("spec"), Map.class);
            Set<Map.Entry<String, String>> entries = specMap.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                FilterQuery filterQuery = new SimpleFilterQuery();
                filterQuery.addCriteria(new Criteria("item_spec_" + entry.getKey()).is(entry.getValue()));
                highlightQuery.addFilterQuery(filterQuery);
            }
        }
        //排序

        //定义搜索对象的结构  category:商品分类
        //$scope.searchMap={'sort':'ASC或DESC','sortField':'price或updatetime'};
        if(null != searchMap.get("sort") && !"".equals(searchMap.get("sort"))){

            //1:先自营 与 销量
            if("DESC".equals(searchMap.get("sort"))){
                highlightQuery.addSort(new Sort(Sort.Direction.DESC,"item_" + searchMap.get("sortField")));//"item_updatetime"  item_price
            }else{
                highlightQuery.addSort(new Sort(Sort.Direction.ASC,"item_" + searchMap.get("sortField")));

            }


        }







        //分页
        String pageNo = searchMap.get("pageNo");
        String pageSize = searchMap.get("pageSize");
        highlightQuery.setOffset((Integer.parseInt(pageNo) - 1) * Integer.parseInt(pageSize));
        highlightQuery.setRows(Integer.parseInt(pageSize));

        //高亮
        HighlightOptions highlightOptions = new HighlightOptions();
        //设置高亮的域
        highlightOptions.addField("item_title");
        //前缀
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        //后缀
        highlightOptions.setSimplePostfix("</em>");

        highlightQuery.setHighlightOptions(highlightOptions);

        //执行查询
        HighlightPage<Item> page = solrTemplate.queryForHighlightPage(highlightQuery, Item.class);

        //高亮的数据  entity=Item对象的值
        List<HighlightEntry<Item>> highlighted = page.getHighlighted();
        for (HighlightEntry<Item> itemHighlightEntry : highlighted) {

            //Item对象的值
            Item entity = itemHighlightEntry.getEntity();

            List<HighlightEntry.Highlight> highlights = itemHighlightEntry.getHighlights();

            if (null != highlights && highlights.size() > 0) {
                //有高亮名称
                entity.setTitle(highlights.get(0).getSnipplets().get(0));
            }
        }

        //分页结果集
        List<Item> content = page.getContent();

        resultMap.put("rows", content);
        //查询总条数
        resultMap.put("total", page.getTotalElements());
        //总页数
        resultMap.put("totalPages", page.getTotalPages());
        return resultMap;
    }

    //查询普通结果集
    public Map<String, Object> search1(Map<String, String> searchMap) {

        Map<String, Object> resultMap = new HashMap<>();
        //关键词
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        Query query = new SimpleQuery(criteria);
        //过滤条件
        //排序
        //分页
        String pageNo = searchMap.get("pageNo");
        String pageSize = searchMap.get("pageSize");
        query.setOffset((Integer.parseInt(pageNo) - 1) * Integer.parseInt(pageSize));
        query.setRows(Integer.parseInt(pageSize));

        //高亮

        //执行查询
        ScoredPage<Item> page = solrTemplate.queryForPage(query, Item.class);

        //分页结果集
        List<Item> content = page.getContent();

        resultMap.put("rows", content);
        //查询总条数
        resultMap.put("total", page.getTotalElements());
        //总页数
        resultMap.put("totalPages", page.getTotalPages());
        return resultMap;
    }

}
