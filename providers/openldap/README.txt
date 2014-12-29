This provider is provided in source only.  Check with the author
for instructions in configuring this provider.  You should test 
this software carefully before deploying.

Configuration will include modifying the files

../component/project.xml
../component/src/webapp/WEB-INF/components.xml

to select this class as the provider implementation and add any necessary
configuration.

This is the detail below as to how to congifure this bean in Sakai 2.0

Edit ../component/src/webapp/WEB-INF/components.xml

Delete or comment out the lines about SampleUserDirectoryProvider

<bean id="org.sakaiproject.service.legacy.user.UserDirectoryProvider"
	class="org.sakaiproject.component.legacy.user.SampleUserDirectoryProvider"
	init-method="init"
	destroy-method="destroy"
	singleton="true">
</bean>

Replacing by this lines, placing correct values on ldapHost, ldapPort and
basePath

<bean id="org.sakaiproject.service.legacy.user.UserDirectoryProvider"
	class="es.udl.asic.user.OpenLdapUserDirectoryProvider"
	init-method="init"
	destroy-method="destroy"
	singleton="true">
	<property name="logger"><ref bean="org.sakaiproject.service.framework.log.Logger"/></property>
	<property name="ldapHost"><value>ldap://your.ldap.server</value></property>
	<property name="ldapPort"><value>your.ldap.port</value></property>
	<propertyname="basePath"><value>ldap.base</value></property>

</bean>


Edit ../component/project.xml

Add the lines

<dependency>
	<groupId>sakaiproject</groupId>
	<artifactId>sakai-openldap-provider</artifactId>
	<version>${pom.currentVersion}</version>
	<properties>
		<war.bundle>true</war.bundle>
	</properties>
</dependency>

