package com.coo.u.vote.rest;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.coo.s.vote.model.Account;
import com.coo.s.vote.model.Focus;
import com.coo.u.vote.VoteUtil;
import com.coo.u.vote.rest.helper.AccountHelper;
import com.kingstar.ngbf.s.mongo.MongoItem;
import com.kingstar.ngbf.s.ntp.SimpleMessage;
import com.kingstar.ngbf.s.ntp.SimpleMessageHead;
import com.kingstar.ngbf.s.util.NgbfRuntimeException;

/**
 * 实现账号的注册、密码修改、账号登录等操作
 * 
 * @Desc
 * @Author Qiaoqiao.Li
 * @Date 2014-5-27 上午10:15:55
 * @since
 */
@Controller
@RequestMapping("/")
public class AccountRestService {
	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(AccountRestService.class);

	/**
	 * 账号状态变更，用于Admin管理
	 */
	@RequestMapping(value = "/account/update/{accountId}/status/{status}")
	@ResponseBody
	public SimpleMessage<?> accountUpdateStatus(
			@PathVariable("accountId") String accountId,
			@PathVariable("status") int status) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("status", status);
		VoteUtil.getMongo().update(Account.C_NAME, accountId, map);
		return SimpleMessage.ok();
	}

	/**
	 * 先通过手机号获取验证码 检查手机号,向手机号发送[验证码]短信...
	 */
	@RequestMapping(value = "/account/sms/mobile/{mobile}")
	@ResponseBody
	public SimpleMessage<?> accountSms(@PathVariable("mobile") String mobile) {
		SimpleMessage<?> sm = SimpleMessage.ok();
		try {
			if (AccountHelper.isMobileExist(mobile)) {
				throw new NgbfRuntimeException("该手机账号已注册!");
			}
			// 生成验证码
			String sms = AccountHelper.generateSms(mobile);
			sm.getHead().setApi_code("account_sms");
			// 暂时返回前端
			sm.set("sms", sms);
		} catch (NgbfRuntimeException e) {
			sm = SimpleMessage.blank().head(
					SimpleMessageHead.BIZ_ERROR.repMsg(e.getMessage()));
		}
		return sm;
	}

	/**
	 * 账号注册! 验证验证码的正确性
	 */
	@RequestMapping(value = "/account/register/mobile/{mobile}/sms/{sms}/password/{password}", method = RequestMethod.GET)
	@ResponseBody
	public SimpleMessage<?> accountRegister(
			@PathVariable("mobile") String mobile,
			@PathVariable("sms") String sms,
			@PathVariable("password") String password) {
		SimpleMessage<?> sm = SimpleMessage.ok();
		try {
			if (AccountHelper.isMobileExist(mobile)) {
				throw new NgbfRuntimeException("该手机账号已注册!");
			}
			boolean valid = AccountHelper.validateVerifyCode(mobile, sms);
			if (!valid) {
				throw new NgbfRuntimeException("验证码错误,请重新获得验证码!");
			}

			// 注册信息
			String _id = AccountHelper.accountRegister(mobile, password);
			if (_id == null) {
				throw new NgbfRuntimeException("注册失败");
			}
			sm.getHead().setApi_code("account_register");
			// 返回前端UUID，用于M端保存
			sm.set("account", mobile).set("id", _id)
					.set("type", Account.TYPE_COMMON);
		} catch (NgbfRuntimeException e) {
			sm = SimpleMessage.blank().head(
					SimpleMessageHead.BIZ_ERROR.repMsg(e.getMessage()));
		}
		return sm;
	}

	/**
	 * 登陆，使用手机号|密码
	 * 
	 * @since 0.1.0.0
	 */
	@RequestMapping(value = "/account/login/mobile/{mobile}/password/{password}", method = RequestMethod.GET)
	@ResponseBody
	public SimpleMessage<?> accountLogin(@PathVariable("mobile") String mobile,
			@PathVariable("password") String password) {
		SimpleMessage<?> sm = SimpleMessage.ok();
		try {
			MongoItem mi = AccountHelper.accountLogin(mobile, password);
			if (mi == null) {
				throw new NgbfRuntimeException("用户名或密码错误!");
			} else {
				sm.set("account", mobile).set("id", mi.get_id())
						.set("type", Account.TYPE_COMMON);
			}
		} catch (NgbfRuntimeException e) {
			sm = SimpleMessage.blank().head(
					SimpleMessageHead.BIZ_ERROR.repMsg(e.getMessage()));
		}
		return sm;
	}

	/**
	 * 密码修改执行，M端绑定accountUuid
	 * 
	 * @since 0.1.3.0
	 */
	@RequestMapping(value = "/account/pwdreset/account/{account}/password/{password}", method = RequestMethod.GET)
	@ResponseBody
	public SimpleMessage<?> accountPwdreset(
			@PathVariable("account") String account,
			@PathVariable("password") String newPassword) {
		SimpleMessage<?> sm = SimpleMessage.ok();
		try {
			boolean tof = AccountHelper.accountPwdreset(account, newPassword);
			if (!tof) {
				throw new NgbfRuntimeException("密码重置失败!");
			}
		} catch (NgbfRuntimeException e) {
			sm = SimpleMessage.blank().head(
					SimpleMessageHead.BIZ_ERROR.repMsg(e.getMessage()));
		}
		return sm;
	}

	/**
	 * 关注Account
	 * 
	 * @since 0.1.1.0
	 */
	@RequestMapping(value = "/focus/account/{account}/{subject} ")
	@ResponseBody
	public SimpleMessage<?> focusAccount(
			@PathVariable("account") String account,
			@PathVariable("subject") String subject) {
		// account可能为mobile，也可能为Email
		return AccountHelper.focus(account, subject, Focus.TYPE_ACCOUNT);
	}

	/**
	 * 关注Channel,频道有两种,系统的和后续服务号的...
	 * 
	 * @since 0.1.1.0
	 */
	@RequestMapping(value = "/focus/channel/{account}/{subject}")
	@ResponseBody
	public SimpleMessage<?> focusChannel(
			@PathVariable("account") String account,
			@PathVariable("subject") String channel) {
		return AccountHelper.focus(account, channel, Focus.TYPE_CHANNEL);
	}

	/**
	 * 关注Topic
	 * 
	 * @since 0.1.1.0
	 */
	@RequestMapping(value = "/focus/topic/{account}/{subject}")
	@ResponseBody
	public SimpleMessage<?> focusTopic(@PathVariable("account") String account,
			@PathVariable("subject") String topic) {
		return AccountHelper.focus(account, topic, Focus.TYPE_TOPIC);
	}

	/**
	 * 取消Account关注
	 * 
	 * @since 0.1.4.0
	 */
	@RequestMapping(value = "/unfocus/account/{account}/{subject}")
	@ResponseBody
	public SimpleMessage<?> unfocusAccount(
			@PathVariable("account") String account,
			@PathVariable("subject") String subject) {
		return AccountHelper.unfocus(account, subject, Focus.TYPE_ACCOUNT);
	}

	/**
	 * 取消channel关注
	 * 
	 * @since 0.1.4.0
	 */
	@RequestMapping(value = "/unfocus/channnel/{account}/{subject}")
	@ResponseBody
	public SimpleMessage<?> unfocusChannel(
			@PathVariable("account") String account,
			@PathVariable("subject") String channel) {
		return AccountHelper.unfocus(account, channel, Focus.TYPE_CHANNEL);
	}

	/**
	 * 取消topic关注
	 * 
	 * @since 0.1.4.0
	 */
	@RequestMapping(value = "/unfocus/topic/{account}/{subject}")
	@ResponseBody
	public SimpleMessage<?> unfocusTopic(
			@PathVariable("account") String account,
			@PathVariable("topic") String topic) {
		return AccountHelper.unfocus(account, topic, Focus.TYPE_TOPIC);
	}
}
