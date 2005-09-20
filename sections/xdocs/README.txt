-------------------------
Sakai Section Info Tool

This document consists the following sections:

I.  Building and deploying the application and its shared services
II. Available service implementations

-------------------------

I.  Building and deploying the application and its shared services

	The section info tool can be built as either a standalone web application,
	or as a tool to be embedded in a sakai portal.

	Sakai Embedded
		Just run 'maven sakai', or build a default (as of Sakai 2.1) sakai
		installation, which includes the sections tool.

	Standalone
		1.  Create a database for the application to use.  All tables and sequences
		will be automatically generated when the app starts up.

		2.  Configure the database connection by specifying the
		'hibernate.properties.dir' property either on the command line or in
		$HOME/build.properties.  This property defines the directory containing
		the custom hibernate.properties file you'd like to use (copy the one in
		/sections-app/src/hibernate to your custom location and edit it there).
		Be sure to include a trailing slash when defining this property.

		3.  Specify the location of your tomcat 5.5.9 webapps directory (not
		your sakai tomcat -- you must use a different tomcat installation!) with
		the 'standalone.deploy.dir' property, either from the command line or in
		/sections-app/project.properties.  Include the trailing slash.

		4.  Run "maven -Dstandalone=true cln bld" to clean and build the app.
		This will also copy the war file into your tomcat webapps directory.  If
		you are using -Dmem=false (see below), make sure you include that when
		running your build:  "maven -Dstandalone=true -Dmem=false cln bld".

	Other Settings
		Each time the sections tool is built, it runs a suite of tests to ensure
		that the business and database logic is operating correctly.  These tests
		use an in-memory hsql database by default, but can also be configured to
		use your chosen database (as defined above in Standalone item #2).  This
		is useful for developers wishing to test the application on different
		database platforms, but shouldn't be needed by someone simply deploying
		the app.  To use your own database settings, issue a build with the
		additional property '-Dmem=false'.  All of the tests roll back any
		database modification performed, so your data should be in the same
		state before and after the test suite is run.  That being said, it is
		highly recommended that you run the tests on a clean (empty of records)
		database to avoid possible data integrity violations (a test may attempt
		to insert a value that already exists in another record, which may not
		be allowed due to uniqueness constraints).

	Data loading
		The sections tool expects the framework to provide the "course context".
		Since the sections tool is not intended to manage courses, it
		can not create new course contexts.  Therefore, we need to bootstrap
		course information so when the app starts, it can find its course context.

		To load the initial course contexts for standalone mode, run one of the
		following commands from the 'sections-app' subdirectory.
		
		Standalone dataload
		maven -Dstandalone=true -Dmem=false loadData
		
		Standalone gradebook dataload (for running the standalone gradebook on
		top of section awareness)
		maven -Dstandalone=true -Dmem=false loadGradebookData

		Sakai 2.0 dataload
		(not yet available)

		Sakai 2.1 dataload
		(not yet available)

	II. Available service implementations

		The following service implementations are available to support the
		Section Info tool and its shared services.
		
		1) Standalone:  This is appropriate for supporting the standalone webapp,
		it is not capable of functioning inside the sakai framework.
		
		2) Sakai 2.0:  This implementation relies solely on the framework services
		available as of Sakai 2.0.  Legacy services provide for user authentication
		and permission checking.  In this implementation, the following
		site-scoped permissions are used to determine user roles.  These services
		use an internal concept of roles, which are not related to Sakai's roles.

		In the Section Info tool, as in the SectionAwareness service, there are
		four roles that describe how a user is associated with a site.  These
		include:
			Instructor	--	Capable of manipulating all non enterprise-managed
							sections, section leadership, and section enrollments.

							Users with the sakai permission "site.upd" are
							considered to be instructors.

			TA			--	Capable of manipulating all non enterprise-managed
							section enrollments.
							
							Users with the sakai permission "section.ta" but not
							"site.upd" are considered to be TAs.
			
			Student		--	Capable of enrolling or switching sections within a
							self-joinable or self-switchable course/site.
							
							Users with the sakai permission "site.visit" but not
							"site.upd" or "section.ta" are considered to be
							students.

		3) Sakai 2.1:  TBD... will probably be based on the new groups framework.
		