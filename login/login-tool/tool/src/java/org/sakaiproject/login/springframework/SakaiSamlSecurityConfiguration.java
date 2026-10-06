/**
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.login.springframework;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Collection;

import org.opensaml.saml.saml2.core.AuthnStatement;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.AuthenticationManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.saml2.core.Saml2Error;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml5AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.core.Saml2ResponseValidatorResult;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;
import org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.authentication.OpenSaml5AuthenticationRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.logout.OpenSaml5LogoutRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2RelyingPartyInitiatedLogoutSuccessHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.sakaiproject.login.filter.SakaiLogoutSamlFilter;

/** Spring Security's Jakarta SAML support, enabled through sakai.properties. */
@Configuration
@EnableWebSecurity
@Conditional(SakaiSamlSecurityConfiguration.SamlEnabledCondition.class)
public class SakaiSamlSecurityConfiguration {

    private final ServerConfigurationService serverConfigurationService;
    private final AuthenticationManager authenticationManager;
    private final UsageSessionService usageSessionService;
    private final SessionManager sessionManager;

    public SakaiSamlSecurityConfiguration(ServerConfigurationService serverConfigurationService,
            AuthenticationManager authenticationManager, UsageSessionService usageSessionService,
            SessionManager sessionManager) {
        this.serverConfigurationService = serverConfigurationService;
        this.authenticationManager = authenticationManager;
        this.usageSessionService = usageSessionService;
        this.sessionManager = sessionManager;
    }

