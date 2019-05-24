package cn.itcast.core.controller;

import cn.itcast.common.utils.PhoneFormatCheckUtils;
import cn.itcast.core.pojo.user.User;
import cn.itcast.core.service.UserService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理中心
 */
@RestController
@RequestMapping("/user")
public class UserController {


    @Reference
    private UserService userService;

    //发短信验证码
    @RequestMapping("/sendCode")
    public Result sendCode(String phone){
        try {

        //1:判断手机号的合法性
            if(PhoneFormatCheckUtils.isPhoneLegal(phone)){
                //2:调用用户接口或实现类 入参手机号
                userService.sendCode(phone);

            }else{
                return new Result(false,"手机号不正确");
            }


            return new Result(true,"发短信成功");
        }catch (Exception e){
            return new Result(false,"发短信失败");

        }


    }
    //完成注册
    @RequestMapping("/add")
    public Result add(String smscode, @RequestBody User user){

        try {
            userService.add(smscode,user);
            return new Result(true,"注册成功");
        } catch (RuntimeException e) {
            return new Result(false,e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"注册失败");
        }

    }
}
