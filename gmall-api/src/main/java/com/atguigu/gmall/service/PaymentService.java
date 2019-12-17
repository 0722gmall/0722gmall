package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PaymentInfo;

public interface PaymentService {
    void updatePayment(PaymentInfo paymentInfo);

    void addPayment(PaymentInfo payment);

    void sendPaySuccessQueue(PaymentInfo paymentInfo);

    void sendPayCheckQueue(PaymentInfo payment,long count);

    PaymentInfo checkPayStatus(String out_trade_no);

    boolean checkPayStatusFromDb(String out_trade_no);
}
