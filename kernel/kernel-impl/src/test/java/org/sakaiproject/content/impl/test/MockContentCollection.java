/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

package org.sakaiproject.content.impl.test;

import java.util.List;
import java.util.Map;

import org.sakaiproject.content.api.ContentCollectionEdit;

/**
 * MockContentCollection
 *
 */
public class MockContentCollection extends MockContentEntity implements ContentCollectionEdit
{
	/**
	 * @param collectionId
	 */
	public MockContentCollection(String collectionId)
	{
	    this.containingCollectionId = collectionId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentCollection#getBodySizeK()
	 */
	public long getBodySizeK()
	{
	    // TODO Auto-generated method stub
	    return 0;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentCollection#getMemberCount()
	 */
	public int getMemberCount()
	{
	    // TODO Auto-generated method stub
	    return 0;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentCollection#getMemberResources()
	 */
	public List getMemberResources()
	{
	    // TODO Auto-generated method stub
	    return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentCollection#getMembers()
	 */
	public List getMembers()
	{
	    // TODO Auto-generated method stub
	    return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentCollectionEdit#setPriorityMap(java.util.Map)
	 */
	public void setPriorityMap(Map priorities)
	{
	    // TODO Auto-generated method stub
	    
	}
}
