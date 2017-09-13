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

import javax.servlet.http.HttpServletRequest;

/**
 * All the transports are at the HTTP level at the moment.
 * 
 * @author buckett
 *
 */
public interface Transport {

	public static final String URL_VER = "url_ver";
	public static final String URL_CTX_VAL = "url_ctx_val";
	public static final String URL_CTX_FMT = "url_ctx_fmt";
	public static final String URL_TIM = "url_tim";
	
	/**
	 * Finds the format and the data.
	 * @param request
	 */
	public RawContextObject parse(HttpServletRequest request);
	
	public String encode(String data);
}
