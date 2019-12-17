package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.OmsOrder;
import com.atguigu.gmall.bean.OmsOrderItem;
import com.atguigu.gmall.order.mapper.OmsOrderItemMapper;
import com.atguigu.gmall.order.mapper.OmsOrderMapper;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.util.ActiveMQUtil;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OmsOrderMapper omsOrderMapper;

    @Autowired
    OmsOrderItemMapper omsOrderItemMapper;

    @Autowired
    RedisUtil redisUtil;
    
    @Autowired
    ActiveMQUtil activeMQUtil;
    


    //添加订单
    @Override
    public void addOrder(OmsOrder omsOrder) {
        omsOrderMapper.insertSelective(omsOrder);
        String orderId = omsOrder.getId();

        List<OmsOrderItem> omsOrderItems = omsOrder.getOmsOrderItems();
        for (OmsOrderItem omsOrderItem : omsOrderItems) {
            omsOrderItem.setOrderId(orderId);
            omsOrderItemMapper.insertSelective(omsOrderItem);

        }



    }

    //根据页面用户id和订单码对比缓存中的tradeCode
    @Override
    public boolean checkTradeCode(String userId, String tradeCode) {
        boolean b=false;
        Jedis jedis =redisUtil.getJedis();
        String tradeCodeFromCache = jedis.get("user:"+userId+":tradeCode");
        //判断页面传来的tradeCode和缓存中的tradeCodeFromCache是否一样
        if (StringUtils.isNotBlank(tradeCodeFromCache)&&tradeCodeFromCache.equals(tradeCode)){
            b=true;
            // 校验交易码后删除交易码
           jedis.del("user:"+userId+":tradeCode");
        }
        jedis.close();


        return b;
    }

    //根据用户id生成tradeCode（订单码）
    @Override
    public String getTradeCode(String userId) {
        String tradeCode = UUID.randomUUID().toString();
        Jedis jedis = redisUtil.getJedis();
        jedis.setex("user:" + userId + ":tradeCode", 60 * 30, tradeCode);
        jedis.close();
        return tradeCode;
    }

    //根据订单号查询订单
    @Override
    public OmsOrder getOrderByOutTradeNo(String outTradeNo) {
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(outTradeNo);
        OmsOrder omsOrderResult= omsOrderMapper.selectOne(omsOrder);

        OmsOrderItem omsOrderItem = new OmsOrderItem();
        omsOrderItem.setOrderSn(outTradeNo);
        omsOrderResult.setOmsOrderItems(omsOrderItemMapper.select(omsOrderItem));

        return omsOrderResult;
    }

    //更新订单
    @Override
    public void updateOrder(OmsOrder omsOrder) {

        Example example = new Example(OmsOrder.class);
        example.createCriteria().andEqualTo("orderSn",omsOrder.getOrderSn());

        OmsOrder omsOrderFromUpdate = new OmsOrder();
        omsOrderFromUpdate.setStatus(omsOrder.getStatus());
        omsOrderFromUpdate.setPaymentTime(omsOrder.getPaymentTime());

        //更新订单
        omsOrderMapper.updateByExampleSelective(omsOrderFromUpdate,example);

    }

    //订单接收到支付发送订单已经支付的队列给订单消费，然后修改，然后订单再发送订单已经支付的队列库存，由库存系统消费
    @Override
    public void sendOrderSuccessQueue(OmsOrder omsOrder) {

        //工厂
        ConnectionFactory factory = activeMQUtil.getConnectionFactory();

        Session session =null;
        Connection connection =null;

        try {
            //连接
            connection=factory.createConnection();
            connection.start();

            //session
            session=connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue("ORDER_SUCCESS_QUEUE");

            //发送对象
            MessageProducer producer = session.createProducer(queue);

            TextMessage textMessage = new ActiveMQTextMessage();

            OmsOrder omsOrderParam = new OmsOrder();
            omsOrderParam.setOrderSn(omsOrder.getOrderSn());
            OmsOrder omsOrderResult = omsOrderMapper.selectOne(omsOrderParam);

            OmsOrderItem omsOrderItem = new OmsOrderItem();
            omsOrderItem.setOrderSn(omsOrder.getOrderSn());
            List<OmsOrderItem> omsOrderItems = omsOrderItemMapper.select(omsOrderItem);

            omsOrderResult.setOmsOrderItems(omsOrderItems);
            textMessage.setText(JSON.toJSONString(omsOrderResult));

            producer.send(textMessage);

            //session提交
            session.commit();


        } catch (JMSException e) {
            //sesiion回滚
            try {
                session.rollback();
            } catch (JMSException e1) {
                e1.printStackTrace();
            }

            e.printStackTrace();
        }finally {
            try {
                session.close();
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }

        }

        System.out.println("输出检查支付结果的延迟队列");

    }


}
