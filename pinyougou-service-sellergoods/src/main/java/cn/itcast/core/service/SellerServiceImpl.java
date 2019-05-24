package cn.itcast.core.service;

import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.seller.Seller;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商家管理
 */
@Service
@Transactional
public class SellerServiceImpl implements  SellerService {


    @Autowired
    private SellerDao sellerDao;
    //添加
    @Override
    public void add(Seller seller) {
        //1:商家名称
        //2:商家密码
        seller.setPassword(new BCryptPasswordEncoder().encode(seller.getPassword()));
        //3:商家的店铺
        //4:商家的公司名称
        //5:状态 默认是未审核 0
        seller.setStatus("0");

        sellerDao.insertSelective(seller);



    }

    //根据用户名查询用户对象
    @Override
    public Seller findOne(String username) {
        return sellerDao.selectByPrimaryKey(username);
    }
}
