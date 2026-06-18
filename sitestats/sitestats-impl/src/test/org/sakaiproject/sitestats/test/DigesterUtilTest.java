/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.impl.parser.DigesterUtil;

public class DigesterUtilTest {

	@Test
	public void parseToolEventsDefinitionReadsAttributeXml() throws Exception {
		String xml = "<toolEventsDef><tool toolId=\"sakai.assignment\" selected=\"true\" additionalToolIds=\"sakai.assignment.grades,sakai.assignment2\">"
				+ "<event eventId=\"asn.new.assignment\" selected=\"true\" anonymous=\"true\" resolvable=\"true\"/>"
				+ "<eventParserTip for=\"contextId\" separator=\"/\" index=\"4\"/>"
				+ "</tool></toolEventsDef>";

		List<ToolInfo> tools = DigesterUtil.parseToolEventsDefinition(input(xml));

		assertEquals(1, tools.size());
		ToolInfo tool = tools.get(0);
		assertEquals("sakai.assignment", tool.getToolId());
		assertTrue(tool.isSelected());
		assertEquals(Arrays.asList("sakai.assignment.grades", "sakai.assignment2"), tool.getAdditionalToolIds());
		assertEquals(1, tool.getEvents().size());
		EventInfo event = tool.getEvents().get(0);
		assertEquals("asn.new.assignment", event.getEventId());
		assertTrue(event.isSelected());
		assertTrue(event.isAnonymous());
		assertTrue(event.isResolvable());
		assertEquals(1, tool.getEventParserTips().size());
		assertEquals("contextId", tool.getEventParserTips().get(0).getFor());
		assertEquals("/", tool.getEventParserTips().get(0).getSeparator());
		assertEquals("4", tool.getEventParserTips().get(0).getIndex());
	}

	@Test
	public void parsePrefsReadsAttributesAndToolEvents() throws Exception {
		String xml = "<prefs listToolEventsOnlyAvailableInSite=\"false\" showOwnStatisticsToStudents=\"false\""
				+ " chartIn3D=\"true\" chartTransparency=\"0.75\" itemLabelsVisible=\"false\" useAllTools=\"true\">"
				+ "<toolEventsDef><tool toolId=\"sakai.resources\" selected=\"true\">"
				+ "<event eventId=\"content.read\" selected=\"true\"/>"
				+ "</tool></toolEventsDef>"
				+ "</prefs>";

		PrefsData prefsData = DigesterUtil.parsePrefs(input(xml));

		assertFalse(prefsData.isListToolEventsOnlyAvailableInSite());
		assertFalse(prefsData.isShowOwnStatisticsToStudents());
		assertTrue(prefsData.isChartIn3D());
		assertEquals(0.75f, prefsData.getChartTransparency(), 0.001f);
		assertFalse(prefsData.isItemLabelsVisible());
		assertTrue(prefsData.isUseAllTools());
		assertEquals(1, prefsData.getToolEventsDef().size());
		assertEquals("sakai.resources", prefsData.getToolEventsDef().get(0).getToolId());
		assertEquals("content.read", prefsData.getToolEventsDef().get(0).getEvents().get(0).getEventId());
	}

	@Test
	public void parsePrefsRejectsInvalidBooleanValues() throws Exception {
		String xml = "<prefs chartIn3D=\"definitely\"/>";

		try {
			DigesterUtil.parsePrefs(input(xml));
			fail("Expected invalid boolean value to fail parsing.");
		} catch(Exception e) {
			assertTrue(e.getMessage().contains("Invalid boolean value 'definitely'"));
		}
	}

