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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public abstract class EnhancedDataProvider extends SortableDataProvider {

	protected String searchField;
	protected Object filterField;
	protected String instructions;
	protected boolean isLimited;
	protected boolean isFilterVisible = false;
	protected boolean isFilterLimiterVisible = false;
	protected boolean isFilterConfigurerVisible = false;
	protected boolean isSearchVisible = false;
	protected boolean isInstructionVisible = false;
	protected String tableTitle;

	public String getSearchField() {
		return searchField;
	}

	public void setSearchField(String searchField) {
		this.searchField = searchField;
	}

	public Object getFilterField() {
		return filterField;
	}

	public void setFilterField(Object filterField) {
		this.filterField = filterField;
	}

	public List getFilterList() {
		return new LinkedList();
	}
	
	public IChoiceRenderer getFilterChoiceRenderer() {
		return new ChoiceRenderer();
	}
	
	public String getLimitField() {
		return null;
	}

	public boolean isLimited() {
		return isLimited;
	}

	public void setLimited(boolean isLimited) {
		this.isLimited = isLimited;
	}
	
	public IModel model(Object object) {
		return new Model((Serializable)object);
	}
	
	public void onClickConfigurer(AjaxRequestTarget target) {

	}

	public boolean isFilterVisible() {
		return isFilterVisible;
	}

	public void setFilterVisible(boolean isFilterVisible) {
		this.isFilterVisible = isFilterVisible;
	}

	public boolean isFilterLimiterVisible() {
		return isFilterLimiterVisible;
	}

	public void setFilterLimiterVisible(boolean isFilterLimiterVisible) {
		this.isFilterLimiterVisible = isFilterLimiterVisible;
	}

	public boolean isFilterConfigurerVisible() {
		return isFilterConfigurerVisible;
	}

	public void setFilterConfigurerVisible(boolean isFilterConfigurerVisible) {
		this.isFilterConfigurerVisible = isFilterConfigurerVisible;
	}

	public boolean isSearchVisible() {
		return isSearchVisible;
	}

	public void setSearchVisible(boolean isSearchVisible) {
		this.isSearchVisible = isSearchVisible;
	}

	public String getInstructions() {
		return instructions;
	}

	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}

	public boolean isInstructionVisible() {
		return isInstructionVisible;
	}

	public void setInstructionVisible(boolean isInstructionVisible) {
		this.isInstructionVisible = isInstructionVisible;
	}

	public String getTableTitle() {
		return tableTitle;
	}

	public void setTableTitle(String tableTitle) {
		this.tableTitle = tableTitle;
	}

	

}
