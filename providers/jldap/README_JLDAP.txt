JLDAPDirectoryProvider
=======================

This module contains an implementation of 
org.sakaiproject.user.api.UserDirectoryProvider which delegates user 
authentication and attribute loading to an LDAP service provider. The Novell
JLDAP library (http://www.openldap.org/jldap/) acts as a driver layer between
the UserDirectoryProvider and the LDAP host.

In its current form, this codebase is a refactored version of the code
originally contributed by David Ross and Rishi Pande, as well as patches 
contributed by Erik Froese.

To include and enable this provider in your Sakai deployment, uncomment the
"sakai-jldap-provider" and "ldap" artifact dependencies in
../component/pom.xml and uncomment the "jldap-beans.xml" import in
../component/src/webapp/WEB-INF/components.xml. Then rebuild and redeploy
with maven: 'mvn clean install sakai:deploy'. 

Architecture
=======================

  JLDAPDirectorProvider
  ------------------------

JLDAPDirectoryProvider is a concrete class responsible for:

  1) Collecting "standard" configuration options
  2) Ensuring initialization of LdapConnectionManager and LdapAttributeMapper
     collaborators
  3) Preparing and executing LDAP searches
  4) Caching LDAP search results

The current implementation makes every effort to preserve backward compatibility
with existing Spring bean definitions. Thus JLDAPDirectoryProvider implements
the LdapConnectionManagerConfig interface as a mixin, which allows most
"standard" behavior options to be specified directly on the 
JLDAPDirectoryProvider bean, as has been the case in previous releases. This
should be considered a concession rather than a feature. Future revisions
may push configuration design to a more modular model. As a small step in
that direction, as well as an attempt to un-clutter 
sakai-provider-pack/WEB-INF/components.xml in general, 
JLDAPDirectoryProvider-related bean definitions have been relocated into
a sibling file named jldap-beans.xml.

Internally, JLDAPDirectoryProvider executes searches much like a Spring
"template" class, where searchDirectory() is similar to 
org.springframework.jdbc.core.JdbcOperations.execute(). This method applies 
search constraints, executes the search, passes the results to a 
LdapEntryMapper which acts very much like a 
org.springframework.jdbc.core.RowMapper, and performs connection cleanup.

JLDAPDirectoryProvider's cache implementation is simplistic, synchronized Map
of User eid's to LdapDataObjects, the latter being a DTO
represention of a User's mapped LDAPEntry. Empty search results are not cached.
Caching behaviors could be significantly improved in future revisions.
Performance of bulk lookup operations (getUsers(Collection)) could also be 
radically improved, although 
http://jira.sakaiproject.org/jira/browse/SAK-10830 suggests that the relative
performance gains might not be significant.

Certain portions of JLDAPDirectoryProvider's work are delegated to two 
collaborator interfaces: LdapConnectionManager and LdapAttributeMapper.

  LdapConnectionManager
  ------------------------

LdapConnectionManager encapsulates LDAP connection resource management. Two
concrete implementations are available OOTB: SimpleLdapConnectionManager and
PoolingLdapConnectionManager.

PoolingLdapConnectionManager implements a superset of 
SimpleLdapConnectionManager features. As indicated by its name, 
PoolingLdapConnectionManager implements (optional) LDAPConnection pooling
features, delegating the actual pooling mechanics to an instance of
org.apache.commons.pool.ObjectPool. Both LdapConnectionManager implementations
support optional "auto-binding" and SSL/TLS connectivity. "Auto-binding" is
a significant distiguishing feature for this implementation: previous
implementations were not deployable where the LDAP host disallowed
anonymous bindings.

  LdapAttributeMapper
  ------------------------

LdapAttributeMapper encapsulates search filter building and user attribute
mapping rules. One concrete implementation is available OOTB:
SimpleLdapAttributeMapper.

SimpleLdapAttributeMapper expects the administrator to configure a simple map
of logical attribute names to physical LDAP attribute names. This map is
used to specify the set of attributes to return from _any_ LDAP search. Certain
"well-known" attributes (defined by AttributeMappingConstants) will be
mapped to first-class org.sakaiproject.user.api.User
instance members. Any additional attributes will be mapped to the User's
ResourceProperties instance.

