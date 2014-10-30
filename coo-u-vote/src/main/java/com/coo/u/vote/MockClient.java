package com.coo.u.vote;

import java.util.HashMap;
import java.util.Map;

import com.coo.s.vote.model.Topic;
import com.coo.s.vote.model.TopicLeg;
import com.kingstar.ngbf.s.ntp.NtpHelper;
import com.kingstar.ngbf.s.ntp.SimpleMessage;
import com.kingstar.ngbf.s.ntp.SimpleMessageHead;

/**
 * @description
 * @author boqing.shen
 * @date 2014-9-28 上午9:30:04
 * @since 1.0.0.0
 */

public class MockClient {

	public static void main(String[] args) {
		// MockClient.createTopic("Q2", 4);
		// MockClient.voteTopic("5427683dcce5d3dc82564e00", "3");
		// MockClient.contactSync();
		// MockClient.accountFocusTopic(ACCOUNT, "5427683dcce5d3dc82564e00");

		// Focus focus = new Focus();
		// focus.setAccount(ACCOUNT);
		// focus.setSubject(TOPICID);
		// focus.setType(Focus.TYPE_ACCOUNT);
		// // 转化为Map对象
		// Map<String, Object> item = ModelManager.toMap(focus);
		// System.out.println(item);

		// QueryAttrs query = QueryAttrs.blank().and("account", "13917081673");
		// MongoItem mi = VoteUtil.getMockMongo().findItemOne(Focus.C_NAME,
		// query);

	}

	public static String TOPICID = "5427683dcce5d3dc82564e00";
	private static String ACCOUNT = "13917081673";
	private static String SERVERHOST = "http://10.253.45.103:8080/vote/rest";

	public static void accountFocusTopic(String account, String topicId) {
		String uri = SERVERHOST + "/focus/topic/" + account + "/" + topicId;
		SimpleMessage<?> resp = HttpUtils2.doGet(uri, null);
		System.out.println("isRespOK:" + isRespOK(resp));
	}

	/**
	 * 本地Contact同步 M端将设备的通讯薄信息同步到SQLite之后,通过此方法实现和服务器端的同步
	 */
	public static void contactSync() {
		// M端基础数据，即所有SQLite的MContact部分信息
		SimpleMessage<?> data = new SimpleMessage<Object>();
		// 设置请求者
		data.set("host", ACCOUNT);
		for (int i = 0; i < 5; i++) {
			// 参见MContact
			Map<String, Object> item = new HashMap<String, Object>();
			item.put("mobile", "mobile-" + i);
			item.put("name", "name-" + i);
			item.put("host", ACCOUNT);
			data.add(item);
		}
		String uri = SERVERHOST + "/contact/sync/";
		String json = data.toJson();
		SimpleMessage<?> resp = HttpUtils2.doPost(uri, json);
		System.out.println("isRespOK:" + isRespOK(resp));
	}

	/**
	 * 投票
	 */
	public static void voteTopic(String toipicId, String legSeq) {
		String uri = SERVERHOST + "/topic/vote/account/" + ACCOUNT + "/topic/"
				+ toipicId + "/legSeq/" + legSeq;
		SimpleMessage<?> resp = HttpUtils2.doGet(uri, null);
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
		String uri = SERVERHOST + "/topic/create";
		SimpleMessage<?> resp = HttpUtils2.doPost(uri, json);
		System.out.println("isRespOK:" + isRespOK(resp));
	}

	private static boolean isRespOK(SimpleMessage<?> resp) {
		boolean tof = resp.getHead().getRep_code()
				.equals(SimpleMessageHead.REP_OK);
		return tof;
	}

}
