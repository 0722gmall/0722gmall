package com.atguigu.gmall.user.service.impl;



import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.bean.UmsMemberReceiveAddress;
import com.atguigu.gmall.service.UmsMemberService;
import com.atguigu.gmall.user.mapper.UmsMemberReceiveAddressMappe;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.util.ActiveMQUtil;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;


import javax.jms.*;
import java.util.List;

@Service
public class UmsMemberServiceimpl implements UmsMemberService {

    @Autowired
    private UserInfoMapper userinfoMapper;

    @Autowired
    RedisUtil redisUtil;
    @Autowired
    UmsMemberReceiveAddressMappe umsMemberReceiveAddressMappe;

    @Autowired
    ActiveMQUtil activeMQUtil;


    @Override
    public List<UmsMember> selectAllUmsMember() {
        return userinfoMapper.selectAllUmsMember();
    }

    @Override
    public List<UmsMember> selectAllUmsMemberMapper() {

        return userinfoMapper.selectAll();
    }

    //查询登录人是否存在
    @Override
    public UmsMember login(UmsMember umsMember) {
        UmsMember umsMemberParam = new UmsMember();
        umsMemberParam.setUsername(umsMember.getUsername());
        umsMemberParam.setPassword(umsMember.getPassword());
        UmsMember umsMemberResult = userinfoMapper.selectOne(umsMemberParam);

       return umsMemberResult;
    }

    //把用户的token存储到redid缓存
    @Override
    public void putTokenCache(String token, String userId) {

        Jedis jedis = null;
        try {
            //获取缓存连接
            jedis=redisUtil.getJedis();
            //设置值
            jedis.setex("user:"+token+":token",60*60,userId);

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            jedis.close();
        }

    }


    //获取缓存中的用户token
    @Override
    public String getTokenCache(String token) {
        Jedis jedis = null;
        String userId="";
        try {
            jedis=redisUtil.getJedis();
             userId = jedis.get("user:" + token + ":token");

        }catch (Exception e){

        }finally {
            jedis.close();

        }

        return userId;
    }

    @Override
    public UmsMember isOUserExit(String uid) {
        UmsMember umsMember = new UmsMember();
        umsMember.setSourceUid(uid);
        UmsMember umsMember1 = userinfoMapper.selectOne(umsMember);
        return umsMember1;
    }

    @Override
    public UmsMember addOuser(UmsMember umsMember) {
       userinfoMapper.insertSelective(umsMember);
        return umsMember;
    }

    //根据用户id获取用户收货地址
    @Override
    public List<UmsMemberReceiveAddress> getAddressByUserId(String userId) {
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(userId);
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = umsMemberReceiveAddressMappe.select(umsMemberReceiveAddress);

        return umsMemberReceiveAddresses;
    }

    //根据收货地址的id查询收货地址
    @Override
    public UmsMemberReceiveAddress getAddressById(String addressId) {
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setId(addressId);
        UmsMemberReceiveAddress umsMemberReceiveAddress1 = umsMemberReceiveAddressMappe.selectOne(umsMemberReceiveAddress);
    return umsMemberReceiveAddress1;
    }


    //登录成功后合并购物车列表队列
    @Override
    public void sendCartQueue(String id, String cartListCookie) {


        //工厂
        ConnectionFactory factory =activeMQUtil.getConnectionFactory();

        Session session =null;
        Connection connection =null;
        //连接
        try {
            connection=factory.createConnection();
            connection.start();

            //session
            session=connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue("CART_QUEUE");


            //发送对象
            MessageProducer producer = session.createProducer(queue);

            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("userId",id);
            mapMessage.setString("cartListCookie",cartListCookie);

            producer.send(mapMessage);
            session.commit();


        } catch (JMSException e) {
            e.printStackTrace();
        }finally {
            try {
                session.close();
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }

        }



    }


}
