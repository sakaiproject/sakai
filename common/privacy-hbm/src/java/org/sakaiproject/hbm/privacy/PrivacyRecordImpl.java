/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/access/trunk/access-impl/impl/src/java/org/sakaiproject/access/tool/AccessServlet.java $
 * $Id: AccessServlet.java 17063 2006-10-11 19:48:42Z jimeng@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.hbm.privacy;

public class PrivacyRecordImpl
{
  private Long surrogateKey;
	private Integer lockId;
	private String contextId;
	private String recordType;
	private String userId;
	private boolean viewable;
	
	public PrivacyRecordImpl()
	{
		
	}

	public PrivacyRecordImpl(String userId, String contextId, String recordType, boolean viewable)
	{
		this.userId = userId;
		this.contextId = contextId;
		this.recordType = recordType;
		this.viewable = viewable;
	}
	
	public String getContextId()
	{
		return contextId;
	}

	public Integer getLockId()
	{
		return lockId;
	}

	public String getRecordType()
	{
		return recordType;
	}

	public String getUserId()
	{
		return userId;
	}

	public boolean getViewable()
	{
		return viewable;
	}

	public void setContextId(String contextId)
	{
		this.contextId = contextId;
	}

	public void setLockId(Integer lockId)
	{
		this.lockId = lockId;
	}

	public void setRecordType(String recordType)
	{
		this.recordType = recordType;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public void setViewable(boolean viewable)
	{
		this.viewable = viewable;
	}

	public Long getSurrogateKey()
	{
		return surrogateKey;
	}

	public void setSurrogateKey(Long surrogateKey)
	{
		this.surrogateKey = surrogateKey;
	}

}
