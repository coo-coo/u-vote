package com.coo.u.vote.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.coo.s.vote.model.MModel;
import com.coo.s.vote.model.SChannel;
import com.coo.u.vote.ModelManager;
import com.coo.u.vote.VoteUtil;
import com.kingstar.ngbf.s.mongo.MongoItem;
import com.kingstar.ngbf.s.mongo.QueryAttrs;
import com.kingstar.ngbf.s.ntp.NtpHead;
import com.kingstar.ngbf.s.ntp.NtpMessage;

/**
 * 同步Mobile端的MChannel、MContact、MGroup等信息
 * 
 * @author boqing.shen
 * 
 */
@Controller
@RequestMapping("/")
public class LiteRestService extends CommonRest {

	// @SuppressWarnings("unused")
	 private Logger logger = Logger.getLogger(LiteRestService.class);

	/**
	 * 同步M端的MContact信息,即从M端Post过来列表MContact信息 NtpMessage add(Map<String,Object>)
	 * 参见M端:MContactRemoteSyncTask
	 */
	@RequestMapping(value = "/mcontact/sync", method = RequestMethod.POST)
	@ResponseBody
	public NtpMessage mcontactSync(@RequestBody String data) {
		// 获得Android客户端传来的data(NtpMessage.add(Map<String,Object>)的数据
		NtpMessage sm = NtpMessage.bind(data);
		if (sm == null) {
			return error("请求数据有误");
		}
		// 同步到LiteChannel存储中
		sync(MModel.C_MCONTACT_NAME, sm, "mobile");
		return NtpMessage.ok();
	}

	/**
	 * M端可能新装或者是删除又重新安装之后，需要向服务端获取(曾经同步过的)MContact信息进行M端本地存储 url?op=139xxxxxxxx
	 */
	@RequestMapping(value = "/mcontact/all", method = RequestMethod.GET)
	@ResponseBody
	public NtpMessage mcontactAll(HttpServletRequest req) {
		String host = this.getOperator(req);
		NtpMessage resp = NtpMessage.ok();
		merge(resp, MModel.C_MCONTACT_NAME, host);
		return resp;
	}

	/**
	 * M端可能新装或者是删除又重新安装之后，需要向服务端获取(曾经同步过的)MContact信息进行M端本地存储、 url?op=139xxxxxxxx
	 */
	@RequestMapping(value = "/mchannel/all", method = RequestMethod.GET)
	@ResponseBody
	public NtpMessage mchannelAll(HttpServletRequest req) {
		String host = this.getOperator(req);
		NtpMessage resp = NtpMessage.ok();
		merge(resp, MModel.C_MCHANNEL_NAME, host);
		return resp;
	}

	/**
	 * M端可能新装或者是删除又重新安装之后，需要向服务端获取(曾经同步过的)MContact信息进行M端本地存储 url?op=139xxxxxxxx
	 */
	@RequestMapping(value = "/mgroup/all", method = RequestMethod.GET)
	@ResponseBody
	public NtpMessage mgroupAll(HttpServletRequest req) {
		String host = this.getOperator(req);
		NtpMessage resp = NtpMessage.ok();
		merge(resp, MModel.C_MGROUP_NAME, host);
		return resp;
	}

	/**
	 * 同步M端的MChannel信息,即从M端Post过来列表MContact信息 NtpMessage add(Map<String,Object>)
	 */
	@RequestMapping(value = "/mchannel/sync", method = RequestMethod.POST)
	@ResponseBody
	public NtpMessage mchannelSync(@RequestBody String data) {
		// 获得Android客户端传来的data(NtpMessage.add(Map<String,Object>)的数据
		try {
			NtpMessage nm = NtpMessage.bind(data);
			logger.debug(nm.toJson());
			// 同步到LiteChannel存储中
			syncChannel(SChannel.C_NAME, nm, "code");
			return NtpMessage.ok();
		} catch (Exception e) {
//			e.printStackTrace();
			logger.debug(e.getMessage());
			return error(e.getMessage());
		}
	}

