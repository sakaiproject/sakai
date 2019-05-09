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
package org.sakaiproject.sitestats.tool.wicket.components.dropdown;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.StringResourceModel;

/**
 * A ChoiceRenderer using a StringResourceModel for i18n
 * @author plukasew
 */
public class SakaiStringResourceChoiceRenderer implements IChoiceRenderer<String>
{
	private final String msgKey;
	private final Component component;

	/**
	 * Constructor
	 * @param msgKey the message bundle key
	 * @param component the component
	 */
	public SakaiStringResourceChoiceRenderer(String msgKey, Component component)
	{
		this.msgKey = msgKey;
		this.component = component;
	}

	@Override
	public Object getDisplayValue(String object)
	{
		return new StringResourceModel(msgKey, component, null, new Object[] { object }).getString();
	}

	@Override
	public String getIdValue(String object, int index)
	{
		return object;
	}
}
