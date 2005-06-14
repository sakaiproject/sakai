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
