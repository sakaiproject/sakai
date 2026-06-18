/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.test;

import static org.mockito.Mockito.mock;

import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(SiteStatsTestConfiguration.class)
public class SiteStatsViewBehaviorTestConfiguration {

	private static final String STATS_MANAGER = "org.sakaiproject.sitestats.api.StatsManager";
	private static final String EVENT_REGISTRY_SERVICE = "org.sakaiproject.sitestats.api.event.EventRegistryService";
	private static final String REPORT_MANAGER = "org.sakaiproject.sitestats.api.report.ReportManager";

	@Bean
	public static BeanDefinitionRegistryPostProcessor siteStatsViewBehaviorMocks() {
		return new BeanDefinitionRegistryPostProcessor() {

			@Override
			public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
				replaceWithMock(registry, STATS_MANAGER, StatsManager.class);
				replaceWithMock(registry, EVENT_REGISTRY_SERVICE, EventRegistryService.class);
				replaceWithMock(registry, REPORT_MANAGER, ReportManager.class);
			}

			@Override
			public void postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory beanFactory) {
			}
		};
	}

	private static <T> void replaceWithMock(BeanDefinitionRegistry registry, String beanName, Class<T> type) {
		if (registry.containsBeanDefinition(beanName)) {
			registry.removeBeanDefinition(beanName);
		}
		RootBeanDefinition beanDefinition = new RootBeanDefinition(type);
		beanDefinition.setInstanceSupplier(() -> mock(type));
		beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		registry.registerBeanDefinition(beanName, beanDefinition);
	}
}
