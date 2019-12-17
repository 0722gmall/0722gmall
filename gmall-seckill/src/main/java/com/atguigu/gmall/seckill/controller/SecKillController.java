package com.atguigu.gmall.seckill.controller;

import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class SecKillController {

    @Autowired
    RedisUtil redisUtil;


    /*抢购的方法二*/
    @RequestMapping("secKillTwo")
    @ResponseBody
    public String secKillTwo(){
        BigDecimal bigDecimal = new BigDecimal("0");
        Jedis jedis=null;
        try {
            jedis = redisUtil.getJedis();
            String stock = jedis.get("sku:" + 118 + ":stock");
            bigDecimal = new BigDecimal(stock);
            int i = bigDecimal.compareTo(new BigDecimal("0"));
            if (i>0){
                //BigDecimal subtract = bigDecimal.subtract(new BigDecimal("1"));
                //加入watch和事务，保证redis中库存数量的一致性，如果该线程被打断了，就会返回一个null
                jedis.watch("sku:"+118+":stock");
                Transaction transaction = jedis.multi();
                transaction.incrBy("sku:"+118+":stock",-1);
                List<Object> exec =transaction.exec();
                if (exec==null||exec.size()<1){
                    System.out.println("非洲人看到的剩余数量"+jedis.get("sku:"+118+":stock"));
                    return "非洲人抢不到，抢购结束，略略";
                }else {
                    System.out.println("有剩余库存可以抢购"+exec.get(0));
                    return "抢购成功，剩余库存"+exec.get(0);
                }


            }else {
                System.out.println("没有剩余库存，抢购已经结束");
                return "没有剩余库存，抢购已经结束";
            }
        }finally {
            jedis.close();
        }

    }



/*
    *//* 抢购的方法一*//*
    @RequestMapping("secKillOne")
    @ResponseBody
    public String secKillOne(){
        BigDecimal bigDecimal = new BigDecimal("0");
        Jedis jedis=null;
        try {
            jedis=redisUtil.getJedis();
            //获取缓存中的118商品
            String stock = jedis.get("sku:" + 118 + ":stock");
            //覆盖之前的bigDecimal
            bigDecimal = new BigDecimal(stock);
            //与零比较(看缓存中是否还有库存)
            int i = bigDecimal.compareTo(new BigDecimal("0"));
            if(i>0){
                BigDecimal subtract = bigDecimal.subtract(new BigDecimal(("1")));
                System.out.println("有剩余库存,还可以抢购:"+subtract);
                //缓存中的库存也要减一(所以要重新设置)
                jedis.set("sku:"+118+":stock",subtract.toString());
                return ("抢购成功，还有库存:"+bigDecimal);
            }else {
                System.out.println("没有剩余库存，抢购结束");
                return "没有剩余库存，抢购结束";

            }

        }finally {
            jedis.close();
        }




    }*/

}
