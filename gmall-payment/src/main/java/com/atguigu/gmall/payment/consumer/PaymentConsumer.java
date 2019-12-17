package com.atguigu.gmall.payment.consumer;


import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class PaymentConsumer {

    @Autowired
    PaymentService paymentService;

    @JmsListener(destination = "PAY_CHECK_QUEUE", containerFactory = "jmsQueueListener")
    public void consumerPayCheckQueue(MapMessage mapMessage) throws JMSException {


        String out_trade_no = mapMessage.getString("out_trade_no");
        long count = mapMessage.getLong("count");

        PaymentInfo paymentInfo = paymentService.checkPayStatus(out_trade_no);


        System.out.println(" 消费延迟检查，检查支付结果");

        //支付成功

        if (paymentInfo.getPaymentStatus() != null && !paymentInfo.getPaymentStatus().equals("WAIT_BUYER_PAY")) {
            //幂等性检查（在更新之前先检查是否已经支付）
            boolean b = paymentService.checkPayStatusFromDb(out_trade_no);
            if (!b) {
                System.out.println("支付成功，不再发送延迟检查队列");

                //更新订单信息
                paymentService.updatePayment(paymentInfo);

                //发送系统信息，更新订单已经支付
                paymentService.sendPaySuccessQueue(paymentInfo);
            }

        } else {  //支付失败
            //再次发送检查支付结果的延迟队列
            if (count > 0) {
                count--;
                System.out.println("消费延迟检查次数" + count + "再次发送");
                paymentInfo.setOrderSn(out_trade_no);
                paymentService.sendPayCheckQueue(paymentInfo, count);
            } else {
                System.out.println("检查延迟次数耗尽，结束任务");
            }


        }


    }
}
