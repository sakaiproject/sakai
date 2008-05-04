The following steps are required to run the conversion from 2.4 to 2.5 
database schema for content (ContentHostingService) to improve performance
of the Resources tool and other tools that depend on it.  Unless otherwise 
indicated, all files referred to are in the root director of the content 
project. These instructions apply to MySQL or Oracle.  If a different 
database is used, a new version of the config file will be needed.

1) Edit the runconversion.sh and provide the path to the appropriate JDBC 
   connector for your database.  Examples are shown for a MySQL driver in 
   the local maven-2 repository or in the tomcat-commons-lib directory and
   for the Oracle driver in the tomcat-commons-lib directory.  If a different
   path is needed to find your driver, please add it to the CLASSPATH.
   
2) Edit the appropriate upgradeschema-*.config file for your database 
   (either upgradeschema-mysql.config or upgradeschema-oracle.config) and
   supply URL, username and password for your database where indicated:

		dbDriver=oracle.jdbc.driver.OracleDriver
		dbURL=PUT_YOUR_URL_HERE
		dbUser=PUT_YOUR_USERNAME_HERE
		dbPass=PUT_YOUR_PASSWORD_HERE

3) Run the conversion by running the shellscript and supplying the name of
   the config file as a parameter.  For example, to convert the database 
   schema for MySQL, the script would be invoked as follows:
   
		> ./runconversion.sh upgradeschema-mysql.config 

   To convert an Oracle database, the script would be invoked as follows:
   
		> ./runconversion.sh upgradeschema-oracle.config 
   
   