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
package org.sakaiproject.sitestats.api.report;

public interface ReportFormattedParams {

	public abstract String getReportSite(Report report);
	
	public abstract String getReportTitle(Report report);
	
	public abstract String getReportDescription(Report report);

	public abstract String getReportGenerationDate(Report report);

	public abstract String getReportActivityBasedOn(Report report);

	public abstract String getReportActivitySelectionTitle(Report report);

	public abstract String getReportActivitySelection(Report report);

	public abstract String getReportResourceActionTitle(Report report);

	public abstract String getReportResourceAction(Report report);

	public abstract String getReportTimePeriod(Report report);

	public abstract String getReportUserSelectionType(Report report);

	public abstract String getReportUserSelectionTitle(Report report);

	public abstract String getReportUserSelection(Report report);

	public abstract boolean isStringLocalized(String string);
}