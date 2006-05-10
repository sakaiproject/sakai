/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
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


package org.sakaiproject.tool.assessment.ui.listener.author;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Qingru Zhang
 * @version $Id$
 */

public class RemovePublishedAssessmentThread extends Thread
{

  private static Log log = LogFactory.getLog(RemovePublishedAssessmentThread.class);
  private String assessmentId;
  public RemovePublishedAssessmentThread(String assessmentId)
  {
    this.assessmentId = assessmentId;
  }

  public void run(){
    PublishedAssessmentService assessmentService = new PublishedAssessmentService();
    //log.info("** remove assessmentId= "+this.assessmentId);
    assessmentService.removeAssessment(this.assessmentId);
  }

}
