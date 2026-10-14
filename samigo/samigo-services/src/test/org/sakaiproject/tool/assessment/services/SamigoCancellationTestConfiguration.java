/**
 * Copyright (c) 2026 The Apereo Foundation
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

package org.sakaiproject.tool.assessment.services;

import java.util.Properties;

import org.hibernate.SessionFactory;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.test.SakaiTestConfiguration;
import org.sakaiproject.tool.assessment.facade.AssessmentGradingFacadeQueries;
import org.sakaiproject.tool.assessment.facade.ItemFacadeQueries;
import org.sakaiproject.tool.assessment.facade.ItemHashUtil;
import org.sakaiproject.tool.assessment.facade.TypeFacadeQueries;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.facade.PublishedItemFacadeQueries;
import org.sakaiproject.tool.assessment.facade.authz.AuthzQueriesFacade;
import org.sakaiproject.tool.assessment.integration.helper.impl.GradebookServiceHelperImpl;
import org.sakaiproject.tool.assessment.integration.helper.impl.AgentHelperImpl;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import static org.mockito.Mockito.mock;

/**
 * Uses the existing Samigo Hibernate test mappings and real facade/persistence wiring.
 * The full pack also starts unrelated jobs and integrations, so the cancellation-relevant
 * bean definitions from its components.xml are wired here. Only external boundaries are mocked.
 */
@Configuration
@ImportResource("classpath:/spring-hibernate.xml")
@PropertySource("classpath:/hibernate.properties")
public class SamigoCancellationTestConfiguration extends SakaiTestConfiguration {
    @Autowired private ApplicationContext applicationContext;

    @Override
    @Bean(name = "org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory")
    @Primary
    public SessionFactory sessionFactory() {
        return applicationContext.getBean("sessionFactory", SessionFactory.class);
    }

    @Override
    @Bean
    public Properties hibernateProperties() {
        // The imported XML owns the SessionFactory configuration; expose those actual properties.
        Properties properties = new Properties();
        properties.putAll(sessionFactory().getProperties());
        return properties;
    }

    @Bean(name = "org.sakaiproject.grading.api.GradingService")
    public GradingService gradebookService() {
        return mock(GradingService.class);
    }

    @Bean(name = "org.sakaiproject.content.api.ContentHostingService")
    public ContentHostingService contentHostingService() {
        return mock(ContentHostingService.class);
    }

    @Bean(name = "org.sakaiproject.util.api.LocaleService")
    public org.sakaiproject.util.api.LocaleService localeService() {
        return mock(org.sakaiproject.util.api.LocaleService.class);
    }

    @Bean(name = "agentHelper")
    public AgentHelperImpl agentHelper() {
        AgentHelperImpl helper = new AgentHelperImpl();
        helper.setAuthzGroupService(authzGroupService());
        return helper;
    }

    @Bean(name = "gradebookServiceHelper")
    public GradebookServiceHelperImpl gradebookServiceHelper() {
        GradebookServiceHelperImpl helper = new GradebookServiceHelperImpl();
        helper.setSecurityService(securityService());
        helper.setLocaleService(localeService());
        helper.setAssessmentGradingFacadeQueries(applicationContext.getBean("assessmentGradingFacadeQueries", AssessmentGradingFacadeQueries.class));
        return helper;
    }

    @Bean
    public PersistenceHelper persistenceHelper() {
        return new PersistenceHelper();
    }

    @Bean
    public PublishedAssessmentService publishedAssessmentService() {
        return new PublishedAssessmentService();
    }

    @Bean
    public ItemHashUtil itemHashUtil() {
        ItemHashUtil util = new ItemHashUtil();
        util.setSessionFactory(sessionFactory());
        util.setContentHostingService(contentHostingService());
        util.setGradingService(gradebookService());
        util.setSecurityService(securityService());
        util.setServerConfigurationService(serverConfigurationService());
        PlatformTransactionManager manager = applicationContext.getBean("transactionManager", PlatformTransactionManager.class);
        util.setTransactionTemplate(new TransactionTemplate(manager));
        TransactionTemplate requiresNew = new TransactionTemplate(manager);
        requiresNew.setPropagationBehaviorName("PROPAGATION_REQUIRES_NEW");
        util.setRequiresNewTransactionTemplate(requiresNew);
        return util;
    }

    @Bean(name = "PublishedAssessmentFacadeQueries")
    public PublishedAssessmentFacadeQueries publishedAssessmentFacadeQueries() {
        PublishedAssessmentFacadeQueries queries = new PublishedAssessmentFacadeQueries();
        queries.setSessionFactory(sessionFactory());
        queries.setSiteService(siteService());
        queries.setToolManager(toolManager());
        queries.setUserDirectoryService(userDirectoryService());
        queries.setPersistenceHelper(persistenceHelper());
        queries.setAuthzQueriesFacade(authzQueriesFacade());
        queries.setGbsHelper(gradebookServiceHelper());
        queries.setGradebookService(gradebookService());
        return queries;
    }

    @Bean
    public TypeFacadeQueries typeFacadeQueries() {
        TypeFacadeQueries queries = new TypeFacadeQueries();
        queries.setSessionFactory(sessionFactory());
        PlatformTransactionManager manager = applicationContext.getBean("transactionManager", PlatformTransactionManager.class);
        new TransactionTemplate(manager).executeWithoutResult(status -> queries.setTypeFacadeMap());
        return queries;
    }

    @Bean
    public AuthzQueriesFacade authzQueriesFacade() {
        AuthzQueriesFacade queries = new AuthzQueriesFacade();
        queries.setSessionFactory(sessionFactory());
        queries.setAuthzGroupService(authzGroupService());
        return queries;
    }

    @Bean(name = "PersistenceService")
    public PersistenceService persistenceService() {
        ItemFacadeQueries items = applicationContext.getBean("itemFacadeQueries", ItemFacadeQueries.class);
        items.setItemHashUtil(itemHashUtil());
        PublishedItemFacadeQueries publishedItems = applicationContext.getBean("publishedItemFacadeQueries", PublishedItemFacadeQueries.class);
        publishedItems.setItemHashUtil(itemHashUtil());
        publishedItems.setPersistenceHelper(persistenceHelper());
        AssessmentGradingFacadeQueries grades = applicationContext.getBean("assessmentGradingFacadeQueries", AssessmentGradingFacadeQueries.class);
        grades.setContentHostingService(contentHostingService());
        grades.setSecurityService(securityService());
        grades.setUserDirectoryService(userDirectoryService());
        grades.setPersistenceHelper(persistenceHelper());
        grades.setLocaleService(localeService());
        PersistenceService service = new PersistenceService();
        service.setTypeFacadeQueries(typeFacadeQueries());
        service.setItemFacadeQueries(items);
        service.setPublishedItemFacadeQueries(publishedItems);
        service.setPublishedAssessmentFacadeQueries(publishedAssessmentFacadeQueries());
        service.setAssessmentGradingFacadeQueries(grades);
        service.setAuthzQueriesFacade(authzQueriesFacade());
        return service;
    }
}
