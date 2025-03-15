# Adding a New Social Authentication Provider

This guide explains how to add support for a new OAuth2 social authentication provider (beyond Google and Microsoft) to the Sakai login system.

## Overview of Social Authentication Components

The social authentication system in Sakai has these main components:

1. **Configuration:** OAuth2 client registration in Spring Security
2. **Provider Configuration:** Default settings in `SocialAuthenticationServiceImpl`
3. **UI Elements:** Button styling and display in the login template
4. **Authentication Flow:** OAuth2 authentication and user mapping

## Step 1: Add Client Registration

Edit `/login/login-tool/tool/src/webapp/WEB-INF/xlogin-context.oauth2.xml` to add a new client registration bean:

```xml
<!-- Example for adding GitHub as a provider -->
<bean id="githubClientRegistration" 
      factory-method="withRegistrationId"
      class="org.springframework.security.oauth2.client.registration.ClientRegistration">
    <constructor-arg value="github"/>
    <constructor-arg>
        <bean factory-method="getBuilder"
              class="org.springframework.security.oauth2.client.registration.ClientRegistration$Builder">
            <constructor-arg value="github"/>
            <constructor-arg value="github"/>
            <constructor-arg value="https://github.com/login/oauth/authorize"/>
        </bean>
    </constructor-arg>
    <property name="clientId" value="${social.authentication.provider.github.client_id:}"/>
    <property name="clientSecret" value="${social.authentication.provider.github.client_secret:}"/>
    <property name="redirectUri" 
              value="{baseUrl}/login/oauth2/code/{registrationId}"/>
    <property name="scope">
        <list>
            <value>read:user</value>
            <value>user:email</value>
        </list>
    </property>
    <property name="authorizationUri" value="https://github.com/login/oauth/authorize"/>
    <property name="tokenUri" value="https://github.com/login/oauth/access_token"/>
    <property name="userInfoUri" value="https://api.github.com/user"/>
    <property name="userNameAttributeName" value="id"/>
</bean>
```

Add this bean to the list of client registrations in the `clientRegistrationRepository` bean in the same file:

```xml
<constructor-arg>
    <list>
        <!-- Existing Google and Microsoft registrations -->
        <!-- Your new provider registration -->
        <ref bean="githubClientRegistration"/>
    </list>
</constructor-arg>
```

## Step 2: Update SocialAuthenticationServiceImpl

Edit `/login/login-tool/tool/src/java/org/sakaiproject/login/social/SocialAuthenticationServiceImpl.java` to add default configurations for your new provider:

```java
static {
    // Existing Google and Microsoft defaults
    
    // GitHub defaults
    Map<String, String> githubDefaults = new HashMap<>();
    githubDefaults.put(PROVIDER_DISPLAY_NAME, "GitHub");
    githubDefaults.put(PROVIDER_ICON, "bi-github");
    githubDefaults.put(PROVIDER_USER_ID_ATTRIBUTE, "email");
    DEFAULT_PROVIDER_CONFIGS.put("github", githubDefaults);
}
```

## Step 3: Handle User Attribute Mapping

In some cases, you may need to update the user mapping logic in `SakaiSocialAuthenticationSuccessHandler.java`. Review the `mapSocialUserToSakaiUser` method to ensure it correctly extracts user information from your new provider.

For GitHub example:
- GitHub may return multiple emails or store the primary email in a different field
- You may need to customize how the email is extracted

## Step 4: Configuration Properties

Document the required properties for your new provider in `SOCIAL_AUTH.md`:

```properties
# Enable the new provider
social.authentication.provider.github.enabled=true

# Configure OAuth2 client credentials for GitHub
social.authentication.provider.github.client_id=your_github_client_id
social.authentication.provider.github.client_secret=your_github_client_secret

# Optional: Customize display name and icon
social.authentication.provider.github.displayName=GitHub
social.authentication.provider.github.icon=bi-github

# Configure which attribute to use for user ID mapping
social.authentication.provider.github.userIdAttribute=email
```

## Step 5: Test Your Implementation

1. Register an OAuth2 client with your provider (e.g., GitHub Developer settings)
2. Configure the redirect URI to `https://your-sakai-instance.edu/login/oauth2/code/github`
3. Add the required properties to your `sakai.properties`
4. Restart Sakai and try logging in with the new provider

## Provider-Specific Considerations

### GitHub
- GitHub returns user information differently than Google/Microsoft
- You'll need access to the user's email which requires the `user:email` scope

### Facebook
- Facebook requires the `openid` scope to be omitted
- You may need to request specific fields using `userInfoUri` parameters

### LinkedIn
- LinkedIn requires using v2 of their API for OAuth2
- User profile information is structured differently and may require custom mapping

## Troubleshooting

If your new provider doesn't appear or doesn't work:

1. Check server logs for OAuth2 error messages
2. Verify client registration is correctly configured
3. Ensure redirect URIs match exactly between Sakai config and provider settings
4. Check that all required scopes are included
5. Test the user attribute mapping logic with a debugger

Remember that different OAuth2 providers may have slightly different implementations of the standard, requiring small adjustments to the configuration.