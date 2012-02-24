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
package org.sakaiproject.sitestats.tool.wicket.widget;

import org.apache.wicket.Page;
import org.sakaiproject.sitestats.api.report.ReportDef;

/** Mini stat class for displaying a single link in the widget top bar. */
public abstract class WidgetMiniStatLink extends WidgetMiniStat {
	private static final long	serialVersionUID	= 1L;

	public abstract Page getPageLink();
	
	public abstract String getPageLinkTooltip();

	@Override
	public String getValue() {
		return " ";
	}

	@Override
	public String getSecondValue() {
		return null;
	}

	@Override
	public boolean isWiderText() {
		return false;
	}
	
	@Override
	public String getTooltip() {
		return null;
	}
	
	@Override
	public ReportDef getReportDefinition() {
		return null;
	}

}
