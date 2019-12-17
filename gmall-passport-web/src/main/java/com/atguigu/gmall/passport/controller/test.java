package com.atguigu.gmall.passport.controller;

import com.atguigu.gmall.util.HttpclientUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;


public class test {

    @Autowired
    HttpclientUtil httpclientUtil;


    public static void main(String[] args) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("client_id","2067366399");
        paramMap.put("client_secret","eeda2ec361060a0141d81a31b15be696");
        paramMap.put("redirect_uri","http://passport.gmall.com:8086/vlogin");
        paramMap.put("grant_type","authorization_code");
        paramMap.put("code","4e18b2c1b1c371ba0e95f96c4db68e4d");
    }




}
