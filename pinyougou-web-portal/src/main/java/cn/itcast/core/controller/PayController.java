package cn.itcast.core.controller;

import cn.itcast.core.service.PayService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 支付管理
 */
@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private PayService payService;

    //生成二维码的Value值  订单ID  金额
    @RequestMapping("/createNative")
    public Map<String,String> createNative(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        return payService.createNative(name);
    }
    //查询订单支付状态
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){

        try {

            int x = 0;

            //无限循环
            while (true) {

                //查询订单支付状态
                Map<String,String>  map = payService.queryPayStatus(out_trade_no);
                //判断支付状态
                if("NOTPAY".equals(map.get("trade_state"))){
                    //未支付
                    //睡一会  毫秒
                    Thread.sleep(3000);
                    x++;
                    if(x > 100){
                        //过了5分钟
                        //调用 关闭订单API  同学完成
                        return new Result(false,"支付超时");
                    }

                }else{
                    //已支付  修改 支付日志表  银行流水号  状态0-1  支付时间  当前时间..... 同学完成
                    return new Result(true,"支付成功");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"支付失败");
        }
    }
}
