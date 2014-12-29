/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.content.impl.serialize.api;

import java.util.Collection;

import org.sakaiproject.content.api.ResourceTypeRegistry;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.entity.api.serialize.SerializableEntity;
import org.sakaiproject.time.api.Time;

/**
 * <pre>
 * This is an accessor interface for the Resource to enable the searializer to do its work
 * All methods should be straight getters and setters with no logic.
 * </pre>
 * @author ieb
 *
 */
public interface SerializableResourceAccess
{

	/**
	 * @return
	 */
	SerializableEntity getSerializableProperties();

	/**
	 * @return
	 */
	String getSerializableId();

	/**
	 * @return
	 */
	boolean getSerializableHidden();

	/**
	 * @return
	 */
	AccessMode getSerializableAccess();

	/**
	 * @return
	 */
	Time getSerializableReleaseDate();

	/**
	 * @return
	 */
	Time getSerializableRetractDate();

	/**
	 * @return
	 */
	Collection<String> getSerializableGroup();

	/**
	 * @return
	 */
	byte[] getSerializableBody();

	/**
	 * @return
	 */
	String getSerializableContentType();

	/**
	 * @return
	 */
	String getSerializableFilePath();

	/**
	 * @return
	 */
	String getSerializableResourceType();

	/**
	 * @return
	 */
	long getSerializableContentLength();

	/**
	 * @param id
	 */
	void setSerializableId(String id);

	/**
	 * @param access
	 */
	void setSerializableAccess(AccessMode access);

	/**
	 * @param hidden
	 */
	void setSerializableHidden(boolean hidden);

	/**
	 * @param resourceType
	 */
	void setSerializableResourceType(String resourceType);

	/**
	 * @param releaseDate
	 */
	void setSerializableReleaseDate(Time releaseDate);

	/**
	 * @param retractDate
	 */
	void setSerializableRetractDate(Time retractDate);

	/**
	 * @param groups
	 */
	void setSerializableGroups(Collection<String> groups);

	/**
	 * @param contentType
	 */
	void setSerializableContentType(String contentType);

	/**
	 * @param contentLength
	 */
	void setSerializableContentLength(long contentLength);

	/**
	 * @param filePath
	 */
	void setSerializableFilePath(String filePath);

	/**
	 * @param body
	 */
	void setSerializableBody(byte[] body);

	/**
	 * @return
	 */
	ResourceTypeRegistry getResourceTypeRegistry();

}
