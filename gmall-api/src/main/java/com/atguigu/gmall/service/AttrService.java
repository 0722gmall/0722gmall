package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PmsBaseAttrInfo;
import com.atguigu.gmall.bean.PmsBaseAttrValue;

import java.util.List;
import java.util.Set;

public interface AttrService {
        List<PmsBaseAttrInfo> attrInfoList(String catalog3Id);

    void saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);

    List<PmsBaseAttrValue> getAttrValueList(String attrId);

    List<PmsBaseAttrInfo> getAttrInfoListByValueIds(Set<String> valueIdsSet);
}
