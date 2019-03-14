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
package org.sakaiproject.sitestats.tool.wicket.components;

import java.util.Date;
import java.util.TimeZone;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.StatsUpdateManager;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.sitestats.tool.wicket.pages.NotAuthorizedPage;
import org.sakaiproject.time.api.UserTimeService;


/**
 * @author Nuno Fernandes
 */
public class LastJobRun extends Panel {
	private static final long		serialVersionUID	= 1L;

	private String					realSiteId;

	public LastJobRun(String id) {
		this(id, null);
	}
	
	public LastJobRun(String id, String siteId) {
		super(id);
		realSiteId = Locator.getFacade().getToolManager().getCurrentPlacement().getContext();
		if(siteId == null){
			siteId = realSiteId;
		}
		boolean allowedView = Locator.getFacade().getStatsAuthz().isUserAbleToViewSiteStats(siteId);
		boolean allowedOwn = Locator.getFacade().getStatsAuthz().isUserAbleToViewSiteStatsOwn(siteId);
		if(!allowedView && !allowedOwn) {
			setResponsePage(NotAuthorizedPage.class);
		}
	}
	
	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		renderBody();
	}

	private void renderBody() {
		StatsManager statsManager = Locator.getFacade().getStatsManager();
		StatsUpdateManager statsUpdateManager = Locator.getFacade().getStatsUpdateManager();
		
		setRenderBodyOnly(true);
		
		final WebMarkupContainer lastJobRun = new WebMarkupContainer("lastJobRun");
		boolean lastJobRunVisible = !statsUpdateManager.isCollectThreadEnabled() && statsManager.isLastJobRunDateVisible(); 
		lastJobRun.setVisible(lastJobRunVisible);
		add(lastJobRun);
		final Label lastJobRunDate = new Label("lastJobRunDate");
		final Label lastJobRunServerDate = new Label("lastJobRunServerDate");
		if(lastJobRunVisible) {
			try{
				Date d = statsUpdateManager.getEventDateFromLatestJobRun();
				UserTimeService timeServ = Locator.getFacade().getUserTimeService();
				String dStr = timeServ.shortLocalizedTimestamp(d.toInstant(), getSession().getLocale());
				String serverDateStr = timeServ.shortLocalizedTimestamp(d.toInstant(), TimeZone.getDefault(), getSession().getLocale());
				lastJobRunDate.setDefaultModel(new Model(dStr));
				String localSakaiName = Locator.getFacade().getStatsManager().getLocalSakaiName();
				StringResourceModel model = new StringResourceModel("lastJobRun_server_time", getPage(), null,
						new Object[] {localSakaiName, serverDateStr});
				lastJobRunServerDate.setDefaultModel(model);
			}catch(Exception e){
				lastJobRunDate.setDefaultModel(new Model());
				lastJobRunServerDate.setDefaultModel(new Model());
			}
		}
		lastJobRun.add(lastJobRunDate);
		lastJobRun.add(lastJobRunServerDate);
	}

}
