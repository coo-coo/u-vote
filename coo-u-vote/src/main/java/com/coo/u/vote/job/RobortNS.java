package com.coo.u.vote.job;

/**
 * 
 * @description
 * @author boqing.shen
 * @date 2014-6-24 上午10:28:19
 * @since 0.1.5.0
 */

public interface RobortNS {

	public final static int ONE_MINIUTE = 60*1;
	
	/**
	 * 60s * 10
	 */
	// public static final int CACHE_STAND_600 = 60 * 10;
	
	int LIMIT_LATEST_CREATE = 100;
	int LIMIT_LATEST_UPDATE = 100;
	int LIMIT_TOP = 100;
	int LIMIT_FETCH = 500;
}
