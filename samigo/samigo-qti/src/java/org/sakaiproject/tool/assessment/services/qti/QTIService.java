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

package org.sakaiproject.tool.assessment.services.qti;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.qti.constants.QTIVersion;
import org.sakaiproject.tool.assessment.qti.exception.RespondusMatchingException;
import org.sakaiproject.tool.assessment.qti.helper.AuthoringHelper;
import org.sakaiproject.tool.assessment.qti.util.XmlUtil;
import org.sakaiproject.tool.assessment.shared.api.qti.QTIServiceAPI;

/**
 * <p>This service provides translation between database and QTI representations.
 * This is used to import/export IMS QTI format XML, and for web services.
 *  </p>
 * <p>Copyright: Copyright (c) 2005 Sakai</p>
 * <p> </p>
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id$
 */
public class QTIService implements QTIServiceAPI
{
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

  public AssessmentFacade createImportedAssessment(Document document, int qtiVersion, String unzipLocation, String templateId) {
	  return createImportedAssessment(document, qtiVersion, unzipLocation, templateId, null);
  }

  public AssessmentFacade createImportedAssessment(Document document, int qtiVersion, String unzipLocation, String templateId, String siteId) {  
	  testQtiVersion(qtiVersion);

	  try {
		  AuthoringHelper helper = new AuthoringHelper(qtiVersion);
		  return helper.createImportedAssessment(document, unzipLocation, templateId, siteId);
	  } catch (Exception ex) {
		  throw new QTIServiceException(ex);
	  }
  }

  /**
   * Import an assessment XML document in QTI format, extract & persist the data.
   * @param document the assessment XML document in QTI format
   * @param qtiVersion either QTIVersion.VERSION_1_2 or QTIVersion.VERSION_2_0;
   * @return a persisted assessment
   */
  public AssessmentFacade createImportedAssessment(Document document, int qtiVersion, String unzipLocation)
  {
	  return createImportedAssessment(document, qtiVersion, unzipLocation, false, null);
  }
  
  public AssessmentFacade createImportedAssessment(Document document, int qtiVersion, String unzipLocation, boolean isRespondus, List failedMatchingQuestions)
  {
    testQtiVersion(qtiVersion);

    try
    {
      AuthoringHelper helper = new AuthoringHelper(qtiVersion);
      return helper.createImportedAssessment(document, unzipLocation, isRespondus, failedMatchingQuestions);
    }
    catch (Exception ex)
    {
      throw new QTIServiceException(ex);
    }
  }

  /**
   * Import an assessment XML document in QTI format, extract & persist the data.
   * @param documentPath the pathname to a file with the assessment XML document in QTI format
   * @param qtiVersion either 1=QTI VERSION 1.2  or 2=QTI Version 2.0
   * @param siteId the site the assessment will be associated with
   * @return a persisted assessment
   */
    public AssessmentFacade createImportedAssessment(String documentPath, int qtiVersion, String siteId) 
    {
        try {
            return createImportedAssessment(XmlUtil.readDocument(documentPath, true),
                                            qtiVersion, null, null, siteId);
        } catch (Exception e) {
            throw new QTIServiceException(e);
        }
                                                
    }

  
  /**
   * Import an assessment XML document in QTI format, extract & persist the data.
   * import process assumes assessment structure, not objectbank or itembank
   * based on usage in other potential migration systems, Respondus, BlackBoard, etc.
   * QTI version 2.x will probably focus on content packaging for question pools  
   * @param document the assessment XML document in QTI format
   * @param qtiVersion QTIVersion.VERSION_1_2;
   * @return a persisted assessment
   */  
  public QuestionPoolFacade createImportedQuestionPool(Document document, int qtiVersion)
  {
	  testQtiVersion(qtiVersion);

	  try
	  {
		  AuthoringHelper helper = new AuthoringHelper(qtiVersion);
	      return helper.createImportedQuestionPool(document);
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
