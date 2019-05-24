package cn.itcast.core.listener;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * 消息处理类  删除索引
 */
public class ItemDeleteListener implements MessageListener {

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public void onMessage(Message message) {
        ActiveMQTextMessage atm =  (ActiveMQTextMessage)message;
        try {
            String id = atm.getText();
            System.out.println("搜索项目在删除索引时接收到的ID:" + id);
            //2:删除索引库中的商品信息  商品的ID  库存表的外键
            SolrDataQuery solrDataQuery = new SimpleQuery();
            solrDataQuery.addCriteria(new Criteria("item_goodsid").is(id));
            solrTemplate.delete(solrDataQuery);
            solrTemplate.commit();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
