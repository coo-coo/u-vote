package com.coo.u.vote.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.coo.s.vote.model.m.LiteChannel;
import com.coo.s.vote.model.m.LiteContact;
import com.coo.s.vote.model.m.LiteGroup;
import com.coo.u.vote.VoteUtil;
import com.coo.u.vote.rest.helper.RestHelper;
import com.kingstar.ngbf.s.mongo.MongoItem;
import com.kingstar.ngbf.s.mongo.QueryAttrs;
import com.kingstar.ngbf.s.ntp.SimpleMessage;

/**
 * 同步Mobile端的MChannel、MContact、MGroup等信息
 * 
 * @author boqing.shen
 * 
 */
@Controller
@RequestMapping("/")
public class LiteRestService {

	/**
	 * 同步M端的MContact信息,即从M端Post过来列表MContact信息 SimpleMessage<?>
	 * add(Map<String,Object>)
	 */
	@RequestMapping(value = "/mcontact/sync", method = RequestMethod.POST)
	@ResponseBody
	public SimpleMessage<?> mcontactSync(@RequestBody String data) {
		// 获得Android客户端传来的data(SimpleMessage<?>.add(Map<String,Object>)的数据
		SimpleMessage<?> smData = SimpleMessage.bind(data);
		if (smData == null) {
			return RestHelper.error("请求数据有误");
		}
		// 同步到LiteChannel存储中
		sync(LiteContact.C_NAME, smData, "mobile");
		return SimpleMessage.ok();
	}

	/**
	 * M端可能新装或者是删除又重新安装之后，需要向服务端获取(曾经同步过的)MContact信息进行M端本地存储
	 * 
	 */
	@RequestMapping(value = "/mcontact/all/host/{host}", method = RequestMethod.GET)
	@ResponseBody
	public SimpleMessage<?> mcontactAll(@PathVariable("host") String host) {
		SimpleMessage<?> resp = SimpleMessage.ok();
		merge(resp, LiteContact.C_NAME, host);
		return resp;
	}

	/**
	 * M端可能新装或者是删除又重新安装之后，需要向服务端获取(曾经同步过的)MContact信息进行M端本地存储
	 * 
	 */
	@RequestMapping(value = "/mchannel/all/host/{host}", method = RequestMethod.GET)
	@ResponseBody
	public SimpleMessage<?> mchannelAll(@PathVariable("host") String host) {
		SimpleMessage<?> resp = SimpleMessage.ok();
		merge(resp, LiteChannel.C_NAME, host);
		return resp;
	}

	/**
	 * M端可能新装或者是删除又重新安装之后，需要向服务端获取(曾经同步过的)MContact信息进行M端本地存储
	 * 
	 */
	@RequestMapping(value = "/mgroup/all/host/{host}", method = RequestMethod.GET)
	@ResponseBody
	public SimpleMessage<?> mgroupAll(@PathVariable("host") String host) {
		SimpleMessage<?> resp = SimpleMessage.ok();
		merge(resp, LiteGroup.C_NAME, host);
		return resp;
	}

	/**
	 * 同步M端的MChannel信息,即从M端Post过来列表MContact信息 SimpleMessage<?>
	 * add(Map<String,Object>)
	 */
	@RequestMapping(value = "/mchannel/sync", method = RequestMethod.POST)
	@ResponseBody
	public SimpleMessage<?> mchannelSync(@RequestBody String data) {
		// 获得Android客户端传来的data(SimpleMessage<?>.add(Map<String,Object>)的数据
		SimpleMessage<?> smData = SimpleMessage.bind(data);
		if (smData == null) {
			return RestHelper.error("请求数据有误");
		}
		// 同步到LiteChannel存储中
		sync(LiteChannel.C_NAME, smData, "code");
		return SimpleMessage.ok();
	}

	/**
	 * 同步M端的MChannel信息,即从M端Post过来列表MContact信息 SimpleMessage<?>
	 * add(Map<String,Object>)
	 */
	@RequestMapping(value = "/mgroup/sync", method = RequestMethod.POST)
	@ResponseBody
	public SimpleMessage<?> mgroupSync(@RequestBody String data) {
		// 获得Android客户端传来的data(SimpleMessage<?>.add(Map<String,Object>)的数据
		SimpleMessage<?> smData = SimpleMessage.bind(data);
		if (smData == null) {
			return RestHelper.error("请求数据有误");
		}
		// 同步到LiteChannel存储中
		sync(LiteGroup.C_NAME, smData, "name");
		return SimpleMessage.ok();
	}

	// //////////////////////////////////////////////////////////////

	private void merge(SimpleMessage<?> resp, String collectionName, String host) {
		// 查找到所有相關的MContact信息,和M端传来的信息进行同步
		QueryAttrs query = QueryAttrs.blank().add("host", host);
		List<MongoItem> list = VoteUtil.findItems(collectionName, query);
		for (MongoItem mi : list) {
			resp.add(mi.toMap());
		}
	}

	/**
	 * 执行Mobile端传过来的SimpleMessage信息，同步到指定collection(Mongo)中
	 */
	private void sync(String collectionName, SimpleMessage<?> smData,
			String itemKeyName) {
		// 先获得M端传过来的Host
		String host = smData.getData("host");
		// 查找到所有相關的MContact信息,和M端传来的信息进行同步
		QueryAttrs query = QueryAttrs.blank().add("host", host);
		List<MongoItem> list = VoteUtil.getMongo().findItems(collectionName,
				query);
		// 存成Map便於匹配
		Map<String, MongoItem> map = new HashMap<String, MongoItem>();
		for (MongoItem mi : list) {
			// M端ID仅作为一个字段,没有实际业务意义,采用Mobile作为Key
			String key = (String) mi.get(itemKeyName);
			map.put(key, mi);
		}

		// 获得M端的信息,参见MockClient.contactSync()
		List<Map<String, Object>> items = smData.getItems();
		for (Map<String, Object> item : items) {
			// 将Map对象Merge到mi中，然后进行更新
			String key = (String) item.get(itemKeyName);
			MongoItem mi = map.get(key);
			if (mi != null) {
				// 表示服务器端已经有此数据，需要进行MongoDB的更新操作
				String _id = mi.get_id();
				VoteUtil.getMongo().update(collectionName, _id, item);
			} else {
				// 表示服务器端已经有此数据，需要进行MongoDB的存储操作
				VoteUtil.getMongo().insert(collectionName, item);
			}
		}
	}

}
