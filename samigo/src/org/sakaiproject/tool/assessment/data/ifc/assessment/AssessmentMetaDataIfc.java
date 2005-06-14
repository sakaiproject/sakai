package org.sakaiproject.tool.assessment.data.ifc.assessment;

public interface AssessmentMetaDataIfc
    extends java.io.Serializable
{
  public static String AUTHORS = "ASSESSMENT_AUTHORS";
  public static String KEYWORDS = "ASSESSMENT_KEYWORDS";
  public static String OBJECTIVES = "ASSESSMENT_OBJECTIVES";
  public static String RUBRICS = "ASSESSMENT_RUBRICS";
  public static String BGCOLOR = "ASSESSMENT_BGCOLOR";
  public static String BGIMAGE = "ASSESSMENT_BGIMAGE";
  public static String ALIAS = "ALIAS";

  Long getId();

  void setId(Long id);

  AssessmentBaseIfc getAssessment();

  void setAssessment(AssessmentBaseIfc assessment);

  String getLabel();

  void setLabel(String label);

  String getEntry();

  void setEntry(String entry);

}
