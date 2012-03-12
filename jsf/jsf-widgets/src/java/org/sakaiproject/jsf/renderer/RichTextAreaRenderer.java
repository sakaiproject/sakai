/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.jsf.renderer;

import java.io.IOException;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.sakaiproject.jsf.util.RendererUtil;
import org.sakaiproject.util.FormattedText;

public class RichTextAreaRenderer extends Renderer
{
    public boolean supportsComponentType(UIComponent component)
    {
        return (component instanceof org.sakaiproject.jsf.component.RichTextAreaComponent);
    }

    public void encodeBegin(FacesContext context, UIComponent component)
            throws IOException
    {
        String clientId = component.getClientId(context);
        String textareaId = clientId+"_textarea";

        ResponseWriter writer = context.getResponseWriter();

        // value (text) of the HTMLArea
        Object value = null;
        if (component instanceof UIInput) value = ((UIInput) component).getSubmittedValue();
        if (value == null && component instanceof ValueHolder) value = ((ValueHolder) component).getValue();
        if (value == null) value = "";

        //escape the value so the wysiwyg editors don't get too clever and turn things
        //into tags that are not tags. 
        value = FormattedText.escapeHtmlFormattedTextarea((String) value);

        // character height of the textarea
        int columns = -1;
        String columnsStr = (String) RendererUtil.getAttribute(context, component, "columns");
        if (columnsStr != null && columnsStr.length() > 0) columns = Integer.parseInt(columnsStr);
        
        // character width of the textarea
        int rows = -1;
        String rowsStr = (String) RendererUtil.getAttribute(context, component, "rows");
        if (rowsStr != null && rowsStr.length() > 0) rows = Integer.parseInt(rowsStr);
        	
        writer.write("<table border=\"0\"><tr><td>");
        writer.write("<textarea name=\"" + textareaId + "\" id=\"" + textareaId + "\"");
        if (columns > 0) writer.write(" cols=\""+columns+"\"");
        if (rows > 0) writer.write(" rows=\""+rows+"\"");
        writer.write(">");
        if (value != null)
           writer.write((String) value);
        writer.write("</textarea>");
        
        writer.write("<script type=\"text/javascript\">sakai.editor.launch('" + textareaId + "');</script>");
        
        //SAK-20818 be sure to close the table
        writer.write("</td></tr></table>\n");
        
 
    }


    public void decode(FacesContext context, UIComponent component)
    {
        if (null == context || null == component
                || !(component instanceof org.sakaiproject.jsf.component.RichTextAreaComponent))
        {
            throw new IllegalArgumentException();
        }

        String clientId = component.getClientId(context);

        Map requestParameterMap = context.getExternalContext()
                .getRequestParameterMap();

        String newValue = (String) requestParameterMap.get(clientId + "_textarea");

        org.sakaiproject.jsf.component.RichTextAreaComponent comp = (org.sakaiproject.jsf.component.RichTextAreaComponent) component;
        comp.setSubmittedValue(newValue);
    }
}


