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

package org.sakaiproject.tool.assessment.facade;

import java.io.Serializable;

import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookHelper;

/**
 * <p>Description: Implements the internal gradebook information.
 * Uses Sakai services through the gradebook helper.</p>
 * <p>Sakai Project Copyright (c) 2005</p>
 * <p> </p>
 * @author Ed Smiley <esmiley@stanford.edu>
 *
 */
public class GradebookFacade implements Serializable
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
  private static final GradebookHelper helper =
      (GradebookHelper) SpringBeanLocator.getInstance().getBean("gradebookHelper");

  /**
   * Get current gradebook uid.
   * @return the current gradebook uid.
   */
  public static String getGradebookUId(String siteId)
  {
    return helper.getGradebookUId(siteId);
  }
  
  public static String getGradebookUId()
  {
    return getGradebookUId(null);
  }

}
