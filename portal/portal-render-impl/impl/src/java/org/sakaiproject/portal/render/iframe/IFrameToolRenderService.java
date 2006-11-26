package org.sakaiproject.portal.render.iframe;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.portal.render.api.RenderResult;
import org.sakaiproject.portal.render.api.ToolRenderException;
import org.sakaiproject.portal.render.api.ToolRenderService;
import org.sakaiproject.portal.render.portlet.PortletToolRenderService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Web;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class IFrameToolRenderService implements ToolRenderService {

    private static final Log LOG =
        LogFactory.getLog(IFrameToolRenderService.class);

    private static ResourceLoader rb = new ResourceLoader("sitenav");

    public boolean preprocess(HttpServletRequest request,
                           HttpServletResponse response,
                           ServletContext context)
        throws IOException, ToolRenderException {

        return true;
    }

    public RenderResult render(ToolConfiguration configuration,
                       HttpServletRequest request,
                       HttpServletResponse response,
                       ServletContext context)
        throws IOException, ToolRenderException {

        final String titleString = Web.escapeHtml(configuration.getTitle());
        String toolUrl = ServerConfigurationService.getToolUrl() + "/"
            + Web.escapeUrl(configuration.getId());


        final StringBuffer sb = new StringBuffer();
        sb.append("<iframe")
            .append("	name=\"").append(Web.escapeJavascript("Main" + configuration.getId())).append("\"\n")
            .append("	id=\"")
            .append(Web.escapeJavascript("Main" + configuration.getId())).append("\n")
            .append("\"").append("\n")
            .append("	title=\"").append(titleString).append(" ").append(Web.escapeHtml(rb.getString("sit.contentporttit"))).append("\"").append("\n")
            .append("	class =\"portletMainIframe\"").append("\n")
            .append("	height=\"50\"").append("\n")
            .append("	width=\"100%\"").append("\n")
            .append("	frameborder=\"0\"").append("\n")
            .append("	marginwidth=\"0\"").append("\n")
            .append("	marginheight=\"0\"").append("\n")
            .append("	scrolling=\"auto\"").append("\n")
            .append("	src=\"").append(toolUrl).append("?panel=Main\">").append("\n")
            .append("</iframe>");

        RenderResult result = new RenderResult() {
        	public String getTitle() {
        		return titleString;
        	}
        	public String getContent() {
        		return sb.toString();
        	}
        };

        return result;
    }

	public boolean accept(ToolConfiguration configuration,
                          HttpServletRequest request,
                          HttpServletResponse response,
                          ServletContext context) {
		if ( PortletToolRenderService.isPortletTool(configuration) ) {
			return false;
		}
		return true;
	}
}
