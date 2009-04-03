/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/integration/helper/standalone/PublishingTargetHelperImpl.java $
 * $Id: PublishingTargetHelperImpl.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
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

package org.sakaiproject.tool.assessment.integration.helper.standalone;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.integration.helper.ifc.PublishingTargetHelper;
import org.sakaiproject.tool.assessment.qti.constants.AuthoringConstantStrings;

/**
 *
 * <p>Description:
 * This is a stub standalone context implementation helper delegate class for
 * the PublishingTarget class.  All but one method is unimplemented.
 * "Standalone" means that Samigo (Tests and Quizzes)
 * is running without the context of the Sakai portal and authentication
 * mechanisms, and therefore we leave standalone-unused methods unimplemented.</p>
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

public class PublishingTargetHelperImpl implements PublishingTargetHelper
{
  private static Log log = LogFactory.getLog(PublishingTargetHelperImpl.class);

  /**
   * Gets to whom you can publish.
   * @return map of key value pairs:
   *  e.g. "Authenticated Users"->"AUTHENTICATED_USERS"
   */
  public HashMap getTargets()
  {
     HashMap map = new HashMap();
     map.put(AuthoringConstantStrings.ANONYMOUS, "ANONYMOUS_USERS");
     map.put(AuthoringConstantStrings.AUTHENTICATED, "AUTHENTICATED_USERS");
     return map;
  }

}
