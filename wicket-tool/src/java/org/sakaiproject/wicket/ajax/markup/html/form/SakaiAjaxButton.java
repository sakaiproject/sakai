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
package org.sakaiproject.wicket.ajax.markup.html.form;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;

import org.sakaiproject.wicket.ajax.markup.html.listener.SakaiSpinnerAjaxCallListener;

/**
 * Disables the button on click, sets the standard Sakai spinner on it, and removes it/re-enables the button after the AJAX call completes.
 *
 * @author plukasew, bjones86
 */
public class SakaiAjaxButton extends AjaxButton
{
	protected boolean willRenderOnClick = false;
	private List<String> elementsToDisable = new ArrayList<>();

	public SakaiAjaxButton(String id)
	{
		super(id);
	}

	public SakaiAjaxButton(String id, Form<?> form)
	{
		super(id, form);
	}

	/**
	 * Whether or not the button itself will be re-rendered as part of the AJAX update
	 * @param value true if button will be re-rendered
	 * @return the button, for method chaining
	 */
	public SakaiAjaxButton setWillRenderOnClick(boolean value)
	{
		willRenderOnClick = value;
		return this;
	}

	/**
	 * Set a list of HTML component IDs that should be disabled when the button is clicked.
	 * @param elementsToDisable list of IDs that will be disabled on click
	 * @return the button, for method chaining
	 */
	public SakaiAjaxButton setElementsToDisableOnClick(List<String> elementsToDisable)
	{
		if (CollectionUtils.isNotEmpty(elementsToDisable))
		{
			this.elementsToDisable = elementsToDisable;
		}

		return this;
	}

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	{
		super.updateAjaxAttributes(attributes);
		attributes.setChannel(new AjaxChannel("blocking", AjaxChannel.Type.ACTIVE));

		AjaxCallListener listener = new SakaiSpinnerAjaxCallListener(getMarkupId(), willRenderOnClick, elementsToDisable);
		attributes.getAjaxCallListeners().add(listener);
	}
}
