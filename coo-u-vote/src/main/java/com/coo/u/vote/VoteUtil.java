package com.coo.u.vote;

import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
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
import com.kingstar.ngbf.s.ntp.SimpleMessage;
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
	private static Logger logger = Logger.getLogger(VoteUtil.class);

	private static INgbfMongoClient mongoClient;

	public static INgbfMongoClient getMockMongo() {
		NgbfMongoConfiguration config = NgbfMongoConfiguration.VOTE;
		return new NgbfMongoClient(config);
	}

	/**
	 * 查找Mongo条目对象
	 */
	public static List<MongoItem> findItems(String collectionName,QueryAttrs query) {
		return getMongo().findItems(collectionName, query);
	}
	
	
	/**
	 * 获得Mongo客户端
	 */
	public static INgbfMongoClient getMongo() {
		if (mongoClient == null) {
//			mongoClient = MongoUtil.getClient();
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
	 * @deprecated
	 */
	public static void addOperation(IOperation operation) {
		AsynManager.getInstance().put(operation);
	}

	
	public static SimpleMessage<?> doPost(String url, String jsondata) {
		SimpleMessage<?> resp = null;
		try {
			HttpClient client = new HttpClient();
			// 创建POST方法的实例
			PostMethod method = new PostMethod(url);
			method.setRequestEntity(new StringRequestEntity(jsondata,
					"text/json", "UTF-8"));
			logger.debug(url + "\t" + jsondata);
			// 获得状态，如果是200
			int status = client.executeMethod(method);
			if (status == HttpStatus.SC_OK) {
				resp = SimpleMessage.bind(method.getResponseBodyAsString());
			}
		} catch (Exception e) {
			logger.error("请求失败:" + url, e);
		}
		return resp;
	}

	/**
	 * 提交SimpleMessage对象的Json数据到服务器端,返回SimpleMessage到客户端 如果失败返回Null TODO
	 * 落到s-util中或s-ntp中
	 * @deprecated
	 * @since 0.1.2.0
	 */
	public static SimpleMessage<?> doPost(String url, SimpleMessage<?> sm) {
		return doPost(url, sm.toJson());
	}

	

	// /////////////////////////////////////////////////////////

	// /**
	// * 创建自动主题分表,暂时以年分表(数据量不够)
	// * @deprecated @since 0.1.5.0 交到相关处实现
	// * @since 0.1.0.0
	// * @return
	// */
	// public String getAutoTopicColName() {
	// String suffix = TimeUtil.getNow(FORMAT_YYYY);
	// return INameSpace.COLLECTION_TOPIC + "_" + suffix;
	// }
	//
	// /**
	// * 创建投票分表,暂时以月分表(数据量不够)
	// * @deprecated @since 0.1.5.0 交到相关处实现
	// * @since 0.1.0.0
	// * @return
	// */
	// public String getAutoVoteColName() {
	// String suffix = TimeUtil.getNow(FORMAT_YYYYMM);
	// return INameSpace.COLLECTION_VOTE + "_" + suffix;
	// }

	// ////////////////////////////////////////////////////////////////
	// 获得一些句柄资源,替代VoteFactory
	// ////////////////////////////////////////////////////////////////

	
}
