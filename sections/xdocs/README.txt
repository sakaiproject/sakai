-------------------------
Sakai Section Info Tool

This document consists the following sections:

I.  Building and deploying the application and its shared services
II. Available service implementations
III. Authorization and Section Awareness rules
-------------------------

I.  Building and deploying the application and its shared services

	The section info tool can be built as either a standalone web application,
	or as a tool to be embedded in a sakai portal.  The tool depends on several
	sakai artifacts, even when building standalone.  You must therefore build the
	entire sakai project prior to building the Section Info tool either in
	embedded or standalone mode.
	

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
		the 'standalone.deploy.dir' property, either from the command line,
		in $HOME/build.properties, or in /sections-app/project.properties.  Make
		sure you nclude the trailing slash.

		4.  Run "maven -Dmode=standalone cln bld" to clean and build the app.
		This will also copy the war file into your tomcat webapps directory.  If
		you are using -Dmem=false (see below), make sure you include that when
		running your build:  "maven -Dmode=standalone -Dmem=false cln bld", and
		make sure that your database is empty so the tests don't attempt to insert
		duplicate records.  Due to these constraints, using -Dmem=false is not
		recommended.

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
		maven -Dmode=standalone -Dmem=false loadData
		
		Standalone gradebook dataload (for running the standalone gradebook on
		top of section awareness)
		maven -Dmode=standalone -Dmem=false loadGradebookData

		Sakai 2.1 dataload
		(not available)

	II. Available service implementations

		The following service implementations are available to support the
		Section Info tool and its shared services.
		
		1) Standalone:  This is appropriate for supporting the standalone webapp,
		it is not capable of functioning inside the sakai framework.

		2) Sakai 2.1:  This is the default mode when building the application.
		This implementation decorates Sakai's native group capabilities with
		section-specific metadata.

	III. Authorization and Section Awareness rules
		
		The Section Info tool applies the following authorization rules for
		checking whether a user can perform a particular operation:
		
			Add/Edit/Remove Sections -- requires the 'site.upd' authorization
			function.
			
			Modify Section Options -- requires the 'site.upd' authorization
			function.

			Modify TA Section Memberships -- requires the 'site.upd' authorization
			function.
			
			Modify Student Section Memberships -- requires either the 'site.upd'
			or 'site.upd.grp.mbrshp' authorization function.
			
			View All Sections -- requires either 'site.upd', 'site.upd.grp.mbrshp'
			or 'section.role.ta'.
			
			View Own Section Enrollments -- requires the 'section.role.student'
			marker function at the site level.
			
			The Section Info tool will display in one of four modes:
				1) Full control / Sakai control:  Users with site.upd have full
				control of sections and their memberships as long as the site
				is not flagged as externally managed.
				
				2) Full control / Enterprise control: Users with site.upd have
				control of TA memberships in sections, but can not change student
				enrollments or section metadata, and can not add or remove sections.
				
				3) Enrollment control / Sakai control:  Users without site.upd but
				with site.upd.grp.mbrshp can modify section enrollments, but not
				TA memberships.  They can not add/edit/remove sections or modify
				section options.
				
				4) Own enrollment control / Sakai control:  Users enrolled in a
				non-enterprise-managed site can join or switch their section
				enrollments if those options have been enabled in the site.

		The SectionAwareness API uses the following rules when determining the
		membership lists of sites and sections:

			Sites use a "marker" authorization function to determine whether a
			user is a student, a TA, or an instructor.  There could be more than
			one role with the "marker" authorization function (e.g both 'TA' and
			'Head TA' could have the TA marker set, and members of both roles
			would be returned as Site TAs by SectionAwareness.
			
				Students:  Find the users who have been granted the
				'section.role.student' authorization function.  The members in
				the site with this authorization function are considered students
				in the site.
				
				TAs:  Find the users who have been granted the
				'section.role.ta' authorization function.  The members in
				the site with this authorization function are considered TAs
				in the site.
				
				Instructors:  Find the users who have been granted the
				'section.role.instructor' authorization function.  The members in
				the site with this authorization function are considered instructors
				in the site.

				
			Sections use a "marker" authorization function to determine which
			sakai role contains the list of members.  Only one role per
			section can contain the marker function.  If more than one role
			contains the marker function, SectionAwareness will fail quickly.
			Since there is no UI exposed to the user for editing section authzGroups,
			this kind of misconfiguration shouldn't occur.
			
				Students:  Find the role that contains the 'section.role.student'
				authorization function.	 The members in this section's role
				are considered students in the section.
				
				TAs:  Find the role that contains the 'section.role.ta'
				authorization function.	 The members in this section's role
				are considered TAs.
				
				Instructors:  Find the role that contains the 'section.role.instructor'
				authorization function.	 The members in this section's role
				are considered instructors.

			We must insist that only one section role have any particular marker
			in a site because, when adding a user to a section, we need to find
			the one (and only one) role to use when adding the user.  If two roles
			with the same marker function existed in the section's authzGroup,
			it would be impossible to know which roles to use when determining
			the section's membership.

