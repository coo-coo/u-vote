package com.coo.u.vote;

import java.util.List;
import java.util.Map;

import com.coo.s.vote.model.Account;
import com.coo.s.vote.model.Feedback;
import com.coo.s.vote.model.SChannel;
import com.coo.s.vote.model.SContact;
import com.coo.s.vote.model.Topic;
import com.coo.s.vote.model.TopicLeg;
import com.google.gson.Gson;
import com.kingstar.ngbf.s.mongo.MongoItem;
import com.kingstar.ngbf.s.mongo.QueryAttrs;
import com.kingstar.ngbf.s.ntp.NtpHead;
import com.kingstar.ngbf.s.ntp.NtpHelper;
import com.kingstar.ngbf.s.ntp.NtpMessage;

/**
 * @description
 * @author boqing.shen
 * @date 2014-9-28 上午9:30:04
 * @since 1.0.0.0
 */
@SuppressWarnings("unused")
public class MockClient {

	public static void main(String[] args) {
		// MockClient.createTopic("Q2", 4);
		// MockClient.voteTopic("5427683dcce5d3dc82564e00", "3");
		// MockClient.mcontactSync();
		// MockClient.profileUpdate();
		// MockClient.createFeedback("中文....");
		// MockClient.accountFocusTopic(ACCOUNT, "5427683dcce5d3dc82564e00");

		// MockClient.findAccounts();
		// MockClient.updateAccountParam();
		// MockClient.findTopics();
		// MockClient.createTopic2("topic3",3);
		MockClient.mcontactSync();
		// MockClient.mchannelFind();
	}

	public static void mchannelFind() {
		String host = "13917081673";
		// 查找到所有相關的MContact信息,和M端传来的信息进行同步
		QueryAttrs query = QueryAttrs.blank().add("host", host);
		List<MongoItem> list = VoteUtil.findItems(SContact.C_NAME, query);
		System.out.println(list.size());
		// for (MongoItem mi : list) {
		//
		// }
	}

	/**
	 * 本地Contact同步 M端将设备的通讯薄信息同步到SQLite之后,通过此方法实现和服务器端的同步
	 */
	public static void mcontactSync() {
		// M端基础数据，即所有SQLite的MContact部分信息
		NtpMessage data = new NtpMessage();
		// 设置请求者
		data.set("host", ACCOUNT);
		for (int i = 0; i < 8; i++) {
			// 参见MContact
			SChannel item = new SChannel();
			item.setHost(ACCOUNT);
			item.setCode("code-" + i);
			item.setLabel("频道241-" + i);
			item.setId(""+i);
			data.add(item);
		}

		 String uri = SERVERHOST + "/mchannel/sync";
		 NtpMessage resp = HttpUtils2.doPostNtp(uri, data.toJson());
		 System.out.println("isRespOK:" + isRespOK(resp));

		// System.out.println(json);
		//
//		String json = data.toJson();
//		System.out.println(json);
//		NtpMessage nm2 = NtpMessage.bind(json);
//		System.out.println(nm2.toJson());
//		List<SChannel> items = nm2.getItems(SChannel.class);
//		for (SChannel item : items) {
//			
//			System.out.println(item.getId());
//			Map<String, Object> itemMap = ModelManager.toMap(item);
////			Integer id = (Integer)itemMap.get("id");
////			System.out.println(itemMap);
//		}
	}

