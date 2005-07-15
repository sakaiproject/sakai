Sakai Sections Tool -- Building and Deploying
	The sections tool can be built as either a standalone web application, or as
	a tool to be embedded in a sakai portal.
	
	Sakai Embedded
		Just run 'maven sakai', or build a default (as of Sakai 2.1) sakai
		installation, which includes the sections tool.

	Standalone
		1.  Create a database for the application to use.  All tables and sequences
		will be automatically generated when the app starts up.
		
		2.  Configure the database connection by doing one of the following:
			a.  configure /sections-app/src/hibernate/hibernate.properties with
				the settings for your local database configuration, or
			b.  specify a different 'hibernate.properties.dir' property either
				on the command line or in /sections-app/project.properties.  This
				property should define the directory containing the custom
				hibernate.properties file you'd like to use (copy the one
				in /sections-app/src/hibernate to your custom location and edit
				it there).  Be sure to include a trailing slash when defining
				this property.
		If you are planning to do development on the sections tool, please
		utilize the latter method and refrain from editing the distributed
		hibernate.properties file.
		
		3.  Specify the location of your tomcat 5.5.9 webapps directory (not
		your sakai tomcat -- you must use a different tomcat installation!) with
		the 'standalone.deploy.dir' property, either from the command line or in
		/sections-app/project.properties.  Include the trailing slash.
		
		4.  Run "maven -Dstandalone=true cln bld" to clean and build the app.
		This will also copy the war file into your tomcat webapps directory.
		
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
		highly recommended that you run the tests on a clean (empty or records)
		database to avoid possible data integrity violations (a test may attempt
		to insert a value that already exists in another record, which may not
		be allowed due to uniqueness constraints).
		
		