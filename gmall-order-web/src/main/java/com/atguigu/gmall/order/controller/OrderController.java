package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotation.LoginRequired;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.bean.OmsOrder;
import com.atguigu.gmall.bean.OmsOrderItem;
import com.atguigu.gmall.bean.UmsMemberReceiveAddress;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.UmsMemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class OrderController {

    @Reference
    UmsMemberService umsMemberService;

    @Reference
    CartService cartService;

    @Reference
    OrderService orderService;

    @LoginRequired
    @RequestMapping("/toTrade")
    public String toTrade(HttpServletRequest request, ModelMap modelMap){
        //获取用户id
        String userId= (String) request.getAttribute("userId");


        //获取当前用户的收货地址列表
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses=umsMemberService.getAddressByUserId(userId);


        //获取当前用户要结算的商品
        List<OmsCartItem> omsCartItems = cartService.getCartListByUserId(userId);

        //迭代遍历所有的商品
        Iterator<OmsCartItem> iterator = omsCartItems.iterator();
        while (iterator.hasNext()){
            OmsCartItem omsCartItem = iterator.next();
            //没有被选中的商品remove掉
            if (!omsCartItem.getIsChecked().equals("1")){
                iterator.remove();
            }

        }
        //创建一个订单详情集合
        List<OmsOrderItem> omsOrderItems=new ArrayList<>();
        for (OmsCartItem omsCartItem : omsCartItems) {
            OmsOrderItem omsOrderItem = new OmsOrderItem();
            // 将购物车对象封装成订单详情对象
          omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
          omsOrderItem.setProductPic(omsCartItem.getProductPic());
          omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
          omsOrderItem.setProductName(omsCartItem.getProductName());
          omsOrderItem.setProductPrice(omsCartItem.getPrice());
          omsOrderItem.setProductId(omsCartItem.getProductId());
          omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());

          omsOrderItems.add(omsOrderItem);


        }

        //保存订单详情到页面
        modelMap.put("orderDetailList",omsOrderItems);
        //保存收货地址到页面
        modelMap.put("userAddressList",umsMemberReceiveAddresses);

        //保存总价钱到页面
        modelMap.put("totalAmount",getMyCartAmount(omsCartItems));

        //获取订单码(根据用户id生成tradeCode)
        String tradeCode=orderService.getTradeCode(userId);

        //保存tradecode到页面
        modelMap.put("tradeCode",tradeCode);






        return "trade";

    }

    //购买商品的总价钱
    private BigDecimal getMyCartAmount(List<OmsCartItem> omsCartItems) {
        BigDecimal amount = new BigDecimal("0");
        for (OmsCartItem omsCartItem : omsCartItems) {
           if (omsCartItem.getIsChecked().equals("1")){
               amount=amount.add(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
           }

        }
        return amount;
    }




    @RequestMapping("submitOrder")
    @LoginRequired
    public String submitOrder(HttpServletRequest request,String addressId,String tradeCode){
        String userId= (String) request.getAttribute("userId");
        String nickname = (String) request.getAttribute("nickname");
        boolean b=orderService.checkTradeCode(userId,tradeCode);


      UmsMemberReceiveAddress umsMemberReceiveAddress=umsMemberService.getAddressById(addressId);
      if (b){
          //生成订单数据
          OmsOrder omsOrder = new OmsOrder();
          List<OmsCartItem> omsCartItems = cartService.getCartListByUserId(userId);
          String outTradeNo="atguigu0722";
          SimpleDateFormat slf = new SimpleDateFormat("yyyyMMddHHmmss");
          String nowTime = slf.format(new Date());
          long currentTimeMillis = System.currentTimeMillis();
          outTradeNo+=nowTime+currentTimeMillis;
          omsOrder.setOrderSn(outTradeNo);//设置外部订单号
          omsOrder.setStatus("0");
          omsOrder.setPayType(2);
          omsOrder.setSourceType(1);
          omsOrder.setOrderType(1);
          omsOrder.setNote("鼓励商城测试订单");
          omsOrder.setCreateTime(new Date());
          omsOrder.setTotalAmount(getMyCartAmount(omsCartItems));
          omsOrder.setReceiverProvince(umsMemberReceiveAddress.getProvince());
          omsOrder.setReceiverRegion(umsMemberReceiveAddress.getRegion());
          omsOrder.setReceiverDetailAddress(umsMemberReceiveAddress.getDetailAddress());
          omsOrder.setReceiverCity(umsMemberReceiveAddress.getCity());
          omsOrder.setReceiverName(umsMemberReceiveAddress.getName());
          omsOrder.setReceiverPostCode("22222");
          omsOrder.setPayAmount(getMyCartAmount(omsCartItems));
          omsOrder.setMemberUsername(nickname);
          omsOrder.setMemberId(userId);
          omsOrder.setReceiverPhone(umsMemberReceiveAddress.getPhoneNumber());


          //封装订单详情
          //删掉未被选中的
          Iterator<OmsCartItem> iterator = omsCartItems.iterator();
          while (iterator.hasNext()){
              OmsCartItem omsCartItem = iterator.next();
              if (!omsCartItem.getIsChecked().equals("1")){
                  iterator.remove();
              }
          }

          List<OmsOrderItem>  omsOrderItems= new ArrayList<>();
          List<String>skuIds=new ArrayList<>();
          for (OmsCartItem omsCartItem : omsCartItems) {
              OmsOrderItem omsOrderItem = new OmsOrderItem();
              omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());
              omsOrderItem.setProductId(omsCartItem.getProductId());
              omsOrderItem.setProductName(omsCartItem.getProductName());
              omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
              omsOrderItem.setProductPic(omsCartItem.getProductPic());
              omsOrderItem.setProductPrice(omsCartItem.getPrice());
              omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
              omsOrderItem.setOrderSn(outTradeNo);

              omsOrderItems.add(omsOrderItem);

              skuIds.add(omsCartItem.getProductSkuId());

          }
          omsOrder.setOmsOrderItems(omsOrderItems);
          orderService.addOrder(omsOrder);

          //删除已经提交的购物车数据
          //cartService.deleteCarts(skuIds,userId);


          //成功跳到支付页面
          return "redirect:http://payment.gmall.com:8087/index?outTradeNo="+outTradeNo+"&totalAmount="+getMyCartAmount(omsCartItems);


      }else {
          //失败跳到失败页面
          return "tradeFail";
      }


    }
}