SimpleLdapAttributeMapper collaborates with an injected UserTypeMapper
implementation to calculate Sakai User "type". Four concrete UserTypeMapper
implementations are available OOTB: 

  EmptyStringUserTypeMapper -- The default. Mimics historical 
    JLDAPDirectoryProvider behavior. Always assigns an empty String to 
    UserEdit.type.
    
  StringUserTypeMapper -- A generalization of EmptyStringUserTypeMapper
    which allows a given String to be assigned to UserEdit.type in
    all cases.

  EntryAttributeToUserTypeMapper -- Assigns a mapped attribute value, typically
    an attribute representing group membership(s), to UserEdit.type. This is a 
    somewhat simplified version of the user type mapping code commented out in 
    the original JLDAPDirectoryProvider implementation.

  EntryContainerRdnToUserTypeMapper -- Maps the user's LDAPEntry's container's
    RDN value to a Sakai user type (will use most local RDN value by default,
    recurseRdnIfNoMapping enabled will recurse through available RDNs). For 
    example, if the user's DN is cn=user1,ou=faculty,ou=users,dc=university,dc=edu, 
    the ou=faculty RDN can be mapped to a Sakai user type String.

SimpleLdapAttributeMapper mapping behaviors can also be extended by
overriding mapLdapEntryOntoUserData(LDAPEntry ldapEntry, LdapUserData userData)
and/or mapLdapAttributeOntoUserData(LDAPAttribute attribute, 
LdapUserData userData, String logicalAttrName). Override the former to
localize mapping behaviors at the LDAPEntry scope. Override the latter
to localize mapping behaviors at the LDAPAttribute scope.

As of this writing, SimpleLdapAttributeMapper treats all LDAP attributes
as single-valued.


Configuration
=======================

Until work is complete on fully externalizing UDP bean configuration
into [sakai|local|security].properties, localization of
JLDAPDirectoryProvider configuration usually requires modifications to
../component/WEB-INF/components.xml and 
../component/WEB-INF/jldap-beans.xml, relative to this readme.

../component/WEB-INF/jldap-beans.xml is merged into 
../component/WEB-INF/components.xml by by import. If you believe you've
made all the correct changes to jldap-beans.xml, but Sakai still does not
seem to behave as expected, be sure that you have uncommented the
jldap-beans.xml import in components.xml.

OOTB, jldap-beans.xml describes nearly all available configuration
options. Several options are worthy of special note:

  Non-Anonymous Binding (aka "auto-binding")
  ------------------------

Set the following properties on the JLDAPDirectoryProvider bean definition:

  1) autoBind = true
  2) ldapUser = {some-DN}
  3) ldapPassword = {ldapUser-password}
  
Please note that as of this writing, ldapUser and ldapPassword will be
ignored if autoBind is set to false.

Because bean definitions are checked into source control, 
ldapPassword is typically defined in one of the three standard Sakai
property-override configuration files, e.g. ${sakai.home}/security.properties.
For example:

  ldapPassword@org.sakaiproject.user.api.UserDirectoryProvider=#######

  ldaps:// and StartTLS
  ------------------------

For ldaps:// connectivity, set the following properties on the 
JLDAPDirectoryProvider bean definition:

  1) ldapPort = 636
  2) secureConnection = true

Check with your LDAP administrator for the correct port for ldaps://
connections. 636 is just a commonly configured value.

For StartTLS support set the following:

  1) secureConnection = true
  2) secureSocketFactory=com.novell.ldap.LDAPJSSEStartTLSFactory

Most likely, you will leave ldapPort set to the default value (389) for
StartTLS-secured connections.

If the secureConnection property is true, the current implementation will
force the deployer to configure keystoreLocation and keystorePassword properties
on the JLDAPDirectoryProvider, unless the javax.net.ssl.trustStore and
javax.net.ssl.trustStorePassword system properties have been set. Future
implementations may be more flexible in this regard.

  EID Blacklisting
  -----------------------

