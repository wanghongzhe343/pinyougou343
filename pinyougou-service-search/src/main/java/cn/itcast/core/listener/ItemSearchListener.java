package cn.itcast.core.listener;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.List;

/**
 * 自定义消息处理类
 */
public class ItemSearchListener implements MessageListener {


    @Autowired
    private ItemDao itemDao;
    @Autowired
    private SolrTemplate solrTemplate;


    @Override
    public void onMessage(Message message) {

        ActiveMQTextMessage atm =  (ActiveMQTextMessage)message;

        //商品ID
        try {
            String id = atm.getText();
            System.out.println("搜索管理项目中接收到的ID:" + id);

            //2:将商品信息保存到索引库

            //根据商品ID 外键 查询库存集合  库存数量 低于
            ItemQuery itemQuery = new ItemQuery();
            itemQuery.createCriteria().andGoodsIdEqualTo(Long.parseLong(id)).andIsDefaultEqualTo("1").andStatusEqualTo("1");
            List<Item> itemList = itemDao.selectByExample(itemQuery);
            solrTemplate.saveBeans(itemList,1000);


        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
