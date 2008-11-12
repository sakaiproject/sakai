package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;
import org.sakaiproject.sitestats.tool.wicket.pages.AdminPage;
import org.sakaiproject.sitestats.tool.wicket.pages.ServerWidePage;


/**
 * @author Nuno Fernandes
 */
public class AdminMenu extends Panel {
	private static final long	serialVersionUID	= 1L;

	@SpringBean
	private transient SakaiFacade facade;

	/**
	 * Default constructor.
	 * @param id The wicket:id
	 */
	public AdminMenu(String id) {
		super(id);
		setRenderBodyOnly(true);
		renderBody();
	}
	
	/**
	 * Render Sakai Menu
	 */
	@SuppressWarnings("unchecked")
	private void renderBody() {
		// site id
		String siteId = facade.getToolManager().getCurrentPlacement().getContext();
		Class currentPageClass = getRequestCycle().getResponsePageClass();
		PageParameters pageParameters = new PageParameters("siteId="+siteId);
				
		// --------- ADMIN SECTION ---------
		
		// Admin page
		boolean adminPageVisible = 
			facade.getStatsAuthz().isUserAbleToViewSiteStatsAdmin(siteId);
		MenuItem adminPage = new MenuItem("adminPage", new ResourceModel("menu_sitelist"), AdminPage.class, pageParameters, true);
		adminPage.setVisible(adminPageVisible);
		//adminPage.add(new AttributeModifier("class", true, new Model("firstToolBarItem")));
		add(adminPage);
		
		// Admin ServerWide page
		boolean serverWidePageVisible = 
			facade.getStatsManager().isServerWideStatsEnabled();
		MenuItem serverWidePage = new MenuItem("serverWidePage", new ResourceModel("menu_serverwide"), ServerWidePage.class, pageParameters, false);
		serverWidePage.setVisible(serverWidePageVisible);
		add(serverWidePage);		
	}

	/* (non-Javadoc)
	 * @see org.apache.wicket.markup.html.panel.Panel#onComponentTag(org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		checkComponentTag(tag, "menu");
	}

}
