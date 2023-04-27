/*
 * Copyright (c) 2021- Charles R. Severance
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

package org.sakaiproject.plus.api.model;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.UniqueConstraint;
import javax.persistence.Table;
import javax.persistence.Basic;
import static javax.persistence.FetchType.LAZY;

import org.hibernate.annotations.GenericGenerator;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.StringUtils;

@Entity
@Table(name = "PLUS_TENANT",
  indexes = { @Index(columnList = "ISSUER, CLIENT_ID") },
  uniqueConstraints = { @UniqueConstraint(columnNames = { "ISSUER", "CLIENT_ID" }) }
)
@Getter
@Setter
public class Tenant extends BaseLTI implements PersistableEntity<String> {

	@Id
	@Column(name = "TENANT_GUID", length = LENGTH_GUID, nullable = false)
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	private String id;

	@Column(name = "TITLE", length = LENGTH_TITLE, nullable = false)
	private String title;

	@Column(name = "DESCRIPTION", length = LENGTH_MEDIUMTEXT, nullable = true)
	private String description;

	// Issuer and client_id can be null while a key is being built but a key is not usable
	// until both fields are defined and the other values are present
	@Column(name = "ISSUER", length = LENGTH_EXTERNAL_ID, nullable = true)
	protected String issuer;

	@Column(name = "CLIENT_ID", length = LENGTH_EXTERNAL_ID, nullable = true)
	private String clientId;

	// This *may* be the *required* deployment_id as part of a security contract,
	// But for Canvas, we can get many deployment_ids per clientId / issuer combination
	// This deployment_id is used for authorization, the deployment_id for access
	// token callbacks is stored in each context - thanks to Peter F. for this
	// observation.
	@Column(name = "DEPLOYMENT_ID", length = LENGTH_EXTERNAL_ID, nullable = true)
	private String deploymentId;

	// Default this to true - it is the most common approach
	@Column(name = "TRUST_EMAIL")
	private Boolean trustEmail = Boolean.TRUE;

	@Column(name = "TIMEZONE", length = 100, nullable = true)
	private String timeZone;

	@Column(name = "ALLOWED_TOOLS", length = 500, nullable = true)
	private String allowedTools;

	@Column(name = "NEW_WINDOW_TOOLS", length = 500, nullable = true)
	private String newWindowTools;

	@Column(name = "VERBOSE")
	private Boolean verbose = Boolean.FALSE;

	@Column(name = "SITE_TEMPLATE", length = LENGTH_SAKAI_ID, nullable = true)
	private String siteTemplate;

	@Column(name = "REALM_TEMPLATE", length = LENGTH_SAKAI_ID, nullable = true)
	private String realmTemplate;

	@Lob
	@Column(name = "INBOUND_ROLE_MAP", nullable = true)
	private String inboundRoleMap;

	@Column(name = "OIDC_AUTH", length = LENGTH_URI, nullable = true)
	private String oidcAuth;

	@Column(name = "OIDC_KEYSET", length = LENGTH_URI, nullable = true)
	private String oidcKeySet;

	@Column(name = "OIDC_TOKEN", length = LENGTH_URI, nullable = true)
	private String oidcToken;

	// This is usually optional except for D2L
	@Column(name = "OIDC_AUDIENCE", length = LENGTH_EXTERNAL_ID, nullable = true)
	private String oidcAudience;

	// https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/429
	// HTTP/1.1 429 Too Many Requests
	// Content-Type: text/html
	// Retry-After: Date: Wed, 21 Oct 2015 07:28:00 GMT
	// Retry-After: 3600
	@Column(name = "RETRY_AT", nullable = true)
	private Instant retryAt = null;

	@Lob
	@Column(name = "CACHE_KEYSET", nullable = true)
	private String cacheKeySet;

	// Need to unlock Dynamic registration
	@Column(name = "OIDC_REGISTRATION_LOCK", length = LENGTH_EXTERNAL_ID, nullable = true)
	private String oidcRegistrationLock;

	@Column(name = "OIDC_REGISTRATION_ENDPOINT", length = LENGTH_URI, nullable = true)
	private String oidcRegistrationEndpoint;

	@Basic(fetch=LAZY)
	@Lob
	@Column(name = "OIDC_REGISTRATION", nullable = true)
	private String oidcRegistration;

	public boolean isDraft()
	{
		if ( issuer == null || clientId == null ||
				oidcAuth == null || oidcKeySet == null || oidcToken == null ) return true;

		if ( issuer.length() < 1 || clientId.length() < 1 ||
				oidcAuth.length() < 1 ||
				oidcKeySet.length() < 1 || oidcToken.length() < 1 ) return true;
		return false;
	}

	/*
	 * Validate an incoming deployment_id against the tenant deployment_id
	 *
	 * For Canvas, they makes *lots* of deployment_id values for each clientId
	 * so the tenant deployment_id matching to incoming deployment_id values is
	 * not straighforward.  So we have a heuristic match here.
	 *
	 * The *actual* deployment_id for use on AccessToken calls is kept
	 * in the Context object rather than the Tenant object.  The tenant deployment_id is
	 * for authorization and the Context deployment_id is for callbacks.
	 *
	 */
	public boolean validateDeploymentId(String launchDeploymentId)
	{
		if ( deploymentId == null ) return true;
		if ( StringUtils.isEmpty(deploymentId) ) return true;
		if ( deploymentId.equals("*") ) return true;
		if ( deploymentId.equals(launchDeploymentId) ) return true;

		// Allow for did1,did2,did3 as the deployment_id in case we need that later...
		if ( deploymentId.contains(launchDeploymentId) ) return true;
		return false;
	}

}
