/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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
  private static final String numberValues[] = {"10", "20", "30", "50", "100" };
  private static final String DISABLED_ATTRIB = " disabled=\"disabled\"";
  // this needs to be internationalized, tricky because of fragments
  private static final String OF = "of";
  private static final String VIEWING = "Viewing";
  private static final String SHOW = "Show";
  private static final String ITEMS = "items";
  private static final String ITEMS_PER = "items per page";


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
