/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_VISITS;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.StatsAuthz;
import org.sakaiproject.sitestats.api.view.SiteStatsReportExportService;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsViewService;
import org.sakaiproject.sitestats.impl.view.SiteStatsReportAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SiteStatsTestConfiguration.class})
public class SiteStatsSpringWiringTest {

	private static final String SITE_ID = "site-a";
	private static final String SITE_REF = "/site/" + SITE_ID;

	@Autowired private SecurityService securityService;
	@Autowired private SiteService siteService;
	@Autowired private SiteStatsReportAccess siteStatsReportAccess;
	@Autowired private SiteStatsReportExportService siteStatsReportExportService;
	@Autowired private SiteStatsViewService siteStatsViewService;

	@Before
	public void setUp() throws Exception {
		when(siteService.siteReference(SITE_ID)).thenReturn(SITE_REF);
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_VIEW, SITE_REF)).thenReturn(true);
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_ALL, SITE_REF)).thenReturn(true);
	}

	@Test
	public void componentsXmlWiresSharedReportAccessIntoViewAndExportServices() {
		assertNotNull(siteStatsReportAccess);
		assertNotNull(siteStatsViewService);
		assertNotNull(siteStatsReportExportService);

		assertThrows(IllegalArgumentException.class, () ->
				siteStatsViewService.getWidgetMetricReport(SITE_ID, WIDGET_VISITS, "missing-metric", new SiteStatsReportRequest()));
		assertFalse(siteStatsReportExportService.canExportPersistedReport(SITE_ID, 0L));
		assertFalse(siteStatsReportExportService.canExportWidgetMetricReport(SITE_ID, WIDGET_VISITS, "missing-metric"));
	}
}
