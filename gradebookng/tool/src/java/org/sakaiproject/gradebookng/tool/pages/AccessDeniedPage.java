/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.gradebookng.tool.pages;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Access denied page.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class AccessDeniedPage extends BasePage {

	private static final long serialVersionUID = 1L;

	private String message;

	public AccessDeniedPage(PageParameters params) {
		this.message = params.get("message").toString();
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		add(new Label("message", this.message));
	}

}
