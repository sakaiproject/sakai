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

import java.util.Map;
import java.util.HashMap;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.UniqueConstraint;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.CascadeType;

import org.hibernate.annotations.GenericGenerator;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;

/*
 *  The logical key for this table is is either (tenant, email) or (tenant, subject)
 *  If we are trusting email, and we see a new subject for (tenant, email) we update new subject
 *  If we are trusting subject and see a new email for (tenant, subject) we update the email
 */
@Entity
@Table(name = "PLUS_SUBJECT",
  indexes = @Index(columnList = "SUBJECT, TENNANT_GUID, SAKAI_USER_ID, EMAIL")
)
@Getter
@Setter
public class Subject extends BaseLTI implements PersistableEntity<String> {

	@Id
	@Column(name = "SUBJECT_GUID", length = LENGTH_GUID, nullable = false)
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	private String id;

	@Column(name = "SAKAI_USER_ID", length = LENGTH_SAKAI_ID, nullable = true)
	private String sakaiUserId;

	@Column(name = "SUBJECT", length = LENGTH_URI, nullable = false)
	private String subject;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TENNANT_GUID", nullable = false)
	private Tenant tenant;

	@Column(name = "DISPLAYNAME", length = LENGTH_TITLE, nullable = true)
	private String displayName;

	@Column(name = "EMAIL", length = LENGTH_TITLE, nullable = true)
	private String email;

	@Column(name = "LOCALE", length = LENGTH_TITLE, nullable = true)
	private String locale;
}
