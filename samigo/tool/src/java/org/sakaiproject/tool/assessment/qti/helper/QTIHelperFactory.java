/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003-2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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


