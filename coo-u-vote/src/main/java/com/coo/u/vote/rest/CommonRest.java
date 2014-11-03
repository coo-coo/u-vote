package com.coo.u.vote.rest;

import javax.servlet.http.HttpServletRequest;

public abstract class CommonRest {
	
	/**
	 * 获得操作者账号，即M端的Host，采用doGet获取 url?op=130xxxxxxxx
	 */
	protected String getOperator(HttpServletRequest req){
		String operator = req.getParameter("op");
		if(operator==null){
			operator = "UNKNOWN";
		}
		return operator;
	}
	
}
