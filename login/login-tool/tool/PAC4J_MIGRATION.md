# PAC4J Migration Guide

This document outlines the migration from legacy CAS and SAML implementations to PAC4J-based authentication.

## Overview

The login tool has been updated to use PAC4J instead of:
- `jasig.cas.client` for CAS authentication
- `spring-security-saml2-core` for SAML authentication

## Dependencies Changed

### Removed Dependencies
- `org.springframework.security.extensions:spring-security-saml2-core:1.0.10.RELEASE`
- `org.jasig.cas.client:cas-client-core:3.6.4`

### Added Dependencies  
- `org.pac4j:pac4j-cas:5.7.5`
- `org.pac4j:pac4j-saml:5.7.5`
- `org.pac4j:pac4j-core:5.7.5`
- `org.pac4j:pac4j-javaee:5.7.5`

## Configuration Changes

### Spring Context
- **New**: `xlogin-context.pac4j.xml` - Unified PAC4J configuration
- **Removed**: `xlogin-context.cas.xml` - Legacy CAS configuration
- **Removed**: `xlogin-context.cas3.xml` - Legacy CAS3 configuration  
- **Removed**: `xlogin-context.saml.xml` - Legacy SAML configuration

### Properties Configuration
PAC4J uses property-based configuration instead of XML beans:

```properties
# CAS Configuration
sakai.login.cas.server.login.url=https://cas.example.edu/cas/login
sakai.login.cas.server.prefix.url=https://cas.example.edu/cas

# SAML Configuration
sakai.login.saml.idp.metadata.path=/path/to/idp-metadata.xml
sakai.login.saml.sp.metadata.path=/path/to/sp-metadata.xml
sakai.login.saml.entity.id=SakaiSAMLApp
```

## Classes Changed

### Removed Classes
- `SakaiCasAuthenticationFilter` - Replaced by `SakaiPac4jAuthenticationFilter`
- `SakaiLogoutSamlFilter` - Replaced by `SakaiPac4jLogoutHandler`
- `SHA256SAMLBootstrap` - No longer needed with PAC4J SAML

### New Classes
- `Pac4jConfig` - Main PAC4J configuration
- `SakaiPac4jAuthenticationFilter` - PAC4J authentication integration
- `SakaiPac4jLogoutHandler` - PAC4J logout integration
- `SakaiPac4jEntryPoint` - PAC4J authentication entry point
- `SakaiPac4jAuthenticationProvider` - Spring Security authentication provider

### Preserved Classes
- `SafeDelegatingFilterProxy` - General purpose filter proxy

## Migration Steps

1. **Update sakai.properties** with PAC4J configuration properties
2. **Update applicationContext.xml** to import `xlogin-context.pac4j.xml`
3. **Remove all old CAS/SAML context files** - they have been completely removed
4. **Test authentication flows** for both CAS and SAML
5. **Update documentation** and deployment guides

## Benefits of PAC4J

- **Unified approach**: Single library for multiple authentication protocols
- **Better maintenance**: Actively maintained with regular security updates
- **Modern implementation**: Uses current best practices and standards
- **Flexibility**: Easier to configure and extend for new protocols
- **Spring Security 5.7+ compatibility**: Better integration with modern Spring Security

## Troubleshooting

### Common Issues
1. **ClassNotFoundException**: Ensure old dependencies are removed and new ones added
2. **Configuration not found**: Check property names and paths
3. **Authentication loops**: Verify callback URLs match PAC4J expectations

### Debug Configuration
Enable debug logging for PAC4J:
```properties
log4j.logger.org.pac4j=DEBUG
```

## References
- [PAC4J Documentation](https://www.pac4j.org/docs/spring-security.html)
- [Spring Security PAC4J](https://github.com/pac4j/spring-security-pac4j)