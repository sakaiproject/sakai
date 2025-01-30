package org.sakaiproject.signup.tool.jsf;

import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import java.io.IOException;

/**
 * Modified to add a col scope to a th element
 */
public class HtmlTableRenderer extends org.apache.myfaces.renderkit.html.ext.HtmlTableRenderer {

    @Override
    protected void renderHtmlColumnAttributes(FacesContext facesContext, ResponseWriter writer, UIComponent uiComponent, String prefix) throws IOException {
        super.renderHtmlColumnAttributes(facesContext, writer, uiComponent, prefix);
        if ("header".equals(prefix)) {
            writer.writeAttribute(HTML.SCOPE_ATTR, "col", null);
        }
    }
}
