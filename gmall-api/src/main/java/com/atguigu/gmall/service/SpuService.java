package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.*;

import java.util.List;

public interface SpuService {
    List<PmsProductInfo> spuList(String catalog3Id);

    List<PmsBaseSaleAttr> baseSaleAttrList();


    void saveSpuInfo(PmsProductInfo pmsProductInfo);

    List<PmsProductSaleAttr> spuSaleAttrList(String spuId);

    List<PmsProductImage> spuImageList(String spuId);

    List<PmsProductSaleAttr> spuSaleAttrListChecked(String spuId,String skuId);
}
