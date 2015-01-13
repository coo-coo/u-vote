package com.coo.u.vote.job;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.coo.s.cloud.job.GenericCloudJob;
import com.coo.s.vote.model.Topic;
import com.coo.s.vote.model.Vote;
import com.kingstar.ngbf.s.mongo.MongoItem;
import com.kingstar.ngbf.s.mongo.MongoUtil;
import com.kingstar.ngbf.s.mongo.QueryAttrs;

/**
 * Topic表最新状态更新 Topic.old + Vote(s) = Topic.new 进行快照设定
 */
public class TopicShotJob extends GenericCloudJob {

	private static Logger logger = Logger.getLogger(TopicShotJob.class);

	@Override
	public void execute() {
		// 获得上一次Job的执行时间,以获得时间戳，如果没有，则获得全部的Vote
		// 查询获得指定范围[lastTs,currentTs]的未被统计(status=="0")的Vote
		// long lastStartTs = this.getLastStartTs();
		// if (lastStartTs > 0l) {
		// // 表示有结算过，获取[lastTs,currentTs]范围内的Vote
		// query.and("_tsi", QueryAttr.OP_NOTBIGTHAN,
		// System.currentTimeMillis()).and("_tsi",
		// QueryAttr.OP_BIGTHAN, lastStartTs);
		// }

		// 查询获得未被统计(status=="0")的Vote
		QueryAttrs query = QueryAttrs.blank().add("status", 0);
		List<MongoItem> items = MongoUtil.getClient().findItems(Vote.SET,
				query);
		logger.debug("vote count=" + items.size());
		// 获得新的未统计过的投票数
		if (items.size() > 0) {
			Map<String, VoteCount> voteCounts = new HashMap<String, VoteCount>();
			// 统计各Topic的投票数量,Leg数量等
			for (MongoItem mi : items) {
				String topicId = (String) mi.get("topic_id");
				String legSeq = (String) mi.get("leg_seq");
				// 創建VoteCount，进行投票计数
				VoteCount vc = voteCounts.get(topicId);
				if (vc == null) {
					vc = new VoteCount(topicId);
				}
				vc.legCount(legSeq);
				voteCounts.put(topicId, vc);
			}

			// 定义快照时间
			long snapshot = System.currentTimeMillis();
			// 进行Topic的更新...
			// 获取所有的相关Topic
			Iterator<VoteCount> it = voteCounts.values().iterator();
			while (it.hasNext()) {
				VoteCount vc = it.next();
				String topicId = vc.getTopicId();
				// 获得Topic对象,准备更新
				MongoItem mi = MongoUtil.getClient().getItem(Topic.SET,
						topicId);

				if (mi != null) {
					Map<String, Object> data = mi.toMap();
					// 定义更新的数据
					Map<String, Object> update = new HashMap<String, Object>();
					Integer old = (Integer) data.get("vote");
					// 设置总投票数
					update.put("vote", old + vc.getCount());

					// 更新各Leg的投票数：总票数 == SUM(Leg投票数)
					@SuppressWarnings("unchecked")
					List<Map<String, Object>> legs = (List<Map<String, Object>>) data
							.get("legs");
					for (Map<String, Object> leg : legs) {
						String legSeq = (String) leg.get("leg_seq");
						Integer legVote = (Integer) leg.get("leg_vote");
						// 获得legSeq的投票值，可能为null，表明没有投票
						Integer vote = vc.getLegCount(legSeq);
						if (vote != null) {
							// 表明有对该LegSeq进行投票,即有 vote值
							// 否则为没有投票,也就没有Vote值
							Integer legTotal = legVote + vote;
							// 设置Leg的投票数
							leg.put("leg_vote", legTotal);
						}
					}
					// 重新UpdateLeg信息
					update.put("legs", legs);
					// 设置快照时间戳
					update.put("snapshot", snapshot);
					// 更新Topic数据,实现快照
					MongoUtil.getClient().update(Topic.SET, topicId, update);
				}
			}

			// 设置已计算过的Vote
			for (MongoItem vote : items) {
				Map<String, Object> item = new HashMap<String, Object>();
				// 设置Vote已统计
				item.put("status", Vote.STATUS_COUNTED);
				MongoUtil.getClient().update(Vote.SET, vote.get_id(), item);
			}

		}
	}

	@Override
	public String getName() {
		return TopicShotJob.class.getName();
	}
}
