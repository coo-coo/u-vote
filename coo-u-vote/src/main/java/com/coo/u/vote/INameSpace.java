package com.coo.u.vote;

/**
 * 基本命名空间
 */

public interface INameSpace {

	public static String FORMAT_YYYYMM = "yyyyMM";
	public static String FORMAT_YYYY = "yyyy";
	
	/**
	 * 1分钟....
	 */
	public final static int ONE_MINIUTE = 60*1;
	
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
}
