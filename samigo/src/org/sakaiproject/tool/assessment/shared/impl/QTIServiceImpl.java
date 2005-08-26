/**********************************************************************************
* $URL: https://source.sakaiproject.org/svn/trunk/sakai/sam/src/org/sakaiproject/tool/assessment/services/qti/QTIService.java $
* $Id: QTIService.java 632 2005-07-14 21:22:50Z janderse@umich.edu $
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

package org.sakaiproject.tool.assessment.shared.impl;

import org.w3c.dom.Document;

import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.shared.api.qti.QTIServiceAPI;
import org.sakaiproject.tool.assessment.services.qti.QTIService;
import org.sakaiproject.tool.assessment.shared.api.qti.QTIServiceException;

public class QTIServiceImpl implements QTIServiceAPI
{
  /**
 * Import an assessment XML document in QTI format, extract & persist the data.
 * @param document the assessment XML document in QTI format
 * @param qtiVersion either 1=QTI VERSION 1.2  or 2=QTI Version 2.0;
 * @return a persisted assessment
 */

  public AssessmentIfc createImportedAssessment(Document document, int qtiVersion)
  {
    try
    {
      QTIService nativeQTIService = new QTIService();
      return (AssessmentIfc) nativeQTIService.createImportedAssessment(document, qtiVersion);
    }
    catch (Exception ex)
    {
       new QTIServiceException(ex);
    }
    return null;
  }

  /**
   * Import an item XML document in QTI format, extract & persist the data.
   * @param document the item XML document in QTI format
   * @param qtiVersion either 1=QTI VERSION 1.2  or 2=QTI Version 2.0;
   * @return a persisted item
   */

  public ItemDataIfc createImportedItem(Document document, int qtiVersion)
  {
    try
    {
      QTIService nativeQTIService = new QTIService();
      return (ItemDataIfc) nativeQTIService.createImportedItem(document, qtiVersion);
    }
    catch (Exception ex)
    {
       new QTIServiceException(ex);
    }
    return null;
  }


  /**
   * Get an assessment in Document form.
   *
   * Note:  this service requires a Faces context.
   *
   * @param assessmentId the assessment's Id
   * @param qtiVersion either 1=QTI VERSION 1.2  or 2=QTI Version 2.0;
   * @return the Document with the assessment data
   */
  public Document getExportedAssessment(String assessmentId, int qtiVersion)
  {
    try
    {
      QTIService nativeQTIService = new QTIService();
      return nativeQTIService.getExportedAssessment(assessmentId, qtiVersion);
    }
    catch (Exception ex)
    {
       new QTIServiceException(ex);
    }
    return null;
  }

  /**
   * Get an item in Document form.
   *
   * Note:  this service requires a Faces context.
   *
   * @param itemId the item's Id
   * @param qtiVersion either 1=QTI VERSION 1.2  or 2=QTI Version 2.0;
   * @return the Document with the assessment data
   */

  public Document getExportedItem(String itemId, int qtiVersion)
  {
    try
    {
      QTIService nativeQTIService = new QTIService();
      return nativeQTIService.getExportedAssessment(itemId, qtiVersion);
    }
    catch (Exception ex)
    {
       new QTIServiceException(ex);
    }
    return null;
  }


  /**
   * Get an item bank in Document form.
   *
   * Note:  this service requires a Faces context.
   *
   * @param itemIds an array of item ids
   * @param qtiVersion either 1=QTI VERSION 1.2  or 2=QTI Version 2.0;
   * @return the Document with the item bank
   */
  public Document getExportedItemBank(String[] itemIds, int qtiVersion)
  {
    try
    {
      QTIService nativeQTIService = new QTIService();
      return nativeQTIService.getExportedItemBank(itemIds, qtiVersion);
    }
    catch (Exception ex)
    {
       new QTIServiceException(ex);
    }
    return null;
  }

}