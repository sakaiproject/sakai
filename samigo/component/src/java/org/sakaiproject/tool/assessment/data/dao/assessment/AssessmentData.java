/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
package org.sakaiproject.tool.assessment.data.dao.assessment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;

public class AssessmentData extends org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentBaseData
    implements java.io.Serializable, AssessmentIfc
{

  // both Assessment and AssessmentTemplate inherits all the properties & methods from
  // AssessmentBaseData.
  // These are the properties that an assessment has and an assessmentTemplate don't
  private Long assessmentTemplateId;
  private Set sectionSet;
  /* Assessment has AssessmentAccessControl and EvaluationModel
   * as well as a set of Sections
   * private AssessmentAccessControlIfc assessmentAccessControl;
   * private EvaluationModelIfc evaluationModel;
   */

  public AssessmentData(){
    setIsTemplate(new Boolean("false"));
  }

  public AssessmentData(Long assessmentTemplateId, String title, Date lastModifiedDate){
    // in the case of template assessmentBaseId is the assessmentTemplateId
    super(assessmentTemplateId,title,lastModifiedDate);
  }

  public AssessmentData(Long parentId,
                  String title, String description, String comments,
                  Long assessmentTemplateId, Long typeId,
                  Integer instructorNotification, Integer testeeNotification,
                  Integer multipartAllowed, Integer status, String createdBy,
                  Date createdDate, String lastModifiedBy,
                  Date lastModifiedDate) {
    super(new Boolean("false"),parentId,
               title, description, comments,
               typeId,
               instructorNotification, testeeNotification,
               multipartAllowed, status, createdBy,
               createdDate, lastModifiedBy,
               lastModifiedDate);
    this.assessmentTemplateId = assessmentTemplateId;
  }

  public Long getAssessmentId(){
    return getAssessmentBaseId();
  }

  public Long getAssessmentTemplateId() {
    return this.assessmentTemplateId;
 }

  public void setAssessmentTemplateId(Long assessmentTemplateId) {
    this.assessmentTemplateId = assessmentTemplateId;
  }

  public Set getSectionSet() {
    return sectionSet;
  }

  public void setSectionSet(Set sectionSet) {
    this.sectionSet = sectionSet;
  }

  public ArrayList getSectionArray() {
    ArrayList list = new ArrayList();
    Iterator iter = sectionSet.iterator();
    while (iter.hasNext()){
      list.add(iter.next());
    }
    return list;
  }

  public ArrayList getSectionArraySorted() {
    ArrayList list = getSectionArray();
    Collections.sort(list);
    return list;
  }

  public SectionDataIfc getSection(Long sequence){
    ArrayList list = getSectionArraySorted();
    if (list == null)
      return null;
    else
      return (SectionDataIfc) list.get(sequence.intValue()-1);
  }

  public SectionDataIfc getDefaultSection(){
    ArrayList list = getSectionArraySorted();
    if (list == null)
      return null;
    else
      return (SectionDataIfc) list.get(0);
  }

}
