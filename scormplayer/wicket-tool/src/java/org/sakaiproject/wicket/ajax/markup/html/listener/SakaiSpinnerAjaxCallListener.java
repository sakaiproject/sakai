/**
 * Copyright (c) 2006-2019 The Apereo Foundation
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

import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

/**
 * Adds the Sakai overlay spinner to the component making the AJAX call
 * @author plukasew, bjones86
 */
public class SakaiSpinnerAjaxCallListener extends AbstractSakaiSpinnerAjaxCallListener
{
	private static final String SPIN = "$('#%s').addClass('" + SPINNER_CLASS + "');";
	private static final String STOP = "$('#%s').removeClass('" + SPINNER_CLASS + "');";
	private static final String DISABLE_AND_SPIN = DISABLED + SPIN;
	private static final String ENABLE_AND_STOP = ENABLED + STOP;

	/**
	 * Constructor
	 * @param componentMarkupId the markup id of the component making the AJAX call
	 */
	public SakaiSpinnerAjaxCallListener(String componentMarkupId)
	{
		this(componentMarkupId, false);
	}

	/**
	 * Constructor
	 * @param componentMarkupId the markup id of the component making the AJAX call
	 * @param elementsToDisableOnClick the markup IDs of components that should be disabled when the component making the AJAX call is clicked
	 */
	public SakaiSpinnerAjaxCallListener(String componentMarkupId, List<String> elementsToDisableOnClick)
	{
		this(componentMarkupId, false, elementsToDisableOnClick);
	}

	/**
	 * Constructor
	 * @param componentMarkupId the markup id of the component making the AJAX call
	 * @param componentWillRender whether the AJAX call will result in the component being re-rendered
	 */
	public SakaiSpinnerAjaxCallListener(String componentMarkupId, boolean componentWillRender)
	{
		this(componentMarkupId, componentWillRender, Collections.emptyList());
	}

	/**
	 * Constructor
	 * @param componentMarkupId the markup id of the component making the AJAX call
	 * @param componentWillRender whether the AJAX call will result in the component being re-rendered
	 * @param elementsToDisableOnClick the markup IDs of components that should be disabled when the component making the AJAX call is clicked
	 */
	public SakaiSpinnerAjaxCallListener(String componentMarkupId, boolean componentWillRender, List<String> elementsToDisableOnClick)
	{
		super(componentMarkupId, componentWillRender);

		// on the client side, disable the control and show the spinner after click
		onBefore(String.format(DISABLE_AND_SPIN, id, id));

		// if we have supplemental HTML components to disable on click, set up the necessary JavaScript
		appendJavaScriptForElements(elementsToDisableOnClick, true);

		// if the control is re-rendered the disabled property will be set by wicket and the spinner
		// class will not be on the component as wicket doesn't know about it
		if (!willRender)
		{
			onComplete(String.format(ENABLE_AND_STOP, id, id));

			// if we have supplemental HTML components to enable, set up the necessary JavaScript
			appendJavaScriptForElements(elementsToDisableOnClick, false);
		}
	}

	/**
	 * Utility method to build JavaScript enable/disable commands for a list of HTML components
	 * @param elementsToDisableOnClick list of HTML component IDs to enable or disable
	 * @param disable true if elements should be disabled, false otherwise
	 * @return the JavaScript required to disable or enable the given HTML element IDs, or empty string if no IDs are supplied
	 */
	private void appendJavaScriptForElements(List<String> elementsToDisableOnClick, boolean disable)
	{
		if (CollectionUtils.isNotEmpty(elementsToDisableOnClick))
		{
			StringBuilder javaScript = new StringBuilder();
			for (String elementID : elementsToDisableOnClick)
			{
				javaScript.append(String.format((disable ? DISABLED : ENABLED), elementID));
			}

			if (disable)
			{
				onBefore(javaScript.toString());
			}
			else
			{
				onComplete(javaScript.toString());
			}
		}
	}
}
