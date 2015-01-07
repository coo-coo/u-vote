package com.coo.u.vote.job;

import org.apache.log4j.Logger;

import com.coo.u.vote.job.helper.TopicFetchHandler;
import com.kingstar.ngbf.s.util.SpringContextFactory;

/**
 * 获取最新,投票数最多，customer关注的channel等数据 JOB,由scheduleHook吊起,没有进入Spring环境
 * Topic和数据获取者的投票关系，还没有确定，需要在REST层进行
 * 
 * @author guoliang.li
 * @date 2014-6-4 下午3:13:14
 * @since 0.1.0.0
 */
public class TopicFetchJob extends AbstractJob {

	protected static Logger logger = Logger.getLogger(TopicFetchJob.class);

	// 最新创建条目数
	private int LIMIT_LATEST_CREATE = 100;
	// 最新更新条目数
	@SuppressWarnings("unused")
	private int LIMIT_LATEST_UPDATE = 100;
	// 投票最多条目数
	private int LIMIT_TOP = 100;
	// 静态Channel获取条目数
	private int LIMIT_CHANNEL_FETCH = 500;

	@Override
	public void execute() {
		// 获得最新创建
		getHandler().fetchLatest(LIMIT_LATEST_CREATE);
		// 获得投票最高
		getHandler().fetchTop(LIMIT_TOP);
		// 获得各个静态Channel下的Topic
		getHandler().fetchChannelTypes(LIMIT_CHANNEL_FETCH);
		// 获得Account关注的Topic
		getHandler().fetchAccountFocusTopic();

		// TODO 我关注的人的topic
		// Util.getTopicFetchHandler().fetchAccountFocusAccount();
	}

	@Override
	public String getName() {
		return TopicFetchJob.class.getName();
	}

	private synchronized static TopicFetchHandler getHandler() {
		return SpringContextFactory.getSpringBean(TopicFetchHandler.class);
	}

}
