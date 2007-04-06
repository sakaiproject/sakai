Build instructions for HSQLDB 1.8.0

A jar file for HSQLDB is provided in the /lib directory of the 
.zip package. This jar contains both the database and the 
utilities and has been built with JDK 1.4.2.

The supplied jar can be used with JRE version 1.4.x., and 1.5.x.
It cannot be used with older version or JRE.

JDK and JRE versions

For use with JRE 1.1.x, 1.2.x or 1.3.x recompilation is necessary.
For all the older versions, always use JDK 1.3.x to build the jar.
The default build with JDK 1.3.x is not compatible to run under
1.1. If you require this compatibility, you should use the following
ant command prior to the build (note the digit 1 in java1):
ant switchtojava1target
The jars compiled after this switch will run under JRE 1.3 too, but
they use some deprecated JDK 1.1 methods.

To switch back to non-deprecated code, use:
ant switchoffjava1target


Different jar sizes

HSQLDB can be built in any combination of five different sizes.

The smallest jar, hsqljdbc.jar, contains only the client side
of the JDBC driver, without any server or client standalone programs.
The next smallest jar, hsqldbmin.jar, contains only the database
and JDBC support for in-process mode databases. The next smallest
jar, hsqldbmain.jar, also contains support for server modes.
The default size jar, hsqldb.jar, additionally contains the
utilities such as Database Manager and Transfer Tool. The largest 
jar, hsqldbtest.jar, includes some test classes as well. You need
the JUnit jar in the /lib directory in order to build and run the
test classes.

Run "ant explainjars" to see a summary of the contents of the different
pre-defined jar targets.

Javadoc can be built with Ant and batch files.

The JDK used for the build has a marginal effect on the size.
Newer JDK's support more JDBC methods and classes, resulting in
slightly larger jars.

JDK 1.1.x

It is recommended not to use JDK 1.1.x for building the
jar, as this version produces much larger jar sizes and the result
is not upward compatible with newer JDK'S / JRE's. Use JDK 1.3.x
to build the jar instead. You can then deploy the jar in JRE 1.1.


Build methods:

The preferred method of rebuilding the jar is with Ant. After
installing Ant on your system use the following command from the
/build directory:

ant

The command displays a list of different options for building 
different sizes of the HSQLDB Jar. The default jar is built using:

ant jar

The Ant method always builds a jar with the JDK that is used by Ant
and specified in the JAVA_HOME environment variable. The script
automatically converts the source files for compatibility with the
given JDK.

Before building the hsqldbtest.jar package, you should download the
junit.jar and put it in the /lib directory, alongside servlet.jar, 
which is included in the .zip package.

Batch Build

A set of MSDOS batch files is also provided. These produce only
the default jar size. The path and classpath variables for the JDK
should of course be set before running any of the batch files.

If you are compiling for JDK's other than 1.4.x, you should
use the appropriate switchtoJDK11.bat or switchtoJDK12.bat to adapt
the source files to the target JDK before running the appropriate
buildJDK11.bat or buildJDK12.bat. As explained before, it is not
recommended to use this method for building jars for use on JRE 1.1.x
targets.



fredt@users