	@Test
	public void convertXmlToReportParamsReadsLegacyBetwixtXml() throws Exception {
		String xml = "<?xml version='1.0' ?><ReportParams>"
				+ "<siteId>site-a</siteId>"
				+ "<what>what-events</what>"
				+ "<whatEventSelType>what-events-bytool</whatEventSelType>"
				+ "<whatEventIds/>"
				+ "<whatToolIds><whatToolIds>sakai.assignment</whatToolIds><whatToolIds>sakai.resources</whatToolIds></whatToolIds>"
				+ "<whatResourceIds/>"
				+ "<when>when-custom</when>"
				+ "<whenFrom>Wed Jun 17 00:00:00 GMT-04:00 2026</whenFrom>"
				+ "<whenTo>Thu Jun 18 00:00:00 GMT-04:00 2026</whenTo>"
				+ "<who>who-custom</who>"
				+ "<whoRoleId/>"
				+ "<whoUserIds><whoUserIds>user-a</whoUserIds><whoUserIds>user-b</whoUserIds></whoUserIds>"
				+ "<howTotalsBy><howTotalsBy>event</howTotalsBy><howTotalsBy>user</howTotalsBy></howTotalsBy>"
				+ "</ReportParams>";

		ReportParams params = DigesterUtil.convertXmlToReportParams(xml);

		assertEquals("site-a", params.getSiteId());
		assertEquals(ReportManager.WHAT_EVENTS, params.getWhat());
		assertEquals(Arrays.asList("sakai.assignment", "sakai.resources"), params.getWhatToolIds());
		assertEquals(Arrays.asList("user-a", "user-b"), params.getWhoUserIds());
		assertEquals(Arrays.asList(StatsManager.T_EVENT, StatsManager.T_USER), params.getHowTotalsBy());
		assertEquals(0, params.getWhatEventIds().size());
		assertEquals(0, params.getWhatResourceIds().size());
		assertNull(params.getWhoRoleId());
		assertEquals(legacyDate("Wed Jun 17 00:00:00 GMT-04:00 2026"), params.getWhenFrom());
		assertEquals(legacyDate("Thu Jun 18 00:00:00 GMT-04:00 2026"), params.getWhenTo());
	}

	@Test
	public void convertReportParamsToXmlRoundTripsPopulatedParams() throws Exception {
		ReportParams params = new ReportParams("site-a");
		params.setWhat(ReportManager.WHAT_EVENTS);
		params.setWhatToolIds(Arrays.asList("sakai.assignment", "sakai.resources"));
		params.setWhatEventIds(Arrays.asList("assignment.new", "content.read"));
		params.setWho(ReportManager.WHO_CUSTOM);
		params.setWhoUserIds(Arrays.asList("user-a", "user-b"));
		params.setHowTotalsBy(Arrays.asList(StatsManager.T_EVENT, StatsManager.T_USER));
		params.setWhen(ReportManager.WHEN_CUSTOM);
		params.setWhenFrom(legacyDate("Wed Jun 17 00:00:00 GMT-04:00 2026"));
		params.setWhenTo(legacyDate("Thu Jun 18 00:00:00 GMT-04:00 2026"));

		String xml = DigesterUtil.convertReportParamsToXml(params);
		ReportParams roundTrip = DigesterUtil.convertXmlToReportParams(xml);

		assertTrue(xml.startsWith("<ReportParams>"));
		assertTrue(xml.contains("<whatToolIds><whatToolIds>sakai.assignment</whatToolIds><whatToolIds>sakai.resources</whatToolIds></whatToolIds>"));
		assertEquals(params.getSiteId(), roundTrip.getSiteId());
		assertEquals(params.getWhat(), roundTrip.getWhat());
		assertEquals(params.getWhatToolIds(), roundTrip.getWhatToolIds());
		assertEquals(params.getWhatEventIds(), roundTrip.getWhatEventIds());
		assertEquals(params.getWho(), roundTrip.getWho());
		assertEquals(params.getWhoUserIds(), roundTrip.getWhoUserIds());
		assertEquals(params.getHowTotalsBy(), roundTrip.getHowTotalsBy());
		assertEquals(params.getWhenFrom(), roundTrip.getWhenFrom());
		assertEquals(params.getWhenTo(), roundTrip.getWhenTo());
	}

