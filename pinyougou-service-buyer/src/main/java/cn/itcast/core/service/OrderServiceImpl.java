package cn.itcast.core.service;

import cn.itcast.common.utils.IdWorker;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.log.PayLogDao;
import cn.itcast.core.dao.order.OrderDao;
import cn.itcast.core.dao.order.OrderItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.pojo.order.OrderItem;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import vo.Cart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 订单管理
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderDao orderDao;
    @Autowired
    private OrderItemDao orderItemDao;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private PayLogDao payLogDao;

    //保存订单主表
    //保存订单详情表(多张)
    @Override
    public void add(Order order) {
        //收货人
        //手机号
        //地址
        //支付类型

        //真正的总金额 多张订单的总金额
        long tp = 0;
        //订单ID 集合
        List<String> ids = new ArrayList<>();

        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("CART").get(order.getUserId());
        for (Cart cart : cartList) {//商家ID 库存ID  数量
            //订单ID    分布式ID生成器
            long id = idWorker.nextId();
            ids.add(String.valueOf(id));
            order.setOrderId(id);
            //实付金额
            double totalPrice = 0;

            List<OrderItem> orderItemList = cart.getOrderItemList();
            for (OrderItem orderItem : orderItemList) {

                //ID
                orderItem.setId(idWorker.nextId());

                Item item = itemDao.selectByPrimaryKey(orderItem.getItemId());
                //商品ID
                orderItem.setGoodsId(item.getGoodsId());
                //标题
                orderItem.setTitle(item.getTitle());
                //单价
                orderItem.setPrice(item.getPrice());
                //小计
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));
                //订单ID
                orderItem.setOrderId(id);
                //图片
                orderItem.setPicPath(item.getImage());
                //商家ID
                orderItem.setSellerId(item.getSellerId());
                //小计之和
                totalPrice += orderItem.getTotalFee().doubleValue();

                //保存订单详情表
                orderItemDao.insertSelective(orderItem);
            }

            //实付金额
            order.setPayment(new BigDecimal(totalPrice));

            tp += order.getPayment().doubleValue()*100;

            //状态
            order.setStatus("1");
            //添加时间
            order.setCreateTime(new Date());
            //更新时间
            order.setUpdateTime(new Date());
            //订单来源
            order.setSourceType("2");
            //商家ID
            order.setSellerId(cart.getSellerId());
            //保存订单
            orderDao.insertSelective(order);
        }

        //保存日志表 (支付表)
        PayLog payLog = new PayLog();
        //ID
        payLog.setOutTradeNo(String.valueOf(idWorker.nextId()));

        //添加时间
        payLog.setCreateTime(new Date());
        //总金额  多张订单的总金额   单位是分

        payLog.setTotalFee(tp);
        //用户ID
        payLog.setUserId(order.getUserId());
        //支付状态
        payLog.setTradeState("0");
        //订单ID 列表     35,36
        payLog.setOrderList(ids.toString().replace("[","").replace("]",""));
        //支付类型
        payLog.setPayType(order.getPaymentType());

        //保存
        payLogDao.insertSelective(payLog);

        //保存到缓存中
        //redisTemplate.boundHashOps("payLog").put(order.getUserId(),payLog);

        //24小时就清除了
        redisTemplate.boundValueOps(order.getUserId()).set(payLog,24, TimeUnit.HOURS);

        //清理购物车
        //redisTemplate.delete("CART");
        //redisTemplate.boundHashOps("CART").delete(order.getUserId());

    }
}
