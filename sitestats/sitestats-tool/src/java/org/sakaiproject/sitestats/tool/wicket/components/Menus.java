package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;

public class Menus extends Panel {
	private static final long		serialVersionUID	= 1L;

	@SpringBean
	private transient SakaiFacade	facade;

	private String					siteId;
	private String					realSiteId;
	private PageParameters			pageParameters;
	

	public Menus(String id) {
		this(id, null);
	}
	
	public Menus(String id, String siteId) {
		super(id);
		
		// site Id
		realSiteId = facade.getToolManager().getCurrentPlacement().getContext();
		if(siteId != null) {
			this.siteId = siteId;
		}else{
			this.siteId = realSiteId;
		}

		// page parameters
		pageParameters = new PageParameters("siteId="+this.siteId);
		
		renderBody();
	}
	
	/** Render Sakai Menu. */
	@SuppressWarnings("unchecked")
	private void renderBody() {
		setRenderBodyOnly(true);
		
		boolean isSiteStatsAdminPage = facade.getStatsAuthz().isSiteStatsAdminPage();
		boolean isBrowsingThisSite = siteId.equals(realSiteId);
		
		// admin menu
		AdminMenu adminMenu = new AdminMenu("adminMenu");
		add(adminMenu);
		
		// standard menu
		WebMarkupContainer standardMenuContainer = new WebMarkupContainer("standardMenuContainer");
		// menu
		Menu standardMenu = new Menu("standardMenu", siteId);
		standardMenuContainer.add(standardMenu);
		add(standardMenuContainer);
		
		// menus rendering
		if(isSiteStatsAdminPage) {
			adminMenu.setVisible(true);
			if(!isBrowsingThisSite) {
				standardMenuContainer.setVisible(true);
				standardMenuContainer.add(new AttributeModifier("style", true, new Model("margin: 10px 5px 5px 5px;")));
			}else{
				standardMenuContainer.setVisible(false);
			}
		}else{
			adminMenu.setVisible(false);
			standardMenuContainer.setVisible(true);
		}
	}
}
