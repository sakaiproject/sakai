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
 * This isn't part of the the OpenURL spec but it's the best way to think of the COinS spec.
 * Currently this isn't used.
 * @see <a href="http://ocoins.info/">http://ocoins.info/</a>
 * @author buckett
 *
 */
public class COinSTransport implements Transport {

	public RawContextObject parse(HttpServletRequest request) {
		// This isn't supported as normally COinS come out of a HTML page.
		return null;
	}

	public String encode(String data) {
		return "<span class=\"Z3988\" title=\""+ data+ "\"></span>";
	}

}
