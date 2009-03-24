This is the Entity Broker project - an enhancement to the way entities work inside Sakai

This contains information related to the Entity Broker system (entity 2.0) in Sakai. 
Ideally we want flexible entities which make development of integrated Sakai tools 
easier and more flexible. Our goals are to make the entity system easier on developers 
(easier to use and understand) and more powerful (easier to extend and improve).

Ideally we want flexible entities and data handling which make development of integrated tools 
easier and more flexible. Our goals are to make the entity system easier on developers 
(easier to use and understand) and more powerful (easier to extend and improve).
This project is designed to make REST handling easy to do with known system entities. It also
provides REST documentation automatically which can be customized and internationalized. 
EB REST interfaces adhere to the REST microformat and the HTTP spec.

More info here:
http://confluence.sakaiproject.org/confluence/x/Sac

Usage:
The jars are available in the following Maven 2 repository:
        <repository>
            <id>sakai-maven</id>
            <name>Sakai Maven Repo</name>
            <layout>default</layout>
            <url>http://source.sakaiproject.org/maven2</url>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
        </repository>

You can include the jars in your project manually (hard) or use Maven (easy) like so:
        ...
        <dependencies>
            ...
            <dependency>
                <groupId>org.sakaiproject.entitybroker</groupId>
                <artifactId>entitybroker-api</artifactId>
                <version>1.3.7</version><!--entitybroker.version-->
                <scope>provided</scope>
            </dependency>
            <dependency>
                <groupId>org.sakaiproject.entitybroker</groupId>
                <artifactId>entitybroker-impl</artifactId>
                <version>1.3.7</version><!--entitybroker.version-->
            </dependency>
            <dependency>
                <groupId>org.sakaiproject.entitybroker</groupId>
                <artifactId>entitybroker-rest</artifactId>
                <version>1.3.7</version><!--entitybroker.version-->
            </dependency>
            <dependency>
                <groupId>org.sakaiproject.entitybroker</groupId>
                <artifactId>entitybroker-utils</artifactId>
                <version>1.3.7</version><!--entitybroker.version-->
            </dependency>
            ...
        </dependencies>

Note that if you are adding jars manually then you have to include other jars like the 
servlet-api jar (2.4). Look at the listing in the maven pom.xml
files for more info about the required jars to use this. I highly recommend you use Maven 2
for building your java projects, it will save you a lot of trouble.

More info on the parent project here:
http://code.google.com/p/entitybus/

## Merging instructions (for developers)
svn diff -r54:55 http://entitybus.googlecode.com/svn/trunk/ > eb-54-55.patch
# do the following replacements in the patch file:
/src/main/java => /src/java
/src/test/java => /src/test
/sakaiproject/entitybus => /sakaiproject/entitybroker
.sakaiproject.entitybus. => .sakaiproject.entitybroker.
# in unix:
cp eb-54-55.patch eb.patch
sed -i '' -e 's%/src/main/java%/src/java%' eb.patch
sed -i '' -e 's%/src/test/java%/src/test%' eb.patch
sed -i '' -e 's%/sakaiproject/entitybus%/sakaiproject/entitybroker%' eb.patch
sed -i '' -e 's%\.sakaiproject\.entitybus\.%\.sakaiproject\.entitybroker\.%' eb.patch
# try the patch:
patch --dry-run -p0 -l < eb.patch
# do the patch:
patch -p0 -l < eb.patch
rm eb-54-55.patch eb.patch
## Ending merging instructions

Comments or questions about the entity bus should go to 
Aaron Zeckoski (azeckoski@gmail.com OR aaronz@vt.edu)
