/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2018- Charles R. Severance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */
package org.sakaiproject.lti13.util;


import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("deprecation")
@Slf4j
public class SakaiAccessToken extends  org.tsugi.lti13.objects.BaseJWT {

	/**
	 * Allows the tool to use basic outcomes (one grade per resource)
	 */
	public static final String SCOPE_BASICOUTCOME = "sakai.ims.ags.basicoutcome";

	/**
	 * Allows the tool to list their own grade book columns (lineitems)
	 */
	public static final String SCOPE_LINEITEMS_READONLY = "sakai.ims.ags.lineitems.readonly";

	/**
	 * Allows the tool to create and manage their own grade book columns (lineitems)
	 */
	public static final String SCOPE_LINEITEMS = "sakai.ims.ags.lineitems";

	/**
	 *  Allows the tool to read the membership roster (AGS)
	 */
	public static final String SCOPE_ROSTER = "sakai.ims.membership";

	@JsonProperty("scope")
	public String scope;
	@JsonProperty("tool_id")
	public Long tool_id;

	public void addScope(String newScope) {
		if ( this.scope == null ) {
			this.scope = newScope;
		}
		if ( this.scope.contains(newScope)) return;
		this.scope += " " + newScope;
	}

	public boolean hasScope(String scope) {
		if ( this.scope == null ) return false;
		return this.scope.contains(scope);
	}

}
