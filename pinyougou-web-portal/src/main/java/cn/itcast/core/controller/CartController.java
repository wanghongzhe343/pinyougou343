package cn.itcast.core.controller;

import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.service.CartService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vo.Cart;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车管理
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;

    //加入购物车
    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins = {"http://localhost:9003"})
    public Result addGoodsToCartList(Long itemId, Integer num, HttpServletResponse response
            , HttpServletRequest request) {

        try {

            List<Cart> cartList = null;

            //标记
            boolean l = false;

//            1:获取Cookie
            Cookie[] cookies = request.getCookies();
            if (null != cookies && cookies.length > 0) {
                for (Cookie cookie : cookies) {
//            2:获取Cookie中购物车集合
                    if ("CART".equals(cookie.getName())) {
                        cartList = JSON.parseArray(cookie.getValue(), Cart.class);
                        l = true;
                    }
                }
            }
//            3:没有 创建购物车
            if (null == cartList) {
                cartList = new ArrayList<>();
            }
//            4:追加当前款
            Cart newCart = new Cart();

            //创建新的订单详情对象
            OrderItem newOrderItem = new OrderItem();
            //库存ID
            newOrderItem.setItemId(itemId);
            //数量
            newOrderItem.setNum(num);
            //创建订单详情集合
            List<OrderItem> newOrderItemList = new ArrayList<>();
            newOrderItemList.add(newOrderItem);
            newCart.setOrderItemList(newOrderItemList);

            //根据库存ID查询 商家ID
            Item item = cartService.findItemById(itemId);

            newCart.setSellerId(item.getSellerId());

            //1:判断新购物车中商家是否在老购物车集合中已经存在
            int indexOf = cartList.indexOf(newCart);   // indexOf 不存在是-1  存在返回角标
            if (indexOf != -1) {
                //-- 存在
                //2:判断新购物车中的新订单详情在    从老购物车集合中找出跟新购物车相同商家的老购物车的老订单详情集合中是否存在
                Cart oldCart = cartList.get(indexOf); //此老车就和新车是同商家
                List<OrderItem> oldOrderItemList = oldCart.getOrderItemList();
                int i = oldOrderItemList.indexOf(newOrderItem);
                if (i != -1) {
                    //-- 存在   追加数量
                    OrderItem oldOrderItem = oldOrderItemList.get(i);
                    oldOrderItem.setNum(oldOrderItem.getNum() + newOrderItem.getNum());
                } else {
                    //-- 不存在  直接添加
                    oldOrderItemList.add(newOrderItem);
                }

            } else {
                //-- 不存在 直接添加
                cartList.add(newCart);
            }
            //判断是否登陆 是否获取当前登陆人的用户名        空指针异常
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            if (!"anonymousUser".equals(name)) {
                //登陆了
//                5:将合并后的购物车合并到Redis缓存中
                cartService.addCartListToRedis(cartList, name);


                if (l) {
//                6: 清空Cookie 回写浏览器
                    Cookie cookie = new Cookie("CART", null);
                    cookie.setPath("/");
                    cookie.setMaxAge(0);// 0 :立即销毁  -1:关闭浏览器销毁 >0:到时间销毁 (秒)
                    response.addCookie(cookie);
                }
            } else {
                //未登陆
//            5:创建Cookie添加购物车集合
                Cookie cookie = new Cookie("CART", JSON.toJSONString(cartList));
                cookie.setMaxAge(60 * 60 * 24 * 365);
                cookie.setPath("/");
                //为什么设置/
                //http://www.pinyougou.com/cart/addGoodsToCartList.do
                //http://www.pinyougou.com/shopping/submitOrder.do
//            6:回写浏览器
                response.addCookie(cookie);

            }

            return new Result(true, "加入购物车成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "加入购物车失败");
        }

    }

    //查询购物车集合或列表
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(HttpServletRequest request,HttpServletResponse response) {

        List<Cart> cartList = null;
//       1:获取Cookie
        Cookie[] cookies = request.getCookies();
        if (null != cookies && cookies.length > 0) {
            for (Cookie cookie : cookies) {
//            2:获取Cookie中购物车集合
                if ("CART".equals(cookie.getName())) {
                    cartList = JSON.parseArray(cookie.getValue(), Cart.class);
                    break;
                }
            }
        }
        //判断是否登陆 是否获取当前登陆人的用户名        空指针异常
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!"anonymousUser".equals(name)) {
            //登陆了
//            3:有 将此购物车合并到Redis缓存中  清空Cookie 回写浏览器
            if(null != cartList){
                cartService.addCartListToRedis(cartList,name);
                Cookie cookie = new Cookie("CART", null);
                cookie.setPath("/");
                cookie.setMaxAge(0);// 0 :立即销毁  -1:关闭浏览器销毁 >0:到时间销毁 (秒)
                response.addCookie(cookie);
            }
//            4: 从缓存中将购物车集合查询出来
            cartList = cartService.findCartListFromRedis(name);
        }
//        5:有  将购物车装满
        if (null != cartList) {//商家ID 库存ID  数量 其它值都没有
            cartList = cartService.findAllCartList(cartList);
        }
//        6:回显
        return cartList;
    }
}
