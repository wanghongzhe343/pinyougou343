package cn.itcast.core.controller;

import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.service.OrderService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单管理
 */
@RestController
@RequestMapping("/order")
public class OrderController {


    @Reference
    private OrderService orderService;
    //添加
    @RequestMapping("/add")
    public Result add(@RequestBody Order order){

        try {

            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            //用户ID
            order.setUserId(name);

            orderService.add(order);
            return new Result(true,"提交成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"提交失败");
        }

    }
}
