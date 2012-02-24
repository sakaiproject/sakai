/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.content.api;

import java.util.Date;

/**
 * Lock is a long-term lock on a Content object.
 */
public interface Lock
{
	String getId();

	void setId(String id);

	boolean isActive();

	void setActive(boolean active);

	Date getDateAdded();

	void setDateAdded(Date dateAdded);

	Date getDateRemoved();

	void setDateRemoved(Date dateRemoved);

	String getQualifier();

	void setQualifier(String qualifier);

	String getReason();

	void setReason(String reason);

	String getAsset();

	void setAsset(String asset);

	boolean isSystem();

	void setSystem(boolean system);
}
