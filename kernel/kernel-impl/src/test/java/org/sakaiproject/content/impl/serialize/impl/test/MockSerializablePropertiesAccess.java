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

package org.sakaiproject.content.impl.serialize.impl.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sakaiproject.entity.api.serialize.SerializableEntity;
import org.sakaiproject.entity.api.serialize.SerializablePropertiesAccess;

/**
 * @author ieb
 *
 */
public class MockSerializablePropertiesAccess implements SerializableEntity,
		SerializablePropertiesAccess
{

	public Map<String, Object> properties = new HashMap<String, Object>();
	public Map<String,Object> set_properties = new HashMap<String, Object>();
	
	public MockSerializablePropertiesAccess() {
		properties.put("testProperty1", "propertyValue1");
		properties.put("testProperty2", "propertyValue2");
		properties.put("testProperty3", "propertyValue3");
		properties.put("testProperty4", "propertyValue4");
		properties.put("testProperty5", "propertyValue5");
		List<String> list = new ArrayList<String>();
		list.add("StringArray1");
		list.add("StringArray2");
		list.add("StringArray3");
		list.add("StringArray4");
		list.add("StringArray5");
		properties.put("testProperty5", list);
	}
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
		set_properties = properties;
	}

	/**
	 * @throws Exception 
	 * 
	 */
	public void check() throws Exception
	{
		check("Properties Size",properties.size(),set_properties.size());
		for ( Iterator<String> ig = properties.keySet().iterator(); ig.hasNext(); ) {
			String key = ig.next();
			check("Key "+key, properties.get(key), set_properties.get(key) );
		}
		for ( Iterator<String> ig = set_properties.keySet().iterator(); ig.hasNext(); ) {
			String key = ig.next();
			check("Key "+key, properties.get(key), set_properties.get(key) );
		}

		
	}
	private void check(String name , Object id2, Object set_id2) throws Exception
	{
		 if ( id2 != null && !id2.equals(set_id2) ) throw new Exception(name+" does not match "+id2+"]!=["+set_id2+"]");
		
	}

}
