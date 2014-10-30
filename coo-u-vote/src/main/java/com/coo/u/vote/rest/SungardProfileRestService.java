package com.coo.u.vote.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kingstar.ngbf.s.mongo.INgbfMongoClient;
import com.kingstar.ngbf.s.mongo.MongoItem;
import com.kingstar.ngbf.s.mongo.NgbfMongoClient;
import com.kingstar.ngbf.s.mongo.NgbfMongoConfiguration;
import com.kingstar.ngbf.s.mongo.QueryAttrs;
import com.kingstar.ngbf.s.ntp.SimpleMessage;

/**
 * 获得Sungard的Profile信息,用于Icon显示等
 * 
 * @description
 * @author boqing.shen
 * @date 2014-9-12 上午9:37:45
 * @since 1.0.0.0
 */
@Controller
@RequestMapping("/")
public class SungardProfileRestService {

	/**
	 * 记录所有的数据集合
	 */
	private List<MongoItem> profiles = new ArrayList<MongoItem>();
	/**
	 * 简单的随机挑选
	 */
	private Random random = new Random();

//	@PostConstruct
	public void init() {
		// 初始化，从ngbf_cprofile_profile读取Sungard的Profile信息
		NgbfMongoConfiguration config = NgbfMongoConfiguration.NGBF;
		INgbfMongoClient client = new NgbfMongoClient(config);
		QueryAttrs query = QueryAttrs.blank().desc("_tsi");
		profiles = client.findItems("ngbf_cprofile_profile", query);
	}

	@RequestMapping(value = "/sungard/profile/random", method = RequestMethod.GET)
	@ResponseBody
	public SimpleMessage<?> profileRandom() {
		int index = random.nextInt(profiles.size());
		MongoItem mi = profiles.get(index);
		return SimpleMessage.ok().set(toMap(mi));
	}

	@RequestMapping(value = "/sungard/profiles", method = RequestMethod.GET)
	@ResponseBody
	public SimpleMessage<?> profiles() {
		SimpleMessage<?> sm = SimpleMessage.ok();
		for (MongoItem mi : profiles) {
			sm.add(toMap(mi));
		}
		return sm;
	}

	/**
	 * 转化成为Map对象
	 * 
	 * @param mi
	 * @return
	 */
	private Map<String, Object> toMap(MongoItem mi) {
		Map<String, Object> map = mi.toMap();
		String account = (String) map.get("account");
		String iconBase = "http://10.253.45.103:8080/vote/web/profile/";
		map.put("icon", iconBase + account + ".png");
		return map;
	}

}
