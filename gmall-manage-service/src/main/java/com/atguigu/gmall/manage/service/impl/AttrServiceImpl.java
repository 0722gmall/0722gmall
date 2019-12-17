package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.PmsBaseAttrInfo;
import com.atguigu.gmall.bean.PmsBaseAttrValue;
import com.atguigu.gmall.manage.mapper.PmsBaseAttrInfoMapper;
import com.atguigu.gmall.manage.mapper.PmsBaseAttrValueMapper;
import com.atguigu.gmall.service.AttrService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Set;

@Service
public class AttrServiceImpl implements AttrService{
    @Autowired
    private PmsBaseAttrInfoMapper pmsBaseAttrInfoMapper;
    @Autowired
    private PmsBaseAttrValueMapper pmsBaseAttrValueMapper;

    @Override
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id) {
       //获取所有的平台属性
        PmsBaseAttrInfo pmsBaseAttrInfo=new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setCatalog3Id(catalog3Id);
       List<PmsBaseAttrInfo> pmsBaseAttrInfos=pmsBaseAttrInfoMapper.select(pmsBaseAttrInfo);

        //获取平台属性值
        for (PmsBaseAttrInfo baseAttrInfo : pmsBaseAttrInfos) {
            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
            List<PmsBaseAttrValue> pmsBaseAttrValues = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
            baseAttrInfo.setAttrValueList(pmsBaseAttrValues);


        }

        return pmsBaseAttrInfos;
    }

    @Override
    public void saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {

        //判断是否有主键
        if(pmsBaseAttrInfo.getId().isEmpty()){

            //插入平台属性
            pmsBaseAttrInfoMapper.insertSelective(pmsBaseAttrInfo);
            String attrId=pmsBaseAttrInfo.getId();
            List<PmsBaseAttrValue> attrValueList=pmsBaseAttrInfo.getAttrValueList();
            for(PmsBaseAttrValue pmsBaseAttrValue:attrValueList){
                pmsBaseAttrValue.setAttrId(attrId);
                //插入平台属性值
                pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue);
            }
        }else {
            // 修改平台属性
            pmsBaseAttrInfoMapper.updateByPrimaryKey(pmsBaseAttrInfo);
            String attrId=pmsBaseAttrInfo.getId();
            List<PmsBaseAttrValue> pmsBaseAttrValuelist=pmsBaseAttrInfo.getAttrValueList();
            for(PmsBaseAttrValue pmsBaseAttrValue:pmsBaseAttrValuelist){
                pmsBaseAttrValue.setAttrId(attrId);
               //修改平台属性值
                pmsBaseAttrValueMapper.updateByPrimaryKey(pmsBaseAttrValue);
            }
        }

    }

    @Override
    public List<PmsBaseAttrValue> getAttrValueList(String attrId) {

        //获取所有平台属性值
       PmsBaseAttrValue pmsBaseAttrValue=new PmsBaseAttrValue();
       pmsBaseAttrValue.setAttrId(attrId);
        List<PmsBaseAttrValue> pmsBaseAttrValues=pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
        return pmsBaseAttrValues;
    }

    @Override
    public List<PmsBaseAttrInfo> getAttrInfoListByValueIds(Set<String> valueIdsSet) {



          //添加"，"
          String valueIdsStr = StringUtils.join(valueIdsSet, ",");
            List<PmsBaseAttrInfo>  pmsBaseAttrInfos=pmsBaseAttrInfoMapper.selectAttrInfoListByValueIds(valueIdsStr);

        return pmsBaseAttrInfos;
    }


}

