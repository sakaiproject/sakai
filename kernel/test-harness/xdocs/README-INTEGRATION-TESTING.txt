WHAT'S HERE

The "test-harness" package contains utilities to support automated testing in Sakai.
This file describes support for tests which integrate directly with deployed
component services rather than going through a web server. (The same support
can be used for purposes other than integration testing. See
"README-Data-Loading-and-Migration.txt" for more.)

Traditional class-bounded "unit tests" or project-bounded "integration
tests" will use stubbed or mocked implementations of external Sakai services.
But when working on Sakai core services themselves, or on plug-in "provider"
components, or on bugs in a complex call-stack, tests need to run in a more
realistic Sakai environment. This is when service-level integration comes into play.


ENABLING SERVICE INTEGRATION TESTS

To write and run service-level integration tests from Maven, you need to have
two Java system properties defined:

  maven.tomcat.home - This is where the component loader will go to find
       component WAR files and shared JARs. You should already have this
       defined for the benefit of "sakai:deploy".

  test.sakai.home - This should point to a directory to use instead of
       "sakai.home" when looking for "sakai.properties" and other configuration
       files. I usually want automated regression tests to run
       against an in-memory database and with out-of-the-box default
       settings. However, in certain cases I might want to run a test
       against a specific database, or to let the test code set up its own
       configuration.

For example, here's my ".m2/settings.xml" file:

<settings>
  <profiles>
    <profile>
      <!-- Avoid name conflicts with project-defined profiles. -->
      <id>ray-default</id>
      <properties>
        <maven.tomcat.home>${env.CATALINA_HOME}</maven.tomcat.home>
        <test.sakai.home>C:\java\sakaisettings\hsqldb-mem</test.sakai.home>
        <!-- Provides fuller reporting than the default. -->
        <surefire.reportFormat>plain</surefire.reportFormat>
        <!-- Display test results in normal output rather than in report files. -->
		<surefire.reportFormat>plain</surefire.reportFormat>
		<surefire.useFile>false</surefire.useFile>
      </properties>
    </profile>
  </profiles>
  <activeProfiles>
    <activeProfile>ray-default</activeProfile>
  </activeProfiles>
</settings>

Running the tests from Eclipse is just a matter of setting similar
system environment variables. See the README file in this directory.


WRITING SERVICE INTEGRATION TESTS

You can follow these steps to add integration tests to your sakai project:

1) Create a new maven project in your module named "MYPROJECT-integration-test".

2) Use the pom.sample.xml file as a template for setting up your build.
It's configured so that tests do *not* run unless you explicitly clear
the "skipLongTests" variable:

  mvn -DskipLongTests=false test

Alternatively, you can avoid all the special configuration by
simply not including your integration-test directory in your
project's top-level POM. In this case, your test code won't even
be compiled unless you explicitly move to that directory before
running Maven.

3) Create a src/test directory and add your testing code. The Java source
files should be rooted at "src/test/java". Any resources needed by your
tests (a log4j.properties file, for example) should be kept at
"src/test/resources". Your test cases can extend "SakaiTestBase" (a simple
convenience wrapper around JUnit's "TestCase") or "SakaiDependencyInjectionTests"
(which lets you autowire Sakai services by simply declaring a setter for them).
You can find simple examples of both in "test-harness/src/test".

Or you can use your own favorite test technology and explicitly call
"ComponentContainerEmulator" to gain access to Sakai services.

4) If you're writing multiple test classes, you should decide whether
to have them all run in a single running instance of the Sakai component
manager, or whether to run them completely separately. Running them
together is much faster, since you don't have to load and shut down
all the components each time. However, in some circumstances you'll
need each test case to start with as clean an environment as possible.
(Especially if each test case needs to configure Sakai differently!)

Case A) Faster and dirtier

  Set up your integration-test's pom.xml to run a single test suite
  that includes all your test classes. In your test suite, start up
  and shut down the component container just once.

  For example, if using the SakaiTestBase approach, call "oneTimeSetup()"
  and "oneTimeTearDown()" in the standard TestSuite setup:

	public static Test suite() {
		TestSetup setup = new TestSetup(new TestSuite(MyTestSuite.class)) {
			protected void setUp() throws Exception {
				oneTimeSetup();
			}
			protected void tearDown() throws Exception {
				oneTimeTearDown();
			}
		};
		return setup;
	}

Case B) Slower and cleaner

  Start up and shut down the component container as part of each test
  class. For SakaiDependencyInjectionTests this is the standard behavior.
  For SakaiTestBase, you can use the exact same "suite()" method as above
  -- just paste that boilerplate into each test class.

  Because many Sakai classes store data in static variables, just
  shutting down the component manager between tests isn't enough to
  guarantee a clean room, so you'll also want to unload the classes
  themselves. You do this by uncommenting this line in the integration-test's
  "pom.xml":

          <forkMode>pertest</forkMode>

5) If you need to tailor Sakai's configuration specifically for your
tests, add "sakai.properties" to a test resources directory and then
call the "setSakaiHome" static method to point there. Here's an example from
the test-harness's own integration tests:

public class ChildContextDependencyInjectionTest extends SakaiDependencyInjectionTests {
	...
	static {
		// Sakai will use "resources/childcontext/sakai.properties"
		setSakaiHome("childcontext");
	}

6) To run your tests, simply start from the integration-test directory
and request the normal Maven test goal:

mvn -DskipLongTests=false clean test

If you have multiple service-level tests and start each with a fresh
component manager, running all of them will take a noticeable amount
of time and kick up a fair amount of console noise. To focus on a
particular test class, just use the normal Maven approach:

mvn -DskipLongTests=false -Dtest=ChildContextDependencyInjectionTest clean test
