/**********************************************************************************
* $URL: https://source.sakaiproject.org/svn/trunk/sakai/sam/src/org/sakaiproject/tool/assessment/services/assessment/AssessmentService.java $
* $Id: AssessmentService.java 632 2005-07-14 21:22:50Z janderse@umich.edu $
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

package org.sakaiproject.tool.assessment.shared.api.assessment;

import java.util.List;

import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentTemplateData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentTemplateIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;

/**
 * The AssessmentServiceAPI declares a shared interface to get/set assessment
 * information.
 * @author Ed Smiley <esmiley@stanford.edu>
 */
public interface AssessmentServiceAPI
{
  public AssessmentTemplateIfc getAssessmentTemplate(String assessmentTemplateId);

  public AssessmentIfc getAssessment(String assessmentId);

  public AssessmentIfc getBasicInfoOfAnAssessment(String assessmentId);

  public List getAllAssessmentTemplates();

  public List getAllActiveAssessmentTemplates();

  public List getTitleOfAllActiveAssessmentTemplates();

  public List getAllAssessments(String orderBy);

  public List getAllActiveAssessments(String orderBy);

  public List getSettingsOfAllActiveAssessments(String orderBy);

  public List getBasicInfoOfAllActiveAssessments(String orderBy, boolean ascending);

  public List getBasicInfoOfAllActiveAssessments(String orderBy);

  public List getAllAssessments(
      int pageSize, int pageNumber, String orderBy);

  public AssessmentIfc createAssessment(
    String title, String description, String typeId, String templateId);

  public int getQuestionSize(String assessmentId);

  public void update(AssessmentIfc assessment);

  public void save(AssessmentTemplateIfc template);

  public void saveAssessment(AssessmentIfc assessment);

  public void deleteAssessmentTemplate(Long assessmentId);

  public void removeAssessment(String assessmentId);

  public SectionDataIfc addSection(String assessmentId);

  public void removeSection(String sectionId);

  public SectionDataIfc getSection(String sectionId);

  public void saveOrUpdateSection(SectionDataIfc section);

  public void moveAllItems(String sourceSectionId, String destSectionId);

  public void removeAllItems(String sourceSectionId);

  public List getBasicInfoOfAllActiveAssessmentTemplates(String orderBy);

  public AssessmentIfc createAssessmentWithoutDefaultSection(
      String title, String description, String typeId, String templateId);
}
