package cn.itcast.core.service;

import cn.itcast.core.dao.user.UserDao;
import cn.itcast.core.pojo.user.User;
import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.security.core.userdetails.UserDetailsService;

import javax.jms.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 用户管理
 */
@Service
public class UserServiceImpl implements UserService {


    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private Destination smsDestination;
    @Autowired
    private UserDao userDao;

    //生成验证码
    @Override
    public void sendCode(String phone) {

        //1:6位随机整数
        String randomNumeric = RandomStringUtils.randomNumeric(6);
        //2:保存验证码到缓存中
        redisTemplate.boundValueOps(phone).set(randomNumeric);
        //存活时间  实际上1分钟  5分钟
        redisTemplate.boundValueOps(phone).expire(60, TimeUnit.MINUTES);

        //3:发消息
        jmsTemplate.send(smsDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {

     /*           request.putQueryParameter("PhoneNumbers", map.get("PhoneNumbers"));//"17862655057"
                request.putQueryParameter("SignName", map.get("SignName"));//"品优购商城"
                request.putQueryParameter("TemplateCode", map.get("TemplateCode"));//"SMS_126462276"
                request.putQueryParameter("TemplateParam",map.get("TemplateParam"));//"{'number':123456}"*/

                MapMessage map = session.createMapMessage();
//                验证码
                map.setString("TemplateParam","{\"number\":"+randomNumeric+"}");
//                        手机号
                map.setString("PhoneNumbers",phone);
//                签名
                map.setString("SignName","品优购商城");
//                        模板  注册
                map.setString("TemplateCode","SMS_126462276");

                return map;
            }
        });




    }

    //注册
    @Override
    public void add(String smscode, User user) {
        //1:判断验证码是否正确
        String code = (String) redisTemplate.boundValueOps(user.getPhone()).get();
        if(null != code){
            //判断是否正确
            if(code.equals(smscode)){
                //用户名
                //密码 本次未加密
                //手机号
                //添加时间
                user.setCreated(new Date());
                user.setUpdated(new Date());

                //保存
                userDao.insertSelective(user);
            }else{
                throw new RuntimeException("验证码错误");
            }
        }else{
            //
            throw new RuntimeException("风控验证码失效");
        }
    }
}
