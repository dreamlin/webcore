package org.linjia.webcore;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.linjia.webcore.model.DownFileInfo;
import org.linjia.webcore.model.RequestContext;
import org.linjia.webcore.model.ResponseContext;
import org.linjia.webcore.model.ValidateError;
import org.linjia.webcore.pi.IHttpAction;
import org.linjia.webcore.pi.IHttpBean;
import org.linjia.webcore.utils.Rewrite;
import org.linjia.webcore.utils.VelocityEngineInit;
import org.linjia.webcore.utils.WebCoreUtil;

public class ActionDispatcherServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String actionPrefix; // 请求前缀
	private String uploadTempDirPath;
	private String uploadSavedDirPath;
	// 解决开发环境
	private String siteAppName = "/";

	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String uri = request.getRequestURI();
		String url = getUrl(uri);
		if (url != null) {
			request.getRequestDispatcher(url).forward(request, response);
			return;
		}

		request.setCharacterEncoding("UTF-8");
		RequestContext requestCxt = new RequestContext();
		ResponseContext responseCxt = new ResponseContext();
		requestCxt.setCookieMap(this.getCookieMap(request));
		requestCxt.setUploadTempDirPath(this.uploadTempDirPath);
		requestCxt.setUploadSavedDirPath(this.uploadSavedDirPath);
		requestCxt.setRequest(request);

		// 请求路径
		String rawPath = request.getRequestURI();

		// 根据请求路径 实例化相应的HttpAction
		IHttpAction httpAction = WebCoreUtil.createHttpAction(this.actionPrefix, this.siteAppName, rawPath);

		if (httpAction == null) {
			// "路径为：" + rawPath + "的HttpAction没有找到。"
			httpAction = WebCoreUtil.create404Action(this.actionPrefix);
		}

		WebCoreUtil.wrapHttpRequest(request, requestCxt);

		ValidateError validateError = new ValidateError();
		// 初始化要注入的 HttpBean
		IHttpBean httpBean = WebCoreUtil.findHttpBean(httpAction);
		if (httpBean != null) {
			WebCoreUtil.fillHttpBean(httpBean, validateError, requestCxt.getParamDataMap());
			// 处理错误
		}

		// 注入 HttpBean
		WebCoreUtil.injectHttpBean(httpAction, httpBean);

		httpAction.doProcess(requestCxt, responseCxt);

		Map<String, Cookie> writeCookieMap = responseCxt.getWriteCookieMap();
		if (writeCookieMap != null) {
			for (String str : writeCookieMap.keySet()) {
				response.addCookie(writeCookieMap.get(str));
			}
		}

		if (responseCxt.getOutType().compareTo("outhtml") == 0) {
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(responseCxt.getHtmlContent());
			return;
		}

		if (responseCxt.getOutType().compareTo("redirect") == 0) {
			response.sendRedirect(responseCxt.getSendRedirectUrl());
			return;
		}

		if (responseCxt.getOutType().compareTo("forward") == 0) {
			String forwardUrl = responseCxt.getForwardUrl();
			Map<String, Object> attrMap = requestCxt.getAttributeMap();
			for (Entry<String, Object> entry : attrMap.entrySet()) {
				request.setAttribute(entry.getKey(), entry.getValue());
			}
			request.getRequestDispatcher(forwardUrl).forward(request, response);
			return;
		}

		// 处理文件下载
		if (responseCxt.getOutType().compareTo("filedown") == 0) {
			DownFileInfo fileInfo = responseCxt.getDownFileInfo();
			response.reset();
			response.setContentType(fileInfo.getExtName());
			response.setHeader("Pragma", "No-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);

			// 下面我们将设法让客户端保存文件的时候显示正确的文件名, 具体就是将文件名
			// 转换为 ISO8859-1 编码
			String downFileName = new String(fileInfo.getFileName().getBytes(), "iso8859-1");
			// online open online用于页面展示
			// response.setHeader("Content-Disposition","inline; filename=test.pdf");
			// download attachment下载
			response.setHeader("Content-Disposition", "attachment;filename=" + downFileName);
			int length = fileInfo.getFileStream().available();
			// 设置下载内容大小
			response.setContentLength(length);

			byte[] buffer = new byte[4096]; // 缓冲区
			BufferedOutputStream output = null;
			BufferedInputStream input = null;

			try {
				output = new BufferedOutputStream(response.getOutputStream());
				input = new BufferedInputStream(fileInfo.getFileStream());
				int n = (-1);
				while ((n = input.read(buffer, 0, 4096)) > -1) {
					output.write(buffer, 0, n);
				}
				response.flushBuffer();
			} catch (Exception ex) {
				ex.printStackTrace();
			} // 用户可能取消了下载
			finally {
				if (input != null)
					input.close();
				if (output != null)
					output.close();
			}
			return;
		}
	}

	private Map<String, Cookie> getCookieMap(HttpServletRequest request) {
		Map<String, Cookie> cookieMap = new HashMap<String, Cookie>();
		Cookie[] cookies = request.getCookies();
		if (null != cookies) {
			for (Cookie cookie : cookies) {
				cookieMap.put(cookie.getName(), cookie);
			}
		}
		return cookieMap;
	}

	private String getUrl(String uri) {
		if (Rewrite.getRewrite() == null) {
			// "Rewrite初始化时没有被创建。"
			return null;
		}
		return Rewrite.getRewrite().getRewriteUrl(uri);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	}

	public void init() throws ServletException {
		VelocityEngineInit.create(this.getServletContext().getRealPath("/WEB-INF/templates"));

		String rewriteXmlPath = this.getServletContext().getRealPath("/WEB-INF/rewrite.xml");
		File xmlFile = new File(rewriteXmlPath);
		if (xmlFile.exists())
			Rewrite.create(rewriteXmlPath);

		this.actionPrefix = this.getInitParameter("ActionPrefix");

		this.uploadTempDirPath = this.getInitParameter("UploadTempDirPath");
		if (WebCoreUtil.isNullOrEmptyString(this.uploadTempDirPath)) {
			this.uploadTempDirPath = this.getServletContext().getRealPath("/uploadtemp");
		}
		File tempDir = new File(this.uploadTempDirPath);
		if (!tempDir.exists())
			tempDir.mkdir();

		this.uploadSavedDirPath = this.getInitParameter("UploadSavedDirPath");
		if (WebCoreUtil.isNullOrEmptyString(this.uploadSavedDirPath)) {
			this.uploadSavedDirPath = this.getServletContext().getRealPath("/uploadsaved");
		}

		File savedDir = new File(this.uploadSavedDirPath);
		if (!savedDir.exists())
			savedDir.mkdir();

		String appName = this.getInitParameter("SiteAppName");
		if (!WebCoreUtil.isNullOrEmptyString(appName)) {
			this.siteAppName = "/" + appName + "/";
		}
	}
}
