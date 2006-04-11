/**********************************************************************************
* $URL$
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
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIViewRoot;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.sakaiproject.tool.assessment.jsf.renderer.util.RendererUtil;

/**
 * <p>Description: </p>
 * <p>Render the custom color picker control.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $id: $
 */

public class DatePickerRenderer extends Renderer
{
  // icon height and width
  private static final String HEIGHT = "16";
  private static final String WIDTH = "16";
  private static final String CURSORSTYLE = "cursor:pointer;";
  private static final String CLICKALT = "Click Here to Pick Date";

  private RendererUtil rUtil;

  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof UIInput);
  }

  /**
   * decode the value
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

  public void encodeBegin(FacesContext context,
    UIComponent component) throws IOException
  {
    ;
  }

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

    String jsfId = (String) component.getAttributes().get("id");
    String id = jsfId;

    if (component.getId() != null &&
      !component.getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX))
    {
      id = component.getClientId(context);
    }

    Object value = null;
    if (component instanceof UIInput)
    {
      value = ( (UIInput) component).getSubmittedValue();
    }
    if (value == null && component instanceof ValueHolder)
    {
      value = ( (ValueHolder) component).getValue();
    }
    String valString = "";
    if (value != null)
    {
      valString = value.toString();
    }

    String type = "text";
    String size = (String) component.getAttributes().get("size");
    if (size == null)
    {
      size = "20";

      // script creates unique calendar object with input object
    }
    String calRand = "cal" + ("" + Math.random()).substring(2);
    String calScript =
      "var " + calRand + " = new calendar2(" +
      "document.getElementById('" + id + "'));" +
      "" + calRand + ".year_scroll = true;" +
      "" + calRand + ".time_comp = true;";

    writer.write("<input type=\"" + type + "\" name=\"" + id +
      "\" id=\"" + id + "\" size=\"" + size + "\" value=");
    writer.write("\"" + valString + "\">&#160;<img \n  onclick=");
    writer.write("\"javascript:" + calScript +
      calRand + ".popup('','" + contextPath +
      "/html/');\"\n");
//    "/jsf/widget/datepicker/');\"\n");
    writer.write("  width=\"" + WIDTH + "\"\n");
    writer.write("  height=\"" + HEIGHT + "\"\n");
    writer.write("  style=\"" + CURSORSTYLE + "\" ");
    writer.write("  src=\"" + contextPath + "/images/calendar/cal.gif\"\n");
    writer.write("  border=\"0\"\n");
    writer.write("  id=\"_datePickerPop_" + id + "\"");
    writer.write("  alt=\"" + CLICKALT + "\"/>&#160;&#160;\n");
  }

}
