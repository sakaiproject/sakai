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

public class PagerButtonControlRenderer
  extends Renderer
{

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

  /*** GENERATOR FILE: ...pagerButtonControl.html some hand mods *** */

  /* *** IF SOURCE DOCUMENT CHANGES YOU NEED TO REGENERATE THIS METHOD*** */
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
    String controlId = (String) component.getAttributes().get("controlId");

    // previous
    writer.write("<div align=\"right\">");
    writer.write(" <input id=\"" + formId + "_" + controlId +
      "_pager_button_control_previousbtn\" type=\"button\" name=\"" +
      formId + "_" + controlId +
      "_pager_button_control_previousbtn\" value=\"Previous\"");
    writer.write("   onclick=\"document.forms['" + formId +
      "']['" + controlId + "'].value='<'; document.forms['" + formId +
      "'].submit(); return false;\" />");

      // selct number per page: this is not functional yet
//    writer.write("-->");
    writer.write(" <select id=\"" + formId + "_" + controlId +
      "_pager_button_control_selectlist\" name=\"" + formId + "_" +
      controlId + "_pager_button_control_selectlist\" size=\"1\">");
    writer.write(
      "	<option value=\"10\"  selected>Show 10 Items per Page</option>");
    writer.write("	<option value=\"20\">Show 20 Items per Page</option>");
    writer.write("	<option value=\"30\">Show 30 Items per Page</option>");
    writer.write("	<option value=\"50\">Show 50 Items per Page</option>");
    writer.write("	<option value=\"100\">Show 100 Items per Page</option>");
    writer.write(" </select>");
//    writer.write("<!--");

    // next
    writer.write(" <input id=\"" + formId + "_" + controlId +
      "_pager_button_control_nextbtn\" type=\"button\" name=\"" + formId +
      "_" + controlId + "_pager_button_control_nextbtn\" value=\"Next\"");
    writer.write("   onclick=\"document.forms['" + formId + "']['" +
      controlId + "'].value='>'; document.forms['" + formId +
      "'].submit(); return false;\" />");
    writer.write("</div>");
  }

}
