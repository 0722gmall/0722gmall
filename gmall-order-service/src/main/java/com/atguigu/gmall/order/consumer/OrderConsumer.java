package com.atguigu.gmall.order.consumer;

import com.alibaba.dubbo.rpc.cluster.merger.MapMerger;
import com.atguigu.gmall.bean.OmsOrder;
import com.atguigu.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Date;

@Component
public class OrderConsumer {

    @Autowired
    OrderService orderService;


   //监听支付成功队列
                 //消费支付成功的队列名称                               //消费支付成功的连接池
    @JmsListener(destination = "PAY_SUCCESS_QUEUE",containerFactory = "jmsQueueListener")
    public void consumerPaySuccessQueue(MapMessage mapMessage){

        String out_trade_no=null;
        String status=null;

        try {
            out_trade_no = mapMessage.getString("out_trade_no");
             status = mapMessage.getString("status");

            OmsOrder omsOrder = new OmsOrder();
            omsOrder.setOrderSn(out_trade_no);
            omsOrder.setStatus(status);
            omsOrder.setPaymentTime(new Date());
            //更新
            orderService.updateOrder(omsOrder);

            //发送订单已经支付的队列，由库存系统消费
            orderService.sendOrderSuccessQueue(omsOrder);

        } catch (JMSException e) {
            e.printStackTrace();
        }
        System.out.println("订单支付成功的队列"+out_trade_no);




    }

}
