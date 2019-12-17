package com.atguigu.gmall.testMq;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.jms.listener.adapter.AbstractAdaptableMessageListener;

import javax.jms.*;

public class ConsumerWu {

    //queue队列
  /*  public static void main(String[] args) {
        //工厂
        ActiveMQConnectionFactory connect = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER,ActiveMQConnection.DEFAULT_PASSWORD,"tcp://localhost:61616");

        //连接
        try {
            Connection connection = connect.createConnection();
            connection.start();


            //session
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);


            //接收发送过来的对象
            Queue testqueue =session.createQueue("drink");
            MessageConsumer consumer = session.createConsumer(testqueue);
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    if (message instanceof TextMessage){
                        String text = null;
                        try {
                            text = ((TextMessage) message).getText();
                            System.out.println(text+"小弟");
                        } catch (JMSException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });


        } catch (JMSException e) {
            e.printStackTrace();
        }


    }
*/


    //topic队列
    public static void main(String[] args) {
        //工厂
        ActiveMQConnectionFactory connect = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER,ActiveMQConnection.DEFAULT_PASSWORD,"tcp://localhost:61616");

        //连接
        try {
            Connection connection = connect.createConnection();
            connection.start();


            //session
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);


            //接收发送过来的对象
            Topic topic = session.createTopic("drink");
            MessageConsumer consumer = session.createConsumer(topic);
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    if (message instanceof TextMessage){
                        String text = null;
                        try {
                            text = ((TextMessage) message).getText();
                            System.out.println(text+"小弟");
                        } catch (JMSException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });


        } catch (JMSException e) {
            e.printStackTrace();
        }


    }


}
