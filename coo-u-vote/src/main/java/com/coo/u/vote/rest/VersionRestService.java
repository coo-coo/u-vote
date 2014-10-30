package com.coo.u.vote.rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.coo.u.vote.INameSpace;
import com.kingstar.ngbf.s.ntp.SimpleMessage;
import com.kingstar.ngbf.s.ntp.SimpleMessageHead;
import com.kingstar.ngbf.u.base.ProcessContext;

/**
 * 版本信息服务
 * 
 * @description
 * @author boqing.shen
 * @date 2014-8-3 下午3:04:38
 * @since 0.5.1.0
 */
@Controller
@RequestMapping("/")
public class VersionRestService {

	/**
	 * 检测版本信息
	 * 
	 * @since 0.1.5.0
	 */
	@RequestMapping(value = "/version/update")
	@ResponseBody
	public SimpleMessage<?> versionUpdate() {
		try {
			// TODO 处理和验证业务消息
			// 返回消息
			String appVersion = ProcessContext.getInstance().getStrPropValue(
					INameSpace.PROPID_MOBILE_APP_VERSION, "0.4.0");
			String appApkUrl = "http://10.253.46.75:8082/hudson/view/vote/job/coo-m-vote/ws/target/coo-m-vote-"
					+ appVersion + ".apk";
			String appUpdateForce = ProcessContext
					.getInstance()
					.getStrPropValue(INameSpace.PROPID_MOBILE_UPDATE_FORCE, "0");
			// 返回最新应用版本...
			return SimpleMessage.ok().set("app_version", appVersion)
					.set("app_update_force", appUpdateForce)
					.set("app_apk_url", appApkUrl);
		} catch (Exception e) {
			return SimpleMessage.blank().head(SimpleMessageHead.SERVICE_ERROR);
		}
	}
}
