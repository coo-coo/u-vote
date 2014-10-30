package com.coo.u.vote.rest.helper;

import java.util.List;

import org.apache.log4j.Logger;

import com.coo.s.vote.model.Topic;
import com.coo.u.vote.ModelManager;
import com.coo.u.vote.VoteUtil;
import com.kingstar.ngbf.s.mongo.MongoItem;
import com.kingstar.ngbf.s.ntp.SimpleMessage;
import com.kingstar.ngbf.s.ntp.SimpleMessageHead;

/**
 * 
 */

public final class TopicHelper {

	private static Logger logger = Logger.getLogger(TopicHelper.class);

	/**
	 * 通过channel的KEY在MC找到对应的Channel对象
	 */
	public static SimpleMessage<Topic> findMcByChannelCode(String channelKey,
			String account) {
		SimpleMessage<Topic> sm = new SimpleMessage<Topic>(SimpleMessageHead.OK);
		Object value = VoteUtil.getMC().getValue(channelKey);
		logger.debug("channel=" + channelKey + "\taccount=" + account);
		if (value != null) {
			@SuppressWarnings("unchecked")
			List<MongoItem> items = (List<MongoItem>) value;
			// 添加每一个Topic节点
			for (MongoItem mi : items) {
				Topic topic = ModelManager.mi2Topic(mi);
				// 参见VoteJob, account对此topic是否投過票
				String mcKey = "vote." + account + "." + topic.get_id();
				boolean exist = VoteUtil.isMcExist(mcKey);
				if (exist) {
					topic.setVoted(true);
				}
				sm.addRecord(topic);
			}
		}
		// 如果没有,就空记录返回...
		sm.set("channel", channelKey);
		return sm;
	}

}
