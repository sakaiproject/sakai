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
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess;
import org.sakaiproject.entity.api.serialize.SerializableEntity;
import org.sakaiproject.time.api.Time;

/**
 * @author ieb
 *
 */
public class MockSerializableCollectionAcccess implements SerializableCollectionAccess, SerializableEntity
{

	public MockSerializablePropertiesAccess properties = new MockSerializablePropertiesAccess();
	public Collection<String> group = new Vector<String>();
	public String id = "sdfsd'sdfsdf'sdfsdfs'dfsdfssdfsd'sdfsdf'sdfsdfs'dfsdfs";
	public Time retractDate = new MockTime(System.currentTimeMillis());
	public Time releaseDate = new MockTime(System.currentTimeMillis()-3600*1000);
	public boolean hidden;
	public AccessMode access;
	
	public Time set_retractDate;
	public String set_resourceType;
	public Time set_releaseDate;
	public String set_id;
	public boolean set_hidden;
	public Collection<String> set_groups;
	public AccessMode set_access;
	
	public MockSerializableCollectionAcccess() {
		group.add("Something");
		group.add("SomethingElse");
		group.add("SomethingElseAgain");
		group.add("Somethingsadfsdfsdf");
	}
	

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#getSerializableAccess()
	 */
	public AccessMode getSerializableAccess()
	{
		return access;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#getSerializableGroup()
	 */
	public Collection<String> getSerializableGroup()
	{
		Collection<String> s = new ArrayList<String>(group);
		return s;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#getSerializableHidden()
	 */
	public boolean getSerializableHidden()
	{
		return hidden;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#getSerializableId()
	 */
	public String getSerializableId()
	{
		return id ;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#getSerializableProperties()
	 */
	public SerializableEntity getSerializableProperties()
	{
		return  properties ;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#getSerializableReleaseDate()
	 */
	public Time getSerializableReleaseDate()
	{
		return releaseDate;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#getSerializableRetractDate()
	 */
	public Time getSerializableRetractDate()
	{
		return retractDate;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#setSerializableAccess(org.sakaiproject.content.api.GroupAwareEntity.AccessMode)
	 */
	public void setSerializableAccess(AccessMode access)
	{
		set_access = access;
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#setSerializableGroups(java.util.Collection)
	 */
	public void setSerializableGroups(Collection<String> groups)
	{
		set_groups = groups;
		
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#setSerializableHidden(boolean)
	 */
	public void setSerializableHidden(boolean hidden)
	{
		set_hidden = hidden;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#setSerializableId(java.lang.String)
	 */
	public void setSerializableId(String id)
	{
		set_id = id;
		
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#setSerializableReleaseDate(org.sakaiproject.time.api.Time)
	 */
	public void setSerializableReleaseDate(Time releaseDate)
	{
		set_releaseDate = releaseDate;
		
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#setSerializableResourceType(java.lang.String)
	 */
	public void setSerializableResourceType(String resourceType)
	{
		set_resourceType = resourceType;
		
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#setSerializableRetractDate(org.sakaiproject.time.api.Time)
	 */
	public void setSerializableRetractDate(Time retractDate)
	{
		set_retractDate = retractDate;
		
	}

	public void check() throws Exception {
		check("ID",id,set_id);
		if ( retractDate != null ) {
			check("retractDate",retractDate.getTime(),set_retractDate.getTime());
		}
		check("ResourceType",ResourceType.TYPE_FOLDER,set_resourceType);
		if ( releaseDate != null ) {
			check("releaseDate",releaseDate.getTime(),set_releaseDate.getTime());
		}
		check("hidden",hidden,set_hidden);
		check("group",group.size(),set_groups.size());
		for ( Iterator<String> ig = group.iterator(); ig.hasNext(); ) {
			if ( !set_groups.contains(ig.next()) ) throw new Exception("Missing Groups element ");
		}
		for ( Iterator<String> ig = set_groups.iterator(); ig.hasNext(); ) {
			if ( !group.contains(ig.next()) ) throw new Exception("Missing Groups element ");
		}
		check("access",access,set_access);
		properties.check();
	}


	/**
	 * @param id2
	 * @param set_id2
	 */
	private void check(String name , Object id2, Object set_id2) throws Exception
	{
		 if ( id2 != null && !id2.equals(set_id2) ) throw new Exception(name+" does not match "+id2+"]!=["+set_id2+"]");
		
	}
}
