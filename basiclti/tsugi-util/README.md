
What is it?
-----------

Tsugi-util contains a set of utility classes to aid  in the development
of BasicLTI consumers and providers. They deal with much of the heavy lifting
and make the process more opaque to the developer.

There is no Sakai-specific code but the code is developed and maintained in the 
Sakai repository since it is so core to Sakai and heavily tested being part of 
Sakai.  Please do **not** put any org.sakaiproject dependencies to this folder
please use `basiclti-common` for code with org.sakaiproject dependencies.

This code is released in two ways.  Within Sakai this code is normally released
as part of the Sakai build process:

    <groupId>org.sakaiproject.basiclti</groupId>
    <artifactId>basiclti-util</artifactId>

The *org.sakaiproject.basiclti* artifact simply tracks the Sakai versioning 
and is updated automatically by the Sakai release processes.  Over the years,
folks have used this code in their java apps by pulling in a particular Sakai
version of this code.

From time to time this is also released *pom-tsugi.xml* as the following
artifact:

    <groupId>org.tsugi</groupId>
    <artifactId>tsugi-util</artifactId>

This artifact follows its own semantic versioning and can be released any time.

This artifact is probably a better artifact to use for non-Sakai projects
as bugs can be fixed and a release can be made off-cycle.

Using tsugi-util
----------------

You can add the following to your pom.xml:

    <dependency>
        <groupId>org.tsugi</groupId>
        <artifactId>tsugi-java</artifactId>
        <version>0.1-SNAPSHOT</version>
    </dependency>

    <dependency>
       <groupId>org.tsugi</groupId>
       <artifactId>tsugi-util</artifactId>
       <version>0.1-SNAPSHOT</version>
    </dependency>

If you need to enable snapshot downloading add the following to your
pom.xml:

    <repositories>
        <repository>
            <id>sonatype-nexus-snapshots</id>
            <name>Sonatype Nexus Snapshots</name>
            <url> https://oss.sonatype.org/content/repositories/snapshots </url>
            <releases>
                <enabled>false</enabled>
            </releases>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>

This way when maven sees a SNAPSHOT version, it can find where to download it from.

You can see this all in action in the Tsugi Java Servlet:

    https://github.com/csev/tsugi-java-servlet

Tsugi Architecture
------------------

Tsugi generally has a two-layer architecuture for building APIs.  The lowest
level API simply wraps the protocols.   This "tsugi-util" code is the low level API.

The second level API is opinionated, with conventions for data tables and sessions.
As a result the method signatures for the second level Tsugi APIs are much simpler.
The second-level API for tsugi-java is here:

    https://github.com/csev/tsugi-java

The tsugi-java-servlet using both the tsugi-util and tsugi-java libraries.

Releasing tsugi-util to Sonatype
--------------------------------

Set up `settings.xml` as described above.

    cd trunk/basiclti/tsugi-util
    cp pom-tsugi.xml pom.xml
    mvn compile install deploy
    git checkout pom.xml


Check results of the deploy at:

    https://oss.sonatype.org/#nexus-search;quick~tsugi-util

After a while the files migrate to:

    https://oss.sonatype.org/content/repositories/snapshots/org/tsugi/


