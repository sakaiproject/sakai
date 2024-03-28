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
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

public class Attempt implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Setter @Getter private Long id;
	@Setter @Getter private long contentPackageId;
	@Setter @Getter private long attemptNumber;
	@Setter @Getter private String courseId;
	@Setter @Getter private String learnerId;
	@Setter @Getter private String learnerName;
	@Setter @Getter private Date beginDate;
	@Setter @Getter private Date lastModifiedDate;
	@Setter @Getter private Map<String, Long> scoDataManagerMap;
	@Setter @Getter private boolean isNotExited;
	@Setter @Getter private boolean isSuspended;

	public void setDataManagerId(String scoId, Long dataManagerId)
	{
		if (scoId != null)
		{
			scoDataManagerMap.put(scoId, dataManagerId);
		}
	}

	public Long getDataManagerId(String scoId)
	{
		return scoDataManagerMap.get(scoId);
	}

	public Attempt()
	{
		this.isNotExited = true;
		this.isSuspended = false;
		this.scoDataManagerMap = new HashMap<>();
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

		Attempt other = (Attempt) obj;
		if (id == null)
		{
			if (other.id != null)
			{
				return false;
			}
		}
		else if (!id.equals(other.id))
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
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
}
