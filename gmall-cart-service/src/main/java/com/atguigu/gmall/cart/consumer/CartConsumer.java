package com.atguigu.gmall.cart.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.service.CartService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.List;

@Component
public class CartConsumer {

    @Autowired
    CartService cartService;

    @JmsListener(destination = "CART_QUEUE",containerFactory = "jmsQueueListener")
    public void consumerCartQueue(MapMessage mapMessage){

        String userId =null;
        String cartListCookie =null;

        try {
            userId=mapMessage.getString("userId");
            cartListCookie=mapMessage.getString("cartListCookie");

            //将cartListCookie字符串转化为OmsCartItem集合
            List<OmsCartItem> omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);

            //遍历omsCartItems
            for (OmsCartItem omsCartItem : omsCartItems) {
                //判断登录状态下购物车中是否有该商品，有则修改数量  调用updateCart方法
                OmsCartItem cartItemFromDb = cartService.isCartExists(omsCartItem);
                if (cartItemFromDb!=null&& StringUtils.isNotBlank(cartItemFromDb.getId())){
                    //设置数量
                    cartItemFromDb.setQuantity(cartItemFromDb.getQuantity().add(omsCartItem.getQuantity()));

                    //设置总价
                    cartItemFromDb.setTotalPrice(cartItemFromDb.getPrice().multiply(cartItemFromDb.getQuantity()));

                    cartService.updateCart(cartItemFromDb);

                }else {
                    //购物车中没有该商品，新增omsCartItem 调用addCart方法

                    //设置总价
                    omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));

                    //设置用户id
                    omsCartItem.setMemberId(userId);

                    cartService.addCart(omsCartItem);

                }

            }

        } catch (JMSException e) {
            e.printStackTrace();
        }

        System.out.println("合并购物车队列");

    }
}
