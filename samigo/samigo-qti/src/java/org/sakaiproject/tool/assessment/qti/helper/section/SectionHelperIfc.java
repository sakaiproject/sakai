/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/qti/helper/section/SectionHelperIfc.java $
 * $Id: SectionHelperIfc.java 9274 2006-05-10 22:50:48Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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



package org.sakaiproject.tool.assessment.qti.helper.section;

import java.io.InputStream;

import org.sakaiproject.tool.assessment.qti.asi.Section;

/**
 * Interface for QTI-versioned section helper implementation.
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id: SectionHelperIfc.java 9274 2006-05-10 22:50:48Z daisyf@stanford.edu $
 */

public interface SectionHelperIfc
{
  /**
   * Interface for QTI-versioned section helper implementation.
   * <p>Copyright: Copyright (c) 2005</p>
   * <p>Organization: Sakai Project</p>
   * @author Ed Smiley esmiley@stanford.edu
   * @version $Id: SectionHelperIfc.java 9274 2006-05-10 22:50:48Z daisyf@stanford.edu $
   */

  /**
   * read XML document into Section XML
   *
   * @param inputStream the input stream
   *
   * @return Section XML
   */
  public Section readXMLDocument(InputStream inputStream);


  /**
   * Update section XML XPath with value
   *
   * @param sectionXml XML PENDING
   * @param xpath the XPath
   * @param value the value
   *
   * @return the Section XML
   */
  public Section updateSectionXml(
    Section sectionXml, String xpath,
    String value);

}
