After expanding the tar file, check that 

1. update the database.properties to reflects your database setting

2. add the appropriate JDBC driver in lib/
   e.g. ojdbc14.jar for oracle 9i

3. This is a java application for

   a. correcting the answer score of each individual question 
   created from an imported assessment using a QTI xml document.
   table affected: SAM_ANSWER_T and SAM_PUBLISHEDANSWER_T

   java -classpath .:lib/ojdbc14.jar FixAnswerScore fixAnswerScore

   b. regrading a given published assessment
   table affected: SAM_ITEMGRADING_T and SAM_ASSESSMENTGRADING_T

   java -classpath .:lib/ojdbc14.jar FixAnswerScore regrade <publishedAssessmentId>

   c. to do both 1 and 2 in one go, use this

   java -classpath .:lib/ojdbc14.jar FixAnswerScore fixAndRegrade <publishedAssessmentId>

