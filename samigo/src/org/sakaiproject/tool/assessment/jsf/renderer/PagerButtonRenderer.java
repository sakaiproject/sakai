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
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

/**
 * <p>Description: </p>
 * <p>Render a next/previous control for a pager attached to a dataTable.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class PagerButtonRenderer extends Renderer
{
  // some IO stuff
  private final String numberValues[] = {"10", "20", "30", "50", "100" };
  private final String DISABLED_ATTRIB = " disabled=\"disabled\"";
  // this needs to be internationalized, tricky because of fragments
  private final String OF = "of";
  private final String VIEWING = "Viewing";
  private final String SHOW = "Show";
  private final String ITEMS = "items";
  private final String ITEMS_PER = "items per page";


  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof UIOutput);
  }

  public void decode(FacesContext context, UIComponent component)
  {
  }

  public void encodeChildren(FacesContext context,
    UIComponent component) throws IOException
  {
    ;
  }

  public void encodeBegin(FacesContext context,
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
    String formId = (String) component.getAttributes().get("formId");
    String firstItem = (String) component.getAttributes().get("firstItem");
    String lastItem = (String) component.getAttributes().get("lastItem");
    String dataTableId = (String) component.getAttributes().get(
                         "dataTableId");
    String prevText = (String) component.getAttributes().get("prevText");
    String nextText = (String) component.getAttributes().get("nextText");
    String numItems = (String) component.getAttributes().get("numItems");
    String totalItems = (String) component.getAttributes().get("totalItems");
    String prevDisabled =
      (String) component.getAttributes().get("prevDisabled");
    String nextDisabled =
      (String) component.getAttributes().get("nextDisabled");
    String prevDisabledAttr = "";
    String nextDisabledAttr = "";
    if ("true".equals(prevDisabled))
    {
      prevDisabledAttr = DISABLED_ATTRIB;
    }
    if ("true".equals(nextDisabled))
    {
      nextDisabledAttr = DISABLED_ATTRIB;
    }

    writer.write("  <span class=\"instruction\">"+ VIEWING +" " + firstItem +
      " - " +
      lastItem + " " + OF + " " + totalItems + " " +ITEMS +"</span>");
    writer.write("  <br />");
    writer.write("  <input type=\"submit\"");
    writer.write("    name=\"" + dataTableId + "_" + formId +
      "__pager_button_control_prev_btn\"");
    writer.write("    onclick=\"javascript:document.forms['" + formId +
      "'].submit(); return false;\"");
    writer.write("    value=\"&lt; " + prevText + " " + numItems + "\"");
    writer.write("    " + prevDisabledAttr + "/>");

    String select = dataTableId + "_" + formId +  "__pager_button_control_select";

    writeSelectList(writer, numItems, select, formId);

    writer.write("  <input type=\"submit\"");
    writer.write("    name=\"" + dataTableId + "_" + formId +
      "__pager_button_control_next_btn\"");
    writer.write("    onclick=\"javascript:document.forms['" + formId +
      "'].submit(); return false;\"");
    writer.write("    value=\"" + nextText + " " + numItems + " &gt;\"");
    writer.write("		" + nextDisabledAttr + "/>");
    writer.write("  <br />");
  }

  /**
   *
   * @param writer for output
   * @param numItems number of items to show string
   * @param selectId the name to give the HTML select control
   * @param formId the form to post onchange events to
   * @throws IOException
   */
  private void writeSelectList(ResponseWriter writer, String numItems,
    String selectId, String formId) throws IOException
  {
    writer.write("  <select ");
    writer.write("    onchange=\"javascript:document.forms['" + formId +
      "'].submit(); return false;\"");
    writer.write("    name=\"" + selectId + "\">");

    for (int i = 0; i < numberValues.length; i++) {
      String currentVal = numberValues[i];
      writer.write("    <option ");
      writer.write(" value=\"" + currentVal +"\"");
      if (currentVal.equals(numItems))
      {
        writer.write(" selected=\"selected\" ");
      }
      writer.write(">"+ SHOW +" " + currentVal + " " + ITEMS_PER + "</option>");
    }

    writer.write("  </select>");
  }

}
