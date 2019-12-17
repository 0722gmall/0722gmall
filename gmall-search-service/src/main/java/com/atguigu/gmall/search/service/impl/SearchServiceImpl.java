package com.atguigu.gmall.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.PmsSearchParam;
import com.atguigu.gmall.bean.PmsSearchSkuInfo;
import com.atguigu.gmall.service.SearchService;
import io.searchbox.client.JestClient;


import io.searchbox.client.JestResult;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    JestClient jestClient;


    @Override
    public List<PmsSearchSkuInfo> search(PmsSearchParam pmsSearchParam) {
        List<PmsSearchSkuInfo> pmsSearchSkuInfos=new ArrayList<>();
        //PmsSearchSkuInfo pmsSearchSkuInfo=new PmsSearchSkuInfo();
        //封装搜索方法
        String query=getMyQuery(pmsSearchParam);
        System.out.println(query);
        //创建一个查询容器
        Search search = new Search.Builder(query).addIndex("gmall0722").addType("PmsSearchSkuInfo").build();

        try {
            SearchResult execute = jestClient.execute(search);
            if (execute!=null){
                //获取所PmsSearchSkuInfos
                List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = execute.getHits(PmsSearchSkuInfo.class);
                //遍历PmsSearchSkuInfos
                for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
                    //判断
                    if (hit!=null){
                        //获取pmsSearchSkuInfo
                        PmsSearchSkuInfo pmsSearchSkuInfo = hit.source;
                        pmsSearchSkuInfos.add(pmsSearchSkuInfo);
                    }


                    
                }
            }

            
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pmsSearchSkuInfos;
    }


    private String getMyQuery(PmsSearchParam pmsSearchParam){
       //创建一个搜索容器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //获取三级分类id
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        //获取keyword
        String keyword = pmsSearchParam.getKeyword();
        //获取平台属性值id
        String[] valueIds = pmsSearchParam.getValueId();
      //创建一个过滤搜索容器
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //判断三级分类id是否为空
        if(StringUtils.isNotBlank(catalog3Id)){
            //过滤三级分类
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id",catalog3Id);
            boolQueryBuilder.filter(termQueryBuilder);
        }
        //判断keyword是否为空
        if (StringUtils.isNotBlank(keyword)){
            //按sku名字搜索(关键字)
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName",keyword);
            boolQueryBuilder.must(matchQueryBuilder);
        }

        //判断属性值id是否为空和长度
        if (valueIds!=null&&valueIds.length>0){
            //遍历属性值id
            for (String valueId : valueIds) {
                //过滤属性值
                TermsQueryBuilder termsQueryBuilder = new TermsQueryBuilder("skuAttrValueList.valueId",valueId);
              boolQueryBuilder.filter(termsQueryBuilder);
            }

        }
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(20);
        return searchSourceBuilder.toString();



    }
}
