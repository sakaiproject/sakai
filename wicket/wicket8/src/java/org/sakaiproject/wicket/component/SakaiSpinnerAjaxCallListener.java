/**
 * Copyright (c) 2006-2021 The Apereo Foundation
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
package org.sakaiproject.wicket.component;

/**
 * Adds the Sakai overlay spinner to the component making the Ajax call
 * @author plukasew
 */
public class SakaiSpinnerAjaxCallListener extends AbstractSakaiSpinnerAjaxCallListener
{
	private static final long serialVersionUID = 1L;
	private static final String SPIN = "$('#%s').addClass('" + SPINNER_CLASS + "');";
	private static final String STOP = "$('#%s').removeClass('" + SPINNER_CLASS + "');";
	private static final String DISABLE_AND_SPIN = DISABLED + SPIN;
	private static final String ENABLE_AND_STOP = ENABLED + STOP;

	/**
	 * Constructor
	 * @param componentMarkupId the markup id of the component making the ajax call
	 */
	public SakaiSpinnerAjaxCallListener(String componentMarkupId)
	{
		this(componentMarkupId, false);
	}

	/**
	 * Constructor
	 * @param componentMarkupId the markup id of the component making the ajax call
	 * @param componentWillRender whether the ajax call will result in the component being re-rendered
	 */
	public SakaiSpinnerAjaxCallListener(String componentMarkupId, boolean componentWillRender)
	{
		super(componentMarkupId, componentWillRender);

		// on the client side, disable the control and show the spinner after click
		onBefore(String.format(DISABLE_AND_SPIN, id, id));

		// if the control is re-rendered the disabled property will be set by wicket and the spinner
		// class will not be on the component as wicket doesn't know about it
		if (!willRender)
		{
			onComplete(String.format(ENABLE_AND_STOP, id, id));
		}
	}
}
