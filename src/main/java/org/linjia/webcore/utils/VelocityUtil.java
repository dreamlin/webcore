package org.linjia.webcore.utils;

import java.io.StringWriter;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

public class VelocityUtil {
	private VelocityContext	context		= null;
	private Template		template	= null;

	public VelocityUtil(String vmFile) {
		template = VelocityEngineInit.getEngine().getTemplate(vmFile);
		context = new VelocityContext();
	}

	public void put(String key, Object value) {
		context.put(key, value);
	}

	public String toHtml() {
		StringWriter sw = new StringWriter();
		try {
			template.merge(context, sw);
		} catch (Exception e) {
		}
		return sw.toString();
	}
}