    public static class SamlEnabledCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return context.getBeanFactory().getBean(ServerConfigurationService.class)
                    .getBoolean("saml.enabled", false);
        }
    }

    @Bean
    public RelyingPartyRegistrationRepository relyingPartyRegistrationRepository() throws Exception {
        String metadataLocation = serverConfigurationService.getString("saml.idp.metadata", "");
        Assert.hasText(metadataLocation, "saml.idp.metadata must identify the trusted IdP metadata");
        Assert.isTrue(metadataLocation.startsWith("https:") || metadataLocation.startsWith("file:")
                || metadataLocation.startsWith("classpath:"), "SAML metadata must use HTTPS, file, or classpath resources");
        Collection<RelyingPartyRegistration.Builder> builders =
                RelyingPartyRegistrations.collectionFromMetadataLocation(metadataLocation);
        // An explicit issuer prevents accidentally trusting a different IdP in federation metadata.
        String issuer = serverConfigurationService.getString("saml.idp.entity-id", "");
        Assert.isTrue(StringUtils.hasText(issuer) || builders.size() == 1,
                "saml.idp.entity-id is required when metadata contains multiple IdPs");
        RelyingPartyRegistration.Builder selected = null;
        for (RelyingPartyRegistration.Builder builder : builders) {
            RelyingPartyRegistration candidate = builder.registrationId("sakai").build();
            if (!StringUtils.hasText(issuer) || issuer.equals(candidate.getAssertingPartyMetadata().getEntityId())) {
                selected = builder;
                break;
            }
        }
        Assert.notNull(selected, "Configured SAML IdP was not found in metadata");
        String entityId = serverConfigurationService.getString("saml.entity-id", "");
        Assert.hasText(entityId, "saml.entity-id must match the service provider registered with the IdP");
        String baseUrl = serverConfigurationService.getServerUrl() + "/sakai-login-tool";
        selected.entityId(entityId)
                .assertionConsumerServiceLocation(baseUrl + "/container/saml/SSO")
                .assertionConsumerServiceBinding(Saml2MessageBinding.POST);

        String keystoreLocation = serverConfigurationService.getString("saml.keystore.path", "");
        if (StringUtils.hasText(keystoreLocation)) {
            KeyStore keystore = KeyStore.getInstance(serverConfigurationService.getString("saml.keystore.type", "JKS"));
            String password = serverConfigurationService.getString("saml.keystore.password", "");
            try (InputStream input = new DefaultResourceLoader().getResource(keystoreLocation).getInputStream()) {
                keystore.load(input, password.toCharArray());
            }
            String alias = serverConfigurationService.getString("saml.keystore.alias", "");
            String keyPassword = serverConfigurationService.getString("saml.keystore.key-password", password);
            PrivateKey key = (PrivateKey) keystore.getKey(alias, keyPassword.toCharArray());
            X509Certificate certificate = (X509Certificate) keystore.getCertificate(alias);
            Assert.notNull(key, "SAML keystore alias must contain a private key");
            Assert.notNull(certificate, "SAML keystore alias must contain an X.509 certificate");
            selected.signingX509Credentials(credentials -> credentials.add(Saml2X509Credential.signing(key, certificate)))
                    .decryptionX509Credentials(credentials -> credentials.add(Saml2X509Credential.decryption(key, certificate)))
                    .singleLogoutServiceLocation(baseUrl + "/container/saml/SingleLogout")
                    .singleLogoutServiceBindings(bindings -> {
                        bindings.clear();
                        bindings.add(Saml2MessageBinding.POST);
                        bindings.add(Saml2MessageBinding.REDIRECT);
                    });
        }
        RelyingPartyRegistration registration = selected.build();
        Assert.isTrue(!registration.getAssertingPartyMetadata().getWantAuthnRequestsSigned()
                || !registration.getSigningX509Credentials().isEmpty(), "The IdP requires SP signing credentials");
        return new InMemoryRelyingPartyRegistrationRepository(registration);
    }

    @Bean
    public OpenSaml5AuthenticationProvider samlAuthenticationProvider() {
        OpenSaml5AuthenticationProvider provider = new OpenSaml5AuthenticationProvider();
        OpenSaml5AuthenticationProvider.AssertionValidator validator =
                OpenSaml5AuthenticationProvider.AssertionValidator.withDefaults();
        OpenSaml5AuthenticationProvider.ResponseAuthenticationConverter converter =
                new OpenSaml5AuthenticationProvider.ResponseAuthenticationConverter();
        provider.setResponseAuthenticationConverter(token -> {
            Saml2Authentication authentication = converter.convert(token);
            SakaiSamlPrincipal principal = new SakaiSamlPrincipal(
                    (Saml2AuthenticatedPrincipal) authentication.getPrincipal(),
                    token.getResponse().getAssertions().get(0).getSubject().getNameID());
            return new Saml2Authentication(principal, authentication.getSaml2Response(), authentication.getAuthorities());
        });
        int maxAge = serverConfigurationService.getInt("saml.max-authentication-age", 7200);
        Assert.isTrue(maxAge > 0, "saml.max-authentication-age must be positive");
        provider.setAssertionValidator(token -> {
            Saml2ResponseValidatorResult result = validator.convert(token);
            if (token.getAssertion().getAuthnStatements().isEmpty()) {
                return result.concat(new Saml2Error("missing_authentication_statement", "SAML authentication statement is required"));
            }
            Instant now = Instant.now();
            for (AuthnStatement statement : token.getAssertion().getAuthnStatements()) {
                Instant authenticatedAt = statement.getAuthnInstant();
                if (authenticatedAt == null || authenticatedAt.isBefore(now.minusSeconds(maxAge))) {
                    return result.concat(new Saml2Error("invalid_authentication_age", "SAML authentication is too old"));
                }
            }
            return result;
        });
        return provider;
    }

    @Bean
    public SakaiSamlAuthenticationSuccessHandler samlAuthenticationSuccessHandler() {
        return new SakaiSamlAuthenticationSuccessHandler(authenticationManager, usageSessionService, sessionManager,
                serverConfigurationService, serverConfigurationService.getString("saml.principal.attribute", ""));
    }

    @Bean
    public SakaiLogoutSamlFilter samlLogoutHandler() {
        SakaiLogoutSamlFilter handler = new SakaiLogoutSamlFilter();
        handler.setSessionManager(sessionManager);
        handler.setUsageSessionService(usageSessionService);
        return handler;
    }

    @Bean
    public SecurityFilterChain samlSecurityFilterChain(HttpSecurity http,
            RelyingPartyRegistrationRepository registrations, OpenSaml5AuthenticationProvider provider,
            SakaiSamlAuthenticationSuccessHandler successHandler, SakaiLogoutSamlFilter logoutHandler) throws Exception {
        DefaultRelyingPartyRegistrationResolver registrationResolver = new DefaultRelyingPartyRegistrationResolver(registrations);
        OpenSaml5AuthenticationRequestResolver requestResolver = new OpenSaml5AuthenticationRequestResolver(
                (request, registrationId) -> registrationResolver.resolve(request, "sakai"));
        requestResolver.setRequestMatcher(PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/container/saml/login/**"));
        http.securityMatcher(PathPatternRequestMatcher.withDefaults().matcher("/container/**"))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                PathPatternRequestMatcher.withDefaults().matcher("/container/saml/login/**"),
                                PathPatternRequestMatcher.withDefaults().matcher("/container/saml/SSO"),
                                PathPatternRequestMatcher.withDefaults().matcher("/container/saml/SingleLogout"),
                                PathPatternRequestMatcher.withDefaults().matcher("/container/saml/metadata"),
                                PathPatternRequestMatcher.withDefaults().matcher("/container/saml/logout")).permitAll()
                        .anyRequest().authenticated())
                .requestCache(cache -> cache.requestCache(new NullRequestCache()))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(
                        new LoginUrlAuthenticationEntryPoint("/container/saml/login")))
                .saml2Login(saml -> saml.relyingPartyRegistrationRepository(registrations)
                        .authenticationRequestResolver(requestResolver)
                        .authenticationManager(new ProviderManager(provider))
                        .loginProcessingUrl("/container/saml/SSO")
                        .successHandler(successHandler)
                        .failureHandler((request, response, exception) -> response.sendRedirect(
                                serverConfigurationService.getPortalUrl() + "/xlogin")))
                .saml2Metadata(metadata -> metadata.metadataUrl("/container/saml/metadata"))
                .logout(logout -> logout
                        // Preserve the container's existing GET logout redirect.
                        .logoutRequestMatcher(new OrRequestMatcher(
                                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/container/saml/logout"),
                                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/container/saml/logout")))
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessUrl(serverConfigurationService.getPortalUrl()));
        if (!registrations.findByRegistrationId("sakai").getSigningX509Credentials().isEmpty()) {
            OpenSaml5LogoutRequestResolver logoutResolver = new OpenSaml5LogoutRequestResolver(registrations);
            logoutResolver.setParametersConsumer(parameters -> {
                SakaiSamlPrincipal principal = (SakaiSamlPrincipal) parameters.getAuthentication().getPrincipal();
                parameters.getLogoutRequest().getNameID().setFormat(principal.getNameIdFormat());
                parameters.getLogoutRequest().getNameID().setNameQualifier(principal.getNameQualifier());
                parameters.getLogoutRequest().getNameID().setSPNameQualifier(principal.getSpNameQualifier());
            });
            Saml2RelyingPartyInitiatedLogoutSuccessHandler singleLogout =
                    new Saml2RelyingPartyInitiatedLogoutSuccessHandler(logoutResolver);
            // Spring's SAML logout trigger is POST-only. Sakai's container redirect uses GET.
            // The response filter calls this handler without an Authentication, finishing at the portal.
            http.logout(logout -> logout.logoutSuccessHandler((request, response, authentication) -> {
                if (authentication != null && StringUtils.hasText(registrations.findByRegistrationId("sakai")
                        .getAssertingPartyMetadata().getSingleLogoutServiceLocation())) {
                    singleLogout.onLogoutSuccess(request, response, authentication);
                } else {
                    response.sendRedirect(serverConfigurationService.getPortalUrl());
                }
            }));
            http.saml2Logout(saml -> saml
                        .logoutUrl("/container/saml/logout")
                        .logoutRequest(request -> request.logoutUrl("/container/saml/SingleLogout")
                                .logoutRequestResolver(logoutResolver))
                        .logoutResponse(response -> response.logoutUrl("/container/saml/SingleLogout")));
        }
        return http.build();
    }
}
