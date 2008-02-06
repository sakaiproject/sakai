First off, thanks to Josh, this project has saved me hours of work.
------
If you prefer to run you JUnit tests inside eclipse, you can with a little setup first.

HOWEVER BE WARNED:
This will break some of the seperation with classloaders, and you could end up with 
dependencies on jars not in shared within classes that are in shared. 

eg letting implementation leak into Hibernate pojos.

If this happens, your app will probably not work in the real environment.

So dont stop testing on the maven command line or in tomcat, and be really carefull 
what you put where.

---------


You need to do a full maven build to get the runtime environment so follow the instructions
in the README-INTEGRATION-TESTING.txt first.

Once you have done that you need to 
1. Setup you project classpath.
2. Ensure that all your resources are referenced.


This project contains all the current known dependencies for the test-harness from the 
/shared and /common these are registered as MAVEN_REPO jars so you MUST have built Sakia.
Also this project is configured to use TRUNK.

That being said, this project exports all the necessary jars. So to enable your project, add
this project to your project dependencies and include the exported classpath entries

Eclipse 3.1
In you project go to 
Run->Debug....
or
Run->Run...

Select your test-harness JUnit class
Select the classpath tab
add Project 
select the test-harness project and select both
"Add Exported Entries of project" and
"Add Dependant projects of this project"

Click Ok

Select the Environment Tab
add a variable "sakai.components.dir" that points to the tomcat/components directory.

Click Ok all the way out.

2. Resources
Make certain that any of your resources are present in the classpath otherwise you may get versions
from the tomcat/components space.


The just click on the Run As JUnit test for you test-harness class, and it all runs inside eclipse,
the build cycle for me is about 15-30 seconds for components as opposed to the 5 minute cycle...

That should be enough enticement for everyone to write full JUnit tests for components :)


Issues.
1.
I am not 100% certain where the components classpath comes from, and if there is any separation
between classloaders, but I can change a core component (even my own shared jars), hit the JUnit
button and the change does appear. I have not had to do a maven sakai for 2 days.

2.
Changes to components.xml do need to be re-deployed

3.
I couldn't get the maven itest target to work with my project, which is why I tried eclipse. 


4. 
If you have problems with components initialising, change the log4j.properties (after copying it into 
your classpath space), and enable the ComponentManager logger. Then you will see all the normal 
catalina.out log trafic. This makes it much easier to test component loading.

5.
This does not test tool level implentions.


Ian