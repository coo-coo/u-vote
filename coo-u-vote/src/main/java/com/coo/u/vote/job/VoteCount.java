package com.coo.u.vote.job;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * 投票计数对象,用于投票统计,更新Topic信息
 * 
 * @description
 * @author boqing.shen
 * @date 2014-9-23 上午11:55:57
 * @since 0.6.2.0
 */

public class VoteCount {

	private static Logger logger = Logger.getLogger(VoteCount.class);

	public VoteCount(String topicId) {
		this.topicId = topicId;
	}

	private String topicId = "";

	/**
	 * Topic计数
	 */
	private int count = 0;

	private Map<String, Integer> legCounts = new HashMap<String, Integer>();

	/**
	 * 腿计数
	 * 
	 * @param legSeq
	 */
	public synchronized void legCount(String legSeq) {
		count++;
		Integer v = legCounts.get(legSeq);
		if (v==null) {
			v = 0;
		}
		v++;
		legCounts.put(legSeq, v);
	}

	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	public Integer getLegCount(String legSeq) {
		return legCounts.get(legSeq);
	}

	/**
	 * @return the legs
	 */
	public Map<String, Integer> getLegCounts() {
		return legCounts;
	}

	/**
	 * @return the topicId
	 */
	public String getTopicId() {
		return topicId;
	}

	public void show() {
		logger.debug("topicId=" + this.topicId + "\t" + this.getCount());
		// Map<String, Integer> legCounts
		Set<Entry<String, Integer>> set = legCounts.entrySet();
		for (Entry<String, Integer> entry : set) {
			logger.debug("leg:\t" + entry.getKey() + "=" + entry.getValue());
		}
		logger.debug("");
	}

}
