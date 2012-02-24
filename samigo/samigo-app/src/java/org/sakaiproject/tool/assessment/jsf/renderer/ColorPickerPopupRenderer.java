/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
import org.sakaiproject.util.ResourceLoader;

/**
 * <p>Description: </p>
 * <p>Render the HTML code for a Tigris color picker popup.</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $id: $
 *
 * @todo add resource for strings
 */

public class ColorPickerPopupRenderer extends Renderer
{
  // these should be coming from a resource
  // [DIEGO] Now they come from a resource, but as they are static final, really they are not used and
  // I use directly the string returned in getString...
/*
  private static final String TITLE = "Color Picker";
  private  static final   String WEB_SAFE = "Web Safe Palette";
  private  static final   String WINDOWS_SYSTEM = "Windows System Palette";
  private  static final   String GREY_SCALE = "Grey Scale Palette";
*/
  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof UIOutput);
  }

  public void decode(FacesContext context, UIComponent component)
  {
  }

  public void encodeBegin(FacesContext context, UIComponent component)
    throws IOException
  {
    ;
  }

  public void encodeChildren(FacesContext context, UIComponent component)
    throws IOException
  {
    ;
  }



  /* *** GENERATOR FILE: c:\Navigo\webapp\html\picker.html*** */
  /* *** IF SOURCE DOCUMENT CHANGES YOU NEED TO REGENERATE THIS METHOD*** */
  /* if you do so, make sure you incorporate hand modifications */
  /**
   * <p>Faces render output method .</p>
     * <p>Method Generator: org.sakaiproject.tool.assessment.devtoolsRenderMaker</p>
   *
   *  @param context   <code>FacesContext</code> for the current request
   *  @param component <code>UIComponent</code> being rendered
   *
   * @throws IOException if an input/output error occurs
   */
  public void encodeEnd(FacesContext context, UIComponent component)
    throws IOException
  {
	ResourceLoader rb= new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages");
    ResponseWriter writer = context.getResponseWriter();
    writer.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
    writer.write("<!--\n");
    writer.write("Title: Tigra Color Picker\n");
    writer.write(
      "URL: http://www.softcomplex.com/products/tigra_color_picker/\n");
    writer.write("Version: 1.1\n");
    writer.write("Date: 06/26/2003 (mm/dd/yyyy)\n");
    writer.write(
      "Feedback: feedback@softcomplex.com (specify product title in the subject)\n");
    writer.write(
      "Note: Permission given to use this script in ANY kind of applications if\n");
    writer.write("   header lines are left unchanged.\n");
    writer.write(
      "Note: Script consists of two files: picker.js and picker.html\n");
    writer.write(
      "About us: Our company provides offshore IT consulting services.\n");
    writer.write(
      "    Contact us at sales@softcomplex.com if you have any programming task you\n");
    writer.write(
      "   want to be handled by professionals. Our typical hourly rate is $20.\n");
    writer.write("-->\n");
    writer.write("\n");
    writer.write("<head>\n");
    writer.write("	<title>" + rb.getString("cp_TITLE") + "</title>\n");
    writer.write("	<style>\n");
    writer.write("		.bd { border : 1px inset InactiveBorder; }\n");
    writer.write("		.s  { width:181 }\n");
    writer.write("	</style>\n");
    writer.write("</head>\n");
    writer.write("<body leftmargin=\"5\" topmargin=\"5\" marginheight=\"5\" marginwidth=\"5\" onload=\"P.C(P.initPalette)\">\n");
    writer.write(
      "<table cellpadding=\"0\" cellspacing=\"2\" border=\"0\" width=\"184\">\n");
    writer.write("<form>\n");
    writer.write("<tr><td align=\"center\">\n");
    writer.write(
      "<select name=\"type\" onchange=\"P.C(this.selectedIndex)\" class=\"s\">\n");
    writer.write("	<option>" + rb.getString("cp_WEB_SAFE") + "</option>\n");
    writer.write("	<option>" + rb.getString("cp_WINDOWS_SYSTEM") + "</option>\n");
    writer.write("	<option>" + rb.getString("cp_GREY_SCALE") + "</option>\n");
    writer.write("</select>\n");
    writer.write("</td></tr>\n");
    writer.write("<tr><td align=\"center\">\n");
    writer.write("<script type=\"text/javascript\">\n");
    writer.write("	var P = opener.TCP;\n");
    writer.write("	onload = \"P.show(P.initPalette)\";\n");
    writer.write(
      "	document.forms[0].elements[0].selectedIndex = P.initPalette;\n");
    writer.write("	P.draw(window, document);\n");
    writer.write("</script>\n");
    writer.write("</td></tr>\n");
    writer.write("</form>\n");
    writer.write("</table>\n");
    writer.write("</body>\n");
    writer.write("</html>\n");
  }
}
