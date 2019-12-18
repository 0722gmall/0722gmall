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

import java.util.*;

@Controller
public class SearchController {

    @Reference
    SearchService searchService;

    @Reference
    AttrService attrService;

    @RequestMapping("/list.html")
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
            //面包屑
            List<PmsSearchCrumb> pmsSearchCrumbs=new ArrayList<>();
                for (String valueId : valueIds) {

                // 面包屑请求=当前请求-面包屑的valueID
                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                //面包屑url
                String urlParam = getMyUrlParam(pmsSearchParam, valueId);
                pmsSearchCrumb.setValueId(valueId);
                pmsSearchCrumb.setUrlParam(urlParam);

                //游标迭代
                Iterator<PmsBaseAttrInfo> iterator =pmsBaseAttrInfos.iterator();
                //判断下一个是否有值
                while (iterator.hasNext()){
                    PmsBaseAttrInfo pmsBaseAtrtInfo = iterator.next();
                    List<PmsBaseAttrValue> pmsBaseAttrValues = pmsBaseAtrtInfo.getAttrValueList();
                    for (PmsBaseAttrValue pmsBaseAttrValue : pmsBaseAttrValues) {
                        if (pmsBaseAttrValue.getId().equals(valueId)){
                            //面包屑名称
                            pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());
                            iterator.remove();
                        }

                    }

                }
                // 塞入面包屑
                pmsSearchCrumbs.add(pmsSearchCrumb);
                
            }
            //保存到页面上
            modelMap.put("attrValueSelectedList",pmsSearchCrumbs);

        }




/*

        //面包屑
        String[] valueIdSForCrumb = pmsSearchParam.getValueId();
        if (valueIdSForCrumb!=null&&valueIdSForCrumb.length>0){
            List<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();
            //面包屑请求=当前请求-面包屑valueId
            for (String valueIdForCrumb : valueIdSForCrumb) {
                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                //面包屑url
                String urlParam = getMyUrlParam(pmsSearchParam, valueIdForCrumb);
                pmsSearchCrumb.setValueId(valueIdForCrumb);
                pmsSearchCrumb.setUrlParam(urlParam);
                pmsSearchCrumb.setValueName("不知道");
                pmsSearchCrumbs.add(pmsSearchCrumb);

            }
            modelMap.put("attrValueSelectedList",pmsSearchCrumbs);

        }
*/


        if (pmsSearchSkuInfos!=null&&pmsBaseAttrInfos.size()>0){
            modelMap.put("attrList",pmsBaseAttrInfos);
        }

        //地址栏
        String urlParam=getMyUrlParam(pmsSearchParam);
        modelMap.put("urlParam",urlParam);
        modelMap.put("skuLsInfoList",pmsSearchSkuInfos);

        return "list";

    }

   /* //方法二
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
        
    }*/

    //方法一                                                    //可变参数
    private String getMyUrlParam(PmsSearchParam pmsSearchParam,String...valueIdForCrumb) {
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
            if (valueIdForCrumb!=null&&valueIdForCrumb.length>0){
                // 面包屑 = 当前url-面包屑自己valueId
                for (String valueId : valueIds) {
                    // 只有当前的valueIdForCrumb不等于要拼接的valueId的时候才加入url(不等于选中的面包屑时就拼接url)
                    if (!valueId.equals(valueIdForCrumb[0])){
                        urlParam=urlParam+"&valueId="+valueId;
                    }
                }
            }else {
                //地址栏
                for (String valueId : valueIds) {
                    urlParam=urlParam+"&valueId="+valueId;

                }
            }

        }


        return urlParam;

    }

}
