package org.linjia.webcore.pi;

import org.linjia.webcore.model.RequestContext;
import org.linjia.webcore.model.ResponseContext;

public interface IHttpAction {
	void doProcess(RequestContext requestCxt, ResponseContext responseCxt);
}
