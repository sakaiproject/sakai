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
package org.sakaiproject.microsoft.api.model;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.GenericGenerator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "MC_TEAM_ARCHIVE", uniqueConstraints = { @UniqueConstraint(columnNames = { "SITE_ID", "TEAM_ID" }) })
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MicrosoftTeamArchiveRecord {

	@Id
	@Column(name = "ID", length = 99, nullable = false)
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	private String id;

	@Column(name = "SITE_ID", nullable = false, length = 99)
	private String siteId;

	@Column(name = "TEAM_ID", nullable = false, length = 255)
	private String teamId;

	@Column(name = "ARCHIVE_DATE")
	private ZonedDateTime archiveDate;

	@Column(name = "STATUS", nullable = false)
	private int status = 0;
}