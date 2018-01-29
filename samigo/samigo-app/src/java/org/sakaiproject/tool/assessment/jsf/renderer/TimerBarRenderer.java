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
import javax.faces.component.UIViewRoot;
import java.util.Map;

/**
 * <p>Description: </p>
 * <p>Description:<br />
 * This class is the class that renders the <code>timerBar</code>
 * custom tag.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @author (JavaScript) Brian Gosselin of http://scriptasylum.com
 * @version $Id$
 */

public class TimerBarRenderer extends Renderer
{
  private static final String SCRIPT_PATH = "/jsf/widget/timerBar/";

  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof UIOutput);
  }

  public void decode(FacesContext context, UIComponent component)
  {
  }

  public void encodeChildren(FacesContext context, UIComponent component)
    throws IOException
  {
    ;
  }

  /**
   * <p>Faces render output method .</p>
   * <p>Method Generator: org.sakaiproject.tool.assessment.devtools.RenderMaker</p>
   *
   *  @param context   <code>FacesContext</code> for the current request
   *  @param component <code>UIComponent</code> being rendered
   *
   * @throws IOException if an input/output error occurs
   */
    public void encodeEnd(FacesContext context, UIComponent component)
      throws IOException {

       if (!component.isRendered())
       {
         return;
        }

        ResponseWriter writer = context.getResponseWriter();

        String clientId = null;

        if (component.getId() != null &&
          !component.getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX))
        {
          clientId = component.getClientId(context);
        }

        if (clientId != null)
        {
          writer.startElement("span", component);
          writer.writeAttribute("id", clientId, "id");
        }

        Map attrMap = component.getAttributes();

         writer.write("\n");
         writer.write("\n<script type=\"text/javascript\">");
         writer.write("\n// Timer Bar - Version 1.0");
         writer.write("\n// Based on Script by Brian Gosselin of http://scriptasylum.com");
         writer.write("\n  var loadedcolor='gray' ;            // PROGRESS BAR COLOR");


         writer.write("\n  var unloadedcolor='green';         // COLOR OF UNLOADED AREA");

         writer.write("\n  var bordercolor='navy';            // COLOR OF THE BORDER");
         writer.write("\n  var barheight = " + attrMap.get("height") + "; // HEIGHT OF PROGRESS BAR IN PIXELS");
         writer.write("\n  var barwidth = " + attrMap.get("width") + "; // WIDTH OF THE BAR IN PIXELS");
         writer.write("\n  var waitTime = " + attrMap.get("wait") + "; // NUMBER OF SECONDS FOR PROGRESSBAR");
         writer.write("\n  var loaded = " + attrMap.get("elapsed") + "*10; // TENTHS OF A SECOND ELAPSED");
         writer.write("\n// THE FUNCTION BELOW CONTAINS THE ACTION(S) TAKEN ONCE BAR REACHES 100.");
         writer.write("\n");
         writer.write("\n  var action = function()");
         writer.write("\n {");
         writer.write("\n   " + attrMap.get("expireScript") + "clickSubmitForGrade();");
         writer.write("\n }");
         writer.write("\n");
         writer.write("\n  var action2 = function()");
         writer.write("\n {");
         writer.write ("\n showTimeDueWarning (function (){");
         writer.write("\n   " + attrMap.get("expireScript") + ";");
         writer.write("\n });}");
         writer.write("\n");
         writer.write("\n// THE FUNCTION BELOW CONTAINS THE ACTION TAKEN WHERE THERE ARE 5 MINUTES LEFT.");
         writer.write("\n");
         writer.write("\n  var fiveMinutesAction = function()");
         writer.write("\n {");
         writer.write("\n showTimerWarning();");
         writer.write("\n }");
         writer.write("\n");
         writer.write("\n</script>");
         String contextPath = context.getExternalContext().getRequestContextPath();
         writer.write("\n<script type=\"text/javascript\" src=\"" +
           contextPath + SCRIPT_PATH + "timerbar.js\"></script>");
         writer.write("\n");

        if (clientId != null)
          {
          writer.endElement("span");
        }
    }

}
