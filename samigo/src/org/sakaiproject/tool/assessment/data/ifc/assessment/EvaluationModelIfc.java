package org.sakaiproject.tool.assessment.data.ifc.assessment;


/**
 * This keeps track of the submission scheme, and the number allowed.
 *
 * @author Rachel Gollub
 */
public interface EvaluationModelIfc
    extends java.io.Serializable
{

  public static Integer ANONYMOUS_GRADING = new Integer(1);
  public static Integer NON_ANONYMOUS_GRADING = new Integer(2);
  public static Integer GRADEBOOK_NOT_AVAILABLE = new Integer(0);
  public static Integer TO_DEFAULT_GRADEBOOK = new Integer(1);
  public static Integer TO_SELECTED_GRADEBOOK = new Integer(2);
  public static Integer HIGHEST_SCORE = new Integer(1);
  public static Integer AVERAGE_SCORE = new Integer(2);

  Long getId();

  void setId(Long id);

  void setAssessmentBase(AssessmentBaseIfc assessmentBase);

  AssessmentBaseIfc getAssessmentBase();

  String getEvaluationComponents();

  void setEvaluationComponents(String evaluationComponents);

  Integer getScoringType();

  void setScoringType(Integer scoringType);

  String getNumericModelId();

  void setNumericModelId(String numericModelId);

  Integer getFixedTotalScore();

  void setFixedTotalScore(Integer fixedTotalScore);

  Integer getGradeAvailable();

  void setGradeAvailable(Integer gradeAvailable);

  Integer getIsStudentIdPublic();

  void setAnonymousGrading(Integer anonymousGrading);

  Integer getAnonymousGrading();

  void setAutoScoring(Integer autoScoring);

  Integer getAutoScoring();

  void setIsStudentIdPublic(Integer isStudentIdPublic);

  String getToGradeBook();

  void setToGradeBook(String toGradeBook);
}
