package org.linjia.webcore.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 包装了 HttpRequest 内容，目的是 Action 处理程序与 Http 或者说 Web 环境解耦
 */
public class RequestContext {

	private String uploadTempDirPath; // 上传文件临时保存路径
	private String uploadSavedDirPath; // 上传文件保存路径
	private Map<String, ParamData> paramDataMap; // 参数
	private Map<String, PostedFile> postedFileMap; // 文件
	private Map<String, Object> attributeMap; // forward时传参用
	private Map<String, Cookie> cookieMap;
	private HttpServletRequest request;

	public RequestContext() {
		this.paramDataMap = new HashMap<String, ParamData>();
		this.postedFileMap = new HashMap<String, PostedFile>();
		this.attributeMap = new HashMap<String, Object>();
	}

	public Map<String, Cookie> getCookieMap() {
		return cookieMap;
	}

	public String getCookie(String key) {
		if (this.cookieMap.containsKey(key))
			this.cookieMap.get(key).getValue();
		return null;
	}

	public void setCookieMap(Map<String, Cookie> cookieMap) {
		this.cookieMap = cookieMap;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public String getGlobalTicket() {
		Object globalTicketObj = this.request.getAttribute("_GlobalTicket_");
		if (globalTicketObj != null)
			return (String) globalTicketObj;
		return "";
	}

	public HttpSession getSession() {
		return this.request.getSession();
	}

	public void addParamData(ParamData paramData) {
		this.paramDataMap.put(paramData.getParamName(), paramData);
	}

	public void addPostedFile(PostedFile postedFile) {
		this.postedFileMap.put(postedFile.getFieldName(), postedFile);
	}

	public Map<String, ParamData> getParamDataMap() {
		return paramDataMap;
	}

	public Collection<ParamData> getParamDatas() {
		return this.paramDataMap.values();
	}

	public String getParamData(String key) {
		if (this.paramDataMap.containsKey(key))
			return this.paramDataMap.get(key).getParamValue();
		else
			return "";
	}

	public Collection<PostedFile> getPostedFiles() {
		return this.postedFileMap.values();
	}

	/**
	 * Web 站点应用的根目录
	 * 
	 * @return
	 */
	public String getRootDirPath() {
		return request.getRealPath("/");
	}

	public void setUploadSavedDirPath(String uploadSavedDirPath) {
		this.uploadSavedDirPath = uploadSavedDirPath;
	}

	public String getUploadSavedDirPath() {
		return uploadSavedDirPath;
	}

	public void setUploadTempDirPath(String uploadTempDirPath) {
		this.uploadTempDirPath = uploadTempDirPath;
	}

	public String getUploadTempDirPath() {
		return uploadTempDirPath;
	}

	public void addAttribute(String name, Object value) {
		this.attributeMap.put(name, value);
	}

	public Map<String, Object> getAttributeMap() {
		return this.attributeMap;
	}

}
