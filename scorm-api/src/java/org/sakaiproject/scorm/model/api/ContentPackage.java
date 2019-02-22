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
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

public class ContentPackage implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final int NUMBER_OF_TRIES_UNLIMITED = -1;

	@Getter @Setter private Long contentPackageId;
	@Getter @Setter private String context;
	@Getter @Setter private String title;
	@Getter @Setter private String resourceId;
	@Getter @Setter private String manifestResourceId;
	@Getter @Setter private String url;
	@Getter @Setter private String createdBy;
	@Getter @Setter private String modifiedBy;
	@Getter @Setter private Serializable manifestId;
	@Getter @Setter private Date releaseOn;
	@Getter @Setter private Date dueOn;
	@Getter @Setter private Date acceptUntil;
	@Getter @Setter private Date createdOn;
	@Getter @Setter private Date modifiedOn;
	@Getter @Setter private int numberOfTries = NUMBER_OF_TRIES_UNLIMITED;
	@Getter @Setter private boolean isDeleted;

	public boolean isReleased()
	{
		Date now = new Date();
		return now.after(releaseOn);
	}

	public String getStatus()
	{
		return "Open";
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

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (obj == null)
		{
			return false;
		}

		if (getClass() != obj.getClass())
		{
			return false;
		}

		ContentPackage other = (ContentPackage) obj;
		if (contentPackageId == null)
		{
			if (other.contentPackageId != null)
			{
				return false;
			}
		}
		else if (!contentPackageId.equals(other.contentPackageId))
		{
			return false;
		}

		return true;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((contentPackageId == null) ? 0 : contentPackageId.hashCode());
		return result;
	}
}
