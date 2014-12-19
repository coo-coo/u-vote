package com.coo.u.vote.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.coo.s.vote.model.Topic;
import com.coo.u.vote.ModelManager;
import com.coo.u.vote.VoteUtil;
import com.kingstar.ngbf.s.mongo.MongoItem;
import com.kingstar.ngbf.s.mongo.QueryAttrs;
import com.kingstar.ngbf.s.util.SystemUtil;

/**
 * 根据模板,生成静态的Topic页面
 */
public class TopicHtmlBuildJob extends AbstractJob {

	protected static Logger logger = Logger.getLogger(TopicHtmlBuildJob.class);

	@Override
	public void execute() {
		// TODO 已创建的Topic,不再创建...
		// 获得所有符合条件的条目 0:未被build;1:已生成
		QueryAttrs query = QueryAttrs.blank();// .add("builded", "0");
		List<MongoItem> items = VoteUtil.findItems(Topic.C_NAME, query);
		for (MongoItem mi : items) {
			Topic topic = ModelManager.mi2Topic(mi);
			generateHtmlFile(topic);
		}
	}

	/**
	 * 根据Topic对象生成html文件
	 */
	private void generateHtmlFile(Topic topic) {
		Map<String, Object> map = new HashMap<String, Object>();
		// 放置对象
		map.put("topic", topic);
		// 生成文件
		String template = "topic_template.vm";
		String templateDir = getTemplateDir();
		// String targetDir2 =
		// "C:\\develop\\eclipse_git_workspace\\repo-d-vote\\coo-d-vote\\target\\coo-d-vote-0.7.0.0-bin\\web\\topic\\";
		String fileName = templateDir + "t_" + topic.get_id() + ".html";
		// 通过模板生成文件..
		VoteUtil.getTemplateService().create(template, map, templateDir,
				fileName);
	}

	/**
	 * 获得模板地址
	 * TODO 简单的性能提高...static DIR
	 * @return
	 */
	private String getTemplateDir() {
		return SystemUtil.getWebAppPath().replace("lib/", "")
				.replace("\\", "/")
				+ "web/topic/";
	}

	@Override
	public String getName() {
		return TopicHtmlBuildJob.class.getName();
	}
}
