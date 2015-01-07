package com.coo.u.vote.job;

import java.util.List;

import org.apache.log4j.Logger;

import com.coo.s.vote.model.Topic;
import com.coo.u.vote.VoteManager;
import com.kingstar.ngbf.s.mongo.MongoItem;
import com.kingstar.ngbf.s.mongo.QueryAttrs;

/**
 * 读取所有的Vote信息,放置在MC中,用来提供Account和Topic的投票匹配
 * 
 * @description
 * @author boqing.shen
 * @date 2014-9-19 下午2:04:22
 * @since 1.0.0.0
 */

public class TopicJob extends AbstractJob {

	protected static Logger logger = Logger.getLogger(TopicJob.class);

	@Override
	public void execute() {
		put2MC();
	}

	@Override
	public String getName() {
		return TopicJob.class.getName();
	}

	/**
	 * 放置信息到MC中
	 */
	private void put2MC() {
		// 获得所有符合条件的条目
		QueryAttrs query = QueryAttrs.blank().add("status", Topic.STATUS_VALID);
		List<MongoItem> items = VoteManager.findItems(Topic.C_NAME, query);

		logger.debug("topic size=" + items.size());
		for (MongoItem mi : items) {
			String mcKey = "topic." + mi.get_id();
			VoteManager.getMC().put(mcKey, mi, VoteManager.MIN1 * 60);
		}
	}
}
