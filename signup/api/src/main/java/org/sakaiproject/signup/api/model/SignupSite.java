/**
 * Copyright (c) 2007-2014 The Apereo Foundation
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
/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational 
* Community License, Version 2.0 (the "License"); you may not use this file 
* except in compliance with the License. You may obtain a copy of the 
* License at:
*
* http://opensource.org/licenses/ecl2.txt
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.signup.api.model;

import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.sakaiproject.springframework.data.PersistableEntity;

/**
 * <p>
 * This class holds the information for signup site. This object is mapped
 * directly to the DB storage by Hibernate
 * </p>
 */
@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "signup_sites", indexes = {
	@Index(name = "IDX_SITE_ID", columnList = "site_id")
})
public class SignupSite implements PersistableEntity<Long> {

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "signup_sites_seq")
	@SequenceGenerator(name = "signup_sites_seq", sequenceName = "signup_sites_ID_SEQ")
    @EqualsAndHashCode.Include
    @Column(name = "id")
	private Long id;

	@Version
	@Column(name = "version")
	private int version;

    @Column(name = "title")
	private String title;

    @Column(name = "site_id", length = 99, nullable = false)
	private String siteId;

    @Column(name = "calendar_event_id", length = 2000)
	private String calendarEventId;

    @Column(name = "calendar_id", length = 99)
	private String calendarId;

    @ElementCollection
	@CollectionTable(name = "signup_site_groups", joinColumns = @JoinColumn(name = "signup_site_id", nullable = false))
	@OrderColumn(name = "list_index")
    @ToString.Exclude
	private List<SignupGroup> signupGroups;

    /**
	 * Check if the event/meeting is scoped by site or group
	 * 
	 * @return true if the event/meeting is scoped by site, false if scoped by group
	 */
	public boolean isSiteScope() {
		return signupGroups == null || signupGroups.isEmpty();
	}

}
