/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
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
 *
 **********************************************************************************/

package org.sakaiproject.importer.impl.importables;

import java.util.Date;

/**
 * This is a generic Announcement type object which holds announcements during the
 * migration/import process
 * @author Aaron Zeckoski (aaronz@vt.edu)
 *
 */
public class Announcement extends AbstractImportable {

	private String title;
	private String description;

	private boolean html = false;
	private boolean liternalNewline = false;
	private boolean permanent = false;
	private boolean publicViewable = false;

	private Date created = new Date();
	private Date updated = new Date();
	private Date start;
	private Date end;

	private String emailNotification = "N";

	static final String DISPLAY_TYPE = "Announcement";


	public String getDisplayType() {
		return DISPLAY_TYPE;
	}
	
	public String getTypeName() {
		return "sakai-announcement";
	}


	public Date getCreated() {
		return created;
	}


	public void setCreated(Date created) {
		this.created = created;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public String getEmailNotification() {
		return emailNotification;
	}


	public void setEmailNotification(String emailNotification) {
		this.emailNotification = emailNotification;
	}


	public Date getEnd() {
		return end;
	}


	public void setEnd(Date end) {
		this.end = end;
	}


	public boolean isHtml() {
		return html;
	}


	public void setHtml(boolean html) {
		this.html = html;
	}


	public boolean isLiternalNewline() {
		return liternalNewline;
	}


	public void setLiternalNewline(boolean liternalNewline) {
		this.liternalNewline = liternalNewline;
	}


	public boolean isPermanent() {
		return permanent;
	}


	public void setPermanent(boolean permanent) {
		this.permanent = permanent;
	}


	public boolean isPublicViewable() {
		return publicViewable;
	}


	public void setPublicViewable(boolean publicViewable) {
		this.publicViewable = publicViewable;
	}


	public Date getStart() {
		return start;
	}


	public void setStart(Date start) {
		this.start = start;
	}


	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public Date getUpdated() {
		return updated;
	}


	public void setUpdated(Date updated) {
		this.updated = updated;
	}

}
