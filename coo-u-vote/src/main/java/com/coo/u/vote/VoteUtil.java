package com.coo.u.vote;

import java.util.List;

import org.apache.log4j.Logger;

import com.kingstar.ngbf.s.asyn.AsynManager;
import com.kingstar.ngbf.s.asyn.IOperation;
import com.kingstar.ngbf.s.cache.IRepository;
import com.kingstar.ngbf.s.mongo.INgbfMongoClient;
import com.kingstar.ngbf.s.mongo.LogItem;
import com.kingstar.ngbf.s.mongo.MongoItem;
import com.kingstar.ngbf.s.mongo.NgbfMongoClient;
import com.kingstar.ngbf.s.mongo.NgbfMongoConfiguration;
import com.kingstar.ngbf.s.mongo.QueryAttrs;
import com.kingstar.ngbf.s.template.INgbfTemplateService;
import com.kingstar.ngbf.s.template.NgbfTemplateServiceImpl;
import com.kingstar.ngbf.u.base.UFactory;

/**
 * 基础工具类
 * 
 * @description
 * @author boqing.shen
 * @date 2014-6-4 下午12:25:25
 * @since 0.1.0.0
 */

public final class VoteUtil {
	public static Logger logger = Logger.getLogger(VoteUtil.class);

	private static INgbfMongoClient mongoClient;
	
	private static INgbfTemplateService templateService;
	
	/**
	 * 获得模板服务
	 * @return
	 */
	public static INgbfTemplateService getTemplateService() {
		if(templateService==null){
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
	 * 获得Mongo客户端
	 */
	public static INgbfMongoClient getMongo() {
		if (mongoClient == null) {
			// mongoClient = MongoUtil.getClient();
			mongoClient = getMockMongo();
		}
		return mongoClient;
	}

	/**
	 * 获得Memcached客户端
	 */
	public static IRepository getMC() {
		return UFactory.getXmemcachedRepository();
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

	/**
	 * 添加一个Log 业务/系统日志记录器,各业务类调用,旨在收集/发布系统产生的各日志
	 * 
	 * @since 0.1.5.0
	 */
	public static void addLog(LogItem log) {
		// 暂时现已异步的形式进行存储,存储到MongoDB中
		// TODO 分布式存储
		addOperation(new LogSaveOperation(log));
	}

	/**
	 * 添加异步操作实现,暂时替代队列实现
	 * 
	 * @deprecated
	 */
	public static void addOperation(IOperation operation) {
		AsynManager.getInstance().put(operation);
	}
}
