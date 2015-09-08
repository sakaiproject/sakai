/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.tool.wicket.widget;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;


public class WidgetTabs extends Panel implements IAjaxIndicatorAware {
	private static final long		serialVersionUID		= 1L;
	private LoadSelectedTabBehavior	loadSelectedTabBehavior	= null;
	private List<AbstractTab>		tabs;
	
	
	public WidgetTabs(String id, List<AbstractTab> tabs) {
		this(id, tabs, -1);
	}

	public WidgetTabs(String id, List<AbstractTab> tabs, int selectedTab) {
		super(id, new Model(Integer.valueOf(-1)));
		setOutputMarkupId(true);
		setVersioned(false);
		this.setTabs(tabs);
		if(this.tabs == null) {
			this.tabs = new ArrayList<AbstractTab>();
		}
		
		// tabs	
		Loop tabLoop = new Loop("tabs", tabs.size()) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(LoopItem item) {
				int index = item.getIndex();
				AbstractTab tab = ((AbstractTab) WidgetTabs.this.tabs.get(index));
				
				int selected = getSelectedTab();
				if(index == selected) {
					item.add(new AttributeModifier("class", new Model("tabsel")));
				}
				
				final WebMarkupContainer titleLink = newLink("link", index);
				titleLink.add(new Label("title", tab.getTitle()));
				item.add(titleLink);
			}			
		};
		add(tabLoop);
		
		// add ajax behavior
		loadSelectedTabBehavior = new LoadSelectedTabBehavior();
		add(loadSelectedTabBehavior);	
		
		// select initial tab
		setSelectedTab(selectedTab, false);		
	}
	
	protected void onBeforeRender() {
		super.onBeforeRender();
		if(!hasBeenRendered() && getSelectedTab() == -1){
			setSelectedTab(0, false);
		}
	}
	
	public void setTabs(List<AbstractTab> tabs) {
		this.tabs = tabs;
	}
	public List<AbstractTab> getTabs() {
		return tabs;
	}
	
	public void setSelectedTab(int selectedTab) {
		setSelectedTab(selectedTab, true);
	}

	public void setSelectedTab(int selectedTab, boolean showTabContents) {
		if(selectedTab < 0 || selectedTab >= tabs.size()){
			throw new IndexOutOfBoundsException();
		}
		setDefaultModelObject(Integer.valueOf(selectedTab));
		AbstractTab tab = getTabs().get(selectedTab);
		WebMarkupContainer tabContents = null;
		if(showTabContents) {
			tabContents = tab.getPanel("tabContents");
		}else{
			tabContents = new WebMarkupContainer("tabContents");
		}
		if(get("tabContents") == null){
			add(tabContents);
		}else{
			replace(tabContents);
		}
	}
	public int getSelectedTab() {
		return ((Integer) getDefaultModelObject()).intValue();
	}
	
	public String getLoadSelectedTabScript() {
		return loadSelectedTabBehavior.getScript();
		
	}

	protected WebMarkupContainer newLink(String linkId, final int index) {
		return new IndicatingAjaxFallbackLink(linkId) {
			private static final long	serialVersionUID	= 1L;

			public void onClick(AjaxRequestTarget target) {
				setSelectedTab(index);
				if(target != null){
					target.add(WidgetTabs.this);
					target.appendJavaScript("setMainFrameHeightNoScroll(window.name, 0, 100);");
				}
				onAjaxUpdate(target);
			}
		};
	}
	
	protected void onAjaxUpdate(AjaxRequestTarget target) {}

	public String getAjaxIndicatorMarkupId() {
		return null;
	}
	
	private class LoadSelectedTabBehavior extends AbstractDefaultAjaxBehavior {
		private static final long	serialVersionUID	= 1L;

		@Override
		protected void respond(AjaxRequestTarget target) {
			setSelectedTab(getSelectedTab());
			target.add(WidgetTabs.this);
			target.appendJavaScript("setMainFrameHeightNoScroll(window.name);");
		}
		
		public String getScript() {
			StringBuilder buff = new StringBuilder();
			buff.append("wicketAjaxGet('");
			buff.append(getCallbackUrl());
			buff.append(",function() {}, function() {}");
			buff.append(",null, '").append(getAttributes().getChannel().getName()).append("'");
			buff.append(")");
			return buff.toString();
		}
		
		@Override
		protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
		{
			super.updateAjaxAttributes(attributes);
			
			attributes.setChannel(new AjaxChannel(getId()));
		}
	}
}
