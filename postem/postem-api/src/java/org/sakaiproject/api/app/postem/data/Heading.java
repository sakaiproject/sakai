/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/postem/trunk/postem-hbm/src/java/org/sakaiproject/component/app/postem/data/Heading.java $
 * $Id: Heading.java 17140 2006-10-16 17:40:49Z wagnermr@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.api.app.postem.data;

public interface Heading  {
	public Long getGradebookId();
	public void setGradebookId(Long gradebookId);
	
	public String getHeadingTitle(); 
	public void setHeadingTitle(String heading); 
	
	public Integer getLocation(); 
	public void setLocation(Integer location);
}
