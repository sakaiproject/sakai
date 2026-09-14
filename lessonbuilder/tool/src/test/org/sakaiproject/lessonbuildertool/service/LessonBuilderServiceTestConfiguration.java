/*
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
package org.sakaiproject.lessonbuildertool.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.messaging.api.UserMessagingService;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.test.SakaiTestConfiguration;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.ActiveToolManager;
import org.sakaiproject.util.api.LocaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@ImportResource("classpath:/WEB-INF/components.xml")
@PropertySource("classpath:/hibernate.properties")
public class LessonBuilderServiceTestConfiguration extends SakaiTestConfiguration {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Autowired
    @Qualifier("org.sakaiproject.springframework.orm.hibernate.impl.AdditionalHibernateMappingsImpl.simplepage")
    private AdditionalHibernateMappings additionalHibernateMappings;

    @Override
    protected AdditionalHibernateMappings getAdditionalHibernateMappings() {
        return additionalHibernateMappings;
    }

    @Override
    @Bean(name = "org.sakaiproject.authz.api.FunctionManager")
    public FunctionManager functionManager() {
        FunctionManager functionManager = mock(FunctionManager.class);
        when(functionManager.getRegisteredFunctions("lessonbuilder."))
                .thenReturn(Collections.emptyList());
        return functionManager;
    }

    @Bean(name = "org.sakaiproject.portal.api.PortalService")
    public PortalService portalService() {
        return mock(PortalService.class);
    }

    @Bean(name = "org.sakaiproject.db.api.SqlService")
    public SqlService sqlService() {
        return mock(SqlService.class);
    }

    @Bean(name = "org.sakaiproject.tool.api.ActiveToolManager")
    public ActiveToolManager activeToolManager() {
        return mock(ActiveToolManager.class);
    }

    @Bean(name = "org.sakaiproject.time.api.UserTimeService")
    public UserTimeService userTimeService() {
        return mock(UserTimeService.class);
    }

    @Bean(name = "org.sakaiproject.content.api.ContentHostingService")
    public ContentHostingService contentHostingService() {
        return mock(ContentHostingService.class);
    }

    @Bean(name = "org.sakaiproject.messaging.api.UserMessagingService")
    public UserMessagingService userMessagingService() {
        return mock(UserMessagingService.class);
    }

    @Bean(name = "org.sakaiproject.grading.api.GradingService")
    public GradingService gradingService() {
        return mock(GradingService.class);
    }

    @Bean(name = "org.sakaiproject.util.api.LocaleService")
    public LocaleService localeService() {
        return mock(LocaleService.class);
    }

    @Bean(name = "org.sakaiproject.lessonbuildertool.service.GradebookIfc")
    public GradebookIfc gradebookIfc(GradingService gradingService, LocaleService localeService) {
        GradebookIfc gradebookIfc = new GradebookIfc();
        gradebookIfc.setGradingService(gradingService);
        gradebookIfc.setLocaleService(localeService);
        return gradebookIfc;
    }

    @Bean(name = "org.sakaiproject.lessonbuildertool.service.AssignmentEntity")
    public LessonEntity assignmentEntity() {
        return mock(LessonEntity.class);
    }

    @Bean(name = "org.sakaiproject.lessonbuildertool.service.SamigoEntity")
    public LessonEntity quizEntity() {
        return mock(LessonEntity.class);
    }

    @Bean(name = "org.sakaiproject.lessonbuildertool.service.ForumEntity")
    public LessonEntity forumEntity() {
        return mock(LessonEntity.class);
    }

    @Bean(name = "org.sakaiproject.lessonbuildertool.service.PageIndexService")
    public PageIndexService pageIndexService(SimplePageToolDao simplePageToolDao) {
        PageIndexService pageIndexService = new PageIndexService();
        pageIndexService.setSimplePageToolDao(simplePageToolDao);
        return pageIndexService;
    }

    @Bean(name = "org.sakaiproject.lessonbuildertool.service.PlacementPageService")
    public PlacementPageService placementPageService(
            SimplePageToolDao simplePageToolDao,
            org.sakaiproject.site.api.SiteService siteService,
            org.sakaiproject.authz.api.SecurityService securityService) {
        PlacementPageService placementPageService = new PlacementPageService();
        placementPageService.setSimplePageToolDao(simplePageToolDao);
        placementPageService.setSiteService(siteService);
        placementPageService.setSecurityService(securityService);
        return placementPageService;
    }

    @Bean(name = "org.sakaiproject.lessonbuildertool.service.RemovedPageService")
    public RemovedPageService removedPageService(
            SimplePageToolDao simplePageToolDao,
            PageIndexService pageIndexService,
            org.sakaiproject.site.api.SiteService siteService,
            GradebookIfc gradebookIfc,
            ContentHostingService contentHostingService,
            @Qualifier("org.sakaiproject.lessonbuildertool.service.AssignmentEntity") LessonEntity assignmentEntity,
            @Qualifier("org.sakaiproject.lessonbuildertool.service.SamigoEntity") LessonEntity quizEntity,
            @Qualifier("org.sakaiproject.lessonbuildertool.service.ForumEntity") LessonEntity forumEntity) {
        RemovedPageService removedPageService = new RemovedPageService();
        removedPageService.setSimplePageToolDao(simplePageToolDao);
        removedPageService.setPageIndexService(pageIndexService);
        removedPageService.setSiteService(siteService);
        removedPageService.setGradebookIfc(gradebookIfc);
        removedPageService.setContentHostingService(contentHostingService);
        removedPageService.setAssignmentEntity(assignmentEntity);
        removedPageService.setQuizEntity(quizEntity);
        removedPageService.setForumEntity(forumEntity);
        return removedPageService;
    }
}
