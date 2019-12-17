package com.atguigu.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.service.PaymentService;
import com.atguigu.gmall.util.ActiveMQUtil;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService{


    @Autowired
    ActiveMQUtil activeMQUtil;
    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Autowired
    AlipayClient alipayClient;

    //保存支付信息
    @Override
    public void updatePayment(PaymentInfo paymentInfo) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("orderSn",paymentInfo.getOrderSn());
        paymentInfoMapper.updateByExampleSelective(paymentInfo,example);

    }

    //保存支付信息
    @Override
    public void addPayment(PaymentInfo payment) {
        paymentInfoMapper.insertSelective(payment);

    }

    //// 发送一个消息，通知系统outtradeNo订单已经支付成功
    @Override
    public void sendPaySuccessQueue(PaymentInfo paymentInfo) {

        //工厂
        ConnectionFactory connectionFactory =activeMQUtil.getConnectionFactory();

        //连接
        Connection connection = null;
        Session session=null;
        try {
            connection = connectionFactory.createConnection();
            connection.start();

            //session
             session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue("PAY_SUCCESS_QUEUE");


            //发送对象
            MessageProducer producer = session.createProducer(queue);

           //存字符串
           TextMessage textMessage = new ActiveMQTextMessage();
           textMessage.setText(paymentInfo.getOrderSn());

           //延迟队列设置
            textMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,10*1000);

           //存键值对
            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("out_trade_no",paymentInfo.getOrderSn());
            mapMessage.setString("status","1");

            //持久化
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            //发送消息
            producer.send(mapMessage);

            //提交
            session.commit();
        } catch (JMSException e) {
            e.printStackTrace();
        }finally {
            try {
                session.close();
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }


        }





    }


    //发送检查支付结果的延迟队列
    @Override
    public void sendPayCheckQueue(PaymentInfo payment,long count) {

        //工厂
        ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();


        //连接

        Connection connection=null;
        Session session =null;
        try {
             connection = connectionFactory.createConnection();
             connection.start();

             //session
            session= connection.createSession(true, Session.AUTO_ACKNOWLEDGE);


            //发送对象
            Queue queue = session.createQueue("PAY_CHECK_QUEUE");
            MessageProducer producer = session.createProducer(queue);

           MapMessage mapMessage = new ActiveMQMapMessage();
           mapMessage.setString("out_trade_no",payment.getOrderSn());
           mapMessage.setLong("count",count);

           //延迟队列设置
           mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,10*1000);

           producer.send(mapMessage);

           session.commit();

        } catch (JMSException e) {
            e.printStackTrace();
        }

       finally {
            try {
                session.close();
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }






        System.out.println("发送检查支付结果的延迟队列");

    }

    @Override
    public PaymentInfo checkPayStatus(String out_trade_no) {
        System.out.println("调用支付宝的接口查询支付状态");

        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        Map<String, Object> paramMap = new HashMap<>();

        paramMap.put("out_trade_no",out_trade_no);
        String jsonString = JSON.toJSONString(paramMap);
        request.setBizContent(jsonString);

        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        //调用查询接口返回结果
        if(response.isSuccess()){
            System.out.println("调用成功");
            //封装返回结果
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOrderSn(out_trade_no);
            paymentInfo.setCallbackTime(new Date());
            paymentInfo.setAlipayTradeNo(response.getTradeNo());
            paymentInfo.setPaymentStatus(response.getTradeStatus());
            paymentInfo.setCallbackContent(response.toString());

            return paymentInfo;

        } else {
            System.out.println("调用失败");
            //封装返回结果
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOrderSn(out_trade_no);

            return paymentInfo;

        }


    }

    //幂等性 在更新之前先从数据库查询是否已经更新 再看后续操作
    @Override
    public boolean checkPayStatusFromDb(String out_trade_no) {
        boolean b=false;
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderSn(out_trade_no);
        PaymentInfo paymentInfo1 = paymentInfoMapper.selectOne(paymentInfo);
        if (paymentInfo1.getPaymentStatus()!=null&&(paymentInfo1.getPaymentStatus().equals("已支付")|| paymentInfo1.getPaymentStatus().equals("TRADE_SUCCESS"))){
            b=true;
        }

        return b;
    }
}
