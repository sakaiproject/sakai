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


/**
 * A holder for a contextobject which has yet to be parsed into entities.
 * It might have been parsed into key/values.
 * @author buckett
 *
 */
public class RawContextObject {

	private String format;
	private String data;
	
	public RawContextObject(String format, String data) {
		this.format = format;
		this.data = data;
	}

	public String getFormat() {
		return format;
	}
	public String getData() {
		return data;
	}
}
