/**
 * Copyright (c) 2007 The Apereo Foundation
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
package org.sakaiproject.scorm.model.api;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode
public class ContentPackage implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final int NUMBER_OF_TRIES_UNLIMITED = -1;

	@Getter @Setter private Long contentPackageId;
	@EqualsAndHashCode.Exclude @Getter @Setter private String context;
	@EqualsAndHashCode.Exclude @Getter @Setter private String title;
	@EqualsAndHashCode.Exclude @Getter @Setter private String resourceId;
	@EqualsAndHashCode.Exclude @Getter @Setter private String manifestResourceId;
	@EqualsAndHashCode.Exclude @Getter @Setter private String url;
	@EqualsAndHashCode.Exclude @Getter @Setter private String createdBy;
	@EqualsAndHashCode.Exclude @Getter @Setter private String modifiedBy;
	@EqualsAndHashCode.Exclude @Getter @Setter private Serializable manifestId;
	@EqualsAndHashCode.Exclude @Getter @Setter private Date releaseOn;
	@EqualsAndHashCode.Exclude @Getter @Setter private Date dueOn;
	@EqualsAndHashCode.Exclude @Getter @Setter private Date acceptUntil;
	@EqualsAndHashCode.Exclude @Getter @Setter private Date createdOn;
	@EqualsAndHashCode.Exclude @Getter @Setter private Date modifiedOn;
	@EqualsAndHashCode.Exclude @Getter @Setter private int numberOfTries = NUMBER_OF_TRIES_UNLIMITED;
	@EqualsAndHashCode.Exclude @Getter @Setter private boolean isDeleted;
	@EqualsAndHashCode.Exclude @Getter @Setter private boolean showTOC = false; // default is to not show the table of contents
	@EqualsAndHashCode.Exclude @Getter @Setter private boolean showNavBar = false; // default is to not show the nav bar

	// This is not persisted to the database. It is only used in the context of configuring a content package, and thus
	// only stores the session user's time zone id for display and getter/setter purposes.
	@EqualsAndHashCode.Exclude @Getter @Setter ZoneId sessionUserZoneID;

	public ZonedDateTime getZonedReleaseOn()
	{
		return releaseOn == null ? null : ZonedDateTime.ofInstant(releaseOn.toInstant(), sessionUserZoneID);
	}

	public ZonedDateTime getZonedDueOn()
	{
		return dueOn == null ? null : ZonedDateTime.ofInstant(dueOn.toInstant(), sessionUserZoneID);
	}

	public ZonedDateTime getZonedAcceptUntil()
	{
		return acceptUntil == null ? null : ZonedDateTime.ofInstant(acceptUntil.toInstant(), sessionUserZoneID);
	}

	public void setZonedReleaseOn(ZonedDateTime releaseOn)
	{
		if (releaseOn == null)
		{
			this.releaseOn = null;
		}
		else
		{
			this.releaseOn = Date.from(releaseOn.toInstant());
		}
	}

	public void setZonedDueOn(ZonedDateTime dueOn)
	{
		if (dueOn == null)
		{
			this.dueOn = null;
		}
		else
		{
			this.dueOn = Date.from(dueOn.toInstant());
		}
	}

	public void setZonedAcceptUntil(ZonedDateTime acceptUntil)
	{
		if (acceptUntil == null)
		{
			this.acceptUntil = null;
		}
		else
		{
			this.acceptUntil = Date.from(acceptUntil.toInstant());
		}
	}

	public boolean isReleased()
	{
		Date now = new Date();
		return now.after(releaseOn);
	}

	public ContentPackage()
	{
		this.isDeleted = false;
	}

	public ContentPackage(String title, long contentPackageId)
	{
		this();
		this.title = title;
		this.contentPackageId = contentPackageId;
	}

	public ContentPackage(String title, String resourceId)
	{
		this();
		this.title = title;
		this.resourceId = resourceId;
	}
}
