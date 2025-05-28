/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.login.config;

import lombok.extern.slf4j.Slf4j;

import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * PAC4J configuration for CAS and SAML authentication protocols.
 * Replaces the legacy spring-security-saml2-core and jasig.cas.client implementations.
 */
@Slf4j
@Configuration
public class Pac4jConfig {

    @Value("${sakai.login.cas.server.login.url:}")
    private String casServerLoginUrl;

    @Value("${sakai.login.cas.server.prefix.url:}")
    private String casServerPrefixUrl;

    @Value("${sakai.login.saml.keystore.path:}")
    private String samlKeystorePath;

    @Value("${sakai.login.saml.keystore.password:}")
    private String samlKeystorePassword;

    @Value("${sakai.login.saml.sp.metadata.path:}")
    private String samlSpMetadataPath;

    @Value("${sakai.login.saml.idp.metadata.path:}")
    private String samlIdpMetadataPath;

    @Value("${sakai.login.saml.entity.id:SakaiSAMLApp}")
    private String samlEntityId;

    @Bean
    public Config config() {
        Clients clients = new Clients();
        
        // Set the callback URL pattern for all clients
        clients.setCallbackUrl("/portal/container");
        
        // Add CAS client if configured
        if (casServerLoginUrl != null && !casServerLoginUrl.isEmpty()) {
            clients.setClients(casClient());
            log.info("CAS authentication configured with server: {}", casServerLoginUrl);
        }
        
        // Add SAML client if configured
        if (samlIdpMetadataPath != null && !samlIdpMetadataPath.isEmpty()) {
            if (clients.getClients().isEmpty()) {
                clients.setClients(saml2Client());
            } else {
                clients.getClients().add(saml2Client());
            }
            log.info("SAML authentication configured with IdP metadata: {}", samlIdpMetadataPath);
        }
        
        // If no clients are configured, add a default one to prevent initialization errors
        if (clients.getClients().isEmpty()) {
            log.warn("No PAC4J clients configured. Please configure CAS or SAML authentication.");
        }
        
        return new Config(clients);
    }

    public CasClient casClient() {
        CasConfiguration casConfiguration = new CasConfiguration(casServerLoginUrl);
        if (casServerPrefixUrl != null && !casServerPrefixUrl.isEmpty()) {
            casConfiguration.setPrefixUrl(casServerPrefixUrl);
        }
        
        CasClient casClient = new CasClient(casConfiguration);
        casClient.setName("cas");
        casClient.setCallbackUrl("/container/cas/callback");
        
        return casClient;
    }

    public SAML2Client saml2Client() {
        SAML2Configuration cfg = new SAML2Configuration();
        
        // Configure keystore if provided
        if (samlKeystorePath != null && !samlKeystorePath.isEmpty()) {
            cfg.setKeystorePath(samlKeystorePath);
            if (samlKeystorePassword != null && !samlKeystorePassword.isEmpty()) {
                cfg.setKeystorePassword(samlKeystorePassword);
            }
        }
        
        // Configure SP metadata
        if (samlSpMetadataPath != null && !samlSpMetadataPath.isEmpty()) {
            cfg.setServiceProviderMetadataPath(samlSpMetadataPath);
        }
        cfg.setServiceProviderEntityId(samlEntityId);
        
        // Configure IdP metadata
        cfg.setIdentityProviderMetadataPath(samlIdpMetadataPath);
        
        // Set callback URL
        cfg.setCallbackUrl("/container/saml/SSO");
        
        SAML2Client saml2Client = new SAML2Client(cfg);
        saml2Client.setName("saml2");
        
        return saml2Client;
    }
}