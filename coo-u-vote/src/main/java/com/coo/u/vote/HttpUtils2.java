package com.coo.u.vote;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;

import com.kingstar.ngbf.s.ntp.NtpMessage;

/**
 * Http工具2,针对Ntp的SimpleMessage的调用进行代码封装
 */
public class HttpUtils2 {

	/**
	 * 向服务端POST发送JsonData
	 */
	public static NtpMessage doPostNtp(String restUrl, String jsonData) {
		NtpMessage resp = null;
		try {
			HttpClient client = new org.apache.commons.httpclient.HttpClient();
			// 创建POST方法的实例
			PostMethod method = new PostMethod(restUrl);

			method.setRequestEntity(new StringRequestEntity(jsonData,
					"text/json", "UTF-8"));
			// 获得状态，如果是200
			int status = client.executeMethod(method);
			if (status == HttpStatus.SC_OK) {
				resp = NtpMessage.bind(method.getResponseBodyAsString());
			}
		} catch (Exception e) {
		}
		return resp;
	}

	/**
	 * 向一个REST地址做请求
	 */
	public static NtpMessage doGetNtp(String restUrl) {
		NtpMessage resp = null;
		HttpClient client = new HttpClient();
		// 创建GET方法的实例
		GetMethod method = new GetMethod(restUrl);
		// 缺省支持UTF-8
		client.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
				"UTF-8");
		try {
			client.executeMethod(method);
			String msg = method.getResponseBodyAsString();
			resp = NtpMessage.bind(msg);
		} catch (Exception e) {
		}
		return resp;
	}
}
