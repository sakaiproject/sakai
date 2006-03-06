I. Description of the problem (SAK-3955) that this script solve:

When you answer a MC question (say), you pick A as the answer, a itemGrading record is created holding the answer that you have selected. Then you change you mind, return to the question and select B as the answer. Another itemGrading record is created, so now you have two records of itemGrading. In version before 2.1.1, the itemGrading for the old answer was set to NULL and autoScore set to 0. So the scores add up correctly. 
 
In 2.1.1, when the 2nd record of itemGrading were created, the old record didn't get set to NULL and 0. When the score is being added up, the first answer were always being picked. Like some say, people usually don't remember what they pick. So I guess that's why this bug (SAK-3955) has been hidden. 
 
Lydia & I discovered this problem when we were working on optimizing no. of SQL being called in delivery. We were surprised to find out that the implementation at the time  kept the old answers for MC/MCMR/Surevy. Because of this arrangement, questionScore page is slow and in some occasion, you can get time out exception too. So after much thought, we rewrote the delivery so all old answers will be deleted. This is now in 2.1.2.
 
The script that I wrote looks for old answers based on the itemGradingId and set answer to NULL and score to 0 just like version before 2.1.1. However, it can fix MC & Surevy but it can't fix MCMR. Lydia & I decided the best strategy is to leave MCMR alone as is. 

SAK-3955 managed to slip passed QA and instructors for a month after deployment. This is probably because most users don't change their answers in MC/MCMR/Survey. So maybe you only want to run it on a particular assessment when instructor complains. 

-daisyf 03/06/06


II. Using this script

After expanding the tar file, check that 

1. update the database.properties to reflects your database setting

2. add the appropriate JDBC driver in lib/
   e.g. ojdbc14.jar for oracle 9i

3. This is a java application for correcting the assessment score of each published assessment corrupted by
   SAK-3955. See SAK-3955 for details.

   a. to fix all itemGrading record of MC & Surevy and assessmentGrading records of the given published assessment
   table affected: SAM_ITEMGRADING_T, SAM_ASSESSMENTGRADING_T and GB_GRADE_RECORD_T

   java -classpath .:lib/ojdbc14.jar FixAssessmentScore fixAssessmentScore <publishedAssessmentId>

   b. to get a print out of all the SQL stmt

   java -classpath .:lib/ojdbc14.jar FixAssessmentScore printFixAssessmentScore <publishedAssessmentId>


