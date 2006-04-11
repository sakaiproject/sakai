/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
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
*
**********************************************************************************/


package org.sakaiproject.jsf.renderer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.sakaiproject.jsf.util.ConfigurationResource;
import org.sakaiproject.jsf.util.RendererUtil;

/**
 * <p>OutputDateRenderer is an HTML renderer for Sakai 2.0 JSF outputDate.</p>
 *
 * @author esmiley@stanford.edu
 * @author based partly on some previous code in DateOutput.java ggolden@umich.edu
 * @version $Revision$
 *
 *
 */
public class OutputDateRenderer extends Renderer
{
  /**
   * @todo add more configuration
   */
  private static final String OUTPUT_DATE;// for now we'll jsut use this one...
  private static final String OUTPUT_DATE_TIME;
  private static final String OUTPUT_DATE_SECS;
  private static final String OUTPUT_TIME;
  private static final String OUTPUT_SECS;

// we have static resources for our date formats
  static
  {
    ConfigurationResource cr = new ConfigurationResource();
    OUTPUT_DATE = cr.get("outputDate");
    OUTPUT_DATE_TIME = cr.get("outputDateTime");
    OUTPUT_DATE_SECS = cr.get("outputDateTimeSecs");
    OUTPUT_TIME = cr.get("outputTime");
    OUTPUT_SECS = cr.get("outputTimeSecs");
  }

  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof UIOutput);
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

  public void encodeBegin(FacesContext context, UIComponent component) throws
    IOException
  {
    if (!component.isRendered())
    {
      return;
    }

    ResponseWriter writer = context.getResponseWriter();

    // this is a date object representing our date
    // this is a debug line, we are losing our date
    Date date = ((Date) RendererUtil.getDefaultedAttribute(
      context, component, "value", new Date()));
    // this is the formatted output representation of the date
    String dateStr = "";
    String formatStr = getFormatString(context, component);
    dateStr = format(date, formatStr);


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
    writer.write(dateStr);
    if (clientId != null)
    {
      writer.endElement("span");
    }

  }

  /**
   * Obtain the format string from the
   * @param context Faces context
   * @param component the component
   * @return a date format String
   */
  private String getFormatString(FacesContext context, UIComponent component)
  {
    String fmt = OUTPUT_DATE;

    String showTime = (String) RendererUtil.getAttribute(context, component, "showTime");
    String showDate = (String) RendererUtil.getAttribute(context, component, "showDate");
    String showSeconds = (String) RendererUtil.getAttribute(context, component, "showSeconds");
//    boolean hasTime = true; //"true".equals(showTime)? true: false;
//    boolean hasDate = true; //"true".equals(showDate)? true: false;
//    boolean hasSeconds = true; //"true".equals(showSeconds) && hasTime ? true: false;

    boolean hasTime = "true".equals(showTime)? true: false;
    boolean hasDate = "true".equals(showDate)? true: false;
    boolean hasSeconds = "true".equals(showSeconds) && hasTime ? true: false;

    if (hasTime)
    {
      if (hasDate)
      {
        if (hasSeconds)
        {
          fmt = OUTPUT_DATE_SECS;
        }
        else
        {
          fmt = OUTPUT_DATE_TIME;
        }
      }
      else
      {
        if (hasSeconds)
        {
          fmt = OUTPUT_SECS;
        }
        else
        {
          fmt = OUTPUT_TIME;
        }
      }
    }

    return fmt;
  }

  /**
   * @param date
   * @return the formatted date String
   */
  private String format(Date date, String format)
  {
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    return sdf.format(date);
  }

}
