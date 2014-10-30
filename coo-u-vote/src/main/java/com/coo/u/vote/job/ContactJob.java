package com.coo.u.vote.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.coo.s.vote.model.Contact;
import com.coo.u.vote.VoteUtil;
import com.kingstar.ngbf.s.mongo.MongoItem;
import com.kingstar.ngbf.s.mongo.MongoUtil;
import com.kingstar.ngbf.s.mongo.QueryAttrs;

/**
 * 获取Account的信息(昵称,区域,icon,wxId,签名等),同步更新到Contact信息表中
 * 参见M端VoteManager.getProfileSkeletonItems()
 */

public class ContactJob extends AbstractJob {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.coo.u.vote.robort.job.AbstractJob#execute()
	 */
	@Override
	public void execute() {
		// 获得所有Contact信息，从MC中获取Account账号，进行本地更新
		QueryAttrs query = QueryAttrs.blank();
		List<MongoItem> items = MongoUtil.getClient().findItems(Contact.C_NAME,
				query);
		for (MongoItem mi : items) {
			// Contact c = new Contact();
			// ModelManager.merge(mi, c);
			// 通过mobile(11位,没有+86等字样)来找账号，
			String mobile = (String) mi.get("mobile");
			String mcKey = "account." + mobile;

			// 从MC中获取Account账号,參見AccountJob
			MongoItem accountMI = (MongoItem) VoteUtil.getMC().getValue(mcKey);
			if (accountMI != null) {
				// 表明该Mobile已经有账号注册
				String contactId = mi.get_id();
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("account", mobile);
				map.put("alias", accountMI.get("alias"));
				map.put("weixin", accountMI.get("weixin"));
				map.put("signature", accountMI.get("signature"));
				map.put("area", accountMI.get("area"));
				// TODO 更新操作 有的时候不见得非要更新?
				MongoUtil.getClient().update(Contact.C_NAME, contactId, map);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.coo.u.vote.robort.job.AbstractJob#getName()
	 */
	@Override
	public String getName() {
		return ContactJob.class.getName();
	}

}
