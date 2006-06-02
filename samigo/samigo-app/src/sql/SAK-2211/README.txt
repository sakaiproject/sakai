IMPORTANT: You should upgrade your production server to sakai 2.1 and make sure that the database is running 2.1 schema with 2.1 default data populated before running this patch.

After expanding the tar file, check that 

1. update the database.properties to reflects your database setting

2. add the appropriate JDBC driver in lib/
   e.g. ojdbc14.jar for oracle 9i

3. This is a java application for

   a. get a list of REALM_ID of project site and course site

   java -classpath .:lib/ojdbc14.jar FixRealmPermission getRealm

   b. add the Samigo permissions for project and course site created before sakai v2.1
      If Samigo permissions already exist in these site, no new record would be added.
      So it is safe to run it on system with sites created by sakai 2.1. As always, please
      run script on a copy of your production data, check before running it on the real thing.

   java -classpath .:lib/ojdbc14.jar FixRealmPermission fixRealmPermission


   c. If you wish to inspect the SQL stmt, do this

   java -classpath .:lib/ojdbc14.jar FixRealmPermission getInsertStatement >insert.sql


