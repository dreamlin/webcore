package org.linjia.webcore.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class Rewrite {
	private volatile static Rewrite _MeObj;
	private List<RewriteData> rewriteList;

	public static Rewrite getRewrite() {
		return _MeObj;
	}

	public static void create(String rewriteXmlPath) {
		if (_MeObj == null) {
			synchronized (Rewrite.class) {
				if (_MeObj == null) {
					try {
						_MeObj = new Rewrite(rewriteXmlPath);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private Rewrite(String rewriteXmlPath) {
		this.rewriteList = new ArrayList<RewriteData>();
		SAXBuilder builder = new SAXBuilder();
		Document doc = null;
		try {
			doc = builder.build(new File(rewriteXmlPath));
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Element root = doc.getRootElement();
		List<Element> rewriteNodes = root.getChildren();
		for (Element item : rewriteNodes) {
			RewriteData rewriteData = new RewriteData();
			rewriteData.setRegexs(item.getAttribute("regexs").getValue());
			rewriteData.setTurnUrl(item.getAttribute("turnUrl").getValue());
			this.rewriteList.add(rewriteData);
		}
	}

	public String getRewriteUrl(String url) {
		for (RewriteData rewriteData : this.rewriteList) {
			Pattern p = Pattern.compile(rewriteData.getRegexs());
			Matcher m = p.matcher(url);
			// 默认先用正则查找参数，如果查找到相应的值，就替换TurnUrl后返回。
			if (m.find()) {
				String turnUrl = rewriteData.getTurnUrl() + "";
				int ci = m.groupCount() + 1;
				for (int i = 1; i < ci; i++) {
					turnUrl = turnUrl.replace("$" + i, m.group(i));
				}
				return turnUrl;
			} else {
				// 判断字段串是否相等，如果相等，直接把TurnUrl返回。
				if (rewriteData.getRegexs().compareTo(url) == 0) {
					return rewriteData.getTurnUrl();
				} else {
					continue;
				}
			}
		}
		return null;
	}
}
