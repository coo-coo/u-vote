package com.coo.u.vote.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.coo.s.vote.model.Focus;
import com.coo.s.vote.model.Topic;
import com.coo.u.vote.ModelManager;
import com.coo.u.vote.VoteManager;
import com.kingstar.ngbf.s.mongo.MongoItem;
import com.kingstar.ngbf.s.mongo.QueryAttrs;
import com.kingstar.ngbf.s.util.PubString;

/**
 * 获取TopicShot对象，组织成为Channel对象放置到MC中
 */
@Component
public class TopicFetchHandler {

	protected static Logger logger = Logger.getLogger(TopicFetchHandler.class);

	// 单位时间差:两小时
	private static long DELTA_TS = 6 * 3600 * 1000;

	/**
	 * 获得所有相应的Focus，计算是否需要推送
	 */
	@Async
	public void fetchAccountFocusTopic() {
		// 查找结果集
		QueryAttrs query = QueryAttrs.blank().add("type", Focus.TYPE_TOPIC)
				.add("status", Focus.STATUS_VALID);
		List<MongoItem> items = VoteManager.findItems(Focus.C_NAME, query);

		// 定义分发队列
		Map<String, List<MongoItem>> channels = new HashMap<String, List<MongoItem>>();

		// 获得当前时间戳
		long current = System.currentTimeMillis();
		for (MongoItem mi : items) {
			// 定义Focus对象,初始化
			Focus focus = new Focus();
			ModelManager.merge(mi, focus);
			// 超过周期时间的DELTA_TS(MS)内，该Topic需要进行推送 period(天)
			// int period = focus.getPeriod();
			// TODO 暂定为0天,即关注(DELTA_TS)小时之内的
			int period = 0;
			long diff = current - focus.get_tsi() - period * 24 * 3600 * 1000;
			if (diff > 0l && diff < DELTA_TS) {
				// 从MC中获取该Topic,参见TopicJob
				String mcKey = "topic." + focus.getSubject();
				MongoItem topicMI = (MongoItem) VoteManager.getMC()
						.getValue(mcKey);
				if (topicMI != null) {
					// 放置队列中，Key值就是頻道值
					String channelCode = "channel_focus_" + focus.getAccount();
					List<MongoItem> list = channels.get(channelCode);
					if (list == null) {
						list = new ArrayList<MongoItem>();
						channels.put(channelCode, list);
					}
					// TODO 队列是否有Size的限制?
					list.add(topicMI);
				}
			}
		}

		// 放置MC中
		Set<Entry<String, List<MongoItem>>> set = channels.entrySet();
		for (Entry<String, List<MongoItem>> entry : set) {
			logger.debug(entry.getKey() + "\tMC size="
					+ entry.getValue().size());
			VoteManager.getMC().put(entry.getKey(), entry.getValue(),
					Integer.MAX_VALUE);
		}
	}

	/**
	 * 获取最新的X条topic，放入memcached
	 */
	@Async
	public void fetchLatest(int limit) {
		// 查找MongoItem对象
		QueryAttrs query = QueryAttrs.blank().desc("_tsi").limit(limit);
		List<MongoItem> items = VoteManager.getMongo().findItems(Topic.C_NAME,
				query);
		VoteManager.getMC().put("channel_latest", items, Integer.MAX_VALUE);
	}

	/**
	 * 获取投票数最多的X条topic，放入memcached
	 */
	@Async
	public void fetchTop(int limit) {
		QueryAttrs query = QueryAttrs.blank().desc("vote").limit(limit);
		List<MongoItem> items = VoteManager.getMongo().findItems(Topic.C_NAME,
				query);
//		logger.debug("fetchTop MongoItem size:" + items.size());
		VoteManager.getMC().put("channel_top", items, Integer.MAX_VALUE);
	}

	/**
	 * 根据所有的ChannelTypes，来获得指定Channel下的Topic列表
	 * 根据Topic所在的channels属性(多个以逗号隔开)来进行分离
	 * 获取到所有或者最近limit条数的topic，以最新更新排序，分离到多个channel中 TODO 暂时不考虑前后次的计时时间 TODO
	 * 分佈式，多線程處理，是的分拆的更快
	 */
	@Async
	public void fetchChannelTypes(int limit) {
		// TODO 暂时获得1000条,也可以是全部?
		QueryAttrs query = QueryAttrs.blank().desc("_tsu").limit(limit);
		// 获得条目
		List<MongoItem> items = VoteManager.getMongo().findItems(Topic.C_NAME,
				query);

		// 定義TypeChannels
		Map<String, List<MongoItem>> channels = new HashMap<String, List<MongoItem>>();

		for (MongoItem mi : items) {
			Topic topic = ModelManager.mi2Topic(mi);
			String ccodes = topic.getChannels();
			if (!PubString.isNullOrSpace(ccodes)) {
				// 获得所在频道的Code
				String[] channelCodes = PubString.stringToArray(ccodes);
				for (String cc : channelCodes) {
					String channelCode = "channel_type_" + cc;
					List<MongoItem> list = channels.get(channelCode);
					if (list == null) {
						list = new ArrayList<MongoItem>();
						channels.put(channelCode, list);
					} else {
						list.add(mi);
					}
				}
			}
		}

		// 放置MC中
		Set<Entry<String, List<MongoItem>>> set = channels.entrySet();
		for (Entry<String, List<MongoItem>> entry : set) {
			VoteManager.getMC().put(entry.getKey(), entry.getValue(),
					Integer.MAX_VALUE);
		}
	}

}
