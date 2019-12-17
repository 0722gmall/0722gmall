package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.PmsBaseAttrInfo;
import com.atguigu.gmall.bean.PmsBaseCatalog1;
import com.atguigu.gmall.bean.PmsBaseCatalog2;
import com.atguigu.gmall.bean.PmsBaseCatalog3;
import com.atguigu.gmall.manage.mapper.PmsBaseCatalog1Mapper;
import com.atguigu.gmall.manage.mapper.PmsBaseCatalog2Mapper;
import com.atguigu.gmall.manage.mapper.PmsBaseCatalog3Mapper;
import com.atguigu.gmall.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
@Service
public class Catalog1ServiceImpl implements CatalogService{



    @Autowired
    private PmsBaseCatalog1Mapper pmsBaseCatalog1Mapper;

    @Autowired
    private PmsBaseCatalog2Mapper pmsBaseCatalog2Mapper;

    @Autowired
    private PmsBaseCatalog3Mapper pmsBaseCatalog3Mapper;

    @Override
    public List<PmsBaseCatalog1> getCatalog1() {
        //查询一级分类
        System.out.println(pmsBaseCatalog1Mapper.selectAll());
        return pmsBaseCatalog1Mapper.selectAll();

    }

    @Override
    public List<PmsBaseCatalog2> getCatalog2(String catalog1Id) {
        //查询二级分类
        PmsBaseCatalog2 pmsBaseCatalog2=new PmsBaseCatalog2();
        pmsBaseCatalog2.setCatalog1Id(catalog1Id);
        List<PmsBaseCatalog2> pmsBaseCatalog2s=pmsBaseCatalog2Mapper.select(pmsBaseCatalog2);
        return pmsBaseCatalog2s;
    }

    @Override
    public List<PmsBaseCatalog3> getCatalog3(String catalog2Id) {
      //查询三级分类
       PmsBaseCatalog3 pmsBaseCatalog3=new PmsBaseCatalog3();
       pmsBaseCatalog3.setCatalog2Id(catalog2Id);
        List<PmsBaseCatalog3> pmsBaseCatalog3s=pmsBaseCatalog3Mapper.select(pmsBaseCatalog3);

        return pmsBaseCatalog3s;
    }
}
