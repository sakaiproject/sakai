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
package org.sakaiproject.wicket.markup.html.repeater.util;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public abstract class EnhancedDataProvider<T, S> extends SortableDataProvider
{
	@Getter @Setter protected String searchField;
	@Getter @Setter protected Object filterField;
	@Getter @Setter protected String instructions;
	@Getter @Setter protected boolean isLimited;
	@Getter @Setter protected boolean isFilterVisible = false;
	@Getter @Setter protected boolean isFilterLimiterVisible = false;
	@Getter @Setter protected boolean isFilterConfigurerVisible = false;
	@Getter @Setter protected boolean isSearchVisible = false;
	@Getter @Setter protected boolean isInstructionVisible = false;
	@Getter @Setter protected String tableTitle;

	public List getFilterList()
	{
		return new LinkedList();
	}

	public IChoiceRenderer getFilterChoiceRenderer()
	{
		return new ChoiceRenderer();
	}

	public String getLimitField()
	{
		return null;
	}

	@Override
	public IModel model(Object object)
	{
		return new Model((Serializable)object);
	}

	public void onClickConfigurer(AjaxRequestTarget target) {}
}
