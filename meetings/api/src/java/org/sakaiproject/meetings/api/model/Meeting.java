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
package org.sakaiproject.meetings.api.model;

import java.time.Instant;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "meetings")
@AllArgsConstructor
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Meeting {

    @Id
    @Column(name = "meeting_id", length = 99, nullable = false)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Column(name = "meeting_title", length = 255, nullable = false)
    private String title;
    
    @Lob
    @Column(name = "meeting_description", length = 4000)
    private String description;
    
    @Column(name = "meeting_site_id", length = 99)
    private String siteId;
    
    @Column(name = "meeting_start_date", columnDefinition = "datetime(0)")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant startDate;

    @Column(name = "meeting_end_date", columnDefinition = "datetime(0)")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant endDate;
    
    @Column(name = "meeting_url", length = 255)
    private String url;
    
    @Column(name = "meeting_owner_id", length = 99)
    private String ownerId;
    
    @ManyToOne
    @JoinColumn(name="meeting_provider_id")
    private MeetingsProvider provider;
    
    @OneToMany(mappedBy="meeting", cascade = CascadeType.ALL)
    private List<MeetingAttendee> attendees;
    
    /**
     * Extract meeting ID from URL
     * @return meetingId
     */
    public String getMeetingId() {
    	String ret = null;
    	if(!"".equals(this.url)) {
    		Pattern teamPattern = Pattern.compile("^https://teams.microsoft.com/l/meetup-join/([^/]+)/.*$");
			Matcher matcher = teamPattern.matcher(this.url);
			if(matcher.find()) {
				ret = matcher.group(1);
			}
    	}
    	return ret;
    }
}
