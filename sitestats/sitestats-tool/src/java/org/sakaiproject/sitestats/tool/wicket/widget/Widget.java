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
package org.sakaiproject.sitestats.tool.wicket.widget;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.StatelessLink;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.tool.wicket.components.AjaxLazyLoadFragment;
import org.sakaiproject.sitestats.tool.wicket.components.ExternalImage;
import org.sakaiproject.sitestats.tool.wicket.models.ReportDefModel;
import org.sakaiproject.sitestats.tool.wicket.pages.ReportDataPage;

/**
 * @author Nuno Fernandes
 */



public class Widget extends Panel {
	private static final long		serialVersionUID	= 1L;
	private static final int		MAX_TEXT_LENGTH		= 16;
	private String					iconUrl				= "";
	private String					title				= "";
	private List<WidgetMiniStat>	widgetMiniStats		= null;
	private List<AbstractTab>		tabs				= null;


	public Widget(String id, String iconUrl, String title, List<WidgetMiniStat> widgetMiniStats, List<AbstractTab> widgetTabs) {
		super(id);
		this.iconUrl = iconUrl;
		this.title = title;
		this.widgetMiniStats = widgetMiniStats;
		this.tabs = widgetTabs;
	}
	
	@Override
	protected void onBeforeRender() {
		renderWidget();
		super.onBeforeRender();
	}
	
	private void renderWidget() {
		setRenderBodyOnly(true);
		removeAll();
		
		// Icon
		add(new ExternalImage("icon", iconUrl));
		
		// Title
		add(new Label("title", title));
		
		// Show more/less links
		ExternalLink showMore = new ExternalLink("showMore", "#");
		showMore.setOutputMarkupId(true);
		add(showMore);
		ExternalLink showLess = new ExternalLink("showLess", "#");
		showLess.setOutputMarkupId(true);
		add(showLess);
		
		// Content details (middle)
		WebMarkupContainer middle = new WebMarkupContainer("middle");
		middle.setOutputMarkupId(true);
		add(middle);
	
		// MiniStats ajax load behavior
		AjaxLazyLoadFragment ministatContainer = new AjaxLazyLoadFragment("ministatContainer") {
			private static final long	serialVersionUID	= 12L;

			@Override
			public Fragment getLazyLoadFragment(String markupId) {
				return Widget.this.getLazyLoadedMiniStats(markupId);
			}
			
			@Override
			public Component getLoadingComponent(String markupId) {
				StringBuilder loadingComp = new StringBuilder();
				loadingComp.append("<div class=\"ministat load\">");
				loadingComp.append("  <img src=\"");
				loadingComp.append(RequestCycle.get().urlFor(AbstractDefaultAjaxBehavior.INDICATOR));
				loadingComp.append("\"/>");
				loadingComp.append("</div>");
				return new Label(markupId, loadingComp.toString()).setEscapeModelStrings(false);
			}
		};
		add(ministatContainer);
	
		// Tabs
		WidgetTabs widgetTabs = new WidgetTabs("widgetTabs", tabs, 0); 
		middle.add(widgetTabs);
		
		// Links behaviors
		String showMoreOnclick = "showMoreLess('#"+middle.getMarkupId()+"', '#"+showMore.getMarkupId()+"','#"+showLess.getMarkupId()+"');return false;";
		showMore.add(new SimpleAttributeModifier("onclick", showMoreOnclick));
		String showLessOnclick = "showMoreLess('#"+middle.getMarkupId()+"', '#"+showMore.getMarkupId()+"','#"+showLess.getMarkupId()+"');return false;";
		showLess.add(new SimpleAttributeModifier("onclick", showLessOnclick));
	}
	
	private Fragment getLazyLoadedMiniStats(String markupId) {
		Fragment ministatFragment = new Fragment(markupId, "ministatFragment", this);
		int miniStatsCount = widgetMiniStats != null ? widgetMiniStats.size() : 0;
		Loop miniStatsLoop = new Loop("ministat", miniStatsCount) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(LoopItem item) {
				int index = item.getIteration();
				WidgetMiniStat ms = widgetMiniStats.get(index);
				
				// Value
				WebMarkupContainer value = new WebMarkupContainer("value");
				Label valueLabel = new Label("valueLabel", ms.getValue());
				valueLabel.setRenderBodyOnly(true);
				String tooltip = ms.getTooltip();
				StringBuilder classes = new StringBuilder(); 
				if(tooltip == null) {
					classes.append("value");
				}else{
					classes.append("valueText");
					value.add(new AttributeModifier("title", true, new Model(ms.getTooltip())));
				}
				if(ms.isWiderText()) {
					if(ms.getValue().length() > MAX_TEXT_LENGTH) {
						valueLabel.setDefaultModelObject(ms.getValue().substring(0, MAX_TEXT_LENGTH) + "...");
					}
					item.add(new AttributeModifier("class", true, new Model("ministat wider")));
				}
				value.add(new AttributeModifier("class", true, new Model(classes.toString())));
				value.add(valueLabel);
				item.add(value);
				
				// Second value
				Label secvalue = new Label("secvalue", ms.getSecondValue());
				secvalue.setVisible(ms.getSecondValue() != null);
				value.add(secvalue);
				
				// Link
				final ReportDef reportDefinition = ms.getReportDefinition();
				Link link = new StatelessLink("report") {
					private static final long	serialVersionUID	= 1L;
					@Override
					public void onClick() {
						String siteId = reportDefinition.getSiteId();
						ReportDefModel reportDefModel = new ReportDefModel(reportDefinition);
						setResponsePage(new ReportDataPage(reportDefModel, new PageParameters("siteId="+siteId), getWebPage()));
					}					
				};
				if(reportDefinition != null) {
					link.add(new AttributeModifier("title", true, new ResourceModel("overview_show_report")));
				}else if(ms instanceof WidgetMiniStatLink){
					WidgetMiniStatLink msl = (WidgetMiniStatLink) ms;
					final Page page = msl.getPageLink();
					link = new StatelessLink("report") {
						private static final long	serialVersionUID	= 1L;
						@Override
						public void onClick() {
							setResponsePage(page);
						}					
					};
					if(msl.getPageLinkTooltip() != null) {
						link.add(new AttributeModifier("title", true, new Model(msl.getPageLinkTooltip())));
					}
					link.add(new AttributeModifier("class", true, new Model("extlink")));
				}else{
					link.setEnabled(false);
					link.setRenderBodyOnly(true);
				}
				item.add(link);
				
				// Label
				Label label = new Label("label", ms.getLabel());
				label.setRenderBodyOnly(true);
				link.add(label);
			}
		};
		ministatFragment.add(miniStatsLoop);
		return ministatFragment;
	}
	
}

