/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.parser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.sakaiproject.serialization.MapperFactory;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportParams;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

final class ReportDefinitionXmlCodec {

	private static final XmlMapper XML_MAPPER = createXmlMapper();

	private ReportDefinitionXmlCodec() {
	}

	static ReportParams convertXmlToReportParams(String inputString) throws Exception {
		ReportParams reportParams = XML_MAPPER.readValue(inputString, ReportParams.class);
		normalizeReportParams(reportParams);
		return reportParams;
	}

	static String convertReportParamsToXml(ReportParams reportParams) throws Exception {
		return XML_MAPPER.writeValueAsString(reportParams);
	}

	static List<ReportDef> convertXmlToReportDefs(String inputString) throws Exception {
		ReportDefListXml reportDefListXml = XML_MAPPER.readValue(inputString, ReportDefListXml.class);
		List<ReportDef> reportDefs = reportDefListXml.getReportDefs();
		for(ReportDef reportDef : reportDefs) {
			if(reportDef.getReportParams() != null) {
				normalizeReportParams(reportDef.getReportParams());
			}
		}
		return reportDefs;
	}

	static String convertReportDefsToXml(List<ReportDef> reportDef) throws Exception {
		return XML_MAPPER.writeValueAsString(new ReportDefListXml(reportDef));
	}

	private static XmlMapper createXmlMapper() {
		XmlMapper mapper = MapperFactory.createDefaultXmlMapper();
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		mapper.setDateFormat(new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US));
		mapper.addMixIn(ReportParams.class, ReportParamsXmlMixin.class);
		mapper.addMixIn(ReportDef.class, ReportDefXmlMixin.class);
		return mapper;
	}

	private static void normalizeReportParams(ReportParams reportParams) {
		if(reportParams == null) {
			return;
		}
		reportParams.setSiteId(emptyToNull(reportParams.getSiteId()));
		reportParams.setWhat(emptyToNull(reportParams.getWhat()));
		reportParams.setWhatEventSelType(emptyToNull(reportParams.getWhatEventSelType()));
		reportParams.setWhatResourceAction(emptyToNull(reportParams.getWhatResourceAction()));
		reportParams.setWhen(emptyToNull(reportParams.getWhen()));
		reportParams.setWho(emptyToNull(reportParams.getWho()));
		reportParams.setWhoGroupId(emptyToNull(reportParams.getWhoGroupId()));
		reportParams.setWhoRoleId(emptyToNull(reportParams.getWhoRoleId()));
		reportParams.setHowSortBy(emptyToNull(reportParams.getHowSortBy()));
		reportParams.setHowPresentationMode(emptyToNull(reportParams.getHowPresentationMode()));
		reportParams.setHowChartType(emptyToNull(reportParams.getHowChartType()));
		reportParams.setHowChartSource(emptyToNull(reportParams.getHowChartSource()));
		reportParams.setHowChartCategorySource(emptyToNull(reportParams.getHowChartCategorySource()));
		reportParams.setHowChartSeriesSource(emptyToNull(reportParams.getHowChartSeriesSource()));
		reportParams.setHowChartSeriesPeriod(emptyToNull(reportParams.getHowChartSeriesPeriod()));
		reportParams.setWhatToolIds(normalizeList(reportParams.getWhatToolIds()));
		reportParams.setWhatEventIds(normalizeList(reportParams.getWhatEventIds()));
		reportParams.setWhatResourceIds(normalizeList(reportParams.getWhatResourceIds()));
		reportParams.setWhoUserIds(normalizeList(reportParams.getWhoUserIds()));
		reportParams.setHowTotalsBy(normalizeList(reportParams.getHowTotalsBy()));
	}

	private static String emptyToNull(String value) {
		if(value == null || value.trim().isEmpty()) {
			return null;
		}
		return value;
	}

	private static List<String> normalizeList(List<String> values) {
		List<String> normalized = new ArrayList<String>();
		if(values != null) {
			for(String value : values) {
				String normalizedValue = emptyToNull(value);
				if(normalizedValue != null) {
					normalized.add(normalizedValue);
				}
			}
		}
		return normalized;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@JacksonXmlRootElement(localName = "ReportParams")
	private static abstract class ReportParamsXmlMixin {

		@JacksonXmlElementWrapper(localName = "whatToolIds")
		@JacksonXmlProperty(localName = "whatToolIds")
		abstract List<String> getWhatToolIds();

		@JacksonXmlElementWrapper(localName = "whatEventIds")
		@JacksonXmlProperty(localName = "whatEventIds")
		abstract List<String> getWhatEventIds();

		@JacksonXmlElementWrapper(localName = "whatResourceIds")
		@JacksonXmlProperty(localName = "whatResourceIds")
		abstract List<String> getWhatResourceIds();

		@JacksonXmlElementWrapper(localName = "whoUserIds")
		@JacksonXmlProperty(localName = "whoUserIds")
		abstract List<String> getWhoUserIds();

		@JacksonXmlElementWrapper(localName = "howTotalsBy")
		@JacksonXmlProperty(localName = "howTotalsBy")
		abstract List<String> getHowTotalsBy();
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private static abstract class ReportDefXmlMixin {

		@JsonIgnore
		abstract boolean isTitleLocalized();

		@JsonIgnore
		abstract String getTitleBundleKey();

		@JsonIgnore
		abstract boolean isDescriptionLocalized();

		@JsonIgnore
		abstract String getDescriptionBundleKey();
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@JacksonXmlRootElement(localName = "List")
	private static class ReportDefListXml {

		private List<ReportDef> reportDefs = new ArrayList<ReportDef>();

		public ReportDefListXml() {
		}

		public ReportDefListXml(List<ReportDef> reportDefs) {
			this.reportDefs = reportDefs == null ? new ArrayList<ReportDef>() : reportDefs;
		}

		@JacksonXmlElementWrapper(useWrapping = false)
		@JacksonXmlProperty(localName = "ReportDef")
		public List<ReportDef> getReportDefs() {
			return reportDefs;
		}

		public void setReportDefs(List<ReportDef> reportDefs) {
			this.reportDefs = reportDefs == null ? new ArrayList<ReportDef>() : reportDefs;
		}
	}
}
