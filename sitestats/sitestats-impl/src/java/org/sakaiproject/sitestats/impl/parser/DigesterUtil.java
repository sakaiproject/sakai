/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.impl.parser;

import java.io.InputStream;
import java.util.List;

import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportParams;

/**
 * Compatibility facade for legacy SiteStats XML parser entry points.
 */
public final class DigesterUtil {

	private DigesterUtil() {
	}

	public static List<ToolInfo> parseToolEventsDefinition(InputStream input) throws Exception {
		return ToolEventsXmlParser.parse(input);
	}

	public static PrefsData parsePrefs(InputStream input) throws Exception {
		return PrefsXmlParser.parse(input);
	}

	public static ReportParams convertXmlToReportParams(String inputString) throws Exception {
		return ReportDefinitionXmlCodec.convertXmlToReportParams(inputString);
	}

	public static String convertReportParamsToXml(ReportParams reportParams) throws Exception {
		return ReportDefinitionXmlCodec.convertReportParamsToXml(reportParams);
	}

	public static List<ReportDef> convertXmlToReportDefs(String inputString) throws Exception {
		return ReportDefinitionXmlCodec.convertXmlToReportDefs(inputString);
	}

	public static String convertReportDefsToXml(List<ReportDef> reportDef) throws Exception {
		return ReportDefinitionXmlCodec.convertReportDefsToXml(reportDef);
	}
}
