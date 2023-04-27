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
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.UniqueConstraint;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "PLUS_CONTEXT",
  indexes = { @Index(columnList = "CONTEXT, TENANT_GUID, SAKAI_SITE_ID") },
  uniqueConstraints = { @UniqueConstraint(columnNames = { "CONTEXT", "TENANT_GUID" }) }
)
@Data
public class Context extends BaseLTI implements PersistableEntity<String> {

	@Id
	@Column(name = "CONTEXT_GUID", length = LENGTH_GUID, nullable = false)
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	private String id;

	// Controlling LMS context ID (within Tenant)
	@Column(name = "CONTEXT", length = BaseLTI.LENGTH_EXTERNAL_ID, nullable = false)
	private String context;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TENANT_GUID", nullable = false)
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	private Tenant tenant;

	// For many-deployment systems like Canvas, we can have as many as one
	// deployment_id *per context* - If present - this is preferred for use
	// in out-going token requests
	@Column(name = "DEPLOYMENT_ID", length = LENGTH_EXTERNAL_ID, nullable = true)
	private String deploymentId;

	@Column(name = "SAKAI_SITE_ID", length = LENGTH_SAKAI_ID, nullable = true)
	private String sakaiSiteId;

	@Column(name = "TITLE", length = LENGTH_TITLE, nullable = true)
	private String title;

	@Column(name = "LABEL", length = LENGTH_TITLE, nullable = true)
	private String label;

	// launchjwt.endpoint.lineitems
	@Column(name = "LINEITEMS", length = LENGTH_URI, nullable = true)
	private String lineItems;

	@Column(name = "LINEITEMS_TOKEN", length = LENGTH_URI, nullable = true)
	private String lineItemsToken;

	@Column(name = "GRADE_TOKEN", length = LENGTH_URI, nullable = true)
	private String gradeToken;

	// launchjwt.names_and_roles.context_memberships_url
	@Column(name = "CONTEXT_MEMBERSHIPS", length = LENGTH_URI, nullable = true)
	private String contextMemberships;

	@Column(name = "NRPS_TOKEN", length = LENGTH_URI, nullable = true)
	private String nrpsToken;

	@Column(name = "NRPS_JOB_START", nullable = true)
	private Instant nrpsStart;

	@Column(name = "NRPS_JOB_FINISH", nullable = true)
	private Instant nrpsFinish;

	@Column(name = "NRPS_JOB_STATUS", length = LENGTH_TITLE, nullable = true)
	private String nrpsStatus;

	@Column(name = "NRPS_JOB_COUNT", nullable = true)
	private Long nrpsCount;

}