In some cases it may be appropriate to short-circuit searches on certain EID values.
For example, perhaps your directory loads placeholder entries with stock login
values like "guest" while user accounts are provisioned. Directory ACLs may be such 
that Sakai can read attributes from these entries, but doing so would in fact be
inappropriate since the user record has not yet been properly initialized. In other 
cases, you may be able to predict that certain EIDs will never resolve to directory 
entries, in which cases skipping a network hop to the directory may be appealing. Such 
EID interception policies can be configured by injecting an 
edu.amc.sakai.user.EidValidator instance into the JLDAPDirectoryProvider. 
jldap-beans.xml contains a commented-out example of configuring a 
RegexpBlacklistEidValidator implementation of that interface. That class accepts a 
list of Java Pattern strings and refuses to "validate" any EID matching any pattern 
in that list.

Please note that email address blacklisting is not supported in any way at this time.
For example, if a client invokes JLDAPDirectoryProvider.findUserByEmail(), the result
is _not_ suppressed even if the current EIDValidator would refuse to validate the
resulting user's EID.

Typically, EID blacklists are configured at startup and require a restart to pick up
new configuration. However, it is technically possible to adjust the blacklist
configuration at runtime by any number of means. Please be aware, though, that EID 
blacklisting may not have an immediate effect if a blacklisted user has already been
cached. That is, the cache entry must timeout or otherwise flush the user record before
blacklisting policies will go into effect for that user. 

  Derived Email Addresses
  ------------------------
  
At some institutions, not all user attributes are stored in a single directory. In 
the absence of being able to easily configure attribute merging from multiple directories, 
though, it is at least possible able to calculate email addresses from user EID values.

This behavior is implemented by EmailAddressDerivingLdapAttributeMapper, which takes two
new properties in addition to the standard properties supported by 
SimpleLdapAttributeMapper:

  1) addressPattern -- Regexp describing the addresses which the provider will assume
  are not known to the LDAP host. This will cause the provider to attempt to search for
  a user using an EID derived from the email address.  
  2) defaultAddressDomain -- This domain will be used to calculate an email address for
  users entries returned from the LDAP host which do not contain email attributes.
  Specifically, the address will be created by concatenating the users EID and this
  domain.

In most cases, these two properties are set to nearly identical values. For example:

  <property name="defaultAddressDomain" value="myschool.edu" />
  <property name="addressPattern" value=".*?@myschool.edu$" />
  
However, these configuration properties are asymmetric in order to allow for situations 
where a school may wish to configure this feature for performance reasons rather than 
limited data stores. In these cases, addressPattern would be configured to match several 
email domains such that most findUserByEmail() operations are converted to getUserByEid()
operations in order to leverage the provider's EID-keyed cache. In this situation
defaultAddressDomain could be set to null if the LDAP host in fact supplies email 
attributes. Otherwise a single domain will need to be specified for any user entry which
does not report an email attribute. More complicated domain selection strategies could
be implemented via EmailAddressDerivingLdapAttributeMapper extension.

  Connection Pooling
  ------------------------

Currently, unless a custom LdapConnectionManager is injected into
LDAPDirectoryProvider, configuration options are limited to enabling/disabling 
pooling and setting the pool size. Enable pooling by setting 
JLDAPDirectoryProvider.pooling property to true. This will cause the
JLDAPDirectoryProvider to instantiate a PoolingLdapConnectionManager at
initialization time. PoolingLdapConnectionManager.init() will create and
cache the pool itself, sizing it based on the value configured in
JLDAPDirectoryProvider.poolMaxConns.

During testing at Georgia Tech against Fedora LDAP, it was discovered that the
LDAPConnection.isConnectionAlive() method is unreliable for non-OpenLDAP
service providers. Because of this, PooledLDAPConnectionFactory can be
injected with a LdapConnectionLivenessValidator implementation in which
arbitrary connection validity checks can be executed. The default
implementation simply checks the return value of 
LDAPConnection.isConnectionAlive(). A generalized version of 
the Georgia Tech connection validator is also available out of the box.
The following sample configuration in the JLDAPDirectoryProvider bean
definition illustrates deployment of this validator:

  <!-- ... snip ... -->
  <property name="ldapConnectionManager">
    <bean class="edu.amc.sakai.user.PoolingLdapConnectionManager">
      <property name="factory">
        <bean class="edu.amc.sakai.user.PooledLDAPConnectionFactory">
          <property name="connectionLivenessValidator">
            <bean 
  class="edu.amc.sakai.user.SearchExecutingLdapConnectionLivenessValidator"
  init-method="init">
              <property name="baseDn">
                <value>cn=admin,dc=nodomain</value>
              </property>
              <property name="searchAttributeName">
                <value>cn</value>
              </property>
              <property name="serverConfigService">
              	<ref bean="org.sakaiproject.component.api.ServerConfigurationService" />
              </property>
            </bean>
          </property>
        </bean>
      </property>
    </bean>
  </property>

  <property name="pooling">
    <value>true</value>
  </property>

  <property name="poolMaxConns">
    <value>10</value>
  </property>
  <!-- ... snip ... -->

