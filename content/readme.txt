=============================================================================
Release Notes Sakai 10.0

SAK-25371 - The feature will add "Print File" action option to the Resources tool for those print-eligible-type resource items. The printing will be done on item-by-item base, not with a batch mode. The feature will save user effort of downloading the resource first and then upload to a  server. 

The following is required to enable the "Print File" choice in the Resources tool: 

1. Implement org.sakaiproject.content.api.ContentPrintService 

2. Configure implementation in your provider's components.xml - you'll need to first comment out the default implementation in the kernel (kernel-component/src/main/webapp/WEB-INF/content-components.xml), and then configure your provider. This is an example of UM's provider implementation:

<beans> 
   <bean id="org.sakaiproject.content.api.ContentPrintService" 
class="org.sakaiproject.content.impl.ContentPrintServiceUnivOfMichImpl" 
singleton="true"> 
 </bean> 
</beans> 

3. Define print_server_url in your sakai.properties 

4. An example provider implementation is available at https://source.sakaiproject.org/svn//msub/umich.edu/ctools/ctools-providers/branches/2.9.x-from-2.7.x/contentprint/ 


=============================================================================
Release Notes Sakai 2.5

The following steps are required to run the conversion from 2.4 to 2.5
database schema for content (ContentHostingService) to improve performance
of the Resources tool and other tools that depend on it.  Unless otherwise
indicated, all files referred to are in the root directory of the content
project. These instructions apply to MySQL or Oracle.  If a different
database is used, a new version of the config file will be needed.

A shell script named "content-runconversion.sh" is used to perform the
conversion. Although you don't necessarily have to edit it, you will
need to do at least one Sakai build in order to populate your local
Maven repository cache with the required library JAR files. The standard
"runconversion.sh" shell script is needed, and expected to be at:

../db/db-util/conversion/runconversion.sh

If you want to move that script to another location, or if you want
to specify different locations for the JAR files, you'll have to
edit the scripts.

Otherwise, you can use the scripts as they are by specifying a JDBC
driver JAR, a source of database properties, and an input "config"
file on the command line.

A typical full conversion for an Oracle site on Linux might be:

   nohup ./content-runconversion.sh \
      -j "$CATALINA_HOME/shared/lib/ojdbc14.jar" \
      -p "$CATALINA_HOME/sakai/sakai.properties" \
      upgradeschema-oracle.config &> ~/content-runconversion.log &

A typical full conversion for a MySQL site might be:

   nohup ./content-runconversion.sh \
      -j "$CATALINA_HOME/shared/lib/mysql-connector-java-5.1.5.jar" \
      -p "$CATALINA_HOME/sakai/sakai.properties" \
      upgradeschema-mysql.config &> ~/content-runconversion.log &

For a large site, the script will likely run for many hours, and so
we recommend that you use no-hangup mode and save all output to a log file.

You may see warnings about records with a null source. These are
almost certainly harmless, and may reflect records that have
been created or updated since your move to the new version of
Sakai software.

If you need to interrupt the migration, copy or create a file named
"quit.txt" in the working directory. You can start from where you
left off by deleting the "quit.txt" file and beginning over. Similarly,
if the procedure is interrupted by an error, you should be able to
start safely from where you left off (although you should read
the last paragraph if the interruption occurred during the
second phase of the migration).

Because the procedure is time-consuming, you might prefer to do
the conversion in two phases. To do so, first comment out the
"convert.1" lines in the config script. After the "Type1BlobResourceConversion"
process is finished, you can then comment out the "convert.0" lines,
uncomment the "convert.1" lines, change them to "convert.0", and
run again. In particular, you'll want to do this if the process
has been interrupted after the first phase is complete. Otherwise,
the first phase will start (unnecessarily) over again from the beginning.
