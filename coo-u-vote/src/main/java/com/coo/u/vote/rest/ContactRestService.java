package com.coo.u.vote.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.coo.s.vote.model.Contact;
import com.coo.u.vote.ModelManager;
import com.coo.u.vote.VoteUtil;
import com.kingstar.ngbf.s.mongo.MongoItem;
import com.kingstar.ngbf.s.mongo.QueryAttrs;
import com.kingstar.ngbf.s.ntp.SimpleMessage;
import com.kingstar.ngbf.s.ntp.SimpleMessageHead;

/**
 * 接受M端的同步账号信息，进行本地MongoDB的数据同步 对应M端的MContact Account的所有通讯录信息
 * 
 * @deprecated 参见LiteRestService
 */
@Controller
@RequestMapping("/")
class ContactRestService {

	/**
	 * 本地Contact信息的第一次初始化,即从M端Post过来列表MContact信息 SimpleMessage<?>
	 * add(Map<String,Object>)
	 */
	@RequestMapping(value = "/contact/sync", method = RequestMethod.POST)
	@ResponseBody
	public SimpleMessage<Contact> contactSync(@RequestBody String data) {
		SimpleMessage<Contact> resp = new SimpleMessage<Contact>(
				SimpleMessageHead.OK);

		// 获得Android客户端传来的data(Topic)的数据
		SimpleMessage<?> smData = SimpleMessage.bind(data);

		if (smData != null) {
			String host = smData.getData("host");
			// 查找到所有相關的Contact信息,和M端传来的信息进行同步
			QueryAttrs query = QueryAttrs.blank().add("host", host);
			List<MongoItem> list = VoteUtil.getMongo().findItems(
					Contact.C_NAME, query);
			// 存成Map便於匹配
			Map<String, MongoItem> contactMap = new HashMap<String, MongoItem>();
			for (MongoItem mi : list) {
				String s_host = (String) mi.get("host");
				String s_mobile = (String) mi.get("mobile");
				String key = s_host + "." + s_mobile;
				contactMap.put(key, mi);
			}

			// 获得M端的信息,参见MockClient.contactSync()
			List<Map<String, Object>> items = smData.getItems();
			for (Map<String, Object> item : items) {
				String m_host = (String) item.get("host");
				String m_mobile = (String) item.get("mobile");
				String m_name = (String) item.get("name");
				String key = m_host + "." + m_mobile;
				MongoItem mi = contactMap.get(key);
				if (mi != null) {
					// 表示服务器端已经有此数据，需要返回该信息到M端供M端同步
					Contact record = new Contact();
					ModelManager.merge(mi, record);
					resp.addRecord(record);
				} else {
					// 服务器没有此数据,需要进行存储
					Contact record = new Contact(m_mobile, m_name);
					record.setHost(m_host);
					VoteUtil.getMongo().insert(Contact.C_NAME,
							ModelManager.toMap(record));
				}
			}
		} else {
			resp.head(SimpleMessageHead.PARAMETER_ERROR);
		}
		System.out.println(resp.toJson());
		return resp;
	}

}
