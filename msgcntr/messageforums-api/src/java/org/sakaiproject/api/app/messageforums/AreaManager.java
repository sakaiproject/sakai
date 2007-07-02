/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-api/src/java/org/sakaiproject/api/app/messageforums/AreaManager.java $
 * $Id: AreaManager.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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
package org.sakaiproject.api.app.messageforums;

/**
 * @author rshastri
 *
 */
public interface AreaManager
{
 
  public boolean isPrivateAreaEnabled();
  public void saveArea(Area area);
  public Area createArea(String typeId, String contextId);
  public void deleteArea(Area area);
  public Area getAreaByContextIdAndTypeId(String typeId);
  public Area getAreaByContextIdAndTypeId(String contextId, String typeId);
  public Area getAreaByType(final String typeId);  
  public Area getPrivateArea();
  public Area getDiscusionArea();
  public Area getDiscussionArea(final String contextId);
  public String getResourceBundleString(String key);
}
