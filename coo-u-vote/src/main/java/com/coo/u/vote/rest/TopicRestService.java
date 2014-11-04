package com.coo.u.vote.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.coo.s.vote.model.Topic;
import com.coo.s.vote.model.TopicLeg;
import com.coo.s.vote.model.Vote;
import com.coo.u.vote.ModelManager;
import com.coo.u.vote.VoteUtil;
import com.kingstar.ngbf.s.mongo.MongoItem;
import com.kingstar.ngbf.s.mongo.QueryAttrs;
import com.kingstar.ngbf.s.ntp.NtpHead;
import com.kingstar.ngbf.s.ntp.NtpMessage;

/**
 * @description
 */
@Controller
@RequestMapping("/topic")
public class TopicRestService extends CommonRest {

	// private static Logger logger = Logger.getLogger(TopicRestService.class);

	/**
	 * 状态变更，用于Admin管理
	 */
	@RequestMapping(value = "/update/_id/{_id}/status/{status}", method = RequestMethod.GET)
	@ResponseBody
	public NtpMessage updateStatus(@PathVariable("_id") String _id,
			@PathVariable("status") int status) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("status", status);
		VoteUtil.getMongo().update(Topic.C_NAME, _id, map);
		return NtpMessage.ok();
	}

	/**
	 * 获得我(创建)的Topic列表，从MongoDB中直接获取
	 */
	@RequestMapping(value = "/list/mine", method = RequestMethod.GET)
	@ResponseBody
	public NtpMessage listOfMine(HttpServletRequest req) {
		// 查找MongoItem对象
		String account = this.getOperator(req);
		QueryAttrs query = QueryAttrs.blank().and("owner", account)
				.desc("_tsi");
		List<MongoItem> items = VoteUtil.findItems(Topic.C_NAME, query);
		String channelCode = "channel_mine_" + account;
		return build(channelCode, items);
	}

	/**
	 * 根据List<MongoItem>返回消息
	 */
	private NtpMessage build(String channelCode, List<MongoItem> items) {
		NtpMessage resp = NtpMessage.ok();
		for (MongoItem mi : items) {
			// 对象转换MongoItem->Topic
			Topic topic = ModelManager.mi2Topic(mi);
			resp.add(topic);
		}
		resp.set("channel", channelCode);
		return resp;
	}

	/**
	 * 获得管理的所有最新的Topic,进行管理创建
	 */
	@RequestMapping(value = "/list/admin", method = RequestMethod.GET)
	@ResponseBody
	public NtpMessage listOfAdmin(HttpServletRequest req) {
		// 查找MongoItem对象
		String account = this.getOperator(req);
		QueryAttrs query = QueryAttrs.blank().desc("_tsi").limit(100);
		List<MongoItem> items = VoteUtil.findItems(Topic.C_NAME, query);
		String channelCode = "channel_admin_" + account;
		return build(channelCode, items);
	}

	/**
	 * 根据channelCode类型和请求账号,获得Topic Topic需要增加
	 */
	@RequestMapping(value = "/list/code/{code}", method = RequestMethod.GET)
	@ResponseBody
	public NtpMessage listOfCode(HttpServletRequest req,
			@PathVariable("code") String code) {
		String account = this.getOperator(req);
		// "channel_latest" "channel_top" ……;
		// "channel_type_tiyu" "channel_type_yinyue" ……;
		// "channel_focus_13917081673" ……;
		// .不可以传递！改为_
		return findMcByChannelCode(code, account);
	}

	/**
	 * 创建一个Topic M端传递过来的是NtpMessage，参见TopicCreateActivity
	 */
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	@ResponseBody
	public NtpMessage create(@RequestBody String data) {
		try {
			NtpMessage topic = NtpMessage.bind(data);
			Map<String, Object> item = new HashMap<String, Object>();
			item.putAll(topic.getData()); // title/owner
			item.put("expired", 0l); // 到期时间
			item.put("status", Topic.STATUS_VALID); // 状态
			item.put("vote", 0); // 投票数
			item.put("snapshot", System.currentTimeMillis()); // 快照时间
			List<Map<String, Object>> legsMap = new ArrayList<Map<String, Object>>();
			List<TopicLeg> legs = topic.getItems(TopicLeg.class);
			for (TopicLeg leg : legs) {
				Map<String, Object> lm = new HashMap<String, Object>();
				lm.put("leg_seq", leg.getSeq());
				lm.put("leg_vote", leg.getVote());
				lm.put("leg_title", leg.getTitle());
				legsMap.add(lm);
			}
			item.put("legs", legsMap);
			VoteUtil.getMongo().insert(Topic.C_NAME, item);
			return NtpMessage.ok();
		} catch (Exception e) {
			return NtpMessage.error(e.getMessage());
		}
	}

	/**
	 * 对一个Topic进行投票
	 */
	@RequestMapping(value = "/vote/topic_id/{topic_id}/legSeq/{legSeq}", method = RequestMethod.GET)
	@ResponseBody
	public NtpMessage vote(HttpServletRequest req,
			@PathVariable("topic_id") String topic_id,
			@PathVariable("legSeq") String legSeq) {
		// logger.debug(account + "\t" + topicId + "\t" + legSeq);
		// 存储在MongoDB中
		String account = this.getOperator(req);
		Vote vote = new Vote();
		vote.setVoter(account);
		vote.setLegSeq(legSeq);
		vote.setTopicId(topic_id);
		// TODO 分表存储...
		VoteUtil.getMongo().insert(Vote.C_NAME, ModelManager.toMap(vote));
		return NtpMessage.ok();
	}

	/**
	 * 对一个Topic的某一个参数进行值设定 指定参数名称,参数值,参数数据类型等
	 */
	@RequestMapping(value = "/update/param", method = RequestMethod.POST)
	@ResponseBody
	public NtpMessage updateParam(@RequestBody String data) {
		NtpMessage resp = NtpMessage.ok();
		// 获得Android客户端传来的data(Topic)的数据
		NtpMessage sm = NtpMessage.bind(data);
		if (sm != null) {
			// account._id
			String _id = (String) sm.get("_id");
			// 字段名称 & 值
			String key = (String) sm.get("key");
			Object value = sm.getData().get("value");
			// 直接Map对象传递到数据库中
			Map<String, Object> item = new HashMap<String, Object>();
			item.put(key, value);
			VoteUtil.getMongo().update(Topic.C_NAME, _id, item);
		} else {
			resp = resp.head(NtpHead.PARAMETER_ERROR);
		}
		return resp;
	}

	/**
	 * 通过channel的KEY在MC找到对应的Channel对象
	 */
	private NtpMessage findMcByChannelCode(String channelCode, String account) {
		NtpMessage sm = NtpMessage.ok();
		Object value = VoteUtil.getMC().getValue(channelCode);
		// logger.debug("channel=" + channelKey + "\taccount=" + account);
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
				sm.add(topic);
			}
		}
		// 如果没有,就空记录返回...
		sm.set("channel", channelCode);
		return sm;
	}

	// ///////////////////////////////////////////////
}
