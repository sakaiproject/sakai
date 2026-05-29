/**
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
 */
package org.sakaiproject.lti13.util;

import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JWT payload for Sakai-issued access tokens (SAT).
 */
@SuppressWarnings("deprecation")
public class SakaiAccessToken extends org.tsugi.lti13.objects.BaseJWT {

	public static final String SCOPE_SCORE = "sakai.ims.ags.score";
	public static final String SCOPE_RESULT_READONLY = "sakai.ims.ags.result.readonly";
	public static final String SCOPE_LINEITEMS_READONLY = "sakai.ims.ags.lineitems.readonly";
	public static final String SCOPE_LINEITEMS = "sakai.ims.ags.lineitems";
	public static final String SCOPE_ROSTER = "sakai.ims.membership";
	public static final String SCOPE_CONTEXTGROUP_READONLY = "sakai.ims.contextgroup.readonly";

	/** Prefix for Sakai platform API scopes (webapi, /direct). */
	public static final String SCOPE_SAKAI_PREFIX = "sakai.";

	/** Prefix for LTI-authorized Sakai API scopes requested at the token endpoint. */
	public static final String SCOPE_LTI_API_PREFIX = "sakai.lti.api.";

	@JsonProperty("scope")
	public String scope;

	@JsonProperty("tool_id")
	public Long tool_id;

	@JsonProperty("site_id")
	public String site_id;

	public void addScope(String newScope) {
		if (newScope == null || newScope.isEmpty()) {
			return;
		}
		Set<String> scopes = getScopeTokens();
		if (scopes.add(newScope)) {
			this.scope = String.join(" ", scopes);
		}
	}

	public boolean hasScope(String scope) {
		if (scope == null || this.scope == null) {
			return false;
		}
		return getScopeTokens().contains(scope);
	}

	private Set<String> getScopeTokens() {
		Set<String> tokens = new LinkedHashSet<>();
		if (this.scope != null) {
			for (String token : this.scope.trim().split("\\s+")) {
				if (!token.isEmpty()) {
					tokens.add(token);
				}
			}
		}
		return tokens;
	}

	/** OAuth/SAT scope for an LTI API function (e.g. {@code content.read} → {@code sakai.lti.api.content.read}). */
	public static String functionToLtiApiScope(String functionName) {
		return SCOPE_LTI_API_PREFIX + functionName;
	}

	/** Function name from an LTI API scope, or null if {@code scope} is not under {@link #SCOPE_LTI_API_PREFIX}. */
	public static String ltiApiScopeToFunction(String scope) {
		if (scope == null || !scope.startsWith(SCOPE_LTI_API_PREFIX)) {
			return null;
		}
		String function = scope.substring(SCOPE_LTI_API_PREFIX.length());
		return function.length() > 0 ? function : null;
	}
}
