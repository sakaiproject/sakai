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
   - `xlogin-context.modern-saml.xml` - Uses the new Spring Security SAML2 APIs

## How to Enable Modern SAML

1. In your Sakai deployment, rename the existing configuration file:
   ```
   mv /opt/tomcat/sakai/xlogin-context.saml.xml /opt/tomcat/sakai/xlogin-context.saml.xml.bak
   ```

2. Copy the new configuration file:
   ```
   cp xlogin-context.modern-saml.xml /opt/tomcat/sakai/xlogin-context.saml.xml
   ```

3. Update the configuration to point to your IDP metadata file:
   - Edit the `fromMetadataLocation` parameter in the RelyingPartyRegistrations bean

4. Select the appropriate authentication converter:
   - Use `SakaiSamlAuthenticationConverter` for eduPersonPrincipalName (default)
   - Or use `UpnSamlAuthenticationConverter` for UPN-based authentication
   - Or create your own by extending `SakaiSamlAuthenticationConverter`

## Key Configuration Elements

1. SAML Configuration Properties:
   - Entity ID: The entity ID for your Sakai instance (default: `SakaiSAMLApp`)
   - Assertion Consumer Service: `/container/saml2/SSO/sakai`
   - Single Logout Service: `/container/saml2/logout/sakai`

2. Attribute Mapping:
   - The default attribute for username is `urn:oid:1.3.6.1.4.1.5923.1.1.1.6` (eduPersonPrincipalName)
   - For UPN, the attribute is `http://schemas.xmlsoap.org/ws/2005/05/identity/claims/upn`
   - Customize by setting the `usernameAttributeName` property on the authentication converter

## Troubleshooting

If you encounter issues with the SAML integration:

1. Enable DEBUG logging for SAML components:
   ```
   log4j.logger.org.springframework.security.saml2=DEBUG
   log4j.logger.org.sakaiproject.login.saml=DEBUG
   ```

2. Check that your IDP metadata file is valid and accessible

3. Verify that your SP metadata is properly configured on your IdP:
   - SP metadata is available at: `/container/saml2/metadata/sakai`

4. Common issues:
   - Missing or incorrect attribute mapping
   - Certificate validation issues
   - Clock synchronization between IdP and SP