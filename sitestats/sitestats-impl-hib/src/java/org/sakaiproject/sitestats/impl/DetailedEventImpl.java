/**
 * Copyright (c) 2006-2019 The Apereo Foundation
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
package org.sakaiproject.sitestats.impl;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.sitestats.api.event.detailed.DetailedEvent;

/**
 * @author plukasew
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DetailedEventImpl implements DetailedEvent, Serializable {
	@EqualsAndHashCode.Include
	private long id;
	private String siteId;
	private String userId;
	private String eventId;
	private String eventRef;
	private Date eventDate;

	public DetailedEventImpl() {
		this(0, "", "", "", "", new Date());
	}

	public DetailedEventImpl(long id, String siteId, String userId, String eventId, String eventRef, Date date) {
		this.id = id;
		this.siteId = siteId;
		this.userId = userId;
		this.eventId = eventId;
		this.eventDate = date;
		setEventRef(eventRef);
	}

	public void setEventRef(String eventRef) {
		this.eventRef = StringUtils.abbreviate(eventRef, 512);
	}
}
