package cn.itcast.core.service;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.BrandQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import entity.PageResult;
import org.csource.fastdfs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 品牌管理
 */
@Service
@Transactional
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandDao brandDao;

    //查询分页对象 条件
    @Override
    public PageResult search(Integer pageNum, Integer pageSize, Brand brand) {

        //分页小助手
        PageHelper.startPage(pageNum,pageSize);

        BrandQuery brandQuery = new BrandQuery();
        BrandQuery.Criteria criteria = brandQuery.createCriteria();

        //判断品牌名称
        if(null != brand.getName() && !"".equals(brand.getName())){
            criteria.andNameLike("%" + brand.getName() + "%");
        }
        //判断首字母

        if(null != brand.getFirstChar() && !"".equals(brand.getFirstChar())){
            criteria.andFirstCharEqualTo(brand.getFirstChar());
        }
        //查询
        Page<Brand> p = (Page<Brand>) brandDao.selectByExample(brandQuery);
        //分页对象
        //PageInfo<Brand> info = new PageInfo<>(brands);
        return new PageResult(p.getTotal(),p.getResult());

    }

    //查询所有品牌 返回值 List<Map
    @Override
    public List<Map> selectOptionList() {

        return brandDao.selectOptionList();
    }

    //查询所有品牌
    @Override
    public List<Brand> findAll() {
        return brandDao.selectByExample(null);
    }

    //查询分页对象
    @Override
    public PageResult findPage(Integer pageNum, Integer pageSize) {
        //分页小助手
        PageHelper.startPage(pageNum,pageSize);
        //查询
        Page<Brand> p = (Page<Brand>) brandDao.selectByExample(null);
        //分页对象
        //PageInfo<Brand> info = new PageInfo<>(brands);
        return new PageResult(p.getTotal(),p.getResult());
    }

    //添加品牌
    @Override
    public void add(Brand brand) {
        brandDao.insertSelective(brand);

        // insert into tb_brand (id,name,first_char,1000个) values(1,haha,h,1000个null)  效果 性能不一样 1003个字段
        // insert into tb_brand (id,name,first_char) values(1,haha,h)  三个字段


    }

    //查询一个品牌
    @Override
    public Brand findOne(Long id) {
        return brandDao.selectByPrimaryKey(id);
    }

    //修改
    @Override
    public void update(Brand brand) {

        brandDao.updateByPrimaryKeySelective(brand);
    }

    //删除
    @Override
    public void delete(Long[] ids) {
   /*     for (Long id : ids) {
            //一个一个删除
            brandDao.deleteByPrimaryKey(id);
        }*/
        //delete from tb_brand where id = 1
        //delete from tb_brand where id in (1,2,3)
        //创建条件对象
        BrandQuery brandQuery = new BrandQuery();
        //获取条件对象 内部对象
        BrandQuery.Criteria criteria = brandQuery.createCriteria();

        //逆向工程  单表操作  不能关联  不能聚合 不能分组 不能分组之后再条件查询 等等  手写Sql
        // 集合底层是数组
        criteria.andIdIn(Arrays.asList(ids));
        //批量删除
        brandDao.deleteByExample(brandQuery);
    }


}
