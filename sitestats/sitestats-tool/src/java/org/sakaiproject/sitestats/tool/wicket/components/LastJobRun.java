package org.sakaiproject.sitestats.tool.wicket.components;

import java.util.Date;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.StatsUpdateManager;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;
import org.sakaiproject.sitestats.tool.wicket.pages.NotAuthorizedPage;


/**
 * @author Nuno Fernandes
 */
public class LastJobRun extends Panel {
	private static final long		serialVersionUID	= 1L;

	/** Inject Sakai facade */
	@SpringBean
	private transient SakaiFacade	facade;

	private String					realSiteId;
	private String					siteId;
	private String					siteTitle;

	public LastJobRun(String id) {
		this(id, null);
	}
	
	public LastJobRun(String id, String siteId) {
		super(id);
		realSiteId = facade.getToolManager().getCurrentPlacement().getContext();
		if(siteId == null){
			siteId = realSiteId;
		}
		boolean allowed = facade.getStatsAuthz().isUserAbleToViewSiteStats(siteId);
		if(allowed) {
			renderBody();
		}else{
			setResponsePage(NotAuthorizedPage.class);
		}		
	}
	
	private void renderBody() {
		StatsManager statsManager = facade.getStatsManager();
		StatsUpdateManager statsUpdateManager = facade.getStatsUpdateManager();
		
		setRenderBodyOnly(true);
		
		final WebMarkupContainer lastJobRun = new WebMarkupContainer("lastJobRun");
		boolean lastJobRunVisible = !statsUpdateManager.isCollectThreadEnabled() && statsManager.isLastJobRunDateVisible(); 
		lastJobRun.setVisible(lastJobRunVisible);
		add(lastJobRun);
		final Label lastJobRunDate = new Label("lastJobRunDate");
		if(lastJobRunVisible) {
			try{
				Date d = statsUpdateManager.getEventDateFromLatestJobRun();
				String dStr = facade.getTimeService().newTime(d.getTime()).toStringLocalFull();
				lastJobRunDate.setModel(new Model(dStr));
			}catch(RuntimeException e) {
				lastJobRunDate.setModel(new Model());
			}catch(Exception e){
				lastJobRunDate.setModel(new Model());
			}
		}
		lastJobRun.add(lastJobRunDate);
	}

}
