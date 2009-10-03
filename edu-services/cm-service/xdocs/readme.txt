$URL: $
$Id: $

I. Introduction
	From its inception, Sakai has maintained a strong separation between the concept of
	a generic "site" and an academic "course".  This allows the framework to maintain
	flexibility and to offer non-course-related worksites.  The Course Management project
	provides course-oriented tools and services the ability to utilize course-specific
	data that can not be modeled in generic site -> member relationships while maintaining
	Sakai's generic site-orientation.

II. Related Projects and Components
	The /course-management module contains the Course Management API, a hibernate
	implementation, and a federating implementation that allows for federating course
	data from multiple sources.  See the section on federating CM implementations below.

	The /providers module contains Sakai's default GroupProvider, which makes use of
	the CourseManagementService to resolve users memberships.

	The /roster module contains the Roster tool, which is a client of the CourseManagementService.

	The /site-manage module contains the Worksite Setup and Site Info tools.  Like the Roster
	tool, these tools are also clients of the CourseManagementService.  Additionally, these tools
	also play an important role in associating sites with "Sections".  CM is the source-of-authority
	for official Sections, and Site Info / WS Setup provides the "glue" that allows us to map
	between Sakai's Site & Group "providerIds" and CourseManagement's Section "enterpriseIds".

III. Supplying CM with data

	There are several options for supplying Sakai with CM data via the CourseManagement APIs.

	A. Loading sample data
		To load sample data into the default hibernate implementation, simply start tomcat
		with -Dsakai.demo=true.  You may customize the sample dataloading procedure here:
		/cm-impl/hibernate-impl/impl/src/java/org/sakaiproject/coursemanagement/impl/SampleDataLoader.java

		Note that the presence of existing CM data will cause the SampleDataLoader
		to fail.  If you have existing sample data but would like to update the database
		with a new data set, empty the CM_* tables before running Sakai with -Dsakai.demo=true.

		Using -Dsakai.demo=true also loads the sample UserDirectoryProvider.  Keep in
		mind that the CM data and the user data must be kept in sync.  If CM returns
		a member of a section where the member's EID is 'foo', the UserDirectoryProvider
		should be able to resolve user 'foo'.

	B.Reconciliation
		In order to use the hibernate implementation, you need some way to populate
		the hibernate tables with your enterprise data.  This can be accomplished by
		using the CourseManagementAdministration APIs.  Preiodically running a quartz
		job to add, update, or remove CM objects via this API is a common practice.
		A sample job is distributed with Sakai:
		/cm-impl/hibernate-impl/impl/src/java/org/sakaiproject/coursemanagement/impl/job/ClassPathCMSyncJob.java
		This job reads in simulated enterprise data from an xml file and reconciles the
		data with entries in the hibernate-managed tables.  Consider this job as just
		a sample to use in constructing your own reconciliation jobs.

	C. Re-implementation
		If you prefer to access your enterprise data directly and avoid reconciliation,
		you should write your own implementation of
		org.sakaiproject.coursemanagement.api.CourseManagementService.  Not all
		CourseManagementService methods are called in Sakai.  As of the 2.5 release,
		there are three CM clients: the GroupProvider, the Roster tool, and WS Setup /
		Site Info.  Depending on the specific tools and/or group provider you have deployed,
		you may choose to implement a subset of the CourseManagementService's methods.

		If some kind of remoting (e.g. webservices) is used in your CM implementation,
		you should consider adding a caching mechanism to avoid excessive latency in CM
		service calls.

	D. Federating multiple data sources
		Some institutions may use a custom CM Service implementation to connect
		to an external data source which is 'read only' from the perspective of the
		Sakai administrators.  Using the federated implementation allows an admin to
		append to or override some of the data provided by the custom implementation.
		Sakai's default configuration for CM uses the
		org.sakaiproject.coursemanagement.impl.CourseManagementServiceFederatedImpl
		implementation to demonstrate how to configure a federated service.  See the
		javadocs on this class and on
		org.sakaiproject.coursemanagement.impl.CourseManagementServiceSampleChainImpl
		for more details on writing an implementation that can participate in a federated
		configuration.

IV. Configuring Sakai to use CM
	A. SectionFieldProvider
		The CourseManagementService delivers course information the Site Info and Worksite
		Setup tools.  These tools provide multiple UI widgets for specifying courses, one
		of which is a free-text entry.  Some institutions want to provide a single text box
		for users to specify an section's enterprise ID, while others want multiple text boxes.
		Editing or re-implementing the OOTB SectionFieldProvider implementation allows an
		institution to change the default section selection behavior of WS Setup and Site Info.

	B. GroupProvider
		The CourseManagementGroupProvider uses memberships, enrollments,
		and official instructing status to determine what role, if any, a user has in a site.
		It uses an ordered list of resolvers to check for roles at different levels of the
		CM hierarchy.  As of 2.4, this is Sakai's default group provider.  To configure the
		CM-based group provider, edit /providers/component/src/webapp/WEB-INF/components.xml

		In the following example, only the SectionRoleResolver will be used to find users
		and their memberships in Sections.  Any memberships defined above this level
		in the CM hierarchy will not be resolved, and hence will not be added to sites
		linked to a Section.

		<property name="roleResolvers">
			<list>
				<bean class="org.sakaiproject.coursemanagement.impl.provider.SectionRoleResolver">
					<property name="roleMap">
						<map>
							<entry key="I" value="Instructor" />
							<entry key="S" value="Student" />
							<entry key="GSI" value="Teaching Assistant"/>
						</map>
					</property>
					<property name="officialInstructorRole" value="Instructor" />
					<property name="enrollmentStatusRoleMap">
						<map>
							<entry key="enrolled" value="Student" />
							<entry key="waitlisted" value="Student" />
						</map>
					</property>
				</bean>
			</list>
		</property>

		You may add as many RoleResolvers as you like.  Sakai's default configuration
		includes a SectionRoleResolver, a CourseOfferingRoleResolver, and a
		CourseSetRoleResolver.

		3) Configure the roleMap for each role resolver.  In the example above, if a
		Section member in CM has a role of 'I', this will be translated to the 'Instructor'
		Sakai role for this site.  The ordering of the RoleResolvers is important.  Earlier
		entries override later ones.

		4) The SectionRoleResolver has two extra configurations: officialInstructorRole and
		enrollmentStatusRoleMap.  OfficialInstructors in EnrollmentSets attached to a section
		will be resolved to have a Sakai role as defined by the officialInstructorRole property.
		Enrollments in EnrollmentSets will be resolved using the enrollmentStatusRoleMap.
		If an Enrollment is found to be enrolled in a relevant EnrollmentSet, the Enrollment's
		getStatus() will be compared to keys in this map, and the map's entries define the Sakai
		role to grant to this user.  If the Enrollment status is not found in the map, the user will
		not be 'provided' to Sakai.

	C. User Directory Provider
		Ensure that the active UDP implementation can resolve all user EIDs provided by the
		active CourseManagementService implementation.

