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

public enum SynchronizationStatus {
	OK(2),
	PARTIAL_OK(1), //site-team is OK, but some group-channel are KO
	KO(0),
	NONE(-1),
	ERROR(-2),
	ERROR_GUEST(-3),
	NOT_AVAILABLE(-4);
	
	private Integer code;
	
	private SynchronizationStatus(Integer code) {
		this.code = code;
	}
	
	public Integer getCode() {
		return this.code;
	}
	
	public static SynchronizationStatus fromCode(Integer code) {
		for (SynchronizationStatus v : SynchronizationStatus.values()) {
			if (v.code == code) {
				return v;
			}
		}
		return null;
	}
}
