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

import java.util.Map;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.Certificate;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.AuthenticationManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.converter.RsaKeyConverters;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
@ImportResource("file:src/webapp/WEB-INF/xlogin-context.saml.xml")
public class SamlTestConfiguration {
    @Bean(name = "org.sakaiproject.component.api.ServerConfigurationService")
    public ServerConfigurationService configuration() throws Exception {
        ServerConfigurationService configuration = mock(ServerConfigurationService.class);
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, "test-password".toCharArray());
        PrivateKey key;
        Certificate certificate;
        try (InputStream input = new ClassPathResource("saml/idp-key.pem").getInputStream()) {
            key = RsaKeyConverters.pkcs8().convert(input);
        }
        try (InputStream input = new ClassPathResource("saml/idp-cert.pem").getInputStream()) {
            certificate = CertificateFactory.getInstance("X.509").generateCertificate(input);
        }
        keyStore.setKeyEntry("sakai", key, "test-password".toCharArray(), new Certificate[] { certificate });
        Path path = Files.createTempFile("sakai-saml-test", ".jks");
        path.toFile().deleteOnExit();
        try (OutputStream output = Files.newOutputStream(path)) {
            keyStore.store(output, "test-password".toCharArray());
        }
        Map<String, String> properties = Map.of(
                "saml.idp.metadata", "classpath:saml/idp-metadata.xml",
                "saml.entity-id", "https://sakai.example.test/saml",
                "saml.principal.attribute", "urn:oid:1.3.6.1.4.1.5923.1.1.1.6",
                "saml.keystore.path", path.toUri().toString(),
                "saml.keystore.alias", "sakai",
                "saml.keystore.password", "test-password");
        when(configuration.getString(anyString(), anyString())).thenAnswer(invocation ->
                properties.getOrDefault(invocation.getArgument(0), invocation.getArgument(1)));
        when(configuration.getInt(anyString(), anyInt())).thenAnswer(invocation -> invocation.getArgument(1));
        when(configuration.getServerUrl()).thenReturn("https://sakai.example.test");
        when(configuration.getPortalUrl()).thenReturn("https://sakai.example.test/portal");
        return configuration;
    }

    @Bean(name = "org.sakaiproject.user.api.AuthenticationManager")
    public AuthenticationManager authenticationManager() {
        return mock(AuthenticationManager.class);
    }

    @Bean(name = "org.sakaiproject.event.api.UsageSessionService")
    public UsageSessionService usageSessionService() {
        return mock(UsageSessionService.class);
    }

    @Bean(name = "org.sakaiproject.tool.api.SessionManager")
    public SessionManager sessionManager() {
        return mock(SessionManager.class);
    }
}
