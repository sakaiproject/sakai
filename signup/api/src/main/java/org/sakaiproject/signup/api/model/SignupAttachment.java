/**
 * Copyright (c) 2007-2016 The Apereo Foundation
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Embeddable
@Getter
@Setter
@Slf4j
public class SignupAttachment {

	@Column(name = "resource_Id", length = 255)
	private String resourceId;

    @Column(name = "file_name", length = 255)
	private String filename;

    @Column(name = "mime_type", length = 80)
	private String mimeType;

    @Column(name = "fileSize")
	private Long fileSize; // in kilobyte

	@Column(name = "location", length = 255)
	private String location;

	@Column(name = "isLink")
	private Boolean isLink;

	@Column(name = "timeslot_id")
	private Long timeslotId;

	@Column(name = "view_by_all")
	private Boolean viewByAll;

	@Column(name = "created_by", length = 99, nullable = false)
	private String createdBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_date", nullable = false)
	private Date createdDate;

	@Column(name = "last_modified_by", length = 99, nullable = false)
	private String lastModifiedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "last_modified_date", nullable = false)
	private Date lastModifiedDate;	

	public SignupAttachment(){
		viewByAll = true; //default
	}

	public String getEncodedResourceId() {
		try {
			return URLEncoder.encode(resourceId, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error("UnsupportedEncodingException: " + e.getMessage());
		}
		return "";
	}
}
