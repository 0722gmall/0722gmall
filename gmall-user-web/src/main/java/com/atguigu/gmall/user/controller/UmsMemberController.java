package com.atguigu.gmall.user.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.service.UmsMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UmsMemberController {
    @Reference
    private UmsMemberService umsMemberService;

    @RequestMapping("/getAllUmsMember")
    @ResponseBody
    public List<UmsMember> getAllUmsMember(){
        List<UmsMember>umsMembers=umsMemberService.selectAllUmsMember();
        return umsMembers;

    }

    @RequestMapping("/index")
    @ResponseBody
    public String index(){
        List<UmsMember>umsMembers=umsMemberService.selectAllUmsMember();
        return "index";

    }

    @RequestMapping("/getAllUmsMemberMapper")
    @ResponseBody
    public List<UmsMember> getAllUmsMemberMapper(){
       List<UmsMember> umsMembers=umsMemberService.selectAllUmsMemberMapper();
       return umsMembers;
    }

}
