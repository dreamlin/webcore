package org.linjia.webcore.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.linjia.webcore.model.ParamData;
import org.linjia.webcore.model.PostedFile;
import org.linjia.webcore.model.RequestContext;
import org.linjia.webcore.model.ValidateError;
import org.linjia.webcore.pi.IHttpAction;
import org.linjia.webcore.pi.IHttpBean;

public class WebCoreUtil {

	public static void injectHttpBean(IHttpAction action, IHttpBean bean) {
		if (bean != null) {
			Class<?> clazz = action.getClass();
			try {
				Field beanField = clazz.getDeclaredField("httpBean");
				beanField.setAccessible(true);
				beanField.set(action, bean);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * 查找HttpBean
	 * @param action
	 * @return
	 */
	public static IHttpBean findHttpBean(IHttpAction action) {
		Class<?> clazz = action.getClass();
		IHttpBean bean = null;
		try {
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				if (field.getName().compareTo("httpBean") == 0) {
					Class beanClazz = field.getType();
					bean = (IHttpBean) beanClazz.newInstance();
					break;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return bean;
	}

	/**
	 * 根据包前缀和请求路径 生成一个相应的 HttpAction 处理类
	 * 
	 * @param prefix
	 * @param appName
	 * @param rawPath
	 * @return
	 */
	public static IHttpAction createHttpAction(String prefix, String appName,
			String rawPath) {
		int pos = rawPath.indexOf(".action");
		String actionPath = rawPath.substring(0, pos);
		int appNameLength = appName.length();
		String baseName = actionPath.substring(appNameLength);
		String className = prefix + "." + baseName + "Action";

		return createAciton(className);
	}

	/**
	 * 创建一个处理 404 的HttpAction , 名称必须为 prefix.FourZeroFourAction
	 * 
	 * @param prefix
	 * @return
	 */
	public static IHttpAction create404Action(String prefix) {
		String className = prefix + ".FourZeroFourAction";
		return createAciton(className);
	}

	/**
	 * 创建HttpAction
	 * @param className
	 * @return
	 */
	private static IHttpAction createAciton(String className) {
		IHttpAction action = null;
		try {
			Class<?> clazz = Class.forName(className);
			action = (IHttpAction) clazz.newInstance();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return action;
	}

	/**
	 * 包装 HttpServletRequest
	 * 
	 * @param request
	 * @param requestCxt
	 */
	public static void wrapHttpRequest(HttpServletRequest request,
			RequestContext requestCxt) {
		String method = request.getMethod().toUpperCase();
		if (method.compareTo("GET") == 0)
			wrapHttpRequestForGet(request, requestCxt);

		if (method.compareTo("POST") == 0)
			wrapHttpRequestForPost(request, requestCxt);
	}

	private static void wrapHttpRequestForGet(HttpServletRequest request,
			RequestContext requestCxt) {
		List<ParamData> paramDatas = new ArrayList<ParamData>();
		Enumeration<String> paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			// 参数名
			String paramName = (String) paramNames.nextElement();
			String paramValue = deCode(request.getParameter(paramName));
			String paramKey = paramName.toLowerCase();
			ParamData paramData = new ParamData();
			paramData.setParamKey(paramKey);
			paramData.setParamName(paramName);
			paramData.setParamValue(paramValue);
			paramDatas.add(paramData);
		}

		for (ParamData paramData : paramDatas) {
			requestCxt.addParamData(paramData);
		}
	}

	private static void wrapHttpRequestForPost(HttpServletRequest request,
			RequestContext requestCxt) {
		String contentType = request.getHeader("content-type");
		if (contentType.startsWith("multipart/form-data"))
			wrapHttpRequestPostMultiPart(request, requestCxt);

		if (contentType.startsWith("application/x-www-form-urlencoded"))
			wrapHttpRequestPostUrlEncoded(request, requestCxt);
	}

	/**
	 * 把上文件保存到临时文件夹中
	 * @param request
	 * @param requestCxt
	 */
	private static void wrapHttpRequestPostMultiPart(HttpServletRequest request,
			RequestContext requestCxt) {
		List<PostedFile> postedFiles = new ArrayList<PostedFile>();
		List<ParamData> paramDatas = new ArrayList<ParamData>();

		// 磁盘文件条目工厂
		DiskFileItemFactory factory = new DiskFileItemFactory();
		// 文件上传如果文件小,上传组件可以将文件存放在内存中,
		// 如果过大时会放在临时目录里面,之后再通过IO流操作

		// 设置临时目录
		factory.setRepository(new File(requestCxt.getUploadTempDirPath()));

		// 设置上传文件大小
		factory.setSizeThreshold(1024 * 1024);

		// 创建一个ServletFileUpload 实例
		ServletFileUpload sfu = new ServletFileUpload(factory);

		try {
			// 解析请求,取得FileItem 列表
			List<FileItem> lis = sfu.parseRequest(request);
			// 循环遍历
			for (FileItem item : lis) {
				// 判断是否是简单的表单字段
				if (item.isFormField()) {
					String paramName = item.getFieldName();
					String paramValue = item.getString("UTF-8");
					String paramKey = paramName.toLowerCase();

					ParamData paramData = new ParamData();
					paramData.setParamKey(paramKey);
					paramData.setParamName(paramName);
					paramData.setParamValue(paramValue);
					paramDatas.add(paramData);
				} else {
					long size = item.getSize();
					if (size == 0)
						continue;

					// 取得字段名
					String fieldName = item.getFieldName();
					String filePath = item.getName();
					String fileName = FilenameUtils.getName(filePath);
					if (filePath != null) {

						String fileContentType = item.getContentType();
						String extName = org.apache.commons.io.FilenameUtils
								.getExtension(filePath);
						String uuid = UUID.randomUUID().toString();
						String savedPath = requestCxt.getUploadSavedDirPath()
								+ "\\" + uuid + "." + extName;
						item.write(new File(savedPath));

						PostedFile postedFile = new PostedFile();
						postedFile.setFieldName(fieldName);
						postedFile.setFileName(fileName);
						postedFile.setContentType(fileContentType);
						postedFile.setSavedPath(savedPath);
						postedFile.setSize((int) item.getSize());
						postedFile.setExtName(extName);
						postedFiles.add(postedFile);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		for (ParamData paramData : paramDatas) {
			requestCxt.addParamData(paramData);
		}

		for (PostedFile postedFile : postedFiles) {
			requestCxt.addPostedFile(postedFile);
		}
	}

	private static String joinStringArray(String[] strs) {
		StringBuffer sb = new StringBuffer();
		for (String str : strs) {
			if (sb.length() > 0)
				sb.append(",");
			sb.append(str);
		}
		return sb.toString();
	}

	private static void wrapHttpRequestPostUrlEncoded(
			HttpServletRequest request, RequestContext requestCxt) {
		List<ParamData> paramDatas = new ArrayList<ParamData>();

		Enumeration<String> paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			// 参数名
			String paramName = (String) paramNames.nextElement();
			String[] paramValues = request.getParameterValues(paramName);
			String paramValue = joinStringArray(paramValues);

			String paramKey = paramName.toLowerCase();
			ParamData paramData = new ParamData();
			paramData.setParamKey(paramKey);
			paramData.setParamName(paramName);
			paramData.setParamValue(paramValue);
			paramDatas.add(paramData);
		}
		for (ParamData paramData : paramDatas) {
			requestCxt.addParamData(paramData);
		}
	}

	public static String mapToUrlString(Map<String, String> map) {
		StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			if (sb.length() != 0)
				sb.append("&");
			sb.append(entry.getKey());
			sb.append("=");
			sb.append(urlEncode(entry.getValue()));
		}
		return sb.toString();
	}

	public static void urlStringToMap(String urlString, Map<String, String> map) {

		String[] items = urlString.split("&");
		for (String item : items) {
			String[] pair = item.split("=");
			String key = pair[0];
			String txt = pair[1];
			String value = urlDecode(txt);
			map.put(key, value);
		}
	}

	/**
	 * 把获取的参数注入到HttpBean中
	 * 
	 * @param bean
	 * @param validateError
	 */
	public static void fillHttpBean(IHttpBean bean, ValidateError validateError,
			Map<String, ParamData> paramDataMap) {
		Method[] methods = bean.getClass().getMethods();
		for (Method method : methods) {
			String methodName = method.getName();
			if (methodName.startsWith("set") == false) {
				continue;
			}

			String fieldName = methodName.substring(3);
			String key = fieldName.toLowerCase();
			if (paramDataMap.containsKey(key) == false)
				continue;

			String value = paramDataMap.get(key).getParamValue();
			try {
				if (fieldName.compareTo("Width") == 0) {
					method.invoke(bean, Integer.parseInt(value));
					continue;
				}
				if (fieldName.compareTo("Height") == 0) {
					method.invoke(bean, Integer.parseInt(value));
					continue;
				}
				method.invoke(bean, value);
			} catch (Exception ex) {
				ex.printStackTrace();
				validateError.setAttrName(fieldName);
				validateError.setAttrName(value);
				validateError.setErrorMsg("数据类型错误");
				validateError.setErrorState(true);
			}
		}
	}
	
	public static boolean isNullOrEmptyString(String str) {
		if (str == null)
			return true;
		if (str.trim().isEmpty())
			return true;
		return false;
	}

	public static String urlEncode(String strIn) {
		String strOut = null;
		try {
			strOut = java.net.URLEncoder.encode(strIn, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return strOut;
	}

	public static String urlDecode(String strIn) {
		String strOut = null;
		try {
			strOut = java.net.URLDecoder.decode(strIn, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return strOut;
	}

	public static String deCode(String keyWord) {
		String flagStr = null;
		try {
			flagStr = new String(keyWord.getBytes("ISO-8859-1"), "UTF-8");
			String str = flagStr.replaceAll("[\\p{ASCII}]", "");
			if (str.getBytes().length % 3 != 0 || flagStr.contains("�")) {
				flagStr = new String(keyWord.getBytes("ISO-8859-1"), "GBK");
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return flagStr;
	}
}
