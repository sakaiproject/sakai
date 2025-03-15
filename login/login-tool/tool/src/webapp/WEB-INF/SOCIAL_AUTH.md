# Social Authentication for Sakai

This document provides details about the social authentication implementation in Sakai, which allows users to sign in using their Google or Microsoft accounts.

## Features

- OAuth2 client implementation for authentication with external providers
- Currently supports Google and Microsoft identity providers
- Configurable user attribute mapping
- Integration with Sakai's session management and user directory service
- Customizable UI elements including provider icons and display names

## Configuration

### Basic Configuration

Enable social authentication by adding these properties to your `sakai.properties`:

```properties
# Enable social authentication globally
social.authentication.enabled=true

# Enable specific providers
social.authentication.provider.google.enabled=true
social.authentication.provider.microsoft.enabled=true

# Configure OAuth2 client credentials for Google
social.authentication.provider.google.client_id=your_google_client_id
social.authentication.provider.google.client_secret=your_google_client_secret

# Configure OAuth2 client credentials for Microsoft
social.authentication.provider.microsoft.client_id=your_microsoft_client_id
social.authentication.provider.microsoft.client_secret=your_microsoft_client_secret
```

### Advanced Configuration

Additional optional configuration properties:

```properties
# Customize display names (defaults are "Google" and "Microsoft")
social.authentication.provider.google.displayName=Google Workspace
social.authentication.provider.microsoft.displayName=Microsoft 365

# Customize icons (defaults are Bootstrap icons "bi-google" and "bi-microsoft")
social.authentication.provider.google.icon=bi-google
social.authentication.provider.microsoft.icon=custom-icon-class

# Configure user attribute mapping (which OAuth2 user attribute to use for Sakai user ID)
social.authentication.provider.google.userIdAttribute=email
social.authentication.provider.microsoft.userIdAttribute=mail
```

## OAuth2 Client Registration

The OAuth2 client registration is configured in `xlogin-context.oauth2.xml`. For both Google and Microsoft:

1. Create client credentials (client ID and secret) in the respective developer portals
2. Configure the redirect URLs in the developer portals to match Sakai's URL: `{baseUrl}/login/oauth2/code/{registrationId}`
3. Ensure the required scopes are configured (openid, email, profile)

## Authentication Flow

1. User clicks on a social login button on the Sakai login page
2. User is redirected to the provider's login page
3. After successful authentication, the provider redirects back to Sakai with an authorization code
4. Sakai exchanges the authorization code for an access token and user information
5. The `SakaiSocialAuthenticationSuccessHandler` processes the authentication and creates a Sakai session
6. The user is redirected to their Sakai workspace or the originally requested URL

## Implementation Details

### Components

- `SocialAuthenticationService` - Interface defining methods for provider discovery and configuration
- `SocialAuthenticationServiceImpl` - Implementation of the service with provider configuration
- `SakaiSocialAuthenticationSuccessHandler` - Handles successful authentication and session creation
- `OAuth2LoginAuthenticationFilter` - Spring Security filter for processing OAuth2 login requests
- `OAuth2AuthorizationRequestRedirectFilter` - Filter for handling OAuth2 authorization redirects

### Templates

The login page template (`xlogin.vm`) has been updated to include social login buttons when enabled.

## Troubleshooting

Common issues:

1. **Social login buttons not appearing**: Verify that social authentication is properly enabled in properties
2. **Redirect errors**: Ensure redirect URIs are correctly configured in provider developer consoles
3. **Authentication failures**: Check logs for details about the authentication failure

## Adding New Providers

To add support for additional OAuth2 providers:

1. Add client registration bean in `xlogin-context.oauth2.xml`
2. Add provider defaults in `SocialAuthenticationServiceImpl.DEFAULT_PROVIDER_CONFIGS`
3. Configure the new provider in properties

## References

- [Spring Security OAuth2 Client Documentation](https://docs.spring.io/spring-security/reference/servlet/oauth2/client/index.html)
- [Google Identity Platform](https://developers.google.com/identity/protocols/oauth2)
- [Microsoft Identity Platform](https://learn.microsoft.com/en-us/azure/active-directory/develop/)