	public static void createTopic2(String title, int legCount) {
		NtpMessage nm = new NtpMessage();
		nm.set("title", title);
		nm.set("owner", "13917081673");
		// nm.set("expired", 0l);
		// nm.set("status", Topic.STATUS_VALID);
		// nm.set("vote", 0);
		// nm.set("snapshot", System.currentTimeMillis());
		for (int i = 0; i < legCount; i++) {
			TopicLeg leg = new TopicLeg("" + i, "leg-" + i);
			nm.add(leg);
		}
		// // 提示信息... toJson有问题?
		String json = nm.toJson();
		System.out.println(json);

		// NtpMessage nm2 = NtpMessage.bind(json);
		// System.out.println(nm2.toJson());
		//
		// Map<String, Object> item = new HashMap<String, Object>();
		// item.putAll(nm2.getData());
		// item.put("expired", 0l);
		// item.put("status", Topic.STATUS_VALID);
		// item.put("vote", 0);
		// item.put("snapshot", System.currentTimeMillis());
		//
		// List<Map<String, Object>> legsMap = new ArrayList<Map<String,
		// Object>>();
		// List<TopicLeg> legs = nm2.getItems(TopicLeg.class);
		// // System.out.println(legs.size());
		// for (TopicLeg leg : legs) {
		// Map<String, Object> lm = new HashMap<String, Object>();
		// lm.put("leg_seq", leg.getSeq());
		// lm.put("leg_vote", leg.getVote());
		// lm.put("leg_title", leg.getTitle());
		// legsMap.add(lm);
		// }
		// item.put("legs", legsMap);
		//
		// VoteUtil.getMongo().insert(Topic.C_NAME, item);

		// toast(json);
		String uri = SERVERHOST + "/topic/create";
		NtpMessage resp = HttpUtils2.doPostNtp(uri, json);
		System.out.println("isRespOK:" + isRespOK(resp));
	}

	/**
	 * 创建话题
	 */
	public static void createTopic(String title, int legCount) {
		Topic topic = new Topic(title, ACCOUNT);
		for (int i = 0; i < legCount; i++) {
			TopicLeg leg = new TopicLeg("" + i, "leg-" + i);
			topic.add(leg);
		}
		// // 提示信息... toJson有问题?
		String json = NtpHelper.toJson(topic);

		// toast(json);
		// String uri = SERVERHOST + "/topic/create";
		// NtpMessage resp = HttpUtils2.doPostNtp(uri, json);
		// System.out.println("isRespOK:" + isRespOK(resp));
	}

	public static void findTopics() {
		// String uri = SERVERHOST + "/topic/list/mine?op=13917081673";
		String uri = SERVERHOST + "/topic/list/code/channel_top?op=13917081673";
		NtpMessage resp2 = HttpUtils2.doGetNtp(uri);
		System.out.println(resp2.toJson());
		List<Topic> list = resp2.getItems(Topic.class);
		for (Topic topic : list) {
			System.out.println(topic.getTitle() + "\t" + topic.getVote());
			List<TopicLeg> legs = topic.getLegs();
			// System.out.println(legs.size());
		}

	}

	public static void updateAccountParam() {
		NtpMessage nm = new NtpMessage();
		nm.set("_id", "541155452170e0df13091431");
		nm.set("key", "mail");
		nm.set("value", "sbq@163.com");
		// 发送更新请求...
		String uri = SERVERHOST + "/account/update/param";
		NtpMessage resp2 = HttpUtils2.doPostNtp(uri, nm.toJson());
		System.out.println(resp2.toJson());

		// 修改信息，參見topicUpdate
		// httpCaller.doPost(Constants.BIZ_ACCOUNT_UPDATE_PARAM,
		// Constants.rest(uri), nm);
	}

	public static void findAccounts() {
		// QueryAttrs query = QueryAttrs.blank().desc("_tsu");
		// // 查询获得列表，因为，数据较简单，直接从Mongo数据库中获得
		// List<MongoItem> items =
		// VoteUtil.getMockMongo().findItems(Account.C_NAME, query);
		// NtpMessage resp = NtpMessage.ok();
		// for (MongoItem mi : items) {
		// // TODO 其它属性? 待处理....
		// Account fb = new Account();
		// // Merge对象 先转换成对象，再传递
		// // 不能用MongoItem.toMap()到M端,可能会产生Integer到Double的默认转换(GSON的问题)
		// ModelManager.merge(mi, fb);
		// resp.add(fb);
		// }
		//
		// System.out.println(resp.toJson());

		String uri = SERVERHOST + "/account/list/all";
		NtpMessage resp2 = HttpUtils2.doGetNtp(uri);
		// System.out.println(resp2.toJson());
		// NtpMessage resp2 = NtpMessage.bind(resp.toJson());

		// Gson gson = new Gson();
		// String s =
		// "{_id=5457457404e3fa502330bc58, _tsi=1.415005556581E12, _tsu=0.0, owner=1, ownerId=1, updater=1, status=0.0, attrs={}, selected=false, mobile=13816965673, password=qqqqqq, type=0, account=13816965673}";
		// Account a = gson.fromJson(s, Account.class);
		// System.out.println(a.get_id() + "\t" + a.get_tsi() + "\t"
		// + a.getStatus());

		for (Object obj : resp2.getItems()) {
			String json = obj.toString();
			System.out.println(json);
			// Account item = gson.fromJson(obj.toString(), Account.class);
			// System.out.println(item.get_id() + "\t" + item.get_tsi() + "\t"
			// + item.getStatus());
		}

		List<Account> list = resp2.getItems(Account.class);
		// System.out.println(list.size());
		// for (Account account : list) {
		// // System.out.println(account.getMobile());
		// }

	}

