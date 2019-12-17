package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.manage.mapper.PmsSkuAttrValueMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuImageMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuInfoMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.util.StringUtil;


import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class SkuServiceImpl implements SkuService{
    @Autowired
    private PmsSkuInfoMapper pmsSkuInfoMapper;

    @Autowired
    private PmsSkuAttrValueMapper pmsSkuAttrValueMapper;

    @Autowired
    private PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;


    @Autowired
    private PmsSkuImageMapper skuImageMapper;

    @Autowired
    RedisUtil redisUtil;



    @Override
    public void saveSkuInfo(PmsSkuInfo pmsSkuInfo) {

        //保存sku信息
        pmsSkuInfoMapper.insertSelective(pmsSkuInfo);
        //获取sku主键
        String skuId = pmsSkuInfo.getId();

        //保存sku平台属性值
        for (PmsSkuAttrValue pmsSkuAttrValue : pmsSkuInfo.getSkuAttrValueList()) {

            pmsSkuAttrValue.setSkuId(skuId);
            pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);

        }


        //保存sku销售属性值
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : pmsSkuInfo.getSkuSaleAttrValueList()) {

            pmsSkuSaleAttrValue.setSkuId(skuId);
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);

        }

        //保存图片
        for (PmsSkuImage pmsSkuImage : pmsSkuInfo.getSkuImageList()) {

            pmsSkuImage.setSkuId(skuId);
            skuImageMapper.insertSelective(pmsSkuImage);

        }




    }
    public PmsSkuInfo item(String skuId) {
        PmsSkuInfo pmsSkuInfo=null;
        Jedis jedis=null;
        // /先查询缓存
        try {
          jedis=redisUtil.getJedis();
        //设置key（sku:skuId:info）
        String skuJsonKey="sku:"+skuId+":info";
        String skuJson = jedis.get(skuJsonKey);
        //判断skuJson是否为空
        if(StringUtil.isNotEmpty(skuJson)) {
            //将skuJson转化为pmsSkuInfo对象
            pmsSkuInfo = JSON.parseObject(skuJson, PmsSkuInfo.class);
            //System.out.println("获得缓存数据");


        } else {
            //如果缓存不存在,那就从数据库中查询
           // System.out.println("没有获得缓存数据，领取分布式锁");
            //先领号
            String lock = "sku:" + skuId + ":lock";
            String uuid = UUID.randomUUID().toString();
            String OK = jedis.set(lock,uuid, "nx", "px", 10000);
            if (StringUtil.isNotEmpty(OK) && OK.equals("OK")) {
                pmsSkuInfo = itemFromDb(skuId);
                if (pmsSkuInfo!=null) {
                    //保存到缓存
                    jedis.set("sku:" + skuId + ":info", JSON.toJSONString(pmsSkuInfo));


                    /*//防止误删其他锁的进程
                    String lockValue = jedis.get(lock);
                    if(StringUtil.isNotEmpty(lockValue)&&uuid.equals(lockValue)){
                        //删除自己的锁
                        jedis.del("sku:"+skuId+":info");
                    }*/

                    //如果在redis中的锁已经过期了，然后锁过期的那个请求又执行完毕，回来删锁--->对应的方法
                    String script ="if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                    jedis.eval(script, Collections.singletonList(lock),Collections.singletonList(uuid));


                }

               // System.out.println("没有获得缓存数据，但是获得分布式锁，访问db,返回数据存入缓存");
            }else {
               // System.out.println("没有获得缓存数据，也没有获得分布式锁，开始自旋");
                return item(skuId);
            }

        }

        }catch (Exception e){

        }finally {
            //关流
            jedis.close();
        }
        return  pmsSkuInfo;
    }

   @Override
    public PmsSkuInfo itemFromDb(String skuId) {

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        //sku设置id
        pmsSkuInfo.setId(skuId);
        //查询sku
        PmsSkuInfo pmsSkuInfoReturn = pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        //
        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        //sku图片设置id
        pmsSkuImage.setSkuId(skuId);
        //查询sku图片的集合
        List<PmsSkuImage> pmsSkuImages = skuImageMapper.select(pmsSkuImage);

        //为查到返回的sku设置图片
        pmsSkuInfoReturn.setSkuImageList(pmsSkuImages);

        return pmsSkuInfoReturn;
    }

    @Override
    //通过spu查询销售属性值的组合获取sku
    public List<PmsSkuInfo> skuSaleAttrValueListBySpu(String spuId) {
        List<PmsSkuInfo> pmsSkuInfosForSaleAttrValues=pmsSkuInfoMapper.selectSkuSaleAttrValueListBySpu(spuId);
        return pmsSkuInfosForSaleAttrValues;
    }

    @Override
    public List<PmsSkuInfo> getSkuForSearch() {

        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectAll();
        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
            String id = pmsSkuInfo.getId();
            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(id);
            List<PmsSkuAttrValue> pmsSkuAttrValues = pmsSkuAttrValueMapper.select(pmsSkuAttrValue);
            pmsSkuInfo.setSkuAttrValueList(pmsSkuAttrValues);
        }


        return pmsSkuInfos;
    }

    @Override
    public PmsSkuInfo getSkuById(String id) {
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(id);
        PmsSkuInfo pmsSkuInfo1 = pmsSkuInfoMapper.selectOne(pmsSkuInfo);


        return pmsSkuInfo1;
    }
}
