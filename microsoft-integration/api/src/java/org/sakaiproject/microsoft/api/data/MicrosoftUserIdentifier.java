/**
 * Copyright (c) 2024 The Apereo Foundation
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
package org.sakaiproject.microsoft.api.data;

public enum MicrosoftUserIdentifier {
	USER_ID("userId"),
	EMAIL("email");
	
	private static final String PREFIX_MAPPING = "MAP:";
	public static final String KEY = PREFIX_MAPPING + "MICROSOFT_USER_ID_MAP";
	
	private String code;
	
	private MicrosoftUserIdentifier(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return this.code;
	}
	
	public static MicrosoftUserIdentifier fromString(String text) {
		for (MicrosoftUserIdentifier v : MicrosoftUserIdentifier.values()) {
			if (v.code.equalsIgnoreCase(text)) {
				return v;
			}
		}
		return EMAIL;
	}
}
