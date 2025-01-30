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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.Basic;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import static javax.persistence.FetchType.LAZY;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public class BaseLTI implements Serializable {

	public static final int LENGTH_GUID = 36;
	public static final int LENGTH_URI = 500;
	public static final int LENGTH_EMAIL = 255;
	public static final int LENGTH_TITLE = 500;
	public static final int LENGTH_EXTERNAL_ID = 200;
	public static final int LENGTH_MEDIUMTEXT = 4000;  // Less than 4096 because Oracle
	public static final int LENGTH_SAKAI_ID = 99;

	@Column(name = "UPDATED_AT", nullable = true)
	private Instant updatedAt;

	@Column(name = "SENT_AT", nullable = true)
	private Instant sentAt;

	@Column(name = "SUCCESS")
	private Boolean success = Boolean.TRUE;

	@Column(name = "STATUS", length=200, nullable = true)
	private String status;

	@Basic(fetch=LAZY)
	@Lob
	@Column(name = "DEBUG_LOG")
	private String debugLog;

	@Column(name = "CREATED_AT", nullable = true)
	private Instant createdAt;

	@Column(name = "MODIFIER", length = LENGTH_SAKAI_ID)
	private String modifier;

	@Column(name = "MODIFIED_AT")
	private Instant modifiedAt;

	@Column(name = "DELETED")
	private Boolean deleted = Boolean.FALSE;

	@Column(name = "DELETOR", length = LENGTH_SAKAI_ID)
	private String deletor;

	@Column(name = "DELETED_AT")
	private Instant deletedAt;

	@Column(name = "LOGIN_COUNT")
	private Integer loginCount;

	@Column(name = "LOGIN_IP", length=64)
	private String loginIp;

	@Column(name = "LOGIN_USER", length = LENGTH_SAKAI_ID)
	private String loginUser;

	@Column(name = "LOGIN_AT")
	private Instant loginAt;

	@Basic(fetch=LAZY)
	@Lob
	@Column(name = "JSON")
	private String json;

	@PrePersist
	@PreUpdate
	public void updateDates() {
		if ( createdAt == null ) createdAt = Instant.now();
		modifiedAt = Instant.now();
	}

}
