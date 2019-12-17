package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.bean.UmsMemberReceiveAddress;

import java.util.List;


public interface UmsMemberService {

    List<UmsMember> selectAllUmsMember();

    List<UmsMember> selectAllUmsMemberMapper();

    UmsMember login(UmsMember umsMember);

    void putTokenCache(String token, String userId);

    String getTokenCache(String token);

    UmsMember isOUserExit(String uid);

    UmsMember addOuser(UmsMember umsMember);

    List<UmsMemberReceiveAddress> getAddressByUserId(String userId);

    UmsMemberReceiveAddress getAddressById(String addressId);

    void sendCartQueue(String id, String cartListCookie);
}
