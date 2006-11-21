package org.sakaiproject.portal.render.iframe;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.portal.render.api.RenderResult;
import org.sakaiproject.portal.render.api.ToolRenderException;
import org.sakaiproject.portal.render.api.ToolRenderService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Web;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class IFrameToolRenderService implements ToolRenderService {

    private static ResourceLoader rb = new ResourceLoader("sitenav");

    public void preprocess(ToolConfiguration toolConfiguration,
                           HttpServletRequest request,
                           HttpServletResponse response,
                           ServletContext context)
        throws IOException, ToolRenderException {
    }

    public RenderResult render(ToolConfiguration configuration,
                       HttpServletRequest request,
                       HttpServletResponse response,
                       ServletContext context)
        throws IOException, ToolRenderException {

        String titleString = Web.escapeHtml(configuration.getTitle());
        String toolUrl = ServerConfigurationService.getToolUrl() + "/"
            + Web.escapeUrl(configuration.getId());


        StringBuffer sb = new StringBuffer();
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

        RenderResult result = new RenderResult();
        result.setTitle(titleString);
        result.setContent(sb);

        return result;
    }
}
