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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.KeyFactory;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

/**
 * Need some basic parsing of data.
 * @author buckett
 */
public class InlineHttpTransport implements Transport {


	@SuppressWarnings("unchecked")
	public RawContextObject parse(HttpServletRequest request) {
		Map<String, String[]> parameters;
		String source = null;
		if ("GET".equals(request.getMethod())) {
			// Already decoded
			parameters = (Map<String, String[]>)request.getParameterMap();
			source = request.getQueryString(); // Undecoded incase encoding is in params
		} else if ("POST".equals(request.getMethod())) {
			source = readBody(request);
			parameters = Utils.decode(Utils.split(source), "UTF-8");
		} else {
			throw new IllegalArgumentException("Only POST and GET supported.");
		}
		
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
			// Format: ISO8601-conformant datetime, in the YYYY-MM-DD or YYYY-MM-DDTHH:MM:SSZ representation
			// If time is present it is in UTC.
		}
		
		String format = Utils.getValue(parameters, URL_CTX_FMT);
		if (format != null) {
			if (!KEVFormat.FORMAT_ID.equals(format)) {
				// Only support KEV in inline.
			}
		}
		RawContextObject raw = new RawContextObject(KEVFormat.FORMAT_ID, source);
		return raw;
	}
	
	public String encode(String content) {
		URLBuilder url = new URLBuilder("UTF-8");
		return url.append(URL_VER, ContextObject.VERSION)
				.append(URL_TIM, currentTime(DateTimeType.DATE))
				.append(URL_CTX_FMT, KEVFormat.FORMAT_ID)
				.append(content)
				.toString();
	}

	// Format: ISO8601-conformant datetime, in the YYYY-MM-DD or YYYY-MM-DDTHH:MM:SSZ representation
	public enum DateTimeType {
		DATE("yyyy-MM-dd"), DATETIME("yyyy-MM-ddTHH:mm:ssZ");
		
		private String format;
		DateTimeType(String format) {
			this.format = format;
		}
		
		public SimpleDateFormat getFormat() {
			// SimpleDAteFormat isn't threadsafe so create a new one each time.
			return new SimpleDateFormat(format);
		}
	};
	public String currentTime(DateTimeType type) {
		// Current time must be in UTC.
		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		return type.getFormat().format(cal.getTime());
	}
	
	private String readBody(HttpServletRequest request) {
		try {
			BufferedReader reader = request.getReader();
			String line = null;
			StringBuilder lines = new StringBuilder();
			while((line = reader.readLine()) != null) {
				lines.append(line);
			}
			return lines.toString();
		} catch (IOException e) {
			throw new IllegalStateException();
		}
	}

}
