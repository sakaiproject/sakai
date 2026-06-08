/**********************************************************************************
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.userauditservice.api.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Data;

@Entity
@Table(name = "user_audits_log", indexes = { @Index(name = "user_audits_log_index", columnList = "site_id") })
@Data
public class UserAuditLog implements PersistableEntity<Long> {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "user_audits_log_seq")
	@SequenceGenerator(name = "user_audits_log_seq", sequenceName = "user_audits_log_seq")
	private Long id;

	@Column(name = "site_id", nullable = false, length = 99)
	private String siteId;

	@Column(name = "user_id", nullable = false, length = 99)
	private String userId;

	@Column(name = "role_name", nullable = false, length = 99)
	private String roleName;

	@Column(name = "action_taken", nullable = false, length = 1)
	private String actionTaken;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "audit_stamp", nullable = false)
	private Date auditStamp;

	@Column(name = "source", length = 1)
	private String source;

	@Column(name = "action_user_id", length = 99)
	private String actionUserId;
}
