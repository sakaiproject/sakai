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
package org.sakaiproject.sitestats.tool.wicket.pages;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.Util;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.parser.EventParserTip;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.sitestats.tool.wicket.components.CSSFeedbackPanel;
import org.sakaiproject.sitestats.tool.wicket.components.EventRegistryTree;
import org.sakaiproject.sitestats.tool.wicket.components.Menus;

/**
 * @author Nuno Fernandes
 */
public class PreferencesPage extends BasePage {
	private static final long		serialVersionUID			= 1L;
	
	private String realSiteId;
	private String siteId;

	// UI
	private FeedbackPanel			feedback					= null;
	private EventRegistryTree 		eventRegistryTree 			= null;
	
	private static final String[] transparencyChoices = { "100", "90", "80", "70", "60", "50", "40", "30", "20", "10" };
	private static final List<String> chartTransparencyChoices = Arrays.asList(transparencyChoices);

	// Model
	private PrefsData				prefsdata					= null;
	
	
	public PreferencesPage() {
		this(null);
	}

	public PreferencesPage(PageParameters pageParameters) {
		realSiteId = Locator.getFacade().getToolManager().getCurrentPlacement().getContext();
		if(pageParameters != null) {
			siteId = pageParameters.get("siteId").toString();
		}
		if(siteId == null){
			siteId = realSiteId;
		}
		boolean allowed = Locator.getFacade().getStatsAuthz().isUserAbleToViewSiteStats(siteId);
		if(allowed) {
			setDefaultModel(new CompoundPropertyModel(this));
			renderBody();
		}else{
			setResponsePage(NotAuthorizedPage.class);
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forUrl(JQUERYSCRIPT));
		response.render(OnDomReadyHeaderItem.forScript("toggleCheckboxAll();"));
	}
	
	@SuppressWarnings("serial")
	private void renderBody() {
		add(new Menus("menu", siteId));
		
		Form form = new Form("prefsForm");
		form.setOutputMarkupId(true);
		form.setMarkupId("prefsForm");
		add(form);
		feedback = new CSSFeedbackPanel("messages");
		form.add(feedback);
		
		
		// Section: General
		CheckBox listToolEventsOnlyAvailableInSite = new CheckBox("listToolEventsOnlyAvailableInSite");
		form.add(listToolEventsOnlyAvailableInSite);
		
		
		// Section: Chart
		WebMarkupContainer chartPrefs = new WebMarkupContainer("chartPrefs");
		boolean chartPrefsVisible = Locator.getFacade().getStatsManager().isEnableSiteVisits() || Locator.getFacade().getStatsManager().isEnableSiteActivity();
		chartPrefs.setVisible(chartPrefsVisible);
		form.add(chartPrefs);
		//CheckBox chartIn3D = new CheckBox("chartIn3D");
		//chartPrefs.add(chartIn3D);
		CheckBox itemLabelsVisible = new CheckBox("itemLabelsVisible");
		chartPrefs.add(itemLabelsVisible);

		DropDownChoice chartTransparency = new DropDownChoice("chartTransparency", chartTransparencyChoices, new IChoiceRenderer() {
			public Object getDisplayValue(Object object) {
				return (String) object + "%";
			}
			public String getIdValue(Object object, int index) {
				return (String) object;
			}			
		});
		chartPrefs.add(chartTransparency);
		
		
		// Section: Activity Definition
		CheckBox useAllTools = new CheckBox("useAllTools");
		useAllTools.add(AttributeModifier.replace("onclick", "toggleCheckboxAll();"));
		useAllTools.setOutputMarkupId(true);
		useAllTools.setMarkupId("useAllTools");
		form.add(useAllTools);
		eventRegistryTree = new EventRegistryTree("eventRegistryTree", getPrefsdata().getToolEventsDef()) {
			@Override
			public boolean isToolSuported(final ToolInfo toolInfo) {
				if(Locator.getFacade().getStatsManager().isEventContextSupported()){
					return true;
				}else{
					List<ToolInfo> siteTools = Locator.getFacade().getEventRegistryService().getEventRegistry(siteId, getPrefsdata().isListToolEventsOnlyAvailableInSite());
					Iterator<ToolInfo> i = siteTools.iterator();
					while (i.hasNext()){
						ToolInfo t = i.next();
						if(t.getToolId().equals(toolInfo.getToolId())){
							EventParserTip parserTip = t.getEventParserTip();
							if(parserTip != null && parserTip.getFor().equals(StatsManager.PARSERTIP_FOR_CONTEXTID)){
								return true;
							}
						}
					}
				}
				return false;
			}
		};
		form.add(eventRegistryTree);
		
		
		// Bottom Buttons
		Button update = new Button("update") {
			@Override
			public void onSubmit() {
				savePreferences();
				prefsdata = null;
				super.onSubmit();
			}
		};
		update.setDefaultFormProcessing(true);
		form.add(update);
		Button cancel = new Button("cancel") {
			@Override
			public void onSubmit() {
				prefsdata = null;
				setResponsePage(OverviewPage.class);
			}
		};
		cancel.setDefaultFormProcessing(false);
		form.add(cancel);
	}
	
	private PrefsData getPrefsdata() {
		if(prefsdata == null) {
			prefsdata = Locator.getFacade().getStatsManager().getPreferences(siteId, true);
		}
		return prefsdata;
	}

	private void savePreferences() {
		if(isUseAllTools()) {
			getPrefsdata().setToolEventsDef(Locator.getFacade().getEventRegistryService().getEventRegistry(siteId, isListToolEventsOnlyAvailableInSite()));
		}else{
			getPrefsdata().setToolEventsDef((List<ToolInfo>) eventRegistryTree.getEventRegistry());
		}
		boolean opOk = Locator.getFacade().getStatsManager().setPreferences(siteId, getPrefsdata());		
		if(opOk){
			info((String) new ResourceModel("prefs_updated").getObject());
		}else{
			error((String) new ResourceModel("prefs_not_updated").getObject());
		}
	}

	public void setListToolEventsOnlyAvailableInSite(boolean listToolEventsOnlyAvailableInSite) {
		prefsdata.setListToolEventsOnlyAvailableInSite(listToolEventsOnlyAvailableInSite);
	}

	public boolean isListToolEventsOnlyAvailableInSite() {
		return getPrefsdata().isListToolEventsOnlyAvailableInSite();
	}
	
	public void setChartIn3D(boolean chartIn3D) {
		prefsdata.setChartIn3D(chartIn3D);
	}

	public boolean isChartIn3D() {
		return getPrefsdata().isChartIn3D();
	}
	
	public void setUseAllTools(boolean useAllTools) {
		prefsdata.setUseAllTools(useAllTools);
	}

	public boolean isUseAllTools() {
		return getPrefsdata().isUseAllTools();
	}

	public void setItemLabelsVisible(boolean itemLabelsVisible) {
		prefsdata.setItemLabelsVisible(itemLabelsVisible);
	}

	public boolean isItemLabelsVisible() {
		return getPrefsdata().isItemLabelsVisible();
	}
	
	public void setChartTransparency(String value) {
		float converted = (float) Util.round(Double.parseDouble(value)/100,1);
		getPrefsdata().setChartTransparency(converted);
	}
	
	public String getChartTransparency() {
		return Integer.toString((int) Util.round(getPrefsdata().getChartTransparency()*100,0) );
	}
}

