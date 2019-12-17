package com.atguigu.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotation.LoginRequired;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.AttrService;
import com.atguigu.gmall.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Controller
public class SearchController {

    @Reference
    SearchService searchService;

    @Reference
    AttrService attrService;

    @RequestMapping("/list.html")
    @LoginRequired
    public String search(ModelMap modelMap, PmsSearchParam pmsSearchParam){
        List<PmsSearchSkuInfo> pmsSearchSkuInfos=searchService.search(pmsSearchParam);

        // 将查询结果sku中的属性值去重后抽取出来到一个集合中
        Set<String> valueIdsSet=new HashSet<>();
         for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();

             for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                 String valueId = pmsSkuAttrValue.getValueId();
                 //去重
                 valueIdsSet.add(valueId);

             }
        }


        // 根据属性值查询页面属性和属性值列表
        List<PmsBaseAttrInfo> pmsBaseAttrInfos=attrService.getAttrInfoListByValueIds(valueIdsSet);
         //根据选中的页面属性值删除对应的属性
        String[] valueIds = pmsSearchParam.getValueId();
        if (valueIds!=null&&valueIds.length>0){
            for (String valueId : valueIds) {
                //游标迭代
                Iterator<PmsBaseAttrInfo> iterator =pmsBaseAttrInfos.iterator();
                //判断下一个是否有值
                while (iterator.hasNext()){
                    PmsBaseAttrInfo pmsBaseAtrtInfo = iterator.next();
                    List<PmsBaseAttrValue> pmsBaseAttrValues = pmsBaseAtrtInfo.getAttrValueList();
                    for (PmsBaseAttrValue pmsBaseAttrValue : pmsBaseAttrValues) {
                        if (pmsBaseAttrValue.getId().equals(valueId)){
                            iterator.remove();
                        }

                    }

                }
            }
        }



        modelMap.put("attrList",pmsBaseAttrInfos);
        //属性的请求路径
        String urlParam=getMyUrlParam(pmsSearchParam);
        modelMap.put("urlParam",urlParam);
        modelMap.put("skuLsInfoList",pmsSearchSkuInfos);

        return "list";

    }

    private String getMyUrlParam(PmsSearchParam pmsSearchParam) {
     String urlParam="";
       //获取三级分类id
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        //获取关键字
        String keyword = pmsSearchParam.getKeyword();
        //获取属性id
        String[] valueIds = pmsSearchParam.getValueId();

        if (StringUtils.isNotBlank(catalog3Id)){
            urlParam=urlParam+"&catalog3Id="+catalog3Id;
        }

        if (StringUtils.isNotBlank(keyword)){
            urlParam=urlParam+"&keyword="+keyword;
        }

        if (valueIds!=null&&valueIds.length>0){
            for (String valueId : valueIds) {
                urlParam=urlParam+"&valueId="+valueId;

            }
        }

        if(StringUtils.isNotBlank(urlParam)){
            urlParam = urlParam.substring(1);
        }

        return urlParam;
        
    }

    /*private String getMyUrlParam(PmsSearchParam pmsSearchParam) {
        String urlParam="";
        //获取三级分类id
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        //获取关键字
        String keyword = pmsSearchParam.getKeyword();
        //获取属性id
        String[] valueIds = pmsSearchParam.getValueId();

        if (StringUtils.isNotBlank(catalog3Id)){
            //判断urlParam是否第一个，是第一个的话不加&
            if (StringUtils.isNotBlank(urlParam)){
                urlParam=urlParam+"&";
            }
            urlParam=urlParam+"catalog3Id="+catalog3Id;
        }

        if (StringUtils.isNotBlank(keyword)){
            if (StringUtils.isNotBlank(urlParam)){
                urlParam=urlParam+"&";
            }
            urlParam=urlParam+"keyword="+keyword;
        }

        if (valueIds!=null&&valueIds.length>0){
            for (String valueId : valueIds) {
                urlParam=urlParam+"&valueId="+valueId;

            }
        }


        return urlParam;

    }*/

}
