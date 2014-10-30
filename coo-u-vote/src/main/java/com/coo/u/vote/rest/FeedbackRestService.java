package com.coo.u.vote.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.coo.s.vote.model.Feedback;
import com.coo.u.vote.ModelManager;
import com.coo.u.vote.VoteUtil;
import com.kingstar.ngbf.s.mongo.MongoItem;
import com.kingstar.ngbf.s.mongo.QueryAttrs;
import com.kingstar.ngbf.s.ntp.SimpleMessage;
import com.kingstar.ngbf.s.ntp.SimpleMessageHead;

/**
 * @description
 * @author boqing.shen
 * @date 2014-6-6 下午4:32:10
 * @since 0.1.5.0
 */
@Controller
@RequestMapping("/")
public class FeedbackRestService {

	private static Logger logger = Logger.getLogger(FeedbackRestService.class);

	/**
	 * 创建/存储一个Feedback
	 * 
	 * @since 0.1.5.0
	 */
	@RequestMapping(value = "/feedback/create", method = RequestMethod.POST)
	@ResponseBody
	public SimpleMessage<?> feedbackCreate(@RequestBody String data) {
		SimpleMessage<?> resp = null;
		// 获得Android客户端传来的data(Topic)的数据
		logger.debug(data);
		SimpleMessage<?> sm = SimpleMessage.bind(data);
		if (sm != null) {
			// 直接Map对象传递到数据库中
			sm.set("status", Feedback.STATUS_UNSOLVED.code);
			VoteUtil.getMongo().insert(Feedback.C_NAME, sm.getData());
			resp = SimpleMessage.ok();
		} else {
			logger.debug("数据有误:" + data);
			resp = SimpleMessage.blank()
					.head(SimpleMessageHead.PARAMETER_ERROR);
		}
		return resp;
	}

	/**
	 * 获取最新的Feedback(条目数)
	 * 
	 * @since 0.1.5.0
	 */
	@RequestMapping(value = "/feedback/latest/{latest}", method = RequestMethod.GET)
	@ResponseBody
	public SimpleMessage<Feedback> feedbackLatest(
			@PathVariable("latest") int latest) {
		SimpleMessage<Feedback> sm = new SimpleMessage<Feedback>(
				SimpleMessageHead.OK);
		// 获取所有的Feedback
		QueryAttrs query = QueryAttrs.blank().desc("_tsi");
		if (latest != -1) {
			// 如果是-1,则查询全部状态为0的反馈.处理之后，0->1
			query.limit(latest);
		}
		// 查询获得列表，因为，数据较简单，直接从Mongo数据库中获得
		List<MongoItem> items = VoteUtil.getMongo().findItems(Feedback.C_NAME,
				query);
		for (MongoItem mi : items) {
			Feedback fb = new Feedback();
			// Merge对象
			ModelManager.merge(mi, fb);
			sm.addRecord(fb);
		}
		return sm;
	}

	/**
	 * 状态变更处理 0->1
	 */
	@RequestMapping(value = "/feedback/update/id/{id}/updater/{updater}", method = RequestMethod.GET)
	@ResponseBody
	public SimpleMessage<?> feedbackUpdate(@PathVariable("id") String id,
			@PathVariable("updater") String updater) {
		logger.debug("feedbackUpdate id=" + id);
		Map<String, Object> item = new HashMap<String, Object>();
		item.put("status", Feedback.STATUS_SOLVED.code);
		item.put("updater", updater);
		VoteUtil.getMongo().update(Feedback.C_NAME, id, item);
		return SimpleMessage.ok();
	}
}
