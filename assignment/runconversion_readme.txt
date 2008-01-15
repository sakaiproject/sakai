About the Conversion Script
-------------------------------------------------------------------------------
The conversion script has been included in assignment trunk and post-2-4 branch now. You will need to run the conversion script first before starting your server with new Assignment code.

The purposes of doing the conversion are mainly:
1) remove existing duplicate submission records, if any;
2) prevent future submission duplicates by applying unique constraint on the ASSIGNMENT_SUBMISSION table;
3) improve performance of the Assignment tool;

Please take a look at the related JIRA SAK-11821 for more detailed descriptions about the conversion script. Basically, the conversion script does the following to your existing ASSIGNMENT_SUBMISSION table in Sakai database:
1) read in all tuples as AssignmentSubmission object, parse out data such as submitter_id, submit_time, submitted, and graded, and store those attributes as separate columns in the ASSIGNMENT_SUBMISSION table;
2) run though the table, combine and remove submission duplicates (tuples with same "context" and "submitter_id" combination);
3) apply the unique constraint of "context" + "submitter_id" to ASSIGNMENT_SUBMISSION table.


Steps to Run the Conversion Script
-------------------------------------------------------------------------------
Please follow the following steps to run the conversion from 2.4.0, 2.4.1, 2.4.x or previous post-2-4 Assignment to current Assignment trunk database schema for assignment (AssignmentService) to improve performance of the Assignment tool.  Unless otherwise  indicated, all files referred to are in the root directory of the assignment project. These instructions apply to MySQL or Oracle.  If a different database is used, a new version of the config file will be needed.

Always do a test run on a production snapshot first. During the test run, time the script so you get an idea how long the production conversion takes.

Detail steps to run the scripts are shown as follows:

1) Edit the runconversion.sh and provide the path to the appropriate JDBC connector for your database, and correct name of various jar files.  Examples are shown for a MySQL driver in the local maven-2 repository or in the tomcat-commons-lib directory and for the Oracle driver in the tomcat-commons-lib directory.  If a different path is needed to find your driver, please add it to the CLASSPATH.
   
2) Edit the appropriate upgradeschema_*.config file for your database (either upgradeschema_mysql.config or upgradeschema_oracle.config) and supply datababase URL, username and password for your database where indicated:

		dbDriver=oracle.jdbc.driver.OracleDriver
		dbURL=PUT_YOUR_URL_HERE
		dbUser=PUT_YOUR_USERNAME_HERE
		dbPass=PUT_YOUR_PASSWORD_HERE

3) Shutdown your tomcat instance;

4) Compile and deploy the relevant assignment trunk code;

5) Run the conversion by running the shellscript and supplying the name of the config file as a parameter. Redirect the output into a file for record keeping and potential debugging. 

	For example, to convert the database  schema for MySQL, the script would be invoked as follows:
   
		> time ./runconversion.sh upgradeschema_mysql.config > upgrade-output.txt
	
	To convert an Oracle database, the script would be invoked as follows:
   
		> time ./runconversion.sh upgradeschema_oracle.config > upgrade-output.txt
   
6) If the conversion finishes successfully, start up the tomcat instance again.  
  

FAQ
-------------------------------------------------------------------------------
1) Q: Is there a way to confirm that the conversion was successful? I did a describe on the ASSIGNMENT_SUBMISSION table, but maybe that's not enough.

A: Desc will give you the new table structure, which should be established during the first phase of the conversion. There should be some error messages during that phase if the new columns cannot be added. Besides, the following conversion phases won't be able to proceed because they depend on the new table structure.

As for the duplicates removal part, the 2nd phase started with identifying all the duplicates, combine and mark which record to retain, and let the 3rd phase finish the job by removing the 
unwanted duplicate record. So by the time the third phase finishes, we are in good shape that no more duplicates are left. And that's why the constraints of (context, submitter_id) can be successfully applied.

2) Q: What can I do from the front-end and/or the backend to verify that everything is working OK once the conversion is done? For example, if I create a new assignment, submit it, what shoudl I see in the tables?

A: If you create a new assignment, nothing will be inserted into assignment_submission table yet. But by the time some student submits or the instructor clicks on the grade link, some record should appear in that table.

Ideally, no submitter_id or context column should have null values. This is reinforced once recently fix of SAK-12628. And you shouldn't see any abnormality from the UI.
	