	@Test
	public void convertXmlToReportDefsReadsLegacyBetwixtExportXml() throws Exception {
		String xml = "<List><ReportDef>"
				+ "<createdBy>admin</createdBy>"
				+ "<createdOn>Wed Jun 17 00:00:00 GMT-04:00 2026</createdOn>"
				+ "<description>Description</description>"
				+ "<descriptionBundleKey>Description</descriptionBundleKey>"
				+ "<descriptionLocalized>false</descriptionLocalized>"
				+ "<hidden>false</hidden>"
				+ "<id>7</id>"
				+ "<modifiedBy>admin</modifiedBy>"
				+ "<modifiedOn>Thu Jun 18 00:00:00 GMT-04:00 2026</modifiedOn>"
				+ "<reportDefinitionXml>&lt;?xml version='1.0' ?&gt;&lt;ReportParams&gt;"
				+ "&lt;siteId&gt;site-a&lt;/siteId&gt;"
				+ "&lt;what&gt;what-events&lt;/what&gt;"
				+ "&lt;whatEventIds/&gt;"
				+ "&lt;whatToolIds&gt;&lt;whatToolIds&gt;sakai.assignment&lt;/whatToolIds&gt;&lt;/whatToolIds&gt;"
				+ "&lt;whatResourceIds/&gt;"
				+ "&lt;whoUserIds/&gt;"
				+ "&lt;howTotalsBy&gt;&lt;howTotalsBy&gt;event&lt;/howTotalsBy&gt;&lt;/howTotalsBy&gt;"
				+ "&lt;/ReportParams&gt;</reportDefinitionXml>"
				+ "<reportParams><siteId>site-a</siteId><what>what-events</what>"
				+ "<whatToolIds><whatToolIds>sakai.assignment</whatToolIds></whatToolIds>"
				+ "<howTotalsBy><howTotalsBy>event</howTotalsBy></howTotalsBy>"
				+ "</reportParams>"
				+ "<siteId>site-a</siteId>"
				+ "<title>Title</title>"
				+ "<titleBundleKey>Title</titleBundleKey>"
				+ "<titleLocalized>false</titleLocalized>"
				+ "</ReportDef></List>";

		List<ReportDef> reportDefs = DigesterUtil.convertXmlToReportDefs(xml);

		assertEquals(1, reportDefs.size());
		ReportDef reportDef = reportDefs.get(0);
		assertEquals(7L, reportDef.getId());
		assertEquals("site-a", reportDef.getSiteId());
		assertEquals("Title", reportDef.getTitle());
		assertEquals("Description", reportDef.getDescription());
		assertFalse(reportDef.isHidden());
		assertEquals("admin", reportDef.getCreatedBy());
		assertEquals(legacyDate("Wed Jun 17 00:00:00 GMT-04:00 2026"), reportDef.getCreatedOn());
		assertEquals(Arrays.asList("sakai.assignment"), reportDef.getReportParams().getWhatToolIds());
		assertEquals(Arrays.asList(StatsManager.T_EVENT), reportDef.getReportParams().getHowTotalsBy());
	}

	@Test
	public void convertReportDefsToXmlRoundTripsMultipleReports() throws Exception {
		ReportDef first = reportDef(1L, "site-a", "First", "sakai.assignment");
		ReportDef second = reportDef(2L, "site-a", "Second", "sakai.resources");

		String xml = DigesterUtil.convertReportDefsToXml(Arrays.asList(first, second));
		List<ReportDef> roundTrip = DigesterUtil.convertXmlToReportDefs(xml);

		assertTrue(xml.startsWith("<List>"));
		assertTrue(xml.contains("<ReportDef>"));
		assertEquals(2, roundTrip.size());
		assertEquals("First", roundTrip.get(0).getTitle());
		assertEquals(Arrays.asList("sakai.assignment"), roundTrip.get(0).getReportParams().getWhatToolIds());
		assertEquals("Second", roundTrip.get(1).getTitle());
		assertEquals(Arrays.asList("sakai.resources"), roundTrip.get(1).getReportParams().getWhatToolIds());
	}

	private ReportDef reportDef(long id, String siteId, String title, String toolId) throws Exception {
		ReportDef reportDef = new ReportDef();
		reportDef.setId(id);
		reportDef.setSiteId(siteId);
		reportDef.setTitle(title);
		reportDef.setDescription(title + " description");
		reportDef.setCreatedBy("admin");
		reportDef.setCreatedOn(legacyDate("Wed Jun 17 00:00:00 GMT-04:00 2026"));
		reportDef.setModifiedBy("admin");
		reportDef.setModifiedOn(legacyDate("Thu Jun 18 00:00:00 GMT-04:00 2026"));
		reportDef.setReportParams(reportParams(siteId, toolId));
		reportDef.setReportDefinitionXml(DigesterUtil.convertReportParamsToXml(reportDef.getReportParams()));
		return reportDef;
	}

	private ReportParams reportParams(String siteId, String toolId) {
		ReportParams reportParams = new ReportParams(siteId);
		reportParams.setWhat(ReportManager.WHAT_EVENTS);
		reportParams.setWhatToolIds(Arrays.asList(toolId));
		reportParams.setHowTotalsBy(Arrays.asList(StatsManager.T_EVENT));
		return reportParams;
	}

	private Date legacyDate(String value) throws Exception {
		return new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US).parse(value);
	}

	private ByteArrayInputStream input(String value) {
		return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
	}
}
