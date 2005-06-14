package org.sakaiproject.tool.assessment.data.dao.assessment;
import java.util.Date;

public class AssessmentTemplateData
    extends  org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentBaseData
    implements java.io.Serializable,
               org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentTemplateIfc
{
  public static String AUTHORS = "ASSESSMENTTEMPLATE_AUTHORS";
  public static String KEYWORDS = "ASSESSMENTTEMPLATE_KEYWORDS";
  public static String OBJECTIVES = "ASSESSMENTTEMPLATE_OBJECTIVES";
  public static String BGCOLOR = "ASSESSMENTTEMPLATE_BGCOLOR";
  public static String BGIMAGE = "ASSESSMENTTEMPLATE_BGIMAGE";

  /* AssessmentTemplate also has AssessmentAccessControl and EvaluationModel
   * but it does not have section
   * private AssessmentAccessControlIfc assessmentAccessControl;
   * private EvaluationModelIfc evaluationModel;
   */

  public AssessmentTemplateData(){
    setIsTemplate(new Boolean("true"));
  }

  public AssessmentTemplateData(Long assessmentTemplateId, String title){
    // in the case of template assessmentBaseId is the assessmentTemplateId
    super(assessmentTemplateId,title);
  }

  public AssessmentTemplateData(Long assessmentTemplateId, String title, Date lastModifiedDate){
    super(assessmentTemplateId,title,lastModifiedDate);
  }

  public AssessmentTemplateData(Long parentId,
                  String title, String description, String comments,
                  Long typeId,
                  Integer instructorNotification, Integer testeeNotification,
                  Integer multipartAllowed, Integer status, String createdBy,
                  Date createdDate, String lastModifiedBy,
                  Date lastModifiedDate) {
    super(new Boolean("true"),parentId,
               title, description, comments,
               typeId,
               instructorNotification, testeeNotification,
               multipartAllowed, status, createdBy,
               createdDate, lastModifiedBy,
               lastModifiedDate);
  }

  public Long getAssessmentTemplateId(){
    return super.getAssessmentBaseId();
  }

  public void setAssessmentTemplateId(Long templateId) {
    super.setAssessmentBaseId(templateId);
  }
}
