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

package org.sakaiproject.tool.assessment.qti.helper.assessment;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.assessment.qti.constants.QTIVersion;

/**
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Organization: Sakai Project</p>
 * <p>Support any QTI 2.0 only XML handling code</p>
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id$
 */

// Note assessment for QTI 1.2 and 2.0 are nearly identical
@Slf4j
public class AssessmentHelper20Impl extends AssessmentHelperBase
{

  public AssessmentHelper20Impl()
  {
    log.debug("AssessmentHelper20Impl");
  }

  /**
   * implementation of base class method
   * @return
   */
  protected int getQtiVersion()
  {
    return QTIVersion.VERSION_2_0;
  }
}
