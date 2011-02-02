/**
 * $URL:$
 * $Id:$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.sakaiproject.sitestats.tool.wicket.pages.BasePage;
import org.sakaiproject.sitestats.tool.wicket.providers.SortableSearchableDataProvider;


/**
 * @author Nuno Fernandes
 */
public class SakaiNavigatorSearch extends Panel {
	private static final long				serialVersionUID	= 1L;

	private SortableSearchableDataProvider	dataProvider;
	private TextField						searchBox;

	public SakaiNavigatorSearch(String id, final SortableSearchableDataProvider dataProvider) {
		super(id);
		this.dataProvider = dataProvider;

		setDefaultModel(new CompoundPropertyModel(this));

		Form form = new Form("searchForm");
		add(form);

		searchBox = new TextField("searchKeyword");
		form.add(searchBox);

		Button search = new Button("search") {
			@Override
			public void onSubmit() {
				String keyword = getSearchKeyword();
				if(keyword == null || "".equals(keyword)){
					dataProvider.clearSearchKeyword();
				}else{
					dataProvider.setSearchKeyword(keyword);
				}
				super.onSubmit();
			}
		};
		form.add(search);

		Button clear = new Button("clear") {
			@Override
			public void onSubmit() {
				dataProvider.clearSearchKeyword();
				super.onSubmit();
			}
		};
		form.add(clear);
	}

	@Override
	public void renderHead(HtmlHeaderContainer container) {
		container.getHeaderResponse().renderJavascriptReference(BasePage.JQUERYSCRIPT);
		super.renderHead(container);
	}

	public void setSearchKeyword(String keyword) {
		dataProvider.setSearchKeyword(keyword);
	}

	public String getSearchKeyword() {
		return dataProvider.getSearchKeyword();
	}
}
