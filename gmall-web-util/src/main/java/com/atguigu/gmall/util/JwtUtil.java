package com.atguigu.gmall.util;

import io.jsonwebtoken.*;

import java.util.HashMap;

import java.util.Map;

/**
 * @param
 * @return
 */
public class JwtUtil {

    public static void main(String[] args){

        //浏览器ip
        String ip="127.0.0.1";
        String  broewserName="firefox";
        String salt=ip+broewserName;

        //用户信息
        String userId="1";
        Map<String,String> map=new HashMap<>();
        map.put("userId",userId);

        //服务器密钥
        String atguiguKey="atguigu0722";
        String encode = encode(atguiguKey, map, salt);
        System.out.println(encode);

       // JwtUtil.decode(atguiguKey,"eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOiIxIn0.PIOUeTbAfqHUchbr1fo1UgtcYpxBfMc0DnJF8MTKTIc",salt);


    }


    /***
     * jwt加密
     * @param key
     * @param map
     * @param salt
     * @return
     */
    public static String encode(String key,Map map,String salt){

        if(salt!=null){
            key+=salt;
        }
        JwtBuilder jwtBuilder = Jwts.builder().signWith(SignatureAlgorithm.HS256, key);
        jwtBuilder.addClaims(map);

        String token = jwtBuilder.compact();
        return token;
    }

    /***
     * jwt解密
     * @param key
     * @param token
     * @param salt
     * @return
     * @throws SignatureException
     */
    public static  Map decode(String key,String token,String salt)throws SignatureException {
        if(salt!=null){
            key+=salt;
        }
        Claims map = null;

        map = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();

        System.out.println("map.toString() = " + map.toString());

        return map;

    }

}
