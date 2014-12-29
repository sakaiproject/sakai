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



package org.sakaiproject.tool.assessment.qti.helper;

import java.io.Serializable;

import org.sakaiproject.tool.assessment.qti.constants.QTIVersion;
import org.sakaiproject.tool.assessment.qti.helper.assessment.AssessmentHelper12Impl;
import org.sakaiproject.tool.assessment.qti.helper.assessment.AssessmentHelper20Impl;
import org.sakaiproject.tool.assessment.qti.helper.assessment.AssessmentHelperIfc;
import org.sakaiproject.tool.assessment.qti.helper.item.ItemHelper12Impl;
import org.sakaiproject.tool.assessment.qti.helper.item.ItemHelper20Impl;
import org.sakaiproject.tool.assessment.qti.helper.item.ItemHelperIfc;
import org.sakaiproject.tool.assessment.qti.helper.section.SectionHelper12Impl;
import org.sakaiproject.tool.assessment.qti.helper.section.SectionHelper20Impl;
import org.sakaiproject.tool.assessment.qti.helper.section.SectionHelperIfc;

/**
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id$
 */

public class QTIHelperFactory implements Serializable
{
  private String VERSION_SUPPORTED_STRING =
    "Version Codes supported: QTIVersion.VERSION_1_2, QTIVersion.VERSION_2_0";

  /**
   * Factory method. ItemHelperIfc.
   * @param versionCode supported: QTIVersion.VERSION_1_2, QTIVersion.VERSION_2_0
   * @return ItemHelperIfc
   */
  public ItemHelperIfc getItemHelperInstance(int versionCode)
  {
    switch (versionCode)
    {
      case QTIVersion.VERSION_1_2:
        return new ItemHelper12Impl();// very specific code for v 1.2
      case QTIVersion.VERSION_2_0:
        return new ItemHelper20Impl();// very specific code for v 2.0 (stubbed)
      default:
        throw new IllegalArgumentException(
          VERSION_SUPPORTED_STRING);
    }
  }

  /**
   * Factory method. SectionHelperIfc.
   * @param versionCode supported: QTIVersion.VERSION_1_2, QTIVersion.VERSION_2_0
   * @return SectionHelperIfc
   */
  public SectionHelperIfc getSectionHelperInstance(int versionCode)
  {
    switch (versionCode)
    {
      case QTIVersion.VERSION_1_2:
        return new SectionHelper12Impl();// very thin subclass of SectionHelper
      case QTIVersion.VERSION_2_0:
        return new SectionHelper20Impl();// very thin subclass of SectionHelper
      default:
        throw new IllegalArgumentException(
          VERSION_SUPPORTED_STRING);
    }
  }

  /**
   * Factory method. AssessmentHelperIfc.
   * @param versionCode supported: QTIVersion.VERSION_1_2, QTIVersion.VERSION_2_0
   * @return AssessmentHelperIfc
   */
  public AssessmentHelperIfc getAssessmentHelperInstance(int versionCode)
  {
    switch (versionCode)
    {
      case QTIVersion.VERSION_1_2:
        return new AssessmentHelper12Impl();// very thin subclass of AssessmentHelper
      case QTIVersion.VERSION_2_0:
        return new AssessmentHelper20Impl();// very thin subclass of AssessmentHelper
      default:
        throw new IllegalArgumentException(
          VERSION_SUPPORTED_STRING);
    }
  }

}


