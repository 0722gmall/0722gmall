package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PmsBaseCatalog1;
import com.atguigu.gmall.bean.PmsBaseCatalog2;
import com.atguigu.gmall.bean.PmsBaseCatalog3;
import com.atguigu.gmall.service.CatalogService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@CrossOrigin
public class CatalogController {

    @Reference
    private CatalogService catalogService;

    @RequestMapping("/getCatalog1")
    @ResponseBody
    public List<PmsBaseCatalog1> getCatalog1(){


        //查询一级分类
        // 调用业务实现
        List<PmsBaseCatalog1> catalog1s=catalogService.getCatalog1();
        return catalog1s;
    }

    @RequestMapping("/getCatalog2")
    @ResponseBody
    public List<PmsBaseCatalog2> getCatalog2(String catalog1Id){
         //查询二级分类
         //事务实现
        List<PmsBaseCatalog2>catalog2s=catalogService.getCatalog2(catalog1Id);
        return catalog2s;


    }

    @RequestMapping("/getCatalog3")
    @ResponseBody
    public List<PmsBaseCatalog3> getCatalog3(String catalog2Id){
       //查询三级分类
        //事务实现
        List<PmsBaseCatalog3>catalog3s=catalogService.getCatalog3(catalog2Id);
        return catalog3s;


    }


}
