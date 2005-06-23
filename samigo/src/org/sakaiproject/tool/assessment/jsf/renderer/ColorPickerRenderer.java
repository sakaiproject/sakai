/**********************************************************************************
* $HeadURL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.jsf.renderer;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;
import javax.faces.component.ValueHolder;
import javax.faces.component.UIViewRoot;
import java.util.Map;
import org.sakaiproject.tool.assessment.jsf.renderer.util.RendererUtil;

/**
 * <p>Description: </p>
 * <p>Render the custom color picker control.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $id: $
 */

public class ColorPickerRenderer extends Renderer
{
  // icon height and width
  private final String HEIGHT = "13";
  private final String WIDTH = "15";
  // later we may want to support hidden
  private final String TYPE = "text";
  private final String CURSORSTYLE = "cursor:pointer;";
  private final String CLICKALT = "Click Here to Pick Color";

  private RendererUtil rUtil;

  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof UIInput);
  }

  /**
   * decode method
   * @param context
   * @param component
   */
  public void decode(FacesContext context, UIComponent component)
  {
    // we haven't added these attributes--yet--defensive programming...
    if(rUtil.isDisabledOrReadonly(component))
    {
      return;
    }

    String clientId = component.getClientId(context);
    Map requestParameterMap = context.getExternalContext()
                              .getRequestParameterMap();
    String newValue = (String) requestParameterMap.get(clientId );
    UIInput comp = (UIInput) component;
    comp.setSubmittedValue(newValue);
  }

  /**
   * no-op
   * @param context
   * @param component
   * @throws IOException
   */
  public void encodeBegin(FacesContext context,
    UIComponent component) throws IOException
  {
    ;
  }

  /**
   * no-op
   * @param context
   * @param component
   * @throws IOException
   */
  public void encodeChildren(FacesContext context,
    UIComponent component) throws IOException
  {
    ;
  }

  /**
   * <p>Faces render output method .</p>
   * <p>Method Generator: org.sakaiproject.tool.assessment.devtoolsRenderMaker</p>
   *
   *  @param context   <code>FacesContext</code> for the current request
   *  @param component <code>UIComponent</code> being rendered
   *
   * @throws IOException if an input/output error occurs
   */
  public void encodeEnd(FacesContext context,
    UIComponent component) throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    String contextPath = context.getExternalContext()
                         .getRequestContextPath();

    Object value = null;
    if (component instanceof UIInput)
    {
      value = ( (UIInput) component).getSubmittedValue();
      //System.out.println("renderer component is UIInput, value=" + value);
    }
    if (value == null && component instanceof ValueHolder)
    {
      value = ( (ValueHolder) component).getValue();
      //System.out.println("renderer component is ValueHolder, value=" +
      //  value);
    }
    String valString = "";
    if (value != null)
    {
      valString = value.toString();
    }

    String size = (String) component.getAttributes().get("size");
    if (size == null)
    {
      size = "20";
    }

    String jsfId = (String) component.getAttributes().get("id");
    String id = jsfId;

    if (component.getId() != null &&
      !component.getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX))
    {
      id = component.getClientId(context);
    }

    writer.write("");
    writer.write("<input ");
    writer.write("");
    writer.write("  value=\"" + valString + "\" ");
    writer.write("  type=\"" + TYPE + "\" ");
    writer.write("  size=\"" + size + "\" ");
    writer.write("  name=\"" + id + "\"");
    writer.write("  id=\"" + id + "\" />&#160;");
    writer.write("<img ");
    writer.write("  style=\"" + CURSORSTYLE + "\" ");
    writer.write("  height=\"" + HEIGHT + "\" ");
    writer.write("  width=\"" + WIDTH + "\"");
    writer.write("  src=\"" + contextPath + "/images/sel.gif\" ");
    writer.write("  border=\"0\"");
    writer.write("  alt=\"" + CLICKALT + "\" ");
    writer.write("  id=\"_colorPickerPop_" + id + "\"");
    writer.write("  onclick=\"javascript:TCP.popup(" +
      "document.getElementById('" + id + "'),'','" + contextPath +
      "/jsf/widget/colorpicker/')\" />");
    writer.write("&#160;");
  }
}
