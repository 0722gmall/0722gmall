package com.atguigu.gmall.cart.test;

import java.math.BigDecimal;

public class BigDecimalTest {

    public static void main(String[] args) {

        // 数字的初始化
        double b1 = 0.01d;
        float b2 = 0.01f;
        BigDecimal b3 = new BigDecimal(b1);
        BigDecimal b4 = new BigDecimal(b2);
        BigDecimal b5 = new BigDecimal("0.01");
        System.out.println(b1);
        System.out.println(b2);
        System.out.println(b3);
        System.out.println(b4);
        System.out.println(b5);

        // 比较
        int i = b4.compareTo(b3);// -1 0 1
        System.out.println(i);

        b3.toString().equals(b4);

        if(b3==b4){

        }
        // 运算
        BigDecimal b6 = new BigDecimal("6");
        BigDecimal b7 = new BigDecimal("7");

        BigDecimal add = b6.add(b7);
        System.out.println(add);
        BigDecimal subtract = b6.subtract(b7);
        System.out.println(subtract);

        BigDecimal multiply = b6.multiply(b7);
        System.out.println(multiply);

        BigDecimal divide = b6.divide(b7,10,BigDecimal.ROUND_HALF_DOWN);
        System.out.println(divide);
        // 取值
        BigDecimal add1 = b3.add(b4);
        System.out.println(add1);

        BigDecimal bigDecimal = add1.setScale(10, BigDecimal.ROUND_HALF_DOWN);

        System.out.println(bigDecimal);


    }
}
