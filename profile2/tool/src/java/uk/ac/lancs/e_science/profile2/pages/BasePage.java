package uk.ac.lancs.e_science.profile2.tool.pages;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.cover.SessionManager;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.tool.ProfileApplication;

import org.apache.log4j.Logger;



public class BasePage extends WebPage implements IHeaderContributor {

	private transient Logger log = Logger.getLogger(BasePage.class);
	protected Link viewProfileLink;
	protected Link editProfileLink;
	
	protected SakaiProxy sakaiProxy;
	

	
	public BasePage() {
		log.debug("constructor");
    	//super();
		
		if(log.isDebugEnabled()) log.debug("BasePage()");

		//get sakaiProxy
		sakaiProxy = ProfileApplication.get().getSakaiProxy();
		
    	//view profile link
    	viewProfileLink = new Link("viewProfileLink") {
			public void onClick() {
				setResponsePage(new ViewProfile());
			}
		};
		//viewProfileLink.add(new Label("viewProfileLabel",new ResourceModel("link.view")));
		viewProfileLink.add(new Label("viewProfileLabel","View profile"));
		add(viewProfileLink);
    	
		//edit profile link
		editProfileLink = new Link("editProfileLink") {
			public void onClick() {
				setResponsePage(new EditProfile());
			}
		};
		//editProfileLink.add(new Label("editProfileLabel",new ResourceModel("link.edit")));
		editProfileLink.add(new Label("editProfileLabel","Edit your profile"));
		add(editProfileLink);
		
		add(new Label("testingLabel",sakaiProxy.getCurrentUserId()));

		
    	
    }
	
	
	
	
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	//Style it like a Sakai tool
	protected static final String HEADSCRIPTS = "/library/js/headscripts.js";
	protected static final String BODY_ONLOAD_ADDTL="setMainFrameHeight( window.name )";
	
	public void renderHead(IHeaderResponse response) {
		//get Sakai skin
		String skinRepo = ServerConfigurationService.getString("skin.repo");
		String toolCSS = getToolSkinCSS(skinRepo);
		String toolBaseCSS = skinRepo + "/tool_base.css";
		
		//Sakai additions
		response.renderJavascriptReference(HEADSCRIPTS);
		response.renderCSSReference(toolBaseCSS);
		response.renderCSSReference(toolCSS);
		response.renderOnLoadJavascript(BODY_ONLOAD_ADDTL);
		
		//Tool additions
		response.renderCSSReference("css/profile2.css");
		response.renderJavascriptReference("javascript/profile2.js");
		response.renderJavascriptReference("/library/js/jquery.js");
		response.renderString("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
				
	}
	
	protected String getToolSkinCSS(String skinRepo) {
		String skin = null;
		try {
			skin = SiteService.findTool(SessionManager.getCurrentToolSession().getPlacementId()).getSkin();			
		}
		catch(Exception e) {
			skin = ServerConfigurationService.getString("skin.default");
		}
		
		if(skin == null) {
			skin = ServerConfigurationService.getString("skin.default");
		}
		
		return skinRepo + "/" + skin + "/tool.css";
	}
	
	/*
	protected Label newResourceLabel(String id, Component component) {
		return new Label(id, new StringResourceModel(id, component, null));
	}
	
	public String getResourceModel(String resourceKey, IModel model) {
		return new StringResourceModel(resourceKey, this, model).getString();
	}
	*/
	
	
	
	
}
