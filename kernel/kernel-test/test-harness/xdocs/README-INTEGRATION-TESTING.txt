The Sakai test harness provides integration testing support for sakai tools and
services.  While all projects should include unit testing, integration testing
is an important tool for ensuring that your tool or service is working properly
within a (simulated) sakai environment.

You can follow these steps to add integration tests to your sakai project:

1) Create a new maven project in your module named *integration-test (e.g.
myproject-integration-test). Do NOT include this subproject in the "modules"
list of your top-level project's "pom.xml". If you do, every normal build
will automatically try to build and run integration tests. This will make you
and others unhappy.

2) Use the pom.sample.xml file as a template for setting up your build.

3) Create a src/test directory and add your JUnit tests. The Java source
files should be rooted at "src/test/java". Any resources needed by your
tests (a log4j.properties file, for example) should be kept at
"src/test/resources". Your unit test cases should extend
org.sakaiproject.test.SakaiTestBase.

*** IMPORTANT NOTE #1: If you intend to write multiple test cases (java classes
that extend org.sakaiproject.test.SakaiTestBase), please ensure that your
project.xml is configured to run a single test *suite* that runs all of your
tests.  This is important because, for each test case that maven runs directly, the
test harness will launch a new sakai component manager, which takes a long time
(around 10 seconds on my desktop).  By using a test suire to run all of your tests,
you will incur the startup delay only once.

Your test suite should call oneTimeSetup and oneTimeTearDown, like so:

	public static Test suite() {
		TestSetup setup = new TestSetup(new TestSuite(MyTest.class)) {
			protected void setUp() throws Exception {
				oneTimeSetup();
			}
			protected void tearDown() throws Exception {
				oneTimeTearDown();
			}
		};
		return setup;
	}


*** IMPORTANT NOTE #2: The test harness requires that you have a typical Sakai
development environment configured.  It expects either:
a) a "test.tomcat.home" property pointing to a tomcat instance with all of the sakai
components deployed
b) a ,m2/settings.xml file in your $HOME directory, where it can find a default
profile with a "maven.tomcat.home" property set.

If a "test.sakai.home" system property is defined, the test harness loads your
sakai.properties file from that directory. Otherwise it uses the file found in
"${test.tomcat.home}/sakai" or "${maven.tomcat.home}/sakai/".

If your sakai.properties is configured to use an oracle database, for instance, you
need to add a dependency on the appropriate oracle driver to your integration
testing project.  Understand that the data in this database will be modified by
integration tests, and failing or poorly written tests (those that don't clean up after
themselves) may leave garbage in your DB.  Using an in-memory hsql database is
recommended.

4) To run integration tests, simply start from the integration-test directory
and run a normal maven test goal:

mvn clean test

--------------------------------------------------------------------------------

Your ".m2/settings.xml" file can be used to set some other useful properties
for Maven-run tests. Here's an example:

<settings>
  <profiles>
    <profile>
      <id>sakai</id>
      <properties>
        <maven.tomcat.home>${env.CATALINA_HOME}</maven.tomcat.home>
        <!-- Provides fuller reporting than the default. -->
        <surefire.reportFormat>plain</surefire.reportFormat>
        <!-- Display test results in normal output rather than in report files. -->
        <surefire.useFile>false</surefire.useFile>
      </properties>
    </profile>
  </profiles>
  <activeProfiles>
    <activeProfile>sakai</activeProfile>
  </activeProfiles>
</settings>

--------------------------------------------------------------------------------

Josh Holtzman
jholtzman@berkeley.edu

Revised for Maven 2 by Ray Davis, ray@media.berkeley.edu
