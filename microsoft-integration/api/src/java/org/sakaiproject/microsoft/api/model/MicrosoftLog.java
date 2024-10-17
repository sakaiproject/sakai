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
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.sakaiproject.microsoft.api.converters.JpaConverterMap;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mc_log")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class MicrosoftLog {
	public static final String ERROR_INVITATION = "error.invitation";
	public static final String ERROR_USER_ADDED_TO_AUTHZGROUP = "error.user_added_to_authzgroup";
	public static final String ERROR_USER_REMOVED_FROM_AUTHZGROUP = "error.user_removed_from_authzgroup";
	public static final String ERROR_ELEMENT_CREATED = "error.element_created";
	public static final String ERROR_ELEMENT_DELETED = "error.element_deleted";
	public static final String ERROR_ELEMENT_MODIFIED = "error.element_modified";
	public static final String ERROR_TEAM_ID_NULL = "error.team_id_null";


	public static final String EVENT_SITE_SYNCRHO_START = "event.site_synchro_start";
	public static final String EVENT_SITE_SYNCRHO_END = "event.site_synchro_end";
	
	public static final String EVENT_SITE_CREATED = "event.site_created"; //sakai
	public static final String EVENT_SITE_UNPUBLISHED = "event.site_unpublished"; //sakai
	public static final String EVENT_GROUP_CREATED = "event.group_created"; //sakai
	
	public static final String EVENT_SITE_DELETED = "event.site_deleted"; //sakai
	public static final String EVENT_GROUP_DELETED = "event.group_deleted"; //sakai
	
	public static final String EVENT_USER_ADDED_TO_SITE = "event.user_added_to_site"; //sakai
	public static final String EVENT_USER_ADDED_TO_GROUP = "event.user_added_to_group"; //sakai
	public static final String EVENT_USER_REMOVED_FROM_SITE = "event.user_removed_from_site"; //sakai
	public static final String EVENT_USER_REMOVED_FROM_GROUP = "event.user_removed_from_group"; //sakai
	
	public static final String EVENT_USER_ADDED_TO_MICROSOFT_GROUP = "event.user_added_to_microsoft_group"; //microsoft
	public static final String EVENT_USER_ADDED_TO_TEAM = "event.user_added_to_team"; //microsoft
	public static final String EVENT_USER_ADDED_TO_CHANNEL = "event.user_added_to_channel"; //microsoft
	public static final String EVENT_USER_REMOVED_FROM_TEAM = "event.user_removed_from_team"; //microsoft
	public static final String EVENT_USER_REMOVED_FROM_CHANNEL = "event.user_removed_from_channel"; //microsoft
	public static final String EVENT_ALL_USERS_REMOVED_FROM_TEAM = "event.all_users_removed_from_team"; //microsoft

	public static final String EVENT_CREATE_TEAM_FROM_SITE = "event.create_team_from_site";
	public static final String BINDING_TEAM_FROM_SITE = "event.binding_team_from_site";
	public static final String EVENT_CREATE_TEAM_FROM_GROUP = "event.create_team_from_group"; //microsoft
	public static final String EVENT_CHANNEL_CREATED = "event.channel_created"; //microsoft
	public static final String EVENT_CHANNEL_PRESENT_ON_GROUP = "event.channel_present_on_group"; //microsoft

	public static final String EVENT_INVITATION_SENT = "event.invitation_sent";
	public static final String EVENT_INVITATION_NOT_SENT = "event.invitation_not_sent";

	public static final String EVENT_REACH_MAX_CHANNELS = "event.reach_max_channels";
	public static final String EVENT_REMOVE_MEMBER = "event.remove_member";
	public static final String EVENT_REMOVE_OWNER = "event.remove_owner";
	public static final String EVENT_REMOVE_GUEST = "event.remove_guest";
	public static final String EVENT_INVITATION_CREATED = "event.invitation_created";
	public static final String EVENT_ADD_MEMBER = "event.add_member";
	public static final String EVENT_GROUP_SYNCHRONIZATION = "event.group_synchronization";
	public static final String EVENT_ADD_OWNER = "event.add_owner";

	public static final String EVENT_AUTOCONFIG = "event.autoconfig";

	public static final String EVENT_TOO_MANY_REQUESTS = "event.too_many_requests";
	public static final String EVENT_USER_NOT_FOUND_ON_TEAM = "event.user_not_found_on_team";



	public enum Status {
		KO, OK
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "mc_log_seq")
	@SequenceGenerator(name = "mc_log_seq", sequenceName = "mc_log_seq")
	private Long id;

	@Column(name = "event")
	private String event;
	
	@Column(name = "status")
	private Status status;
	
	@Lob
	@Column(name="context")
	@Convert(converter = JpaConverterMap.class)
	private Map<String, String> context;
	
	@Column(name = "event_date")
	@Builder.Default
	private ZonedDateTime eventDate = ZonedDateTime.now();
	
	//custom builder
	public static class MicrosoftLogBuilder {
		public MicrosoftLogBuilder addData(String key, String value) {
			if(this.context == null){
				this.context = new HashMap<>();
			}
			this.context.put(key, value);
			return this;
		}
	}
}
