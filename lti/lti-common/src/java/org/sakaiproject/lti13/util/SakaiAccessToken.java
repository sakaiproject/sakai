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


import java.util.LinkedHashSet;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("deprecation")
@Slf4j
public class SakaiAccessToken extends  org.tsugi.lti13.objects.BaseJWT {

	/**
	 * Allows the tool to send a score
	 */
	public static final String SCOPE_SCORE = "sakai.ims.ags.score";

	/**
	 * Allows the tool to send a score
	 */
	public static final String SCOPE_RESULT_READONLY = "sakai.ims.ags.result.readonly";

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

	/**
	 *  Allows the tool to read the course groups (AGS)
	 */
	public static final String SCOPE_CONTEXTGROUP_READONLY = "sakai.ims.contextgroup.readonly";

	@JsonProperty("scope")
	public String scope;
	@JsonProperty("tool_id")
	public Long tool_id;

	public static Set<String> parseScopes(String scopes) {
		LinkedHashSet<String> scopeSet = new LinkedHashSet<String>();
		if (scopes == null) {
			return scopeSet;
		}

		String trimmedScopes = scopes.trim();
		if (trimmedScopes.length() < 1) {
			return scopeSet;
		}

		for (String scope : trimmedScopes.split("\\s+")) {
			scopeSet.add(scope);
		}
		return scopeSet;
	}

	public void addScope(String newScope) {
		if ( this.scope == null || this.scope.trim().length() < 1 ) {
			this.scope = newScope;
			return;
		}
		if ( parseScopes(this.scope).contains(newScope)) return;
		this.scope += " " + newScope;
	}

	public boolean hasScope(String scope) {
		if ( this.scope == null || scope == null ) return false;
		return parseScopes(this.scope).contains(scope);
	}

}
