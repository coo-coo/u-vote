package com.coo.u.vote.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.coo.s.vote.model.Account;
import com.coo.u.vote.ModelManager;
import com.coo.u.vote.VoteUtil;
import com.coo.u.vote.rest.helper.AccountHelper;
import com.kingstar.ngbf.s.mongo.MongoItem;
import com.kingstar.ngbf.s.mongo.QueryAttrs;
import com.kingstar.ngbf.s.ntp.NtpHead;
import com.kingstar.ngbf.s.ntp.NtpMessage;
import com.kingstar.ngbf.s.util.NgbfRuntimeException;
import com.kingstar.ngbf.s.util.StringUtil;

/**
 * 实现账号的注册、密码修改、账号登录等操作
 */
@Controller
@RequestMapping("/account")
public class AccountRestService extends CommonRest {
	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(AccountRestService.class);

	/**
	 * [用户自己调用]获得Profile的信息,通过手机号获取
	 */
	@RequestMapping(value = "/info/mobile/{mobile}", method = RequestMethod.GET)
	@ResponseBody
	public NtpMessage info(@PathVariable("mobile") String mobile) {
		QueryAttrs query = QueryAttrs.blank().add("mobile", mobile);
		// 查询获得列表，因为，数据较简单，直接从Mongo数据库中获得
		MongoItem mi = VoteUtil.getMongo().findItemOne(Account.C_NAME, query);
		NtpMessage resp = NtpMessage.ok();
		if (mi != null) {
			// TODO 不好直接过去....
			resp.set(mi.toMap());
		} else {
			resp = resp.head(NtpHead.NOT_FOUND);
		}
		// 设置请求Code
		return resp;
	}

	/**
	 * [用户调用] 获得Profile的信息,通过手机号获取
	 */
	@RequestMapping(value = "/info/_id/{_id}", method = RequestMethod.GET)
	@ResponseBody
	public NtpMessage get(@PathVariable("_id") String _id) {
		// 查询获得列表，因为，数据较简单，直接从Mongo数据库中获得
		MongoItem mi = VoteUtil.getMongo().getItem(Account.C_NAME, _id);
		NtpMessage resp = NtpMessage.ok();
		if (mi != null) {
			resp.set(mi.toMap());
		} else {
			resp = resp.head(NtpHead.NOT_FOUND);
		}
		return resp;
	}

