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

package org.sakaiproject.tool.assessment.business.entity.helper.section;

import java.io.InputStream;
import java.util.ArrayList;

import org.sakaiproject.tool.assessment.business.entity.asi.Section;

/**
 * Interface for QTI-versioned section helper implementation.
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id$
 */

public interface SectionHelperIfc
{
  /**
   * Interface for QTI-versioned section helper implementation.
   * <p>Copyright: Copyright (c) 2005</p>
   * <p>Organization: Sakai Project</p>
   * @author Ed Smiley esmiley@stanford.edu
   * @version $Id$
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
