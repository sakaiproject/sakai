/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
 *
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 *
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/

package org.sakaiproject.tool.messageforums.jsf;

import java.io.IOException;
import java.util.Iterator;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

/**
 * Modified version of HideDivision that will allow links or
 * "buttons" in the H4 tag.
 */

public class HideDivisionButtonBarButtonRenderer extends Renderer {

    public boolean supportsComponentType(UIComponent component) {
        return (component instanceof UIOutput);
    }

    public void decode(FacesContext context, UIComponent component) {}

    /**
     * Simple passthru.
     * 
     * @param context
     * @param component
     * @throws IOException
     */
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        if (!component.isRendered()) {
            return;
        }

        ResponseWriter writer = context.getResponseWriter();
        Iterator children = component.getChildren().iterator();
        while (children.hasNext()) {
            UIComponent child = (UIComponent) children.next();
            writer.writeText(child, null);
        }
    }

    /**
     * <p>
     * Faces render output method .
     * </p>
     * <p>
     * Method Generator: org.sakaiproject.tool.assessment.devtoolsRenderMaker
     * </p>
     * 
     * @param context
     *            <code>FacesContext</code> for the current request
     * @param component
     *            <code>UIComponent</code> being rendered
     * 
     * @throws IOException
     *             if an input/output error occurs
     */
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {

        if (!component.isRendered()) {
            return;
        }

        String action = (String) RendererUtil.getAttribute(context, component, "action");
        String value = (String) RendererUtil.getAttribute(context, component, "value");
   
        ResponseWriter writer = context.getResponseWriter();
        writer.write("<a href=\"" + action + "\">" + value + "</a>");
    }

    /**
     * 
     * @param context
     *            <code>FacesContext</code> for the current request
     * @param component
     *            <code>UIComponent</code> being rendered
     * 
     * @throws IOException
     *             if an input/output error occurs
     */
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {}

}
