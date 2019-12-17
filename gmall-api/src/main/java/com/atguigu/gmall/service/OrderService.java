package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OmsOrder;

public interface OrderService {
    void addOrder(OmsOrder omsOrder);

    boolean checkTradeCode(String userId, String tradeCode);

    String getTradeCode(String userId);


    OmsOrder getOrderByOutTradeNo(String outTradeNo);

    void updateOrder(OmsOrder omsOrder);

    void sendOrderSuccessQueue(OmsOrder omsOrder);
}
