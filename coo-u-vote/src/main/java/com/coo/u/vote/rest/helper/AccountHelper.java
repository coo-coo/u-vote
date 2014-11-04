package com.coo.u.vote.rest.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.coo.s.vote.model.Account;
import com.coo.s.vote.model.Focus;
import com.coo.u.vote.INameSpace;
import com.coo.u.vote.ModelManager;
import com.coo.u.vote.VoteUtil;
import com.kingstar.ngbf.s.cache.ICacheAble;
import com.kingstar.ngbf.s.mongo.MongoItem;
import com.kingstar.ngbf.s.mongo.QueryAttrs;
import com.kingstar.ngbf.s.ntp.NtpMessage;
import com.kingstar.ngbf.s.util.StringUtil;

/**
 * @Desc
 * @Author Qiaoqiao.Li
 * @Date 2014-6-4 下午1:26:33
 * @since
 */
public class AccountHelper {

	private static final Logger logger = Logger.getLogger(AccountHelper.class);

	/**
	 * 判断mobile是否存在
	 * 
	 * @since 0.5.1.0
	 */
	public static boolean isMobileExist(String mobile) {
		boolean tof = false;
		// 直接从mongoDB数据库查询 SBQ @since 0.5.0.1
		QueryAttrs query = QueryAttrs.blank().add("mobile", mobile);
		List<MongoItem> list = VoteUtil.getMongo().findItems(Account.C_NAME,
				query);
		if (list.size() > 0) {
			tof = true;
		}
		return tof;
	}

	/**
	 * 生成短信验证码
	 */
	public static String generateSms(String mobile) {
		// 生成验证码
		String sms = StringUtil.getRandomNumberCode(6);
		// TODO 将验证码以短信的方式发送出去

		// 存储验证码到MC中 默认存一分钟
		VoteUtil.getMC().put(INameSpace.MC_PREFIX_SMS + mobile, sms);
		logger.debug("verifyCode=" + sms);
		return sms;
	}

	/**
	 * 注册第二步，验证验证码的正确性
	 * 
	 * @since 0.5.1.0
	 */
	public static boolean validateVerifyCode(String mobile, String verifyCode) {
		boolean tof = false;
		// 从memcache中取出vertifycode
		String value = (String) getCacheObject(INameSpace.MC_PREFIX_SMS
				+ mobile);
		if (value != null && value.equals(verifyCode)) {
			tof = true;
		}
		return tof;
	}

	/**
	 * 登录
	 * 
	 * @since 0.5.1.0
	 */
	public static MongoItem accountLogin(String mobile, String password) {
		// 登陆在mongoDB中查找
		QueryAttrs query = QueryAttrs.blank().add("mobile", mobile)
				.add("password", password);
		return VoteUtil.getMongo().findItemOne(Account.C_NAME, query);
	}

	/**
	 * 获取memcache中的数据
	 * 
	 * @param cacheId
	 */
	private static Object getCacheObject(String cacheId) {
		ICacheAble co = VoteUtil.getMC().get(cacheId);
		if (co != null) {
			return co.getBindingObject();
		} else {
			return null;
		}
	}

	/**
	 * 激活已注册的用户
	 * 
	 * @since 0.1.1.0
	 */
	public static NtpMessage activeAccount(String account) {
		// 激活账号 是否使用operation TODO
		Map<String, Object> map = new HashMap<String, Object>();
		// map.put("type", Account.STATUS_VERTIFY);
		QueryAttrs query = QueryAttrs.blank().add("mobile", account);
		List<MongoItem> list = VoteUtil.getMongo().findItems(Account.C_NAME,
				query);
		if (list != null && list.size() <= 0) {
			String _id = list.get(0).get_id();
			VoteUtil.getMongo().updateFull(Account.C_NAME, _id, map);
			return NtpMessage.error("账号已激活");
		}
		return NtpMessage.error(account + "不存在");
	}

	/**
	 * 锁定账号
	 * 
	 * @since 0.1.1.0
	 */
	public static NtpMessage lockAccount(String account) {
		// 锁定账号 是否使用operation TODO
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("status", Account.STATUS_LOCKED);
		QueryAttrs query = QueryAttrs.blank().add("mobile", account);
		List<MongoItem> list = VoteUtil.getMongo().findItems(Account.C_NAME,
				query);
		if (list != null && list.size() > 0) {
			String _id = list.get(0).get_id();
			VoteUtil.getMongo().updateFull(Account.C_NAME, _id, map);
			return NtpMessage.error(account + "被锁定");
		}
		return NtpMessage.error(account + "不存在");
	}

	/**
	 * focus建立关联关系
	 * 
	 * @since 0.6.0.0
	 */
	public static NtpMessage focus(String account, String subject, String type) {
		// TODO 判断focus是否存在 暂不考虑Focus的重复性
		Focus focus = new Focus();
		focus.setAccount(account);
		focus.setSubject(subject);
		focus.setType(type);
		// 转化为Map对象
		Map<String, Object> item = ModelManager.toMap(focus);
		VoteUtil.getMongo().insert(Focus.C_NAME, item);
		return NtpMessage.ok();
	}

	/**
	 * 取消focus关注关系
	 * 
	 * @since 0.6.0.0
	 */
	public static NtpMessage unfocus(String account, String subject, String type) {
		// 判断focus是否存在
		QueryAttrs query = QueryAttrs.blank().add("account", account)
				.add("subject", subject).add("type", type)
				.add("status", Focus.STATUS_VALID);
		MongoItem mi = VoteUtil.getMongo().findItemOne(Focus.C_NAME, query);
		if (mi != null) {
			Map<String, Object> item = new HashMap<String, Object>();
			item.put("status", Focus.STATUS_INVALID);
			VoteUtil.getMongo().update(Focus.C_NAME, mi.get_id(), item);
		}
		// 取消 account对subject的关联关系
		return NtpMessage.ok();
	}

	/**
	 * 更改Account密码信息
	 * 
	 * @since 0.5.2.0
	 */
	public static boolean accountPwdreset(String mobile, String password) {
		// 通过uuid找到对象，再更新为新对象
		QueryAttrs query = QueryAttrs.blank().add("mobile", mobile);
		MongoItem mi = VoteUtil.getMongo().findItemOne(Account.C_NAME, query);
		boolean tof = true;
		if (mi != null) {
			String _id = mi.get_id();
			Map<String, Object> item = new HashMap<String, Object>();
			item.put("password", password);
			VoteUtil.getMongo().update(Account.C_NAME, _id, item);
		} else {
			tof = false;
		}
		return tof;
	}

	/**
	 * 账号注册
	 */
	public static String accountRegister(String mobile, String password) {
		// TODO 已注册已验证,后续会进行锁定操作
		Account account = new Account(mobile, Account.TYPE_COMMON);
		account.setPassword(password);
		Map<String, Object> item = ModelManager.toMap(account);
		// 插入,获得MongoItem的ID
		String _id = VoteUtil.getMongo().insert(Account.C_NAME, item);
		return _id;
	}

}
