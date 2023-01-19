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
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Basic;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import static javax.persistence.FetchType.LAZY;

import org.springframework.data.annotation.CreatedDate;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "PLUS_CONTEXT_LOG")
@Data
// @ToString(exclude = {"context", "subject", "debugLog" })
// @EqualsAndHashCode(exclude = {"context", "subject", "debugLog" })
public class ContextLog implements PersistableEntity<Long> {

	// These enums *names* must match the values in the spec as they are matched with strings at times
	public enum LOG_TYPE {
		NRPS_TOKEN, NRPS_LIST, NRPS_MEMBER, NRPS_ERROR,
		LineItem_TOKEN, LineItem_CREATE, LineItem_ERROR,
		Score_TOKEN, Score_SEND, Score_ERROR,
		LineItem_UPDATE
		// Add at the end - don't insert new above
	};

	@Id @GeneratedValue
	@Column(name = "CONTEXT_LOG_ID")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CONTEXT_GUID", nullable = false)
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	private Context context;

	@ManyToOne(fetch = FetchType.LAZY)
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@JoinColumn(name = "SUBJECT_GUID", nullable = true)
	private Subject subject;

	@Column(name = "LOG_TYPE", nullable = true)
	@Enumerated(EnumType.ORDINAL)
	private LOG_TYPE type;

	@Column(name = "SUCCESS")
	private Boolean success = Boolean.TRUE;

	@Column(name = "HTTP_RESPONSE", nullable = true)
	private Integer httpResponse;

	@Column(name = "STATUS", length=200, nullable = true)
	private String status;

	@Column(name = "COUNT", nullable = true)
	private Long count = 0l;

	@Column(name = "ACTION", length=2000, nullable = true)
	private String action;

	@CreatedDate
	@Column(name = "CREATED_AT", nullable = true)
	private Instant createdAt = Instant.now();

	@Basic(fetch=LAZY)
	@Lob
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@Column(name = "DEBUG_LOG")
	private String debugLog;

	public int getPositiveHashCode() { return java.lang.Math.abs(this.hashCode()); }
}
