package com.coo.u.vote.job;

import java.util.List;

import org.apache.log4j.Logger;

import com.coo.s.vote.model.Account;
import com.coo.u.vote.VoteUtil;
import com.kingstar.ngbf.s.mongo.MongoItem;
import com.kingstar.ngbf.s.mongo.QueryAttrs;

/**
 * 读取所有的Account信息,放置在MC中,用来提供Contact和Account的信息同步 参见ContactJob
 */
public class AccountJob extends AbstractJob {

	private static Logger logger = Logger.getLogger(AccountJob.class);

	@Override
	public void execute() {
		// 放置Vote信息到MC中
		put2MC();
	}

	@Override
	public String getName() {
		return AccountJob.class.getName();
	}

	/**
	 * 放置信息到MC中
	 */
	private void put2MC() {
		// 获得所有条目
		QueryAttrs query = QueryAttrs.blank().desc("_tsi");
		List<MongoItem> items = VoteUtil.findItems(Account.C_NAME, query);
		logger.debug("account size=" + items.size());
		for (MongoItem mi : items) {
			String mobile = (String) mi.get("mobile");
			String mcKey = "account." + mobile;
			// 放置到MC中
			VoteUtil.getMC().put(mcKey, mi, RobortNS.ONE_MINIUTE * 600);
		}
	}

}
