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

import java.util.List;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

/**
 * Wraps a DropDownChoice component in a panel that allows the overlay spinner to be shown.
 * Attach to a <span> in your markup. It will provide a <label> and <select>.
 * This panel shares its model with the DropDownChoice so you can set the model object directly on this
 * component. Be careful if replacing the models themselves.
 * 
 * @author plukasew
 */
public class SakaiSpinnerDropDownChoice<T> extends GenericPanel<T>
{
	public final DropDownChoice<T> select;

	/**
	 * Constructor
	 * @param id the wicket id
	 * @param choiceModel the model to hold the selected choice
	 * @param choices the available choices
	 * @param renderer how to render each choice
	 * @param labelModel the model for the <label> tag
	 * @param behavior behaviour invoked when the selection changes
	 */
	public SakaiSpinnerDropDownChoice(String id, IModel<T> choiceModel, List<T> choices, IChoiceRenderer<T> renderer,
			IModel<String> labelModel, SakaiSpinningSelectOnChangeBehavior behavior)
	{
		super(id, choiceModel);
		select = new DropDownChoice<>("spinnerSelect", choiceModel, choices, renderer);
		select.setLabel(labelModel);
		select.add(behavior);
		add(select);
		setRenderBodyOnly(true);
	}
}
