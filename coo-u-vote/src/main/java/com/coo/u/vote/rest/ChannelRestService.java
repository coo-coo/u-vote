package com.coo.u.vote.rest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @description
 * @author liang.ma
 * @date 2014-6-6 下午4:32:10
 * @since 0.1.0.0
 * @deprecated
 */
@Controller
@RequestMapping("/")
public class ChannelRestService {

	protected static Logger logger = Logger.getLogger(ChannelRestService.class);

	// /**
	// * 获得所有的Channel静态对象,供M端的频道管理
	// */
	// @RequestMapping(value = "/channel/all", method = RequestMethod.GET)
	// @ResponseBody
	// public SimpleMessage<Channel> channelAll() {
	// SimpleMessage<Channel> sm = new SimpleMessage<Channel>(
	// SimpleMessageHead.OK);
	// // 从ModelFactory处获得目前支持的Chanenl(Type),供M端同步比较
	// List<Channel> channels = ModelManager.getTypeChannels();
	// for (Channel channel : channels) {
	// sm.addRecord(channel);
	// }
	// return sm;
	// }

	// /**
	// * 获得我关注的所有的Channel对象 TODO 获得所有的Channel，再和Focus进行匹配，获得我关注的Channnel
	// * @deprecated 在M端进行实现
	// */
	// @RequestMapping(value = "/channel/focus/account/{account}", method =
	// RequestMethod.GET)
	// @ResponseBody
	// public SimpleMessage<Channel> channelFocus(
	// @PathVariable("account") String account) {
	// SimpleMessage<Channel> sm = new SimpleMessage<Channel>(
	// SimpleMessageHead.OK);
	// // 从ModelFactory处获得目前支持的Chanenl(Type),供M端同步比较
	// List<Channel> channels = ModelManager.getTypeChannels();
	// for (Channel channel : channels) {
	// sm.addRecord(channel);
	// }
	// return sm;
	// }

}