	/**
	 * 执行Mobile端传过来的SimpleMessage信息，同步到指定collection(Mongo)中
	 */
	private void syncChannel(String collection, NtpMessage nm,
			String itemKeyName) {
		// 先获得M端传过来的Host
		String host = (String) nm.get("host");
		// 查找到所有相關的MContact信息,和M端传来的信息进行同步
		QueryAttrs query = QueryAttrs.blank().add("host", host);
		List<MongoItem> list = VoteUtil.findItems(collection, query);
		// 存成Map便於匹配
		Map<String, MongoItem> map = new HashMap<String, MongoItem>();
		for (MongoItem mi : list) {
			// M端ID仅作为一个字段,没有实际业务意义,采用Mobile作为Key
			String key = (String) mi.get(itemKeyName);
			map.put(key, mi);
		}

		// 获得M端的信息,参见MockClient.contactSync()
		
		// TODO
		List<SChannel> items = nm.getItems(SChannel.class);
		System.out.println("SChannel size==" + items.size());
		for (SChannel item : items) {
			// 将Map对象Merge到mi中，然后进行更新
			String key = item.getCode();
			MongoItem mi = map.get(key);
			// 转化为Map对象
			Map<String, Object> itemMap = ModelManager.toMap(item);
			System.out.println(itemMap);
			// itemMap.remove("_id");
			// itemMap.remove("_tsi");
			// itemMap.remove("_tsu");

			if (mi != null) {
				// 表示服务器端已经有此数据，需要进行MongoDB的更新操作
				String _id = mi.get_id();
				VoteUtil.getMongo().update(collection, _id, itemMap);
			} else {
				// 表示服务器端已经有此数据，需要进行MongoDB的存储操作
				VoteUtil.getMongo().insert(collection, itemMap);
			}
		}
	}

	/**
	 * 同步M端的MChannel信息,即从M端Post过来列表MContact信息 NtpMessage add(Map<String,Object>)
	 */
	@RequestMapping(value = "/mgroup/sync", method = RequestMethod.POST)
	@ResponseBody
	public NtpMessage mgroupSync(@RequestBody String data) {
		// 获得Android客户端传来的data(NtpMessage.add(Map<String,Object>)的数据
		NtpMessage smData = NtpMessage.bind(data);
		if (smData == null) {
			return error("请求数据有误");
		}
		// 同步到LiteChannel存储中
		sync(MModel.C_MGROUP_NAME, smData, "name");
		return NtpMessage.ok();
	}

	// //////////////////////////////////////////////////////////////

	private void merge(NtpMessage resp, String collection, String host) {
		// 查找到所有相關的MContact信息,和M端传来的信息进行同步
		QueryAttrs query = QueryAttrs.blank().add("host", host);
		List<MongoItem> list = VoteUtil.findItems(collection, query);
		for (MongoItem mi : list) {
			resp.add(mi.toMap());
		}
	}

	/**
	 * 执行Mobile端传过来的SimpleMessage信息，同步到指定collection(Mongo)中
	 */
	private void sync(String collection, NtpMessage nm, String itemKeyName) {
		// 先获得M端传过来的Host
		String host = (String) nm.get("host");
		// 查找到所有相關的MContact信息,和M端传来的信息进行同步
		QueryAttrs query = QueryAttrs.blank().add("host", host);
		List<MongoItem> list = VoteUtil.findItems(collection, query);
		// 存成Map便於匹配
		Map<String, MongoItem> map = new HashMap<String, MongoItem>();
		for (MongoItem mi : list) {
			// M端ID仅作为一个字段,没有实际业务意义,采用Mobile作为Key
			String key = (String) mi.get(itemKeyName);
			map.put(key, mi);
		}

		// 获得M端的信息,参见MockClient.contactSync()
		// TODO
		// List<Map<String, Object>> items = smData.getItems();
		List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> item : items) {
			// 将Map对象Merge到mi中，然后进行更新
			String key = (String) item.get(itemKeyName);
			MongoItem mi = map.get(key);
			if (mi != null) {
				// 表示服务器端已经有此数据，需要进行MongoDB的更新操作
				String _id = mi.get_id();
				VoteUtil.getMongo().update(collection, _id, item);
			} else {
				// 表示服务器端已经有此数据，需要进行MongoDB的存储操作
				VoteUtil.getMongo().insert(collection, item);
			}
		}
	}

	/**
	 * TODO STP更新
	 */
	public static NtpMessage error(String errorMsg) {
		return NtpMessage.blank()
				.head(NtpHead.SERVICE_ERROR.repMsg(errorMsg));
	}

}
