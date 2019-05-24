package cn.itcast.core.service;

import cn.itcast.core.dao.specification.SpecificationDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import vo.SpecificationVo;

import java.util.List;
import java.util.Map;

/**
 * 规格管理
 */
@Service
@Transactional
public class SpecificationServiceImpl implements  SpecificationService {


    @Autowired
    private SpecificationDao specificationDao;
    @Autowired
    private SpecificationOptionDao specificationOptionDao;

    //查询规格分页对象 第二遍

    @Override
    public PageResult search(Integer page, Integer rows, Specification specification) {
        //分页小助手
        PageHelper.startPage(page,rows);
        //查询分页对象
        Page<Specification> p  = (Page<Specification>) specificationDao.selectByExample(null);
        return new PageResult(p.getTotal(),p.getResult());
    }

    //添加
    @Override
    public void add(SpecificationVo vo) {
        //规格表 返回规格表的ID
        specificationDao.insertSelective(vo.getSpecification());
        
        //规格选项表 (多条)
        List<SpecificationOption> specificationOptionList = vo.getSpecificationOptionList();
        for (SpecificationOption specificationOption : specificationOptionList) {
            //外键
            specificationOption.setSpecId(vo.getSpecification().getId());
            //保存
            specificationOptionDao.insertSelective(specificationOption);

        }
    }

    //查询一个包装对象
    @Override
    public SpecificationVo findOne(Long id) {
        SpecificationVo vo = new SpecificationVo();
        //规格对象  1
        vo.setSpecification(specificationDao.selectByPrimaryKey(id));
        //规格选项对象是多个
        SpecificationOptionQuery query = new SpecificationOptionQuery();
        query.createCriteria().andSpecIdEqualTo(id);
        vo.setSpecificationOptionList(specificationOptionDao.selectByExample(query));
        return vo;
    }

    //修改
    @Override
    public void update(SpecificationVo vo) {
        //修改规格对象
        specificationDao.updateByPrimaryKeySelective(vo.getSpecification());
        //修改规格选项对象
        //1:删除原来的所有
        SpecificationOptionQuery query = new SpecificationOptionQuery();
        query.createCriteria().andSpecIdEqualTo(vo.getSpecification().getId());
        specificationOptionDao.deleteByExample(query);
        //2:添加现在的所有
        //规格选项表 (多条)
        List<SpecificationOption> specificationOptionList = vo.getSpecificationOptionList();
        for (SpecificationOption specificationOption : specificationOptionList) {
            //外键
            specificationOption.setSpecId(vo.getSpecification().getId());
            //保存
            specificationOptionDao.insertSelective(specificationOption);

        }
    }

    //查询所有规格结果集
    @Override
    public List<Map> selectOptionList() {
        return specificationDao.selectOptionList();
    }
}
