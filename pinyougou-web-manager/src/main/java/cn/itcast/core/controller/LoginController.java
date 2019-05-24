package cn.itcast.core.controller;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 登陆管理
 */
@RestController
@RequestMapping("/login")
public class LoginController {

    //查询当前登陆人
    @RequestMapping("/showName")
    public Map<String,Object> showName(HttpSession session){

        //四种:
        //后端代码 二种
        //1:SprignSecurity 用户名 密码 放在Session中
        //SecurityContext spring_security_session = (SecurityContext) session.getAttribute("SPRING_SECURITY_SESSION");
       // String username = spring_security_session.getAuthentication().getName();
        //2:SpringSecurity提供一个小助手
        String username1 = SecurityContextHolder.getContext().getAuthentication().getName();

       // System.out.println("username:session:" + username);
        System.out.println("username:SpringSecurity" + username1);


        Map<String,Object> map = new HashMap<>();
        map.put("username",username1);
        //登陆的时间是多少?
        map.put("cur_time",new Date());



        return map;


    }
}
