package com.atguigu.gmall.passport.controller;



import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.annotation.LoginRequired;
import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.service.UmsMemberService;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.HttpclientUtil;
import com.atguigu.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Reference
    UmsMemberService umsMemberService;


    //中心化的认证方法
    @RequestMapping("/verity")
    @ResponseBody
    public Map<String,String> verity(HttpServletRequest request,String token,String currentIp){
       //1.cas校验用户凭证
        Map<String,String> map=new HashMap<>();
       String userId= umsMemberService.getTokenCache(token);
       if (StringUtils.isNotBlank(userId)){
           //校验成功返回用户信息
           map.put("success","success");
           map.put("userId",userId);
       }else {
           //校验成功返回用户信息
           map.put("success","fail");

       }



        //2.jwt校验用户凭证
       //获取当前请求的ip
       // String ip = request.getRemoteAddr();
        //服务器密钥
        String atguiguKey="atguiguGmall0722";
        Map<String,String> resultMap = JwtUtil.decode(atguiguKey, token, currentIp);
        if (resultMap!=null){
            //校验成功返回用户信息
            map.put("success","success");
            map.put("userId",(String)resultMap.get("userId"));
        }else {
            //校验成功返回用户信息
            map.put("success","fail");
        }

        return map;

    }

    @RequestMapping("/index")
    public String index(ModelMap modelMap,String isTocartList){
        modelMap.put("isTocartList",isTocartList);
        return "index";
    }


    @RequestMapping("/login")
    @ResponseBody
    public String login(HttpServletRequest request, UmsMember umsMember){


   //登录校验，调用userService，核对用户信息
        UmsMember user=umsMemberService.login(umsMember);

        if (user!=null&&StringUtils.isNotBlank(user.getId())) {

            //获取cookie中的购物车列表
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            //登录成功，发送合并购物车队列
            umsMemberService.sendCartQueue(user.getId(),cartListCookie);

            //用户id
            String  userId=user.getId();
            //校验成功返回用户登录凭证token
            String remoteAddr = request.getRemoteAddr();
            String ip=remoteAddr;
            //密钥
            String atguiguKey="atguiguGmall0722";
            Map<String,String> map=new HashMap<>();
            map.put("userId",userId);
            map.put("nickname",user.getNickname());
            //在sso状态下，jwt相当于生成token的工具
            String token =JwtUtil.encode(atguiguKey,map,ip);

            //把用户的token保存到redis缓存
            umsMemberService.putTokenCache(token,userId);


            return  token;


        }else {
            return "fail";

        }



    }



    @RequestMapping("/vlogin")
    public String vlogin(String code,ModelMap  modelMap,HttpServletRequest request){

        //1用code换取access_token
        Map<String, String> paramMap = new HashMap<>();
        //保存App Key：2067366399
        paramMap.put("client_id","2067366399");
        //保存App Secret：eeda2ec361060a0141d81a31b15be696
        paramMap.put("client_secret","eeda2ec361060a0141d81a31b15be696");
        //回调地址
        paramMap.put("redirect_uri","http://passport.gmall.com:8086/vlogin");
        //
        paramMap.put("grant_type","authorization_code");
        //
        paramMap.put("code",code);
        String result = HttpclientUtil.doPost("https://api.weibo.com/oauth2/access_token?", paramMap);

        //把输出的结果转化成map集合键值对
        Map<String, Object> resultMap = new HashMap<>();
        Map<String,Object> map = JSON.parseObject(result, resultMap.getClass());


        //2用access_token换取用户信息
        String access_token = (String) map.get("access_token");
        String uid = (String) map.get("uid");

       //判断用户是否存在
        UmsMember umsMember=umsMemberService.isOUserExit(uid);
        if (umsMember==null){
            //3保存用户信息
            String url="https://api.weibo.com/2/users/show.json?access_token="+access_token+"uid=1"+uid;
            String oUserString =HttpclientUtil.doGet(url);
            Map<String,Object> userMap= JSON.parseObject(oUserString, resultMap.getClass());
            System.out.println(userMap+"...................");

             umsMember = new UmsMember();
            //设置用户的来源leix
            umsMember.setSourceType("2");
            //设置用户的access_token
            umsMember.setAccessToken(access_token);
            //设置用户的code
            umsMember.setAccessCode(code);
            //设置用户的来源uid
            umsMember.setSourceUid(uid);

            umsMember=umsMemberService.addOuser(umsMember);


        }


        //4生成token

        String userId=" ";
        if (umsMember!=null&&StringUtils.isNotBlank(umsMember.getId())){
            //用户id
            userId=umsMember.getId();
            //校验成功返回用户登录凭证token
            String remoteAddr = request.getRemoteAddr();
            String ip=remoteAddr;
            //密钥
            String atguiguKey="atguiguGmall0722";
            Map<String,String> tokenMap=new HashMap<>();
            tokenMap.put("userId",userId);
            tokenMap.put("nickname","tom");
            //在sso状态下，jwt相当于生成token的工具
            String token =JwtUtil.encode(atguiguKey,tokenMap,ip);

            //把用户的token保存到redis缓存
            umsMemberService.putTokenCache(token,userId);




        }


        //5重定向到

        return "redirect:http://search.gmall.com:8083/index.html";

    }







}
