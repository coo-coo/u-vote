package com.coo.u.vote.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.coo.s.vote.model.Topic;
import com.coo.s.vote.model.Vote;
import com.coo.u.vote.ModelManager;
import com.coo.u.vote.VoteUtil;
import com.coo.u.vote.rest.helper.TopicHelper;
import com.google.gson.Gson;
import com.kingstar.ngbf.s.mongo.MongoItem;
import com.kingstar.ngbf.s.mongo.QueryAttrs;
import com.kingstar.ngbf.s.ntp.SimpleMessage;
import com.kingstar.ngbf.s.ntp.SimpleMessageHead;

/**
 * @description
 */
@Controller
@RequestMapping("/")
public class TopicRestService {

//	private static Logger logger = Logger.getLogger(TopicRestService.class);

	/**
	 * 状态变更，用于Admin管理
	 */
	@RequestMapping(value = "/topic/update/{topicId}/status/{status}")
	@ResponseBody
	public SimpleMessage<?> topicUpdateStatus(
			@PathVariable("topicId") String topicId,
			@PathVariable("status") int status) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("status", status);
		VoteUtil.getMongo().update(Topic.C_NAME, topicId, map);
		return SimpleMessage.ok();
	}

	/**
	 * 获得我(创建)的Topic列表，从MongoDB中直接获取
	 */
	@RequestMapping(value = "/topic/mine/account/{account}", method = RequestMethod.GET)
	@ResponseBody
	public SimpleMessage<Topic> topicMine(
			@PathVariable("account") String account) {
		// 查找对象
		QueryAttrs query = QueryAttrs.blank().and("owner", account)
				.desc("_tsi");
		List<MongoItem> items = VoteUtil.findItems(Topic.C_NAME, query);
		// 返回信息
		SimpleMessage<Topic> resp = new SimpleMessage<Topic>(
				SimpleMessageHead.OK);
		for (MongoItem mi : items) {
			Topic topic = ModelManager.mi2Topic(mi);
			resp.addRecord(topic);
		}
		resp.set("channel", "topic.mine.account." + account);
		return resp;
	}

	/**
	 * 根据channelCode类型和请求账号,获得Topic Topic需要增加
	 */
	@RequestMapping(value = "/topic/code/{code}/account/{account}", method = RequestMethod.GET)
	@ResponseBody
	public SimpleMessage<Topic> topicCode(@PathVariable("code") String code,
			@PathVariable("account") String account) {
		// "c.topic.latest" "c.topic.top" ……;
		// "c.type.tiyu" "c.type.yinyue" ……;
		// "c.account.focus.13917081673" ……;
		// .可以传递?
		return TopicHelper.findMcByChannelCode(code, account);
	}

	/**
	 * 获得某类型(频道)的Topic列表
	 * 
	 * @deprecated 参见topicCode
	 */
	@RequestMapping(value = "/topic/type/{type}/account/{account}", method = RequestMethod.GET)
	@ResponseBody
	public SimpleMessage<Topic> topicType(@PathVariable("type") String type,
			@PathVariable("account") String account) {
		String channelKey = "c.type." + type;
		return TopicHelper.findMcByChannelCode(channelKey, account);
	}

	/**
	 * 获得我关注Topic列表
	 * 
	 * @deprecated 参见topicCode
	 */
	@RequestMapping(value = "/topic/focus/account/{account}", method = RequestMethod.GET)
	@ResponseBody
	public SimpleMessage<Topic> topicFocus(
			@PathVariable("account") String account) {
		String channelKey = "c.account.focus." + account;
		return TopicHelper.findMcByChannelCode(channelKey, account);
	}

	/**
	 * 创建一个Topic 为一个Topic
	 */
	@RequestMapping(value = "/topic/create", method = RequestMethod.POST)
	@ResponseBody
	public SimpleMessage<?> topicCreate(@RequestBody String data) {
		SimpleMessage<?> resp = SimpleMessage.ok();
		// 获得Android客户端传来的data(Topic)的数据
//		logger.debug(data);
		Gson gson = new Gson();
		Topic topic = gson.fromJson(data, Topic.class);
		if (topic != null) {
			// 创建Topic对象的Map数据，存储在MongoDB中...
			Map<String, Object> item = ModelManager.topic2MI(topic);
			VoteUtil.getMongo().insert(Topic.C_NAME, item);
		} else {
//			logger.debug("数据有误:" + data);
			resp = SimpleMessage.blank()
					.head(SimpleMessageHead.PARAMETER_ERROR);
		}
		return resp;
	}

	/**
	 * 对一个Topic进行投票
	 */
	@RequestMapping(value = "/topic/vote/account/{account}/topic/{topicId}/legSeq/{legSeq}", method = RequestMethod.GET)
	@ResponseBody
	public SimpleMessage<?> topicVote(@PathVariable("account") String account,
			@PathVariable("topicId") String topicId, @PathVariable String legSeq) {
//		logger.debug(account + "\t" + topicId + "\t" + legSeq);
		// 存储在MongoDB中
		Vote vote = new Vote();
		vote.setVoter(account);
		vote.setLegSeq(legSeq);
		vote.setTopicId(topicId);
		// TODO 分表存储...
		VoteUtil.getMongo().insert(Vote.C_NAME, ModelManager.toMap(vote));
		SimpleMessage<?> resp = SimpleMessage.ok();
		resp.getHead().setApi_code("topic_vote");
		return resp;
	}

	/**
	 * 对一个Topic的某一个参数进行值设定 指定参数名称,参数值,参数数据类型等
	 */
	@RequestMapping(value = "/topic/update/id/{topicId}/param/{param}/value/{value}/type/{type}", method = RequestMethod.GET)
	@ResponseBody
	public SimpleMessage<?> topicUpdate(
			@PathVariable("topicId") String topicId,
			@PathVariable("param") String param,
			@PathVariable("value") String value,
			@PathVariable("type") int dataType) {
//		logger.debug("topicId=" + topicId + "\tparam=" + param + "\tvalue="
//				+ value);
		Map<String, Object> item = new HashMap<String, Object>();
		// item.put("expired", System.currentTimeMillis());
		// type:0-String;1-Integer;2-Long;3-Double
		switch (dataType) {
		case 0:
			item.put(param, value);
			break;
		case 1:
			item.put(param, Integer.parseInt(value));
			break;
		case 2:
			item.put(param, Long.parseLong(value));
			break;
		case 3:
			item.put(param, Double.parseDouble(value));
			break;
		default:
			break;
		}
		VoteUtil.getMongo().update(Topic.C_NAME, topicId, item);
		SimpleMessage<?> resp = SimpleMessage.ok();
		resp.getHead().setApi_code("topic_update");
		return resp;
	}
}
