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

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.GenericGenerator;
import org.sakaiproject.microsoft.api.converters.JpaConverterSynchronizationStatus;
import org.sakaiproject.microsoft.api.data.SynchronizationStatus;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mc_group_synchronization", uniqueConstraints = { @UniqueConstraint(columnNames = { "parentId", "group_id", "channel_id" }) })
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class GroupSynchronization {

	@Id
	@Column(name = "id", length = 99, nullable = false)
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	private String id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="parentId")
	private SiteSynchronization siteSynchronization;

	@Column(name = "group_id", nullable = false)
	private String groupId;
	
	@Column(name = "channel_id", nullable = false)
	private String channelId;
	
	@Column(name = "status")
	@Builder.Default
	@Convert(converter = JpaConverterSynchronizationStatus.class)
	private SynchronizationStatus status = SynchronizationStatus.NONE;
	
	@Column(name = "status_updated_at")
	private ZonedDateTime statusUpdatedAt;
}
