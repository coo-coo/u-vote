package com.coo.u.vote;

import java.util.List;

import org.apache.log4j.Logger;

import com.coo.s.cloud.CloudFactory;
import com.kingstar.ngbf.s.cache.IRepository;
import com.kingstar.ngbf.s.mongo.INgbfMongoClient;
import com.kingstar.ngbf.s.mongo.MongoItem;
import com.kingstar.ngbf.s.mongo.NgbfMongoClient;
import com.kingstar.ngbf.s.mongo.NgbfMongoConfiguration;
import com.kingstar.ngbf.s.mongo.QueryAttrs;
import com.kingstar.ngbf.s.template.INgbfTemplateService;
import com.kingstar.ngbf.s.template.NgbfTemplateServiceImpl;

/**
 * 基础工具类
 * 
 * @description
 * @author boqing.shen
 * @date 2014-6-4 下午12:25:25
 * @since 0.1.0.0
 */

public final class VoteManager {
	public static Logger logger = Logger.getLogger(VoteManager.class);

	public static String FORMAT_YYYYMM = "yyyyMM";
	public static String FORMAT_YYYY = "yyyy";

	public final static int MIN1 = 60 * 1;
	public final static int HOUR1 = MIN1 * 60;
	public final static int DAY1 = HOUR1 * 24;

	// private static INgbfMongoClient mongoClient;

	private static INgbfTemplateService templateService;

	/**
	 * 获得模板服务
	 * 
	 * @return
	 */
	public static INgbfTemplateService getTemplateService() {
		if (templateService == null) {
			templateService = new NgbfTemplateServiceImpl();
		}
		return templateService;
	}

	/**
	 * TODO 获得MC中的各对象的名称....
	 */
	public static String getMcKey(Class<?> clz, String value) {
		return clz.getName() + "." + value;
	}

	public static INgbfMongoClient getMockMongo() {
		NgbfMongoConfiguration config = NgbfMongoConfiguration.VOTE;
		return new NgbfMongoClient(config);
	}

	/**
	 * 查找Mongo条目对象
	 */
	public static List<MongoItem> findItems(String collectionName,
			QueryAttrs query) {
		return getMongo().findItems(collectionName, query);
	}

	/**
	 * 判定MC是否存在指定的KEY
	 */
	public static boolean isMcExist(String mcKey) {
		boolean tof = false;
		Object value = getMC().getValue(mcKey);
		if (value != null) {
			tof = true;
		}
		return tof;
	}

	// //////////////////////////////////////////////
	// //////////////////////////////////////////////
	// //////////////////////////////////////////////

	/**
	 * 获得Mongo客户端
	 */
	public static INgbfMongoClient getMongo() {
		return CloudFactory.getMongo();
	}

	/**
	 * 获得Memcached客户端
	 */
	public static IRepository getMC() {
		return CloudFactory.getMC();
	}

}
