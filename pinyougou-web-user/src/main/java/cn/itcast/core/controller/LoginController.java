package cn.itcast.core.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 登陆管理
 */
@RestController
@RequestMapping("/login")
public class LoginController {

    //查询当前登陆人
    @RequestMapping("/name")
    public Map<String,String> name(){

        String name = SecurityContextHolder.getContext().getAuthentication().getName();


        Map<String,String> map = new HashMap<>();
        map.put("loginName",name);

        return map;

    }
}
