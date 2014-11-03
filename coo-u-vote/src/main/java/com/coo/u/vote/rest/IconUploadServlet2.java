package com.coo.u.vote.rest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.springframework.stereotype.Component;

import com.kingstar.ngbf.u.base.jetty.GenericJettyServlet;

/**
 * @description
 * @author boqing.shen
 * @date 2014-10-10 下午3:18:34
 * @since 1.0.0.0
 */
@Component("iconUploadServlet2")
public class IconUploadServlet2 extends GenericJettyServlet {

	private static final long serialVersionUID = -9050957469346915809L;

	@Override
	public String getMappingExpression() {
		return "/servlet/icon2";
	}

	// @SuppressWarnings("unchecked")
	@Override
	public void doService(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		List<FileItem> fileItems = new ArrayList<FileItem>();

		String loadpath = "C:/Temp/";
		
		RequestContext req = new ServletRequestContext(request);
		if (FileUpload.isMultipartContent(req)) {
			DiskFileItemFactory factory = new DiskFileItemFactory();
			// threshold 极限、临界值，即硬盘缓存 1M  
//			factory.setSizeThreshold(4 * 1024);  
            // repository 贮藏室，即临时文件目录  
//			factory.setRepository(new File(tempPath))
            
			// 定义文件上传参数等:设置可接收的文件的大小
			ServletFileUpload sfu = new ServletFileUpload(factory);
			sfu.setFileSizeMax(10 * 1024 * 1024); // 10M
			try {
				fileItems = sfu.parseRequest(request);
			} catch (FileUploadException e) {
				System.out.println("parseRequest error...");
				e.printStackTrace();
			}
		}

		System.out.println("fileItems size " + fileItems.size());
		
		// 依次处理每个上传的文件
		Iterator<FileItem> it = fileItems.iterator(); 
		while (it.hasNext()) {
			FileItem item = it.next(); // 忽略其他不是文件域的所有表单信息
			
			try {
				Iterator<String> headers = item.getHeaders().getHeaderNames();
				while (headers.hasNext()) {
					String head = headers.next();
					System.out.println(head + "==" + item.getHeaders().getHeader(head));
				}
			} catch (Exception e) {
				
			}
			
			
			// Form表单 或者 不是
			if (!item.isFormField()) {
//				item.getContentType()
//				ContentType == 参见: http://tool.oschina.net/commons
				// 获取上传文件名,包括客户端路径
				String name = item.getName();
				System.out.println("文件Key:" + item.getFieldName());
				System.out.println("文件名称:" + item.getName());
				System.out.println("文件大小:" + item.getSize());
				System.out.println("文件类型:" + item.getContentType());
				// TODO 需要解析成为本地的文件名或其他
				File fileNew = new File(loadpath, name);
				try {
					item.write(fileNew);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else{
				String name = item.getFieldName();  
		        String value = item.getString();          
		        System.out.println("字段:" + name + " : " + value); 
			}
			
		}
	}

}
