/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.shared.impl.assessment;

import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.services.SectionService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentServiceException;
import org.sakaiproject.tool.assessment.shared.api.assessment.SectionServiceAPI;

/**
 * SectionServiceImpl implements a shared interface to get/set section
 * information.
 * @author Ed Smiley <esmiley@stanford.edu>
 */
public class SectionServiceImpl implements SectionServiceAPI
{
  public SectionServiceImpl()
  {
  }

  // our API just uses our internal service. SectionFacade implements
  // SectionDataIfc.  If we want, we can always replace this internal
  // service and use its implementation as our own.
  public SectionDataIfc getSection(Long secId, String agentId)
  {
    SectionFacade section = null;
    try
    {
      SectionService service = new SectionService();
      section = service.getSection(secId, agentId);
    }
    catch(Exception e)
    {
      throw new AssessmentServiceException(e);
    }

    return section;
  }
}
