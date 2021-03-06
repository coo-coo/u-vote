package com.coo.u.vote.job;

import java.util.List;

import org.apache.log4j.Logger;

import com.coo.s.cloud.job.GenericCloudJob;
import com.coo.s.vote.model.Vote;
import com.coo.u.vote.VoteManager;
import com.kingstar.ngbf.s.mongo.MongoItem;
import com.kingstar.ngbf.s.mongo.QueryAttrs;

/**
 * 读取所有的Vote信息,放置在MC中,用来提供Account和Topic的投票匹配
 */

public class VoteJob extends GenericCloudJob {

	private static Logger logger = Logger.getLogger(VoteJob.class);

	@Override
	public void execute() {
		put2MC();
	}

	@Override
	public String getName() {
		return VoteJob.class.getName();
	}

	/**
	 * 放置Vote信息到MC中
	 */
	private void put2MC() {
		// 获得所有条目
		QueryAttrs query = QueryAttrs.blank().desc("_tsi");
		List<MongoItem> items = VoteManager.findItems(Vote.SET, query);
		logger.debug("vote size=" + items.size());
		for (MongoItem mi : items) {
			String account = (String) mi.get("voter");
			String topic_id = (String) mi.get("topic_id");
			String mcKey = "vote." + account + "." + topic_id;
			VoteManager.getMC().put(mcKey, mi, VoteManager.HOUR1);
		}
	}
}
