/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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

package org.sakaiproject.portal.charon;

import org.apache.commons.lang3.StringEscapeUtils;
import org.sakaiproject.portal.render.api.RenderResult;
import org.sakaiproject.portal.render.api.ToolRenderException;
import org.sakaiproject.site.api.ToolConfiguration;

/**
 * Impl of {@link RenderResult} for a buffered content response
 */
public class BufferedContentRenderResult implements RenderResult {

	ToolConfiguration config;
	String head;
	String body;
	
	public BufferedContentRenderResult(final ToolConfiguration config, final String head, final String body) {
		this.config = config;
		this.head = head;
		this.body = body;
	}
	
	@Override
	public String getTitle() throws ToolRenderException {
		return StringEscapeUtils.escapeHtml4(this.config.getTitle());
	}

	@Override
	public String getContent() throws ToolRenderException {
		return this.body;
	}

	@Override
	public void setContent(String content) {
		return; // N/A
	}

	@Override
	public String getJSR168HelpUrl() throws ToolRenderException {
		return null;
	}

	@Override
	public String getJSR168EditUrl() throws ToolRenderException {
		return null;
	}

	@Override
	public String getHead() {
		return this.head;
	}

}

