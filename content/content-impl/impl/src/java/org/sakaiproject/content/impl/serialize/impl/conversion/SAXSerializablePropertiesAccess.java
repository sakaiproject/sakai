/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.impl.serialize.impl.conversion;

import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.entity.api.serialize.SerializableEntity;
import org.sakaiproject.entity.api.serialize.SerializablePropertiesAccess;

/**
 * @author ieb
 *
 */
public class SAXSerializablePropertiesAccess implements SerializablePropertiesAccess, SerializableEntity
{

	private Map<String, Object> properties = new HashMap<String, Object>();

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.serialize.SerializablePropertiesAccess#getSerializableProperties()
	 */
	public Map<String, Object> getSerializableProperties()
	{
		return properties;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.serialize.SerializablePropertiesAccess#setSerializableProperties(java.util.Map)
	 */
	public void setSerializableProperties(Map<String, Object> properties)
	{
		this.properties = properties;		
	}

}
