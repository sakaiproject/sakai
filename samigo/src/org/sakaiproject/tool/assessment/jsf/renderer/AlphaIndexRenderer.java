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
 * <p>Render an alphabetical index.  Makes any letter that exists in the
 * initials parameter a link.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id: AlphaIndexRenderer.java,v 1.3 2004/11/12 04:58:44 rgollub.stanford.edu Exp $
 */

public class AlphaIndexRenderer
    extends Renderer
{

  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof UIOutput);
  }

  public void decode(FacesContext context, UIComponent component)
  {
    context.getViewRoot().setTransient(true); /* where "context" is of type "FaceContext" */
  }

  public void encodeBegin(FacesContext context, UIComponent component) throws
      IOException
  {
    ;
  }

  public void encodeChildren(FacesContext context, UIComponent component) throws
      IOException
  {
    ;
  }

  /**
   * <p>Render an alphabetical index.  Makes any letter that exists in the
   * initials parameter a link.  Any other is simply displayed. </p>
   * <p>Example: <br />
   * | &lt;a href='#A'&gt;&lt;b&gt;A&lt;/b&gt;&lt;/a&gt; | &lt;b&gt;B&lt;/b&gt;...
   *
   * @param context   FacesContext for the request we are processing
   * @param component UIComponent to be rendered
   *
   * @throws IOException          if an input/output error occurs while rendering
   * @throws NullPointerException if <code>context</code>
   *                              or <code>component</code> is null
   */
  public void encodeEnd(FacesContext context, UIComponent component) throws
      IOException
  {

    if ( (context == null) || (component == null))
    {
      throw new NullPointerException();
    }

    ResponseWriter writer = context.getResponseWriter();
    String initials = (String) component.getAttributes().get("initials");

    if (initials == null) initials = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    // this provides a semi-acceptable default

    initials = initials.toUpperCase();

    writer.write("\n"); // this makes the HTML a little cleaner

    // loop through the index for each letter
    for (char c = 'A'; c < 'Z' + 1; c++)
    {
      // the student's last name starts with this letter, make this a link
      if (initialExists(c, initials))
      {
        writer.write("|<a href='#" + c + "'><b>" + c + "</b></a>");
      } else // the intial list DOES NOT contain this letter
      {
        writer.write("|<b>" + c + "</b>");
      }
    }

    writer.write("\n"); // this makes the HTML a little cleaner
  }

  /**
   * Is the character c in the initials string?
   * @param c
   * @param initials
   * @return true if it is
   */
  private boolean initialExists(char c, String initials)
  {
    return initials.indexOf(""+c) > -1;
  }

}
