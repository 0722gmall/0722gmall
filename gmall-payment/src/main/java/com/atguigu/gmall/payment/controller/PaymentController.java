package com.atguigu.gmall.payment.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.annotation.LoginRequired;
import com.atguigu.gmall.bean.OmsOrder;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.util.HttpClient;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.PaymentService;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.swing.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {


    @Autowired
    PaymentService paymentService;

    @Autowired
    AlipayClient alipayClient;

    @Reference
    OrderService orderService;





    //# 同步回调地址 重定向地址本地浏览器
    @RequestMapping("alipay/callback/return")
    @LoginRequired
    public String callBackReturn(String outTradeNo , ModelMap map, HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");
        String nickname = (String) request.getAttribute("nickname");

        //获取订单号
        String out_trade_no = request.getParameter("out_trade_no");
        //获取支付宝支付编号
        String trade_no = request.getParameter("trade_no");
        //获取支付状态
        String trade_status = request.getParameter("trade_status");
        //获取签名
        String sign = request.getParameter("sign");

       //幂等性先从数据库查询是否已经更新，再看是否需要执行
        boolean b=paymentService.checkPayStatusFromDb(out_trade_no);
        if(!b){
            // 更新支付信息
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setCallbackTime(new Date());
            paymentInfo.setAlipayTradeNo(trade_no);
            paymentInfo.setCallbackContent(request.getQueryString());
            paymentInfo.setOrderSn(out_trade_no);
            paymentService.updatePayment(paymentInfo);

            // 发送系统消息给订单，更新订单信息(已支付)
            paymentService.sendPaySuccessQueue(paymentInfo);

        }





        return "finish";
    }




    //微信支付
    @RequestMapping("wx/submit")
    @ResponseBody
    @LoginRequired
    public Map wxSubmit(String outTradeNo,HttpServletRequest request){
        // 做一个判断：支付日志中的订单支付状态 如果是已支付，则不生成二维码直接重定向到消息提示页面！
        // 调用服务层数据
        // 第一个参数是订单Id ，第二个参数是多少钱，单位是分
        //微信的订单号不能超过32位
        if (outTradeNo.length()>32){
             outTradeNo = outTradeNo.substring(0, 32);
        }
        Map map=createNative(outTradeNo+"","1");

        System.out.println(map.get("code_url"));

        return  map;

    }

    //生成微信二维码的方法
    private Map createNative(String outTradeNo, String money1) {
        // 服务号Id
        String appid = "wxf913bfa3a2c7eeeb";
        // 商户号Id
        String partner = "1543338551";
        // 密钥
        String partnerkey = "atguigu3b0kn9g5v426MKfHQH7X8rKwb";
        //1.创建参数
        Map<String, String> param = new HashMap();//创建参数
        param.put("appid", appid);//公众号
        param.put("mch_id", partner);//商户号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        param.put("body", "尚硅谷");//商品描述
        param.put("out_trade_no", outTradeNo);//商户订单号
        param.put("total_fee", money1);//总金额（分）
        param.put("spbill_create_ip", "127.0.0.1");//IP
        param.put("notify_url", " http://2z72m78296.wicp.vip/wx/callback/notify");//回调地址(随便写)
        param.put("trade_type", "NATIVE");//交易类型


        try {
            //2生成要发送的xml
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println(xmlParam);
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");

             httpClient.setHttps(true);
             httpClient.setXmlParam(xmlParam);
             httpClient.post();

             //3.获得结果
            String result = httpClient.getContent();
            System.out.println(result);
            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
            HashMap<String, String> map = new HashMap<>();
            //支付地址
            map.put("code_url",resultMap.get("code_url"));
            //总金额
            map.put("total_fee",money1);
            //订单号
            map.put("out_trade_no",outTradeNo);

            return map;


        } catch (Exception e) {
            e.printStackTrace();
            return  new HashMap();
        }

    }

    //支付宝支付
    @ResponseBody
    @RequestMapping("alipay/submit")
    @LoginRequired
    public String index(String outTradeNo , ModelMap map, HttpServletRequest request){

        OmsOrder omsOrder = orderService.getOrderByOutTradeNo(outTradeNo);

        String userId = (String) request.getAttribute("userId");
        String nickname = (String) request.getAttribute("nickname");

        // 支付宝的客户端，调用统一收单下单页面支付接口
        //创建API对应的request
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();

        //在公共参数中设置回跳和通知地址
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);

        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("out_trade_no",outTradeNo);
        paramMap.put("product_code","FAST_INSTANT_TRADE_PAY");
        paramMap.put("total_amount",0.01);
        paramMap.put("subject",omsOrder.getOmsOrderItems().get(0).getProductName());

        String json = JSON.toJSONString(paramMap);


        //填充业务参数
        alipayRequest.setBizContent(json);
        String form="";
        try {
            //调用SDK生成表单
            form = alipayClient.pageExecute(alipayRequest).getBody();
            System.out.println(form);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }




        // 生成和保存支付信息
        PaymentInfo payment = new PaymentInfo();
        payment.setOrderSn(outTradeNo);
        payment.setPaymentStatus("未支付");
        payment.setSubject(omsOrder.getOmsOrderItems().get(0).getProductName());
        payment.setTotalAmount(omsOrder.getTotalAmount());
        payment.setCreateTime(new Date());
        payment.setOrderId(omsOrder.getId());
        //添加
        paymentService.addPayment(payment);

        //发送检查支付状态的延迟队列（定时器）
        paymentService.sendPayCheckQueue(payment,7);

        // 返回form表单给页面
        return form;
    }

    @RequestMapping("index")
    @LoginRequired
    public String index(String outTradeNo , BigDecimal totalAmount, ModelMap map, HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");
        String nickname = (String) request.getAttribute("nickname");
        map.put("outTradeNo",outTradeNo);
        map.put("totalAmount",totalAmount);
        return "index";
    }



}
