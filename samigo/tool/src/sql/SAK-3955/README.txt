After expanding the tar file, check that 

1. update the database.properties to reflects your database setting

2. add the appropriate JDBC driver in lib/
   e.g. ojdbc14.jar for oracle 9i

3. This is a java application for correcting the assessment score of each published assessment
   See SAK-3955 for details.

   a. to fix all itemGrading and assessmentGrading records of the given publsihed assessment
   table affected: SAM_ITEMGRADING_T, SAM_ASSESSMENTGRADING_T and GB_GRADE_RECORD_T

   java -classpath .:lib/ojdbc14.jar FixAssessmentScore fixAssessmentScore <publishedAssessmentId>

   b. to get a print out of all the SQL stmt

   java -classpath .:lib/ojdbc14.jar FixAssessmentScore printFixAssessmentScore <publishedAssessmentId>


