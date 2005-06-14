/*
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
 */

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
