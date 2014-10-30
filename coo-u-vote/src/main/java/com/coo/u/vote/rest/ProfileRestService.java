package com.coo.u.vote.rest;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.coo.s.vote.model.Account;
import com.coo.u.vote.VoteUtil;
import com.kingstar.ngbf.s.mongo.MongoItem;
import com.kingstar.ngbf.s.mongo.QueryAttrs;
import com.kingstar.ngbf.s.ntp.SimpleMessage;
import com.kingstar.ngbf.s.ntp.SimpleMessageHead;

/**
 * 创建一个账号,就创建一个Profile,Profile的属性会逐渐增加
 * 
 * @since 0.6.0.0
 */
@Controller
@RequestMapping("/")
public class ProfileRestService {
	private Logger logger = Logger.getLogger(ProfileRestService.class);

	/**
	 * 获得Profile的信息
	 * 
	 * @since 0.6.0.0
	 */
	@RequestMapping(value = "/profile/get/account/{account}", method = RequestMethod.GET)
	@ResponseBody
	public SimpleMessage<?> profileGet(@PathVariable("account") String account) {
		QueryAttrs query = QueryAttrs.blank().add("mobile", account);
		// 查询获得列表，因为，数据较简单，直接从Mongo数据库中获得
		MongoItem mi = VoteUtil.getMongo().findItemOne(Account.C_NAME, query);
		SimpleMessage<?> resp = SimpleMessage.ok();
		if (mi != null) {
			resp.set(mi.toMap());
		} else {
			resp = SimpleMessage.blank().head(SimpleMessageHead.NOT_FOUND);
		}
		// 设置请求Code
		resp.getHead().setApi_code("profile_get");
		return resp;
	}

	/**
	 * 逐条(属性)更新Profile的信息
	 * 
	 * @since 0.6.0.0
	 */
	@RequestMapping(value = "/profile/update/id/{id}/param/{param}/value/{value}/type/{type}", method = RequestMethod.GET)
	@ResponseBody
	public SimpleMessage<?> profileUpdate(@PathVariable("id") String id,
			@PathVariable("param") String param,
			@PathVariable("value") String value,
			@PathVariable("type") int dataType) {
		logger.debug("topicId=" + id + "\tparam=" + param + "\tvalue=" + value);
		Map<String, Object> item = new HashMap<String, Object>();
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
		VoteUtil.getMongo().update(Account.C_NAME, id, item);
		SimpleMessage<?> resp = SimpleMessage.ok();
		resp.getHead().setApi_code("profile_update");
		return resp;
	}
}
