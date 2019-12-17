package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.*;

import com.atguigu.gmall.manage.mapper.*;
import com.atguigu.gmall.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class SpuServiceImpl implements SpuService {
    @Autowired
    private PmsProductInfoMapper pmsProductInfoMapper;
    @Autowired
    private PmsBaseSaleAttrMapper pmsBaseSaleAttrMapper;

    @Autowired
    private PmsProductSaleAttrMapper pmsProductSaleAttrMapper;

    @Autowired
    private PmsProductSaleAttrValueMapper pmsProductSaleAttrValueMapper;

    @Autowired
    private PmsProductImageMapper pmsProductImageMapper;

    @Override
    public List<PmsProductInfo> spuList(String  catalog3Id) {
        //获取三级分类下的所有平台商品()
       PmsProductInfo pmsProductInfo=new PmsProductInfo();
       pmsProductInfo.setCatalog3Id(catalog3Id);
        List<PmsProductInfo> pmsProductInfos=pmsProductInfoMapper.select(pmsProductInfo);
        return pmsProductInfos;
    }

    @Override
    public List<PmsBaseSaleAttr> baseSaleAttrList() {
       //查询所有的销售属性
        return pmsBaseSaleAttrMapper.selectAll();
    }

    @Override
    public void saveSpuInfo(PmsProductInfo pmsProductInfo) {
        //添加spu(pmsProductInfo)
        pmsProductInfoMapper.insertSelective(pmsProductInfo);
       //获取spu的id
        String spuId=pmsProductInfo.getId();

       //添加销售属性
        List<PmsProductSaleAttr> pmsProductSaleAttrs=pmsProductInfo.getSpuSaleAttrList();
        for(PmsProductSaleAttr pmsProductSaleAttr:pmsProductSaleAttrs){
            pmsProductSaleAttr.setProductId(spuId);
            pmsProductSaleAttrMapper.insertSelective(pmsProductSaleAttr);
            //添加销售属性值
            List<PmsProductSaleAttrValue> pmsProductSaleAttrValues=pmsProductSaleAttr.getSpuSaleAttrValueList();
            for(PmsProductSaleAttrValue pmsProductSaleAttrValue:pmsProductSaleAttrValues){
                pmsProductSaleAttrValue.setProductId(spuId);
                pmsProductSaleAttrValueMapper.insertSelective(pmsProductSaleAttrValue);

            }
        }


        //添加图片
        List<PmsProductImage> spuImageList = pmsProductInfo.getSpuImageList();
        for (PmsProductImage pmsProductImage : spuImageList) {
            pmsProductImage.setProductId(spuId);
            pmsProductImageMapper.insertSelective(pmsProductImage);

        }




    }




    //获取spu的销售属性(用自己写的sql)

    public List<PmsProductSaleAttr> spuSaleAttrListChecked(String spuId,String skuId){
        List<PmsProductSaleAttr> pmsProductSaleAttrs=pmsProductSaleAttrMapper.selectSpuSaleAttrListChecked(spuId,skuId);
        return  pmsProductSaleAttrs;
    }


    //获取spu的销售属性
    @Override
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId) {
        //新建一个销售属性
        PmsProductSaleAttr pmsProductSaleAttr=new PmsProductSaleAttr();
       //设置销售属性id
        pmsProductSaleAttr.setProductId(spuId);
        //通过销售属性id查询销售属性
        List<PmsProductSaleAttr> pmsProductSaleAttrs = pmsProductSaleAttrMapper.select(pmsProductSaleAttr);

        //遍历查到的所有销售属性
        for (PmsProductSaleAttr productSaleAttr : pmsProductSaleAttrs) {

            PmsProductSaleAttrValue pmsProductSaleAttrValue=new PmsProductSaleAttrValue();
            //为销售属性值设置spuId
            pmsProductSaleAttrValue.setProductId(spuId);
            //为销售属性值设置属性id
            pmsProductSaleAttrValue.setSaleAttrId(productSaleAttr.getSaleAttrId());
            //查询销售属性集合
            List<PmsProductSaleAttrValue> pmsProductSaleAttrValues = pmsProductSaleAttrValueMapper.select(pmsProductSaleAttrValue);
            //为销售属性赋值所有的属性值
            productSaleAttr.setSpuSaleAttrValueList(pmsProductSaleAttrValues);

        }
        //返回销售属性集合
        return pmsProductSaleAttrs;
    }

    //查询spu图片列表
    @Override
    public List<PmsProductImage> spuImageList(String spuId) {
        PmsProductImage pmsProductImage = new PmsProductImage();
       //为spu图片设置id
        pmsProductImage.setProductId(spuId);
        //查询spu图片列表
        List<PmsProductImage> pmsProductImages = pmsProductImageMapper.select(pmsProductImage);

        //返回 spu图片列表
        return pmsProductImages;
    }




}
