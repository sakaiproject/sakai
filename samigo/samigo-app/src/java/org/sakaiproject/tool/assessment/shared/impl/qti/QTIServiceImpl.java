/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.shared.impl.qti;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;

import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.services.qti.QTIService;
import org.sakaiproject.tool.assessment.shared.api.qti.QTIServiceAPI;
import org.sakaiproject.tool.assessment.services.qti.QTIServiceException;
import org.sakaiproject.tool.assessment.qti.util.XmlUtil;

/**
 * QTIServiceImpl implements a shared interface to get/set assessment
 * information.
 * @author Ed Smiley <esmiley@stanford.edu>
 */
@Slf4j
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
      return (AssessmentIfc) nativeQTIService.createImportedAssessment(document, qtiVersion, null);
    }
    catch (Exception ex)
    {
       log.warn("createImportedAssessment() returning null");
       //new QTIServiceException(ex);
    }
    log.error("createImportedAssessment() returning null");
    return null;
  }

   public AssessmentIfc createImportedAssessment(Document document, int qtiVersion, String unzipLocation, String templateId, String siteId) {
    try
    {
      QTIService nativeQTIService = new QTIService();
      return (AssessmentIfc) nativeQTIService.createImportedAssessment(document, qtiVersion, unzipLocation, templateId, siteId);
    }
    catch (Exception ex)
    {
       log.warn("createImportedAssessment() returning null");
       //new QTIServiceException(ex);
    }
    log.error("createImportedAssessment() returning null");
    return null;

   }

  /**
   * Import an assessment XML document in QTI format, extract & persist the data.
   * @param documentPath the pathname to a file with the assessment XML document in QTI format
   * @param qtiVersion either 1=QTI VERSION 1.2  or 2=QTI Version 2.0
   * @param siteId the site the assessment will be associated with
   * @return a persisted assessment
   */
    public AssessmentIfc createImportedAssessment(String documentPath, int qtiVersion, String siteId) 
    {
        try
        {
            QTIService nativeQTIService = new QTIService();
            return (AssessmentIfc) nativeQTIService.createImportedAssessment(documentPath, qtiVersion, siteId);
        }
        catch (Exception ex)
        {
            log.warn("createImportedAssessment() returning null");
            //new QTIServiceException(ex);
        }
        log.error("createImportedAssessment() returning null");
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
       log.warn("createImportedItem() returning null");
       //new QTIServiceException(ex);
    }
    log.error("createImportedItem() returning null");
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
       log.warn("getExportedAssessment() returning null");
       //new QTIServiceException(ex);
    }
    log.error("getExportedAssessment() returning null");
    return null;
  }


  /**
   * Get an assessment in String form.
   *
   * Note:  this service requires a Faces context.
   *
   * @param assessmentId the assessment's Id
   * @param qtiVersion either 1=QTI VERSION 1.2  or 2=QTI Version 2.0
   * @return the Document with the assessment data
   */
    public String getExportedAssessmentAsString(String assessmentId, int qtiVersion) 
  {
      return XmlUtil.getDOMString(getExportedAssessment(assessmentId, qtiVersion));
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
       log.warn("getExportedItem() returning null");
       //new QTIServiceException(ex);
    }
    log.error("getExportedItem() returning null");
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
      log.warn("getExportedItemBank() returning null");
      // new QTIServiceException(ex);
    }
    log.error("getExportedItemBank() returning null");
    return null;
  }

}
