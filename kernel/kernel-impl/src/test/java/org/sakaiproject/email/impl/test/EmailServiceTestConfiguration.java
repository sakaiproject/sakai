/*
 * Copyright (c) 2026 The Apereo Foundation
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://opensource.org/licenses/ecl2
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.email.impl.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;

/** Loads the production email bean without starting unrelated digest/persistence services. */
public class EmailServiceTestConfiguration implements AutoCloseable {
    private final DefaultListableBeanFactory beans = new DefaultListableBeanFactory();

    public EmailServiceTestConfiguration(int smtpPort) {
        ServerConfigurationService config = mock(ServerConfigurationService.class);
        when(config.getServerName()).thenReturn("localhost");
        when(config.getServerId()).thenReturn("email-test");
        when(config.getString("mail.sendfromsakai", "true")).thenReturn("false");
        when(config.getInt("mail.sendfromsakai.maxsize", 25000000)).thenReturn(25000000);
        beans.registerSingleton(ServerConfigurationService.class.getName(), config);

        new XmlBeanDefinitionReader(beans).loadBeanDefinitions("classpath:WEB-INF/email-components.xml");
        BeanDefinition email = beans.getBeanDefinition(EmailService.class.getName());
        email.getPropertyValues().add("smtp", "127.0.0.1");
        email.getPropertyValues().add("smtpPort", Integer.toString(smtpPort));
        email.getPropertyValues().add("smtpFrom", "sender@example.com");
        // Force recipient batching so delivery tests also exercise multiple SMTP transactions.
        email.getPropertyValues().add("maxRecipients", "1");
    }

    public EmailService emailService() {
        return beans.getBean(EmailService.class.getName(), EmailService.class);
    }

    @Override
    public void close() {
        beans.destroySingletons();
    }
}