	/**
	 * [Admin调用] 账号状态变更，用于Admin管理 url?op=139xxxxxxxx
	 */
	@RequestMapping(value = "/update/_id/{_id}/status/{status}", method = RequestMethod.GET)
	@ResponseBody
	public NtpMessage updateStatus(HttpServletRequest req,
			@PathVariable("_id") String _id, @PathVariable("status") int status) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("status", status);
		map.put("updater", this.getOperator(req));
		VoteUtil.getMongo().update(Account.C_NAME, _id, map);
		return NtpMessage.ok();
	}

	/**
	 * [用户调用] 用户更新子集的属性信息
	 */
	@RequestMapping(value = "/update/param", method = RequestMethod.POST)
	@ResponseBody
	public NtpMessage updateParam(@RequestBody String data) {
		NtpMessage resp = NtpMessage.ok();
		// 获得Android客户端传来的data(Topic)的数据
		NtpMessage sm = NtpMessage.bind(data);
		if (sm != null) {
			// account._id
			String _id = (String) sm.get("_id");
			// 字段名称 & 值
			String key = (String) sm.get("key");
			String value = (String) sm.get("value");
			// 直接Map对象传递到数据库中
			Map<String, Object> item = new HashMap<String, Object>();
			item.put(key, value);
//			System.out.println(item);
			VoteUtil.getMongo().update(Account.C_NAME, _id, item);
		} else {
			resp = resp.head(NtpHead.PARAMETER_ERROR);
		}
		return resp;
	}

	/**
	 * [M端Task调用]获得Profile的列表信息信息,通过手机号获取,多个手机号以逗号隔开 用于MProfile的更新
	 */
	@RequestMapping(value = "/list/mobiles/{mobiles}", method = RequestMethod.GET)
	@ResponseBody
	public NtpMessage list(@PathVariable("mobiles") String mobiles) {
		String[] mobile = StringUtil.stringToArray(mobiles);
		// TODO 从缓存中获取.... 参见AccountJob...
		NtpMessage resp = NtpMessage.ok();
		for (String m : mobile) {
			String mcKey = "account." + m;
			MongoItem mi = (MongoItem) VoteUtil.getMC().getValue(mcKey);
			if (mi != null) {
				// TODO 其它属性? 待处理....
				Account fb = new Account();
				// Merge对象 先转换成对象，再传递
				// 不能用MongoItem.toMap()到M端,可能会产生Integer到Double的默认转换(GSON的问题)
				ModelManager.merge(mi, fb);
				resp.add(fb);
			}
		}
		return resp;
	}

	/**
	 * [M端Task调用]获得全部账号列表信息，供Admin管理
	 */
	@RequestMapping(value = "/list/all", method = RequestMethod.GET)
	@ResponseBody
	public NtpMessage listAll() {
		QueryAttrs query = QueryAttrs.blank().desc("_tsu");
		// 查询获得列表，因为，数据较简单，直接从Mongo数据库中获得
		List<MongoItem> items = VoteUtil.findItems(Account.C_NAME, query);
		NtpMessage resp = NtpMessage.ok();
		for (MongoItem mi : items) {
			// TODO 其它属性? 待处理....
			Account fb = new Account();
			// Merge对象 先转换成对象，再传递
			// 不能用MongoItem.toMap()到M端,可能会产生Integer到Double的默认转换(GSON的问题)
			ModelManager.merge(mi, fb);
			resp.add(fb);
		}
		return resp;
	}

	/**
	 * [用户调用] 先通过手机号获取验证码 检查手机号,向手机号发送[验证码]短信...
	 */
	@RequestMapping(value = "/sms/mobile/{mobile}", method = RequestMethod.GET)
	@ResponseBody
	public NtpMessage sms(@PathVariable("mobile") String mobile) {
		NtpMessage sm = NtpMessage.ok();
		try {
			if (AccountHelper.isMobileExist(mobile)) {
				throw new NgbfRuntimeException("该手机账号已注册!");
			}
			// 生成验证码
			String sms = AccountHelper.generateSms(mobile);
			// sm.getHead().setApi_code("account_sms");
			// 暂时返回前端
			sm.set("sms", sms);
		} catch (NgbfRuntimeException e) {
			sm = NtpMessage.blank().head(
					NtpHead.BIZ_ERROR.repMsg(e.getMessage()));
		}
		return sm;
	}

	/**
	 * [用户调用] 账号注册! 验证验证码的正确性
	 */
	@RequestMapping(value = "/register/mobile/{mobile}/sms/{sms}/password/{password}", method = RequestMethod.GET)
	@ResponseBody
	public NtpMessage register(@PathVariable("mobile") String mobile,
			@PathVariable("sms") String sms,
			@PathVariable("password") String password) {
		NtpMessage sm = NtpMessage.ok();
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
			// 返回前端UUID，用于M端保存
			sm.set("account", mobile).set("id", _id)
					.set("type", Account.TYPE_COMMON);
		} catch (NgbfRuntimeException e) {
			sm = NtpMessage.blank().head(
					NtpHead.BIZ_ERROR.repMsg(e.getMessage()));
		}
		return sm;
	}

	/**
	 * [用户调用] 登陆，使用手机号|密码
	 */
	@RequestMapping(value = "/login/mobile/{mobile}/password/{password}", method = RequestMethod.GET)
	@ResponseBody
	public NtpMessage login(@PathVariable("mobile") String mobile,
			@PathVariable("password") String password) {
		NtpMessage sm = NtpMessage.ok();
		try {
			MongoItem mi = AccountHelper.accountLogin(mobile, password);
			if (mi == null) {
				throw new NgbfRuntimeException("用户名或密码错误!");
			} else {
				sm.set("account", mobile).set("id", mi.get_id())
						.set("type", Account.TYPE_COMMON);
			}
		} catch (NgbfRuntimeException e) {
			sm = NtpMessage.blank().head(
					NtpHead.BIZ_ERROR.repMsg(e.getMessage()));
		}
		return sm;
	}

	/**
	 * [用户调用] 密码修改执行，M端绑定accountUuid
	 */
	@RequestMapping(value = "/pwdreset/mobile/{mobile}/password/{password}", method = RequestMethod.GET)
	@ResponseBody
	public NtpMessage pwdreset(@PathVariable("mobile") String mobile,
			@PathVariable("password") String newPassword) {
		NtpMessage sm = NtpMessage.ok();
		try {
			boolean tof = AccountHelper.accountPwdreset(mobile, newPassword);
			if (!tof) {
				throw new NgbfRuntimeException("密码重置失败!");
			}
		} catch (NgbfRuntimeException e) {
			sm = NtpMessage.blank().head(
					NtpHead.BIZ_ERROR.repMsg(e.getMessage()));
		}
		return sm;
	}

	/**
	 * [用户调用] 关注资源 url?op=139xxxxxxxx
	 */
	@RequestMapping(value = "/focus/type/{type}/subject/{subject}", method = RequestMethod.GET)
	@ResponseBody
	public NtpMessage focus(HttpServletRequest req,
			@PathVariable("type") String type,
			@PathVariable("subject") String subject) {
		// 获得操作者
		String account = this.getOperator(req);
		return AccountHelper.focus(account, subject, type);
	}

	/**
	 * [用户调用] 关注解除 url?op=139xxxxxxxx
	 */
	@RequestMapping(value = "/unfocus/type/{type}/subject/{subject}", method = RequestMethod.GET)
	@ResponseBody
	public NtpMessage unfocus(HttpServletRequest req,
			@PathVariable("type") String type,
			@PathVariable("subject") String subject) {
		// 获得操作者
		String account = this.getOperator(req);
		return AccountHelper.unfocus(account, subject, type);
	}

	// ////////////////////////////////////////////////////

}
