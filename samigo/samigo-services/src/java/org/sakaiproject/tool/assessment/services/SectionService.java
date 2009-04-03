/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/services/SectionService.java $
 * $Id: SectionService.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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


package org.sakaiproject.tool.assessment.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.facade.SectionFacade;

/**
 * The SectionService calls persistent service locator to reach the
 * manager on the back end.
 */
public class SectionService
{
  private static Log log = LogFactory.getLog(SectionService.class);

  /**
   * Creates a new SectionService object.
   */
  public SectionService()
  {
  }


  /**
   * Get a particular item from the backend, with all questions.
   */
  public SectionFacade getSection(Long secId, String agentId)
  {
    SectionFacade section = null;
    try
    {
      section = PersistenceService.getInstance().getSectionFacadeQueries().get(secId);
    }
    catch(Exception e)
    {
      log.error(e); throw new RuntimeException(e);
    }

    return section;
  }


}
