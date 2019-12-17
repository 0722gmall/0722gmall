package com.atguigu.gmall.item.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.PmsProductSaleAttr;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.bean.PmsSkuSaleAttrValue;
import com.atguigu.gmall.service.SkuService;

import com.atguigu.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;


import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@CrossOrigin
public class ItemController {


    @Reference
    private SkuService skuService;

    @Reference
    private SpuService spuService;

    @RequestMapping("/spuSaleAttrValueJson")
    @ResponseBody
    public String spuSaleAttrValueJson(String spuId){
        List<PmsSkuInfo> pmsSkuInfosForSaleAttrValues = skuService.skuSaleAttrValueListBySpu(spuId);
        Map<String,String> mapJson=new HashMap<>();
        //遍历sku
        for (PmsSkuInfo pmsSkuInfosForSaleAttrValue : pmsSkuInfosForSaleAttrValues) {
            //获取销售属性值列表
            List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfosForSaleAttrValue.getSkuSaleAttrValueList();

            String saleAttrValueKey="";
            //遍历销售属性值列表
            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
                //创建key(|123|456)
                saleAttrValueKey=saleAttrValueKey+"|"+pmsSkuSaleAttrValue.getSaleAttrValueId();
            }
            mapJson.put(saleAttrValueKey,pmsSkuInfosForSaleAttrValue.getId());

        }
        //mapJson转化json字符串
        String json=JSON.toJSONString(mapJson);
       // 生成一份静态的json文件存储起来
        File file=new File("F:\\0722gmall\\gmallGit\\0722gmall\\gmall-item-web\\src\\main\\resources\\static\\spu/spu_"+spuId+".json");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            fileOutputStream.write(json.getBytes());
           //关流
            fileOutputStream.close();

        } catch (Exception e) {

        }
  return json;

    }


    @RequestMapping("/{skuId}.html")
    //@PathVariable是spring3.0的一个新功能：接收请求路径中占位符的值
    public String item(@PathVariable String skuId,ModelMap map){

        //获取sku信息详情
        PmsSkuInfo pmsSkuInfo=skuService.item(skuId);
        //获取spuId
        String spuId = pmsSkuInfo.getSpuId();

        //获取销售属性
        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuService.spuSaleAttrListChecked(spuId,pmsSkuInfo.getId());

        //保存sku到域对象中
        map.put("skuInfo",pmsSkuInfo);
        //保存销售属性到域对象中
        map.put("spuSaleAttrListCheckBySku",pmsProductSaleAttrs);


        /*    //查询当前商品的销售属性值对应的hash表json串,根据当前的spu查询对应的组合列表
        List<PmsSkuInfo>pmsSkuInfosForSaleAttrValues=skuService.skuSaleAttrValueListBySpu(spuId);
        Map<String,String> mapJson=new HashMap<>();
        for (PmsSkuInfo pmsSkuInfosForSaleAttrValue : pmsSkuInfosForSaleAttrValues) {
            //遍历sku
             List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfosForSaleAttrValue.getSkuSaleAttrValueList();
            String saleAttrValueKey="";
            //遍历销售属性值
            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
                //创建key（|123|852）
                saleAttrValueKey=saleAttrValueKey+"|"+pmsSkuSaleAttrValue.getSaleAttrValueId();
            }
            //创建mapJson对象
            mapJson.put(saleAttrValueKey,pmsSkuInfosForSaleAttrValue.getId());

        }
        //保存到域对象中
        map.put("skuSaleAttrValueJson", JSON.toJSONString(mapJson));
*/
      map.put("spuId",spuId);

        return "item";
    }


    @RequestMapping("/test")
    public String test(ModelMap map){

        String hello="hello";
        map.put("hello",hello);


        List<String> list=new ArrayList<>();
        for(int i=0;i<5;i++){
            list.add("元素"+i);
        }
        map.put("list",list);
        map.put("flag","a");


        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setSkuName("测试名称");

        pmsSkuInfo=null;

        map.put("pmsSkuInfo",pmsSkuInfo);

        return "test";
    }



}
