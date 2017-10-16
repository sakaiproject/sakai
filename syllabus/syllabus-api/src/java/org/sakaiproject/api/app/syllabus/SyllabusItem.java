/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.api.app.syllabus;

import java.util.Set;

/**
 * @author <a href="mailto:jlannan.iupui.edu">Jarrod Lannan</a>
 * @version $id
 */
public interface SyllabusItem
{
  /**
   * @return Returns the syllabi.
   */
  public Set getSyllabi();

  /**
   * @param syllabi The syllabi to set.
   */
  public void setSyllabi(Set syllabi);

  /**
   * @return Returns the contextId.
   */
  public String getContextId();

  /**
   * @param contextId The contextId to set.
   */
  public void setContextId(String contextId);

  /**
   * @return Returns the lockId.
   */
  public Integer getLockId();

  /**
   * @return Returns the surrogateKey.
   */
  public Long getSurrogateKey();

  /**
   * @return Returns the userId.
   */
  public String getUserId();

  /**
   * @param userId The userId to set.
   */
  public void setUserId(String userId);

  /**
   * @return Returns the redirectURL.
   */
  public String getRedirectURL();

  /**
   * @param redirectURL The redirectURL to set.
   */
  public void setRedirectURL(String redirectURL);

}

