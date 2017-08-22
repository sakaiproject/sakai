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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Small class that builds up a URL.
 * It uses the builder pattern.
 * @author buckett
 *
 */
public class URLBuilder {

	private static final String SEPERATOR = "&";
	private static final String KEY_VALUE_SEP = "=";
	
	private StringBuilder url;
	private String encoding;
	
	public URLBuilder(String encoding) {
		this.encoding = encoding;
		this.url = new StringBuilder();
	}
	
	public URLBuilder append(String key, String value) {
		if (url.length() > 0) {
			this.url.append(SEPERATOR);
		}
		this.url.append(key);
		this.url.append(KEY_VALUE_SEP);
		try {
			this.url.append(URLEncoder.encode(value, encoding));
		} catch (UnsupportedEncodingException uee) {
			throw new RuntimeException(uee);
		}
		return this;
	}
	
	/**
	 * Append some existing data that has already been encoded and put together.
	 * @param data Such as "name=Matthew%20Buckett&amp;login=buckett"
	 * @return The URLBuilder.
	 */
	public URLBuilder append(String data) {
		if (url.length() > 0 && !data.startsWith(SEPERATOR)) {
			this.url.append(SEPERATOR);
		}
		this.url.append(data);
		return this;
	}
	
	public int length() {
		return url.length();
	}
	
	public String toString() {
		return url.toString();
	}
}
