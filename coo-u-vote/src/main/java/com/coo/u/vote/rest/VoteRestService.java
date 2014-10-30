package com.coo.u.vote.rest;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.coo.s.vote.model.Channel;
import com.coo.u.vote.ModelManager;
import com.kingstar.ngbf.s.ntp.SimpleMessage;
import com.kingstar.ngbf.s.ntp.SimpleMessageHead;

@Controller
@RequestMapping("/")
public class VoteRestService {

	protected static Logger logger = Logger.getLogger(VoteRestService.class);

	/**
	 * 获得所有的Channel静态对象,供M端的频道管理
	 */
	@RequestMapping(value = "/channel/all", method = RequestMethod.GET)
	@ResponseBody
	public SimpleMessage<Channel> channelAll() {
		SimpleMessage<Channel> sm = new SimpleMessage<Channel>(
				SimpleMessageHead.OK);
		// 从ModelFactory处获得目前支持的Chanenl(Type),供M端同步比较
		List<Channel> channels = ModelManager.getTypeChannels();
		for (Channel channel : channels) {
			sm.addRecord(channel);
		}
		return sm;
	}
}
