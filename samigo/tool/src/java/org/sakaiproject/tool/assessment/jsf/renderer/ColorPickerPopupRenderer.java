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
import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

/**
 * <p>Description: </p>
 * <p>Render the HTML code for a Tigris color picker popup.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $id: $
 *
 * @todo add resource for strings
 */

public class ColorPickerPopupRenderer extends Renderer
{
  // these should be coming from a resource
  private static final String TITLE = "Color Picker";
  private static final String WEB_SAFE = "Web Safe Palette";
  private static final String WINDOWS_SYSTEM = "Windows System Palette";
  private static final String GREY_SCALE = "Grey Scale Palette";

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
    writer.write("	<title>" + TITLE + "</title>\n");
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
    writer.write("	<option>" + WEB_SAFE + "</option>\n");
    writer.write("	<option>" + WINDOWS_SYSTEM + "</option>\n");
    writer.write("	<option>" + GREY_SCALE + "</option>\n");
    writer.write("</select>\n");
    writer.write("</td></tr>\n");
    writer.write("<tr><td align=\"center\">\n");
    writer.write("<script language=\"JavaScript\">\n");
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