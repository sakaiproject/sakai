/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-app/src/java/org/sakaiproject/tool/messageforums/ui/SiteGroupBean.java $
 * $Id: SiteGroupBean.java $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.tool.messageforums.ui;

import org.sakaiproject.site.api.Group;

public class SiteGroupBean {
	private Group group;
	private boolean createTopicForGroup;
	
	public SiteGroupBean (Group group, boolean createTopicForGroup) 
	{
		this.group = group;
		this.createTopicForGroup = createTopicForGroup;
	}
	
	public Group getGroup() 
	{
		return this.group;
	}
	
	public void setGroup(Group group) 
	{
		this.group = group;
	}
	
	public boolean getCreateTopicForGroup()
	{
		return this.createTopicForGroup;
	}
	
	public void setCreateTopicForGroup(boolean createTopicForGroup)
	{
		this.createTopicForGroup = createTopicForGroup;
	}
}
