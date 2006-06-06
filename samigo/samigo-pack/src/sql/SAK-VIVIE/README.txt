This script is written for Vivie@Foothill to fix finalScore in assessmentGrading record whereas scorings in itemGrading appears to be calculated correctly.

After expanding the tar file, check that 

1. update the database.properties to reflects your database setting

2. add the appropriate JDBC driver in lib/
   e.g. .:lib/mysql-connector-java-3.1.12-bin.jar for mysql

3. This is a java application for correcting the assessment score and gradebook score
   of each published assessment. Note the the score that is written to gradebook is based on
   the scoring type (highest, last) selected for the publsihed assessment.

   a. to fix all assessmentGrading records of the given publsihed assessment
   table affected: SAM_ASSESSMENTGRADING_T and GB_GRADE_RECORD_T

   java -classpath .:lib/mysql-connector-java-3.1.12-bin.jar FixGradingScore fixGradingScore <publishedAssessmentId>

   b. to get a print out of all the SQL stmt

   java -classpath .:lib/mysql-connector-java-3.1.12-bin.jar FixGradingScore printFixGradingScore <publishedAssessmentId>


