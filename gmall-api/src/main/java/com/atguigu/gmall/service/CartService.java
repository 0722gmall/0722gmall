package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OmsCartItem;

import java.util.List;

public interface CartService {
    OmsCartItem isCartExists(OmsCartItem omsCartItem);

    void updateCart(OmsCartItem omsCartItem1FormDb);

   OmsCartItem addCart(OmsCartItem omsCartItem);

    void combineCart(String userId);

    void updateCartForCache(OmsCartItem omsCartItemForCache);

    List<OmsCartItem> getCartListByUserId(String userId);

    void deleteCarts(List<String> skuIds, String userId);
}
