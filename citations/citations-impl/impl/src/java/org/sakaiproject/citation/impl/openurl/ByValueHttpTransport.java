/**
 * Copyright (c) 2003-2011 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
