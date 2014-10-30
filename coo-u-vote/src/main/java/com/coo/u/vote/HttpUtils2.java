package com.coo.u.vote;

import java.lang.reflect.Type;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;

import com.kingstar.ngbf.s.ntp.NtpHelper;
import com.kingstar.ngbf.s.ntp.SimpleMessage;

/**
 * Http工具2,针对Ntp的SimpleMessage的调用进行代码封装
 */
public class HttpUtils2 {

	/**
	 * 向服务端POST发送JsonData
	 */
	public static SimpleMessage<?> doPost(String restUrl, String jsonData) {
		SimpleMessage<?> resp = null;
		try {
			HttpClient client = new org.apache.commons.httpclient.HttpClient();
			// 创建POST方法的实例
			PostMethod method = new PostMethod(restUrl);

			method.setRequestEntity(new StringRequestEntity(jsonData,
					"text/json", "UTF-8"));
			// 获得状态，如果是200
			int status = client.executeMethod(method);
			if (status == HttpStatus.SC_OK) {
				resp = SimpleMessage.bind(method.getResponseBodyAsString());
			}
		} catch (Exception e) {
		}
		return resp;
	}

	/**
	 * 向一个地址Post消息(SimpleMessage)
	 * 
	 * @since 0.6.1.0
	 */
	public static SimpleMessage<?> doPost(String restUrl, SimpleMessage<?> sm) {
		return doPost(restUrl, sm.toJson());
	}

	/**
	 * 向一个REST地址做请求,获取服务器端信息 Type==null作普通字符串封装，Type不为null,做record对象绑定操作
	 * Type，参见NtpHelper的TokenType
	 */
	public static SimpleMessage<?> doGet(String restUrl, Type type) {
		SimpleMessage<?> resp = null;
		HttpClient client = new HttpClient();
		// 创建GET方法的实例
		GetMethod method = new GetMethod(restUrl);
		// 缺省支持UTF-8
		client.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
				"UTF-8");
		try {
			client.executeMethod(method);
			String msg = method.getResponseBodyAsString();
			if (type == null) {
				resp = NtpHelper.bind(msg);
			} else {
				resp = NtpHelper.bind(msg, type);
			}
		} catch (Exception e) {
		}
		return resp;
	}
}
