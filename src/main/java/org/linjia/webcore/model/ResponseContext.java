package org.linjia.webcore.model;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;


public class ResponseContext {

	private String htmlContent;
	private String outType; // 输出类型
	private String sendRedirectUrl; // 浏览器端重定向
	private String forwardUrl; // 服务器端转发请求
	private String extName; // 扩展名
	private DownFileInfo downFileInfo; // 下载文件
	private Map<String, Cookie> writeCookieMap;

	public ResponseContext() {
		this.writeCookieMap = new HashMap<String, Cookie>();
	}

	public Map<String, Cookie> getWriteCookieMap() {
		return this.writeCookieMap;
	}

	public Cookie getWriteCookie(String cookieName) {
		return this.writeCookieMap.get(cookieName);
	}

	public void setWriteCookie(Cookie cookie) {
		this.writeCookieMap.put(cookie.getName(), cookie);
	}

	public void setHtmlContent(String htmlContent) {
		this.outType = "outhtml";
		this.htmlContent = htmlContent;
	}

	public String getHtmlContent() {
		return htmlContent;
	}

	public String getOutType() {
		return outType;
	}

	public void setSendRedirectUrl(String sendRedirectUrl) {
		this.outType = "redirect";
		this.sendRedirectUrl = sendRedirectUrl;
	}

	public String getSendRedirectUrl() {
		return sendRedirectUrl;
	}

	public void setForwardUrl(String forwardUrl) {
		this.outType = "forward";
		this.forwardUrl = forwardUrl;
	}

	public String getForwardUrl() {
		return forwardUrl;
	}

	public String getExtName() {
		return extName;
	}

	public void setDownFileInfo(DownFileInfo downFileInfo) {
		this.outType = "filedown";
		this.downFileInfo = downFileInfo;
	}

	public DownFileInfo getDownFileInfo() {
		return downFileInfo;
	}
}
