This provider is provided in source only.  Check with the author
for instructions in configuring this provider.  You should test 
this software carefully before deploying.

Configuration will include modifying the file

../component/src/webapp/WEB-INF/components.xml

To select this class as the provider implementation and add any necessary
configuration.

This is the detail below as to how to congifure this bean in Sakai 1.0 - it 
may need to be changed for 2.0:

<bean id="org.sakaiproject.service.legacy.user.UserDirectoryProvider"
	class="es.udl.asic.user.OpenLdapUserDirectoryProvider"
	init-method="init"
	destroy-method="destroy"
	singleton="true">
	<property name="logger"><ref bean="org.sakaiproject.service.framework.log.Logger"/></property>
</bean>
