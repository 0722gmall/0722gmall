package com.atguigu.gmall.testMq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

public class ProducerGang {

    //queue题队列
   /* public static void main(String[] args) {
        //工厂
        ActiveMQConnectionFactory connect = new ActiveMQConnectionFactory("tcp://localhost:61616");
        try {
            //连接
            Connection connection =connect.createConnection();
            connection.start();

            //session
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
           //发送的名称
            Queue testqueue = session.createQueue("drink");
            //发送对象
            MessageProducer producer = session.createProducer(testqueue);
            //发送的内容、
            ActiveMQTextMessage textMessage = new ActiveMQTextMessage();
            textMessage.setText("i want drink");
            //持久化
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(textMessage);

            //开启消息事务后，需要提交消息才能发送
            session.commit();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }





    }*/




    public static void main(String[] args) {
        //工厂
        ActiveMQConnectionFactory connect = new ActiveMQConnectionFactory("tcp://localhost:61616");
        try {
            //连接
            Connection connection =connect.createConnection();
            connection.start();

            //session
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            //发送的名称
            Topic topic = session.createTopic("drink");
            //发送对象
            MessageProducer producer = session.createProducer(topic);
            //发送的内容、
            ActiveMQTextMessage textMessage = new ActiveMQTextMessage();
            textMessage.setText("i want drink");
            //持久化
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(textMessage);

            //开启消息事务后，需要提交消息才能发送
            session.commit();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }





    }




}