	public static void findFeedback() {
		NtpMessage sm = new NtpMessage(NtpHead.OK);
		QueryAttrs query = QueryAttrs.blank().desc("_tsi");
		// 查询获得列表，因为，数据较简单，直接从Mongo数据库中获得
		List<MongoItem> items = VoteUtil.getMockMongo().findItems(
				Feedback.C_NAME, query);
		for (MongoItem mi : items) {
			Feedback fb = new Feedback();
			// 不能用MongoItem.toMap()到M端,可能会产生Integer到Double的默认转换(GSON的问题)
			// 先转换成对象，再传递
			ModelManager.merge(mi, fb);
			sm.add(fb);
		}
		String json = sm.toJson();
		System.out.println(json);
		NtpMessage resp = NtpMessage.bind(json);
		Gson gson = new Gson();
		for (Object obj : resp.getItems()) {
			Feedback item = gson.fromJson(obj.toString(), Feedback.class);
			System.out.println(item.get_id() + "\t" + item.get_tsi() + "\t"
					+ item.getStatus());
		}
	}

	/**
	 * 创建话题
	 */
	public static void createFeedback(String note) {
		NtpMessage sm = new NtpMessage();
		sm.set("owner", "13917081673");
		sm.set("owner_id", "mongoid...");
		sm.set("note", note);
		sm.set("app_version", "1.0");
		String uri = SERVERHOST + "/feedback/create/";
		String json = sm.toJson();
		System.out.println(json);
		NtpMessage resp = HttpUtils2.doPostNtp(uri, json);
		System.out.println("isRespOK:" + isRespOK(resp));
	}

	public static void profileUpdate() {
		// M端基础数据，即所有SQLite的MContact部分信息
		NtpMessage data = new NtpMessage();
		// 设置请求者
		data.set("_id", "541155452170e0df13091431");
		// data.set("key", "p_nickname");
		// data.set("value","中国娃");
		data.set("key", "p_gender");
		data.set("value", "不详");
		String uri = SERVERHOST + "/profile/update/param";
		String json = data.toJson();
		System.out.println(json);
		NtpMessage resp = HttpUtils2.doPostNtp(uri, json);
		System.out.println("isRespOK:" + isRespOK(resp));
	}

	public static String TOPICID = "5427683dcce5d3dc82564e00";
	private static String ACCOUNT = "13917081673";
	private static String SERVERHOST = "http://10.253.45.103:8080/vote/rest";

	public static void accountFocusTopic(String account, String topicId) {
		String uri = SERVERHOST + "/focus/topic/" + account + "/" + topicId;
		NtpMessage resp = HttpUtils2.doGetNtp(uri);
		System.out.println("isRespOK:" + isRespOK(resp));
	}

	/**
	 * 投票
	 */
	public static void voteTopic(String toipicId, String legSeq) {
		String uri = SERVERHOST + "/topic/vote/account/" + ACCOUNT + "/topic/"
				+ toipicId + "/legSeq/" + legSeq;
		NtpMessage resp = HttpUtils2.doGetNtp(uri);
		System.out.println("isRespOK:" + isRespOK(resp));
	}

	private static boolean isRespOK(NtpMessage resp) {
		boolean tof = resp.getHead().getRep_code().equals(NtpHead.REP_OK);
		return tof;
	}

}
