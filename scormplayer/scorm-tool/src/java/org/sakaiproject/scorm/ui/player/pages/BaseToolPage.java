package org.sakaiproject.scorm.ui.player.pages;

import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.cover.SessionManager;

public class BaseToolPage extends WebPage implements IHeaderContributor {
	private static final long serialVersionUID = 2L;

	protected static final String TOOLBASE_CSS = "/library/skin/tool_base.css";
	protected static final String SCORM_CSS = "styles/scorm.css";
	
	public void renderHead(IHeaderResponse response) {
		
		// bjones86 - SAK-21890 - new 'neo' skin prefix support
		String skinRepo = ServerConfigurationService.getString( "skin.repo" );
		String skin = SiteService.findTool( SessionManager.getCurrentToolSession().getPlacementId() ).getSkin();
		if( skin == null )
			skin = ServerConfigurationService.getString( "skin.default" );
		String templates = ServerConfigurationService.getString( "portal.templates", "neoskin" );
		String prefix = ServerConfigurationService.getString( "portal.neoprefix", "neo-" );
		if( "neoskin".equals( templates ) && !skin.startsWith( prefix ) )
			skin = prefix + skin;
		response.renderCSSReference( skinRepo + "/" + skin + "/tool.css" );
		
		response.renderCSSReference(TOOLBASE_CSS);
		response.renderCSSReference(SCORM_CSS);
	}

}
