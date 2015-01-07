package com.coo.u.vote.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.coo.s.cloud.rest.GenericRest;
import com.coo.s.vote.model.Feedback;
import com.coo.u.vote.ModelManager;
import com.coo.u.vote.VoteManager;
import com.kingstar.ngbf.s.mongo.MongoItem;
import com.kingstar.ngbf.s.mongo.QueryAttrs;
import com.kingstar.ngbf.s.ntp.NtpHead;
import com.kingstar.ngbf.s.ntp.NtpMessage;

/**
 * @description
 * @author boqing.shen
 * @date 2014-6-6 下午4:32:10
 * @since 0.1.5.0
 */
@Controller
@RequestMapping("/feedback")
public class FeedbackRest extends GenericRest {

	private static Logger logger = Logger.getLogger(FeedbackRest.class);

	/**
	 * 创建/存储一个Feedback
	 * 
	 * @since 0.1.5.0
	 */
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	@ResponseBody
	public NtpMessage create(@RequestBody String data) {
		NtpMessage resp = NtpMessage.ok();
		// 获得Android客户端传来的data(Feedback)的数据
		logger.debug(data);
		NtpMessage sm = NtpMessage.bind(data);
		if (sm != null) {
			// 直接Map对象传递到数据库中
			sm.set("status", Feedback.STATUS_UNSOLVED);
			VoteManager.getMongo().insert(Feedback.C_NAME, sm.getData());
		} else {
			resp = resp.head(NtpHead.PARAMETER_ERROR);
		}
		return resp;
	}

	/**
	 * 获取最新的Feedback(条目数)
	 * 
	 * @since 0.1.5.0
	 */
	@RequestMapping(value = "/latest/{latest}", method = RequestMethod.GET)
	@ResponseBody
	public NtpMessage latest(@PathVariable("latest") int latest) {
		NtpMessage sm = new NtpMessage(NtpHead.OK);
		// 获取所有的Feedback
		QueryAttrs query = QueryAttrs.blank().desc("_tsi");
		if (latest != -1) {
			// 如果是-1,则查询全部状态为0的反馈.处理之后，0->1
			query.limit(latest);
		}
		// 查询获得列表，因为，数据较简单，直接从Mongo数据库中获得
		List<MongoItem> items = VoteManager.getMongo().findItems(Feedback.C_NAME,
				query);
		for (MongoItem mi : items) {
			Feedback fb = new Feedback();
			// Merge对象 先转换成对象，再传递
			// 不能用MongoItem.toMap()到M端,可能会产生Integer到Double的默认转换(GSON的问题)
			ModelManager.merge(mi, fb);
			sm.add(fb);
		}
		return sm;
	}

	/**
	 * 状态变更 ?op=130xxxxxxxx
	 */
	@RequestMapping(value = "/update/_id/{_id}/status/{status}", method = RequestMethod.GET)
	@ResponseBody
	public NtpMessage updateStatus(HttpServletRequest req,
			@PathVariable("_id") String _id, @PathVariable("status") int status) {
		logger.debug("update id=" + _id + "\tstatus=" + status);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("status", status);
		map.put("updater", this.getOperator(req));
		VoteManager.getMongo().update(Feedback.C_NAME, _id, map);
		return NtpMessage.ok();
	}
}
