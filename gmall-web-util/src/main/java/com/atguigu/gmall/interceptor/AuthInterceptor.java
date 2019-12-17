package com.atguigu.gmall.interceptor;


import com.atguigu.gmall.annotation.LoginRequired;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

               //判断是否需要拦截
                    HandlerMethod hm =(HandlerMethod) handler;
                    //判断是否有注解
                    LoginRequired loginRequired = hm.getMethodAnnotation(LoginRequired.class);

                if (loginRequired==null){
                        return  true;
                }

              //有注解，表示需要拦截
                String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
                String newToken = request.getParameter("newToken");
                
                String token=" ";

                if (StringUtils.isNotBlank(oldToken)) {

                        token=oldToken;
                }
                if (StringUtils.isNotBlank(newToken)) {

                        token=newToken;
                }


                if (StringUtils.isNotBlank(token)){
                        //获取当前ip
                        String remoteAddr = request.getRemoteAddr();
                        //获取负载均衡的ip(原始ip)
                      //  String ip = request.getHeader("X-forword--for");

                        //去中心化,独立了验证方法
                        //密钥
                         String atguiguKey="atguiguGmall0722";
                         //解密
                        Map<String,String> decode = JwtUtil.decode(atguiguKey,token, remoteAddr);
                        if (decode!=null){
                                String userId = decode.get("userId");
                                String nickname = decode.get("nickname");

                                request.setAttribute("userId",userId);
                                request.setAttribute("nickname",nickname);

                                //写入cookie
                                CookieUtil.setCookie(request,response,"oldToken",token,60*60*24,true);

                                return  true;
                        }


                       /*//中心化
                        //送去校验中心,检测Token
                        String returnMap = HttpclientUtil.doGet("http://passport.gmall.com:8083/verity?token="+token+"&currentIp="+remoteAddr);//远程访问认证中心
                        Map<String,String> map=new HashMap<>();
                        Map<String,String> resultMap = JSON.parseObject(returnMap, map.getClass());

                        //返回结果
                        String success = resultMap.get("success");
                        String userId = resultMap.get("userId");
                        if (StringUtils.isNotBlank(success)&&success.equals("success")){
                                request.setAttribute("userId",userId);
                                //写入cookie
                                CookieUtil.setCookie(request,response,"oldToken",token,60*60*24,true);
                        }
                        */


                }

                if (loginRequired.isNeedeSuccess()){
                        response.sendRedirect("http://passport.gmall.com:8086/index?isTocartList=true");
                        return false;
                }



                return true;

        }
}