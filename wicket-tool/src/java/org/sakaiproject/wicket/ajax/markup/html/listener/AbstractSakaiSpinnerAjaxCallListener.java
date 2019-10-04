/**
 * Copyright (c) 2006-2018 The Apereo Foundation
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
package org.sakaiproject.wicket.ajax.markup.html.listener;

import org.apache.wicket.ajax.attributes.AjaxCallListener;

/**
 * Abstract base class for AjaxCallListeners used to apply a spinner overlay to the component while Ajax call in progress
 * @author plukasew
 */
public abstract class AbstractSakaiSpinnerAjaxCallListener extends AjaxCallListener
{
	protected static final String SPINNER_CLASS = "spinButton";
	protected static final String DISABLED = "$('#%s').prop('disabled', true);";
	protected static final String ENABLED = "$('#%s').prop('disabled', false);";

	protected boolean willRender = false;
	protected String id = "";

	/**
	 * Call listener to overlay a spinner and disable the a clicked component
	 * @param componentMarkupId the markup id for the component
	 * @param componentWillRender whether or not the component will be re-rendered as a result of the ajax update
	 */
	public AbstractSakaiSpinnerAjaxCallListener(String componentMarkupId, boolean componentWillRender)
	{
		id = componentMarkupId;
		willRender = componentWillRender;
	}
}
