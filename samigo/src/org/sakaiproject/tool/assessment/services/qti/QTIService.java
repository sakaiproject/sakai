/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.services.qti;

import org.w3c.dom.Document;

import org.sakaiproject.tool.assessment.qti.constants.QTIVersion;
import org.sakaiproject.tool.assessment.qti.helper.AuthoringHelper;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>This service provides translation between database and QTI representations.
 * This is used to import/export IMS QTI format XML, and for web services.
 *  </p>
 * <p>Copyright: Copyright (c) 2005 Sakai</p>
 * <p> </p>
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id$
 */

public class QTIService
{
  private static Log log = LogFactory.getLog(QTIService.class);
  public QTIService()
  {
  }

  /**
   * Import an assessment XML document in QTI format, extract & persist the data.
   * @param document the assessment XML document in QTI format
   * @param qtiVersion either QTIVersion.VERSION_1_2 or QTIVersion.VERSION_2_0;
   * @return a persisted assessment
   */
  public AssessmentFacade createImportedAssessment(Document document, int qtiVersion)
  {
    testQtiVersion(qtiVersion);

    try
    {
      AuthoringHelper helper = new AuthoringHelper(qtiVersion);
      return helper.createImportedAssessment(document);
    }
    catch (Exception ex)
    {
      throw new QTIServiceException(ex);
    }
  }

  /**
   * Import an item XML document in QTI format, extract & persist the data.
   * @param document the item XML document in QTI format
   * @param qtiVersion either QTIVersion.VERSION_1_2 or QTIVersion.VERSION_2_0;
   * @return a persisted item
   */
  public ItemFacade createImportedItem(Document document, int qtiVersion)
  {
    testQtiVersion(qtiVersion);

    try
    {
      AuthoringHelper helper = new AuthoringHelper(qtiVersion);
      return helper.createImportedItem(document);
    }
    catch (Exception ex)
    {
      throw new QTIServiceException(ex);
    }

  }

  /**
   * Get an assessment in Document form.
   *
   * Note:  this service requires a Faces context.
   *
   * @param assessmentId the assessment's Id
   * @param qtiVersion either QTIVersion.VERSION_1_2 or QTIVersion.VERSION_2_0;
   * @return the Document with the assessment data
   */
  public Document getExportedAssessment(String assessmentId,
    int qtiVersion)
  {
    testQtiVersion(qtiVersion);

    try
    {
      AuthoringHelper helper = new AuthoringHelper(qtiVersion);
      return helper.getAssessment(assessmentId);
    }
    catch (Exception ex)
    {
      throw new QTIServiceException(ex);
    }
  }

  /**
   * Get an item in Document form.
   *
   * Note:  this service requires a Faces context.
   *
   * @param itemId the item's Id
   * @param qtiVersion either QTIVersion.VERSION_1_2 or QTIVersion.VERSION_2_0;
   * @return the Document with the assessment data
   */
  public Document getExportedItem(String itemId, int qtiVersion)
  {
    testQtiVersion(qtiVersion);

    try
    {
      AuthoringHelper helper = new AuthoringHelper(qtiVersion);
      return helper.getItem(itemId);
    }
    catch (Exception ex)
    {
      throw new QTIServiceException(ex);
    }
  }

  /**
   * Get an item bank in Document form.
   *
   * Note:  this service requires a Faces context.
   *
   * @param itemIds an array of item ids
   * @param qtiVersion either QTIVersion.VERSION_1_2 or QTIVersion.VERSION_2_0;
   * @return the Document with the item bank
   */
  public Document getExportedItemBank(String itemIds[], int qtiVersion)
  {
    testQtiVersion(qtiVersion);

    try
    {
      AuthoringHelper helper = new AuthoringHelper(qtiVersion);
      return helper.getItemBank( itemIds);
    }
    catch (Exception ex)
    {
      throw new QTIServiceException(ex);
    }
  }

  /**
   * utility method
   * @param qtiVersion
   */
  private void testQtiVersion(int qtiVersion)
  {
    if (!QTIVersion.isValid(qtiVersion))
    {
      throw new QTIServiceException(
        new IllegalArgumentException("NOT Legal Qti Version."));
    }
  }

}
