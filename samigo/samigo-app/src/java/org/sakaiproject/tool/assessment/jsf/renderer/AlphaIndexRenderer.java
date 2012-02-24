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
 * <p>Render an alphabetical index.  Makes any letter that exists in the
 * initials parameter a link.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
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
