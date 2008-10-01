/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/integration/helper/standalone/GradebookHelperImpl.java $
 * $Id: GradebookHelperImpl.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 Sakai Foundation
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


package org.sakaiproject.tool.assessment.integration.helper.standalone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookHelper;

/**
 *
 * <p>Description:
 * This is a stub standalone context implementation helper delegate class for
 * the GradebookFacade class.  The helper methods use hardcoded values.
 * "Standalone" means that Samigo (Tests and Quizzes)
 * is running without the context of the Sakai portal and authentication
 * mechanisms, and therefore we use stub methods.</p>
 * <p>Note: To customize behavior you can add your own helper class to the
 * Spring injection via the integrationContext.xml for your context.
 * The particular integrationContext.xml to be used is selected by the
 * build process.
 * </p>
 * <p>Sakai Project Copyright (c) 2005</p>
 * <p> </p>
 * @author Ed Smiley <esmiley@stanford.edu>
 *
 */
public class GradebookHelperImpl implements GradebookHelper
{
  private static Log log = LogFactory.getLog(GradebookHelperImpl.class);

  /**
   * Hardcoded stub.  Get current gradebook uid.
   * @return Gradebook #10
   */
  public String getGradebookUId(String siteId){
    return "QA_8";
  }

  /**
   * Hardcoded stub.  Get teh default gradebook uid.
   * @return Gradebook #1
   */
  public String getDefaultGradebookUId(){
    return "QA_1";
  }
}
