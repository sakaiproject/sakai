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

public enum SakaiUserIdentifier {
	USER_PROPERTY("property"),
	USER_EID("eid"),
	EMAIL("email");
	
	private static final String PREFIX_MAPPING = "MAP:";
	public static final String KEY = PREFIX_MAPPING + "SAKAI_USER_ID_MAP";
	
	public static final String USER_PROPERTY_KEY = "microsoft_mapped_id";
	
	private String code;
	
	private SakaiUserIdentifier(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return this.code;
	}
	
	public static SakaiUserIdentifier fromString(String text) {
		for (SakaiUserIdentifier v : SakaiUserIdentifier.values()) {
			if (v.code.equalsIgnoreCase(text)) {
				return v;
			}
		}
		return EMAIL;
	}
}
