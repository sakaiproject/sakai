package org.sakaiproject.citation.impl.openurl;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class ByValueHttpTransport implements Transport {





	public RawContextObject parse(HttpServletRequest request) {
		Map<String, String[]> parameters = (Map<String, String[]>)request.getParameterMap();
		String version = Utils.getValue(parameters, URL_VER);
		if (version == null) {
			// Should reject this.
		} else {
			if (!version.equals(ContextObject.VERSION)) {
				// bad version, should reject, although being relaxed can be good.. 
			}
		}
		
		String timestamp = Utils.getValue(parameters, URL_TIM);
		if (timestamp != null) {
			// Should parse.
		}
		
		String format = Utils.getValue(parameters, URL_CTX_FMT);
		if (format == null) {
			// This should be fatal.
		}
		String data = Utils.getValue(parameters, URL_CTX_VAL);
		if (data == null) {
			// Fatal, don't continue.
		}
		
		RawContextObject raw = new RawContextObject(format, data);
		return raw;
	}
	
	public String encode(String data) {
		return null; //TODO
	}
}
