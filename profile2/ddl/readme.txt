This is a simple bunch of files put together to make it easy to generate DDL from hibernate HBM files using maven 2
Please let me know if you have any questions
Aaron Zeckoski (aaronz@vt.edu) (azeckoski@gmail.com)

You must have the following:
1) Java and Maven 2 installed and your project setup as a maven 2 project
2) A set of hibernate HBM files and matching persistent POJOs
3) A project structure like so (where impl is the root directory in this case):
impl
- src
-- java
--- **.hbm.xml

How to use this:
1) Copy the contents of this zip/folder into a impl/src/ddl folder (referred to as the ddl folder) so that all the files included are located inside your source code tree
NOTE: You cannot copy this into a folder which already contains a pom.xml
2) Edit the pom.xml file
2A) Change the name, groupId, and artifactId to match your project:
    <name>Sakai Hierarchy DB DDL generator</name>
    <groupId>org.sakaiproject.hierarchy</groupId>
    <artifactId>hierarchy-ddl-generator</artifactId>
2B) Change the parent POM to refer to your project base POM, if you have no project base POM then you can remove the <parent> tag
    <!-- this should refer to your project base POM -->
    <parent>
        <artifactId>hierarchy</artifactId>
        <groupId>org.sakaiproject</groupId>
        <version>1.2.0-SNAPSHOT</version><!--hierarchy.version-->
    </parent>
2C) Change the project.ddl.name to match your project, this will be the name used for the ddl files (example: hierarchy.sql)
    <properties>
        <!-- change this to reflect the name to use for the ddl files -->
        <project.ddl.name>hierarchy</project.ddl.name>
    </properties>
2D) Change the dependencies to pull in your hibernate persistent POJOs (these are needed in order to process the HBM files)
        <!-- this should pull in your hibernate persistent POJOs -->
        <dependency>
            <groupId>org.sakaiproject.hierarchy</groupId>
            <artifactId>hierarchy-api</artifactId>
            <version>1.2.0-SNAPSHOT</version><!--hierarchy.version-->
            <scope>provided</scope>
        </dependency>
2E) Change the build resources to pull in your .hbm.xml files
            <resource>
                <!-- this should pull in your .hbm.xml files -->
                <directory>${basedir}/../java</directory>
                <includes>
                    <include>**/*.xml</include>
                </includes>
                <filtering>false</filtering>
            </resource>
3) Edit the hibernate.cfg.xml file,
the mapping resources must point to all your HBM files
    <session-factory>
        <!-- these mapping resource paths must point to your hibernate template files (.hbm.xml),
            this should be the classpath location (typically after the src directory) -->
        <mapping resource="org/sakaiproject/hierarchy/dao/hbm/HierarchyNodeMetaData.hbm.xml" />
        <mapping resource="org/sakaiproject/hierarchy/dao/hbm/HierarchyPersistentNode.hbm.xml" />
    </session-factory>
4) Run maven 2 to generate the DDL from the ddl folder: mvn install
You should now have a new set of folders inside the ddl folder, each one will contain a ddl file for a specific database with the database name as the name of the folder containing the ddl file
Example: ddl/mysql/hierarchy.sql

That's all there is to it.

If you want to also create a profile so it is easier to run the ddl generator from the root of your project you can do something like this in your base pom.xml:
   <profiles>
      <profile>
         <id>full</id>
         <activation>
            <activeByDefault>true</activeByDefault>
         </activation>
         <modules>
            <!-- put your full set of modules here -->
            <module>api</module>
            <module>impl</module>
            <module>pack</module>
         </modules>
      </profile>
      <profile>
         <id>ddl</id>
         <modules>
            <!-- put the path to the ddl pom.xml here -->
            <module>impl/src/ddl</module>
         </modules>
      </profile>
   </profiles>

Good luck!
-AZ
Aaron Zeckoski (aaronz@vt.edu) (azeckoski@gmail.com)
