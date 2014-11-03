package com.coo.u.vote;

/**
 * 基本命名空间
 */

public interface INameSpace {

	public static String FORMAT_YYYYMM = "yyyyMM";
	public static String FORMAT_YYYY = "yyyy";

	/**
	 * 时间
	 */
	public final static int MIN1 = 60 * 1;
	public final static int HOUR1 = MIN1 * 60;
	public final static int DAY1 = HOUR1 * 24;

	/**
	 * MC:账号注册，手机验证码前缀 sms.mobile = 123456
	 */
	public static String MC_PREFIX_SMS = "sms.";
	/**
	 * 移动应用版本
	 */
	public static String PROPID_MOBILE_APP_VERSION = "mobile.app.version";
	/**
	 * 移动应用强制更新标志
	 */
	public static String PROPID_MOBILE_UPDATE_FORCE = "mobile.app.update.force";

	/**
	 * 60s * 10
	 */
	// public static final int CACHE_STAND_600 = 60 * 10;

	// 最新创建条目数
	int LIMIT_LATEST_CREATE = 100;
	// 最新更新条目数
	int LIMIT_LATEST_UPDATE = 100;
	// 投票最多条目数
	int LIMIT_TOP = 100;
	// 静态Channel获取条目数
	int LIMIT_CHANNEL_FETCH = 500;
}
