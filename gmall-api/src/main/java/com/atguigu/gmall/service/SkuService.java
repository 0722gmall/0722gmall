package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PmsSkuInfo;
import org.springframework.stereotype.Service;

import java.util.List;


public interface SkuService {

    void saveSkuInfo(PmsSkuInfo pmsSkuInfo);

    PmsSkuInfo item(String skuId);

    PmsSkuInfo itemFromDb(String skuId);

    List<PmsSkuInfo> skuSaleAttrValueListBySpu(String spuId);

    List<PmsSkuInfo> getSkuForSearch();

    PmsSkuInfo getSkuById(String id);
}
