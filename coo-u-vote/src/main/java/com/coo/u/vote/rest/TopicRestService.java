package com.coo.u.vote.rest;

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
import com.coo.s.vote.model.Vote;
import com.coo.u.vote.ModelManager;
import com.coo.u.vote.VoteUtil;
import com.google.gson.Gson;
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
		String account = this.getOperator(req);
		// 查找对象
		QueryAttrs query = QueryAttrs.blank().and("owner", account)
				.desc("_tsi");
		List<MongoItem> items = VoteUtil.findItems(Topic.C_NAME, query);
		// 返回信息
		NtpMessage resp = NtpMessage.ok();
		for (MongoItem mi : items) {
			Topic topic = ModelManager.mi2Topic(mi);
			resp.add(topic);
		}
		resp.set("channel", "topic.mine.account." + account);
		return resp;
	}

	/**
	 * 根据channelCode类型和请求账号,获得Topic Topic需要增加
	 */
	@RequestMapping(value = "/list/code/{code}", method = RequestMethod.GET)
	@ResponseBody
	public NtpMessage listOfCode(HttpServletRequest req,
			@PathVariable("code") String code) {
		String account = this.getOperator(req);
		// "c.topic.latest" "c.topic.top" ……;
		// "c.type.tiyu" "c.type.yinyue" ……;
		// "c.account.focus.13917081673" ……;
		// .可以传递?
		return findMcByChannelCode(code, account);
	}

	/**
	 * 创建一个Topic 为一个Topic
	 */
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	@ResponseBody
	public NtpMessage create(@RequestBody String data) {
		NtpMessage resp = NtpMessage.ok();
		// 获得Android客户端传来的data(Topic)的数据
		// logger.debug(data);
		Gson gson = new Gson();
		Topic topic = gson.fromJson(data, Topic.class);
		if (topic != null) {
			// 创建Topic对象的Map数据，存储在MongoDB中...
			Map<String, Object> item = ModelManager.topic2MI(topic);
			VoteUtil.getMongo().insert(Topic.C_NAME, item);
		} else {
			// logger.debug("数据有误:" + data);
			resp = resp.head(NtpHead.PARAMETER_ERROR);
		}
		return resp;
	}

	/**
	 * 对一个Topic进行投票
	 */
	@RequestMapping(value = "/vote/account/{account}/topic/{topicId}/legSeq/{legSeq}", method = RequestMethod.GET)
	@ResponseBody
	public NtpMessage vote(@PathVariable("account") String account,
			@PathVariable("topicId") String topicId, @PathVariable String legSeq) {
		// logger.debug(account + "\t" + topicId + "\t" + legSeq);
		// 存储在MongoDB中
		Vote vote = new Vote();
		vote.setVoter(account);
		vote.setLegSeq(legSeq);
		vote.setTopicId(topicId);
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
			String _id = (String)sm.get("_id");
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
	private NtpMessage findMcByChannelCode(String channelKey,
			String account) {
		NtpMessage sm = NtpMessage.ok();
		Object value = VoteUtil.getMC().getValue(channelKey);
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
		sm.set("channel", channelKey);
		return sm;
	}

	// ///////////////////////////////////////////////

	// /**
	// * 获得我关注Topic列表
	// *
	// * @deprecated 参见topicCode
	// */
	// @RequestMapping(value = "/focus/account/{account}", method =
	// RequestMethod.GET)
	// @ResponseBody
	// public SimpleMessage<Topic> topicFocus(
	// @PathVariable("account") String account) {
	// String channelKey = "c.account.focus." + account;
	// return findMcByChannelCode(channelKey, account);
	// }
	//
	// /**
	// * 获得某类型(频道)的Topic列表
	// *
	// * @deprecated 参见topicCode
	// */
	// @RequestMapping(value = "/topic/type/{type}/account/{account}", method =
	// RequestMethod.GET)
	// @ResponseBody
	// public SimpleMessage<Topic> topicType(@PathVariable("type") String type,
	// @PathVariable("account") String account) {
	// String channelKey = "c.type." + type;
	// return findMcByChannelCode(channelKey, account);
	// }
}