Note that customized connection liveness testing is only available 
when explicitly defining an LdapConnectionManager bean.

Expect pooling configuration to be refactored in future revisions to avoid
splitting configuration options between the LdapConnectionManager and the
LDAPDirectoryProvider.

  User Attribute Mapping
  ------------------------

SimpleLdapAttributeMapper has two configurable properties: 

  1) attributeMappings -- a Map of logical attribute names to physical LDAP
       attribute names. AttributeMappingConstants.DEFAULT_ATTR_MAPPINGS
       defines the set of default attribute names. attributeMappings need
       not be specified if DEFAULT_ATTR_MAPPINGS meets your needs.
       The attributeMappings specified in your bean definition completely
       override DEFAULT_ATTR_MAPPINGS. The two Maps are not merged in
       any way. Keep in mind, though, that the keys in DEFAULT_ATTR_MAPPINGS
       are considered "well known" values and may be used elsewhere, even
       if you do not define mappings for those names. For example,
       getFindUserByEmailFilter() implicitly requires the presence of a
       mapping for AttributeMappingConstants.EMAIL_ATTR_MAPPING_KEY.

       By default, any mapped attribute which does not have a corresponding
       key in DEFAULT_ATTR_MAPPINGS will be mapped onto a User 
       ResourceProperties name-value pair. 
       AttributeMappingConstants.GROUP_MEMBERSHIP_ATTR_MAPPING_KEY is the
       exception to this rule. If the mapped attribute is present in an 
       LDAPEntry, it too will be mapped to ResourceProperties name-value pair.

       SimpleLDAPAttributMapper will also map user DNs into User
       ResourceProperties.

  2) userTypeMapper -- a strategy for calculating Sakai user "type". This
       mechanism, and the three OOTB implementations, were discussed in the 
       "Architecture" section above. By default, SimpleLdapAttributeMapper
       will delegate user type calclulation to an instance of
       EmptyStringUserTypeMapper. This preserves backward-compatible
       behavior.

See the ../component/WEB-XML/jldap-beans.xml, relative to this README, for
an example of a SimpleLdapAttributeMapper bean definition.  


Testing
=======================

JLDAPDirectoryProvider source code includes JUnit and JMock-implemented unit- 
and integration tests. Unit tests are located in ./src/test, relative to this
README. Integration tests are located in a separate Maven project:
../jldap-integration-test/, relative to this README. (Several unit tests exist
in ../jldap-integration-test as well, mainly to verify that utility classes
in that project function as expected.) Unit tests are relatively white-box 
tests intended to verify certain JLDAPDirectoryProvider implementation details 
during each build. The integration tests, which leverage Josh Holtzman's 
"test-harness" framework, are relatively black-box tests intended to verify 
your localized configuration against a "real" LDAP service provider.

No configuration is necessary to enable execution of unit tests.

Integration tests require that Sakai has already been built and deployed to
a Tomcat instance on the local file system. These tests will reuse the bean
definitions and property overrides which have deployed to that Tomcat.

In my experience, executing integration tests from Eclipse is generally more
reliable, faster and more useful than executing integration tests from Maven,
although both approaches will work.

To execute integration tests from Eclipse, you may find that you need to
create a new run configuration for JLDAPDirectoryProviderIntegrationTestSuite,
for example to adjust heap size or specify a value for test.tomcat.home
as a JVM system property. Otherwise, just right click on
JLDAPDirectoryProviderIntegrationTestSuite in the Package Explorer and select
Run As -> JUnit Test. You may experience an extended pause as the Sakai
ComponentManager initializes during TestSuite setup.

