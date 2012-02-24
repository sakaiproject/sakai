/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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



package org.sakaiproject.tool.assessment.ui.bean.author;

import java.io.Serializable;

/**
 * <p>
  */
public class DeleteConfirmBean
  implements Serializable
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1028400984124650211L;
private String name;

  /**
   * Creates a new DeleteConfirmBean object.
   */
  public DeleteConfirmBean()
  {
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getName()
  {
    return this.name;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param name DOCUMENTATION PENDING
   */
  public void setName(String name)
  {
    if(name != null)
    {
      this.name = name;
    }
  }
}
