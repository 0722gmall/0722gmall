package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;

import com.atguigu.gmall.bean.*;

import com.atguigu.gmall.service.SpuService;
import com.atguigu.gmall.util.MyFileUpload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@CrossOrigin
public class SpuController {

    @Reference
    private SpuService spuService;


    @RequestMapping("/spuSaleAttrList")
    @ResponseBody
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId){

       //获取spu的所有销售属性值列表
        List<PmsProductSaleAttr> pmsProductSaleAttrs=spuService.spuSaleAttrList(spuId);
        return  pmsProductSaleAttrs;
    }


    //获取spu的图片列表
    @RequestMapping("/spuImageList")
    @ResponseBody
    public List<PmsProductImage> spuImageList(String spuId){
        List<PmsProductImage> pmsProductImages=spuService.spuImageList(spuId);
        return  pmsProductImages;

    }



    @RequestMapping("/fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam("file")MultipartFile multipartFile){
        String  imageUrl="";
        //上传文件 返会url 路径
        imageUrl = MyFileUpload.uploadImage(multipartFile);
        return imageUrl;


    }





    @RequestMapping("/spuList")
    @ResponseBody
    public List<PmsProductInfo> spuList(String catalog3Id){

        //查询三级分类下的所有商品(spu)
        List<PmsProductInfo> pmsProductInfos=spuService.spuList(catalog3Id);
        return pmsProductInfos;


    }

    @RequestMapping("/baseSaleAttrList")
    @ResponseBody
    public List<PmsBaseSaleAttr> baseSaleAttrList(){

        //查询销售属性的集合
        List<PmsBaseSaleAttr> pmsBaseSaleAttrs=spuService. baseSaleAttrList();
        return pmsBaseSaleAttrs;
    }


    //saveSpuInfo
    @RequestMapping("/saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo){
      //添加spu
        spuService.saveSpuInfo(pmsProductInfo);
        return "success";
    }


}
