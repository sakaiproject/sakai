# Sakai SAML Migration Guide

This document provides guidance for migrating from the legacy `spring-security-saml2-core` extension to the modern built-in Spring Security SAML support.

## Changes Made

1. Updated dependencies:
   - Removed: `org.springframework.security.extensions:spring-security-saml2-core:1.0.10.RELEASE`
   - Added: `org.springframework.security:spring-security-saml2-service-provider:${sakai.spring.security.version}`

2. Created new SAML implementation classes:
   - `SakaiSamlAuthenticationConverter` - Extracts username from SAML assertions
   - `UpnSamlAuthenticationConverter` - Specialization for UPN attribute
   - `SakaiLogoutHandler` - Handles Sakai session logout during SAML logout

3. Created a new modern SAML configuration file:
   - `xlogin-context.modern-saml.xml` - Uses the new Spring Security SAML2 APIs with improved multi-IDP support

## How to Enable Modern SAML

1. In your Sakai deployment, rename the existing configuration file:
   ```
   mv /opt/tomcat/sakai/xlogin-context.saml.xml /opt/tomcat/sakai/xlogin-context.saml.xml.bak
   ```

2. Copy the new configuration file:
   ```
   cp xlogin-context.modern-saml.xml /opt/tomcat/sakai/xlogin-context.saml.xml
   ```

3. Configure your IdP metadata file(s):
   - For a single IdP, edit the `fromMetadataLocation` parameter in the default-idp registration
   - For multiple IdPs, add additional `RelyingPartyRegistration` beans (see example in config)

4. Select the appropriate attribute for username extraction:
   - Default: eduPersonPrincipalName (ePPN): `urn:oid:1.3.6.1.4.1.5923.1.1.1.6`
   - UPN: `http://schemas.xmlsoap.org/ws/2005/05/identity/claims/upn`
   - Email: `urn:oid:0.9.2342.19200300.100.1.3`
   - Other attributes as needed by your institution

## Multi-IdP Configuration

The new configuration makes it easy to support multiple identity providers:

1. Each IdP needs a unique registration ID (e.g., `default-idp`, `campus-idp`)
2. Each IdP needs its own metadata file path
3. The registration ID is included in the assertion consumer service URL

Example for adding a second IdP:

```xml
<bean class="org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration"
      factory-method="withRegistrationId">
    <constructor-arg value="campus-idp" />
    <constructor-arg>
        <bean class="org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations"
              factory-method="fromMetadataLocation">
            <constructor-arg value="file:/opt/tomcat/sakai/campus_idp.xml" />
        </bean>
    </constructor-arg>
    <property name="entityId" value="SakaiSAMLApp" />
    <property name="assertionConsumerServiceLocation" value="{baseUrl}/container/saml2/SSO/campus-idp" />
    <property name="singleLogoutServiceLocation" value="{baseUrl}/container/saml2/logout/campus-idp" />
</bean>
```

## Using HTTPS Metadata URLs

You can use HTTPS URLs for IdP metadata instead of local files:

```xml
<bean class="org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations"
      factory-method="fromMetadataLocation">
    <constructor-arg value="https://idp.example.org/metadata.xml" />
</bean>
```

## Key Configuration Elements

1. SAML Configuration Properties:
   - Entity ID: The entity ID for your Sakai instance (default: `SakaiSAMLApp`)
   - Assertion Consumer Service: `/container/saml2/SSO/{registrationId}`
   - Single Logout Service: `/container/saml2/logout/{registrationId}`
   - SP Metadata: Available at `/container/saml2/metadata/{registrationId}`

2. Attribute Mapping:
   - Configure in the `sakaiSamlAuthenticationConverter` bean
   - Customize by setting the `usernameAttributeName` property

## Troubleshooting

If you encounter issues with the SAML integration:

1. Enable DEBUG logging for SAML components:
   ```
   log4j.logger.org.springframework.security.saml2=DEBUG
   log4j.logger.org.sakaiproject.login.saml=DEBUG
   ```

2. Check that your IdP metadata file is valid and accessible

3. Verify that your SP metadata is properly configured on your IdP:
   - SP metadata is available at: `/container/saml2/metadata/{registrationId}`

4. Common issues:
   - Missing or incorrect attribute mapping
   - Certificate validation issues
   - Clock synchronization between IdP and SP
   - Registration ID mismatch in URLs