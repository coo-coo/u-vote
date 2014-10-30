package com.coo.u.vote;

import com.kingstar.ngbf.s.asyn.GenericOperation;
import com.kingstar.ngbf.s.mongo.LogItem;
import com.kingstar.ngbf.s.util.TimeUtil;

/**
 * 执行日志的存储,存储到MongoDB中,分表处理
 * 
 * @description
 * @author boqing.shen
 * @date 2014-6-26 下午2:04:29
 * @since 0.1.5.0
 */
public class LogSaveOperation extends GenericOperation {

	private LogItem log = null;

	public LogSaveOperation(LogItem log) {
		this.log = log;
	}

	/**
	 * 执行存储
	 */
	@Override
	public void execute() {
		// Map<String, Object> item = new HashMap<String, Object>();
		// item.put("sys_event", log.getEvent());
		// item.put("sys_note", log.getNote());
		// item.put("sys_source", log.getSource());
		// item.put("sys_ts", log.getTs());
		// 放置其它属性
		VoteUtil.getMongo().insert(getColName(), log.toDataMap());
	}

	/**
	 * 获得动态分表
	 * 
	 * @return
	 */
	public String getColName() {
		String suffix = TimeUtil.getNow(INameSpace.FORMAT_YYYYMM);
		return "vote_log" + "_" + suffix;
	}
}
