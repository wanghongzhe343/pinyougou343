package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import com.alibaba.dubbo.config.annotation.Service;
import org.opensaml.xml.signature.J;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import vo.Cart;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车管理
 */
@Service
public class CartServiceImpl implements CartService{


    @Autowired
    private ItemDao itemDao;
    @Autowired
    private RedisTemplate redisTemplate;

    //根据库存ID查询库存对象
    @Override
    public Item findItemById(Long itemId) {
        return itemDao.selectByPrimaryKey(itemId);
    }

    //装购物车装满
    @Override
    public List<Cart> findAllCartList(List<Cart> cartList) {

        for (Cart cart : cartList) {
            //商家名称
            //订单详情集合
            List<OrderItem> orderItemList = cart.getOrderItemList();
            for (OrderItem orderItem : orderItemList) {
                //库存ID 数量
                Item item = findItemById(orderItem.getItemId());
                //图片
                orderItem.setPicPath(item.getImage());
                //标题
                orderItem.setTitle(item.getTitle());
                //单价
                orderItem.setPrice(item.getPrice());
                //小计
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));
                //商家名称
                cart.setSellerName(item.getSeller());
            }

        }

        return cartList;
    }

    //合并购物车到缓存中
    @Override
    public void addCartListToRedis(List<Cart> newCartList,String name) {
        //分析:缓存中使用什么类型保存购物车
        //1:先获取缓存中的购物车
        List<Cart> oldCartList = (List<Cart>) redisTemplate.boundHashOps("CART").get(name);

        //2:在新老车集合大合并  最终合并到老车
        oldCartList = mergeCartList(newCartList,oldCartList);

        //3:将合并后老车再次保存缓存 覆盖掉之前购物车
        redisTemplate.boundHashOps("CART").put(name,oldCartList);

    }

    //从缓存中查询购物车
    @Override
    public List<Cart> findCartListFromRedis(String name) {
        return (List<Cart>) redisTemplate.boundHashOps("CART").get(name);
    }

    //在新老车合并  最终合并到老车
    public List<Cart> mergeCartList(List<Cart> newCartList,List<Cart> oldCartList){
        //判断新车集合是否为空
        if(null != newCartList && newCartList.size() > 0){
            //判断老车集合是否为空
            if(null != oldCartList && oldCartList.size() > 0){
                //新老车大合并
                for (Cart newCart : newCartList) {

                    //1:判断新购物车中商家是否在老购物车集合中已经有了
                    int j = oldCartList.indexOf(newCart);
                    if(j != -1){
                        //-- 存在   oldCart 与 newCart 是同一商家
                        Cart oldCart = oldCartList.get(j);
                        List<OrderItem> oldOrderItemList = oldCart.getOrderItemList();
                        List<OrderItem> newOrderItemList = newCart.getOrderItemList();

                        for (OrderItem newOrderItem : newOrderItemList) {
                        //2:新的订单详情在老的订单详情集合中是否已经存在
                            int k = oldOrderItemList.indexOf(newOrderItem);
                            if(k != -1){
                                //--存在
                                OrderItem oldOrderItem = oldOrderItemList.get(k);
                                oldOrderItem.setNum(oldOrderItem.getNum() + newOrderItem.getNum());
                            }else{
                                //--不存在  直接添加新订单详情
                                oldOrderItemList.add(newOrderItem);
                            }

                        }
                    }else{
                        //-- 不存在 直接添加
                        oldCartList.add(newCart);
                    }
                }
            }else{
                return newCartList;
            }

        }
        return oldCartList;
    }
}
