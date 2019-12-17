package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.OmsCartItem;

import com.atguigu.gmall.cart.mapper.OmsCartItemMpper;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService{

    @Autowired
    OmsCartItemMpper omsCartItemMpper;

    @Autowired
    RedisUtil redisUtil;


    @Override
    public OmsCartItem isCartExists(OmsCartItem omsCartItem) {
        OmsCartItem omsCartItem1=new OmsCartItem();
        //设置用户id
        omsCartItem1.setMemberId(omsCartItem.getMemberId());
        //设置skuId
        omsCartItem1.setProductSkuId(omsCartItem.getProductSkuId());
        //通过omsCartItem1查询
        OmsCartItem omsCartItem2 = omsCartItemMpper.selectOne(omsCartItem1);

        return omsCartItem2;
    }

    @Override
    public void updateCart(OmsCartItem omsCartItem1FormDb) {
        Example e = new Example(OmsCartItem.class);
        //判断
        if (StringUtils.isNotBlank(omsCartItem1FormDb.getId())){
            e.createCriteria().andEqualTo("id",omsCartItem1FormDb.getId());
        }else {
            //判断是否等于用户id和商品id(联合主键)
            e.createCriteria().andEqualTo("memberId",omsCartItem1FormDb.getMemberId()).andEqualTo("productSkuId",omsCartItem1FormDb.getProductSkuId());
        }

        //修改
        omsCartItemMpper.updateByExampleSelective(omsCartItem1FormDb,e);

        //更新缓存前查询数据库
        OmsCartItem omsCartItem1 = omsCartItemMpper.selectOne(omsCartItem1FormDb);
        //更新缓存
        updateCartForCache(omsCartItem1);


    }

    @Override
    public OmsCartItem addCart(OmsCartItem omsCartItem) {
        //添加omsCartItem
         omsCartItemMpper.insertSelective(omsCartItem);
         //更新缓存
         updateCartForCache(omsCartItem);

         return omsCartItem;



    }

    //同步缓存
    @Override
    public void combineCart(String userId) {
        OmsCartItem omsCartItem1=new OmsCartItem();
        //设置用户id
        omsCartItem1.setMemberId(userId);
        //查询
        List<OmsCartItem> omsCartItems = omsCartItemMpper.select(omsCartItem1);
        if(omsCartItems!=null && omsCartItems.size()>0){
            //获取缓存连接
            Jedis jedis = redisUtil.getJedis();
            Map<String,String> map=new HashMap<>();
            //遍历omsCartItems
            for (OmsCartItem omsCartItem : omsCartItems) {
                //map存值
                map.put(omsCartItem.getProductSkuId(), JSON.toJSONString(omsCartItem));
            }
            //存到redis
            jedis.hmset("user:"+userId+":cart",map);
            //关掉redis
            jedis.close();
        }
    }

    //另一中缓存方法
    @Override
    public void updateCartForCache(OmsCartItem omsCartItemForCache) {

        Jedis jedis = redisUtil.getJedis();
        //修改缓存  //hset的key                                                     //map的key                               //map的value
        jedis.hset("user:"+omsCartItemForCache.getMemberId()+":cart",omsCartItemForCache.getProductSkuId(),JSON.toJSONString(omsCartItemForCache));
         //关流
        jedis.close();;

    }

    @Override
    public List<OmsCartItem> getCartListByUserId(String userId) {
        //创建一个OmsCartItem集合
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        //获取缓存连接
        Jedis jedis = redisUtil.getJedis();
        //获取缓存中的数据
        List<String> hvals = jedis.hvals("user:" + userId + ":cart");
       //缓存中存在值
        if (hvals!=null&&hvals.size()>0){
            for (String hval : hvals) {
       //把缓存中的hval转为omsCartItem
            OmsCartItem omsCartItem = JSON.parseObject(hval, OmsCartItem.class);
            omsCartItems.add(omsCartItem);
                }
        }else {
            //查询数据库
            //缓存
            combineCart(userId);


        }

        //关流
        jedis.close();

        return omsCartItems;
    }

    //删除已经体交订单的购物车数据
    @Override
    public void deleteCarts(List<String> skuIds, String userId) {

        Jedis jedis = redisUtil.getJedis();

        for (String skuId : skuIds) {
            OmsCartItem omsCartItem1 = new OmsCartItem();
            omsCartItem1.setMemberId(userId);
            omsCartItem1.setProductSkuId(skuId);
            //1先删掉数据库已经提交的购物车商品
            omsCartItemMpper.delete(omsCartItem1);
            //2再删掉缓存中已经提交的购物车商品
            jedis.hdel("user:"+userId+":cart",skuId);

        }
        jedis.close();

    }

}