To execute integration tests from Maven, you will need to define and activate
a profile which overrides the value of the maven.test.skip property. For
example, in ~/.m2/settings.xml:

<settings xmlns="http://maven.apache.org/POM/4.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
        http://maven.apache.org/xsd/settings-1.0.0.xsd">
   <profiles>
      <profile>
         <id>sakai</id>
         <properties>
            <maven.tomcat.home>/opt/tomcat/</maven.tomcat.home>
         </properties>
      </profile>
      <profile>
         <id>sakai-force-tests</id>
         <properties>
            <maven.tomcat.home>/opt/tomcat/</maven.tomcat.home>
            <maven.test.skip>false</maven.test.skip>
         </properties>
      </profile>
   </profiles>
   <activeProfiles>
      <activeProfile>sakai</activeProfile>
   </activeProfiles>
</settings>

Then, from the command line within ../jldap-directory-test/ 
(after building and deploying Sakai, of course):

  %> mvn -P sakai-force-tests test

  Integration Test Configuration
  ------------------------

For each test method in JLDAPDirectoryProviderTest, a new ApplicationContext
will be created and attached to the Sakai ApplicationContext as a child.
Beans in this child ApplicationContext are defined by
../jldap-integration-test/src/test/resources/jldap-test-context.xml. You can
override, extend and otherwise localize those bean definitions by creating a 
jldap-test-context-local.xml in the same directory.

Many individual bean properties are externalized into
../jldap-integration-test/src/testresources/jldap-test.properties. You can
override, extend and otherwise localize properties in jldap-test.properties by 
defining a jldap-test-local.properties file in the same directory.

Comments in jldap-test-context.xml document most configuration options. The key
abstraction to be aware of when localizing your tests is the UserEditStub.
Instance of that class define test inputs and expectations. For example,
JLDAPDirectoryProviderTest.
testMapsLdapAttributesOntoSakaiUserEditInstanceWhenSearchingByEid()
essentially compares the UserEdit populated by
JLDAPDirectoryProvider.getUser() to a UserEditStub defined in your test
application context, using the latter's eid field to "seed" the UserEdit
to be populated.

At a minimum, you will need to provide definitions for one valid UserEditStub
for positive tests, and one invalid UserEditStub for negative tests. To be
most useful, though, especially if you are deploying a UserTypeMapper, you
should provide at least one additional UserEditStub definition to verify
that your UserTypeMapper can discriminate between user types.

For example, in my current test configuration, I have the following
beans defined in jldap-test-context-local.xml, which reflects the fact that I
intend to map a groupMembership attribute into my valid Sakai User's
ResourceProperties:

  <bean id="user1LocalPropertyOverrides" 
        class="org.springframework.beans.factory.config.PropertiesFactoryBean">
    <property name="properties">
      <props>
        <prop key="groupMembership">${user-1-type}</prop>
      </props>
    </property>
    <property name="singleton"><value>false</value></property>
  </bean>
	
  <bean id="user2LocalPropertyOverrides" 
        class="org.springframework.beans.factory.config.PropertiesFactoryBean">
    <property name="properties">
      <props>
        <prop key="groupMembership">${user-2-type}</prop>
      </props>
    </property>
    <property name="singleton"><value>false</value></property>
  </bean>

Then, in jldap-test-local.properties, I override several properties in
jldap-test.properties to reflect the actual state of my LDAP tree:

  user-1-login=student1
  user-1-type=student
  user-1-property-udp.dn=cn=student1,ou=users,dc=nodomain

  user-2-login=faculty1
  user-2-type=faculty
  user-2-property-udp.dn=cn=faculty1,ou=users,dc=nodomain

Issues Addressed
=======================

This implementation addresses the following open (at least as of Aug 18, 2007)
Jira issues:

  http://bugs.sakaiproject.org/jira/browse/SAK-4184 -- AD support
  http://bugs.sakaiproject.org/jira/browse/SAK-4190 -- AD support
  http://bugs.sakaiproject.org/jira/browse/SAK-4530 -- Connection leak
  http://bugs.sakaiproject.org/jira/browse/SAK-7338 -- User.eid mapping


Questions/Comments
=======================

Please contact Dan Mccallum (dmccallum@unicon.net).
