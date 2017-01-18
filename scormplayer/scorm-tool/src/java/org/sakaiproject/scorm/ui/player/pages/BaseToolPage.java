/**
 * Copyright (c) 2007 The Apereo Foundation
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
package org.sakaiproject.scorm.ui.player.pages;

import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;

public class BaseToolPage extends WebPage implements IHeaderContributor {
	private static final long serialVersionUID = 2L;

	protected static final String TOOLBASE_CSS = "/library/skin/tool_base.css";
	protected static final String TOOL_CSS = "/library/skin/default/tool.css";
	protected static final String SCORM_CSS = "styles/scorm.css";
	
	public void renderHead(IHeaderResponse response) {
		response.renderCSSReference(TOOLBASE_CSS);
		response.renderCSSReference(TOOL_CSS);
		response.renderCSSReference(SCORM_CSS);
	}

}
