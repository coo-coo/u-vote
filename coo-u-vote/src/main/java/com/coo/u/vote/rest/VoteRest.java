package com.coo.u.vote.rest;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.coo.s.cloud.rest.GenericCloudRest;
import com.coo.s.vote.model.Channel;
import com.coo.u.vote.ModelManager;
import com.kingstar.ngbf.s.ntp.NtpMessage;

@Controller
@RequestMapping("/")
public class VoteRest extends GenericCloudRest{

	protected static Logger logger = Logger.getLogger(VoteRest.class);

	/**
	 * 获得所有的Channel静态对象,供M端的频道管理
	 */
	@RequestMapping(value = "/channel/all", method = RequestMethod.GET)
	@ResponseBody
	public NtpMessage channelAll(HttpServletRequest req) {
		NtpMessage sm = NtpMessage.ok();
		// 从ModelFactory处获得目前支持的Chanenl(Type),供M端同步比较
		List<Channel> channels = ModelManager.TYPE_CHANNNELS;
		for (Channel channel : channels) {
			sm.add(channel);
		}
		return sm;
	}
}
