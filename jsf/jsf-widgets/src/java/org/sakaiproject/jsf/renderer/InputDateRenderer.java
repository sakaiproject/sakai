
/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.jsf.renderer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.sakaiproject.jsf.util.ConfigurationResource;
import org.sakaiproject.jsf.util.RendererUtil;

/**
 * <p>Description: </p>
 * <p>Render the custom color picker control.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $id: $
 * @todo handle showSeconds
 */

public class InputDateRenderer
  extends Renderer
{
  // calendar popup configuration
  private static final String HEIGHT = "16";
  private static final String WIDTH = "16";
  private static final String CURSORSTYLE;
  private static final String CLICKALT;
  private static final String CALENDAR_PATH;
  private static final String CALENDAR_ICON;
  // input date and time configuration, from global resources
  private static final boolean inputMonthFirst;
  private static final boolean inputTimeColon;
  private static final boolean inputTime24;
  private static final boolean inputTimeSeconds = true; //todo
  private static final String DATE_FORMAT_STRING;
  private static final String TIME_FORMAT_STRING;
  private static final SimpleDateFormat dateFormat;
  private static final SimpleDateFormat timeFormat;
  private static final  SimpleDateFormat dateTimeFormat;
  private static final String DATE_HINT;
  private static final String TIME_HINT;

  // an admittedly long static intializer block
  // the spec calls for a global setting for each installation
  // much of this is taken up in calculating that in these gory details
  static
  {
    ConfigurationResource cr = new ConfigurationResource();
    String resources = cr.get("resources");
    CURSORSTYLE = cr.get("picker_style");
    CALENDAR_PATH = "/" + resources + "/" + cr.get("inputDatePopup");
    CALENDAR_ICON = "/" + resources + "/" + cr.get("inputDateImage");
    CLICKALT = cr.get("date_pick_alt");
    inputMonthFirst = "true".equals((String) cr.get("inputMonthFirst"));
    inputTimeColon = "true".equals((String) cr.get("inputTimeColon"));
    inputTime24 = "true".equals((String) cr.get("inputTime24"));
    DATE_HINT = cr.get("inputDateHint");
    TIME_HINT = cr.get("inputTimeHint");

    if (inputMonthFirst)
    {
      DATE_FORMAT_STRING = cr.get("inputMonthFirstFormat");
    }
    else
    {
      DATE_FORMAT_STRING = cr.get("inputDayFirstFormat");
    }
    if (inputTimeColon)
    {
      if (inputTime24)
      {
        TIME_FORMAT_STRING = cr.get("inputTimeColonFormat24");
      }
      else
      {
        TIME_FORMAT_STRING = cr.get("inputTimeColonFormatAMPM");
      }
    }
    else
    {
      if (inputTime24)
      {
        TIME_FORMAT_STRING = cr.get("inputTimeDotFormat24");
      }
      else
      {
        TIME_FORMAT_STRING = cr.get("inputTimeDotFormatAMPM");
      }
    }
    dateFormat = new SimpleDateFormat(DATE_FORMAT_STRING);
    timeFormat = new SimpleDateFormat(TIME_FORMAT_STRING);
    dateTimeFormat =
      new SimpleDateFormat(DATE_FORMAT_STRING + " " + TIME_FORMAT_STRING);
  }

  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof UIInput);
  }


  /**
   * decode the value
   * @param context
   * @param component
   */
  public void decode(FacesContext context, UIComponent component)
  {
    if (RendererUtil.isDisabledOrReadonly(context, component))
    {
      return;
    }
    // get request parameters
    Map requestParameterMap = context.getExternalContext()
      .getRequestParameterMap();
    String clientId = component.getClientId(context);

    String dateStr = (String) requestParameterMap.get(clientId + "_date");
    String timeStr = (String) requestParameterMap.get(clientId + "_time");
    if(dateStr==null && timeStr==null) return;

    EditableValueHolder ev = (EditableValueHolder) component;
    // create holder for subcomponent values
//    Map dateParts = new HashMap();
//    // get the subcomponent values
//    // the hidden clientId maintains all the values togethr
//
//    dateParts.put("clientId", clientId);
//    dateParts.put("date", date);
//    dateParts.put("time", time);
//    // set the submitted value to the subcomponent value map
//    ev.setSubmittedValue(dateParts);
    Date date = null;
    if(dateStr==null && timeStr!=null)
    {
      try
      {
        date = timeFormat.parse(timeStr);
      }
      catch (ParseException ex)
      {
        //leave null
      }
    }
    else if(dateStr!=null && timeStr==null)
    {
      try
      {
        date = dateFormat.parse(dateStr);
      }
      catch (ParseException ex)
      {
        //leave null
      }
    }
    else
    {
      try
      {
        date = dateTimeFormat.parse(dateStr + " " + timeStr);
      }
      catch (ParseException ex)
      {
        //leave null
      }

    }


    ev.setSubmittedValue(date);
  }


  /**
   * <p>Faces render output method .</p>
   *  @param context   <code>FacesContext</code> for the current request
   *  @param component <code>UIComponent</code> being rendered
   *
   * @throws IOException if an input/output error occurs
   */
  public void encodeEnd(FacesContext context,
                        UIComponent component) throws IOException
  {
    if (RendererUtil.isDisabledOrReadonly(context, component) || !component.isRendered())
    {
      return;
    }

    ResponseWriter writer = context.getResponseWriter();
    String clientId = component.getClientId(context);

    /////////////////////////////////////////////////////////
    //  VALUE HOLDER AND ATTRIBUTES
    /////////////////////////////////////////////////////////
    String dateString = "";
    String timeString = "";
    Date date = null;

    Date submittedValue =
      (Date) ( (EditableValueHolder) component).getSubmittedValue();
    if (submittedValue != null)
    {
      date = submittedValue;
    }
    else
    {
      Object value = ( (ValueHolder) component).getValue();
      if (value instanceof Date)
      {
        date = (Date) value;
      }
      else if (value instanceof String)
      {
        try
        {
          date = dateTimeFormat.parse( (String) value);
        }
        catch (ParseException ex)
        {
          // leave alone
        }
      }
    }

    if (date !=null)
    {
      dateString = dateFormat.format(date);
      timeString = timeFormat.format(date);
    }


    // display options attributes
    boolean showDate;
    boolean showTime;
    boolean showSecond;

    showDate = "true".equals
      ((String) RendererUtil.getAttribute(context, component, "showDate"));
    showTime = "true".equals
      ((String) RendererUtil.getAttribute(context, component, "showTime"));
    showSecond = "true".equals
      ((String) RendererUtil.getAttribute(context, component, "showSecond"));

    /////////////////////////////////////////////////////////
    //  RENDER DATE INPUT
    /////////////////////////////////////////////////////////
    if (showDate)
    {
      writer.write("<i>&#160;" + DATE_HINT + "&#160;</i>" );
      String dateId = clientId + "_date" ;

      String type ="text";
      writer.write("<input type=\"" + type + "\"" +
                   " size=\"" + DATE_FORMAT_STRING.length() + "\"" +
                   " name=\"" + dateId + "\"" +
                   " id=\"" + dateId + "\"" +
                   " value=\"" + dateString + "\">&#160;");

      // script creates unique javascript popup calendar object
      String calRand = "cal" + ("" + Math.random()).substring(2);
      String calendar;
      if (inputMonthFirst)
      {
        calendar = "calendar2";
      }
      else
      {
        calendar = "calendar1";
      }
      String calScript =
        "var " + calRand + " = new " + calendar+ "(" +
        "document.getElementById('" + dateId + "'));" +
        "" + calRand + ".year_scroll = true;" +
        "" + calRand + ".time_comp = false;";
      // calendar icon with onclick to script
      writer.write("&#160;<img");
      writer.write("  id=\"" + clientId + "_datePickerPopup" + "\"");
      writer.write("  width=\"" + WIDTH + "\"\n");
      writer.write("  height=\"" + HEIGHT + "\"\n");
      writer.write("  style=\"" + CURSORSTYLE + "\" ");
      writer.write("  src=\"" + CALENDAR_ICON + "\"\n");
      writer.write("  border=\"0\"\n");
      writer.write("  onclick=");
      writer.write("\"javascript:" + calScript +
                   calRand + ".popup('','" + CALENDAR_PATH + "');\"\n");
      writer.write("  alt=\"" + CLICKALT + "\"\n");
      writer.write(" />&#160;&#160;\n");
    }

    /////////////////////////////////////////////////////////
    //  RENDER TIME INPUT
    /////////////////////////////////////////////////////////
    if (showTime)
    {
      writer.write("<i>&#160;" + TIME_HINT + "&#160;</i>" );
      String timeId = clientId + "_time" ;
      writer.write("<input type=\"text\"" +
                   " size=\"" + (DATE_FORMAT_STRING.length() + 1) + "\"" +
                  " name=\"" + timeId + "\"" +
                  " id=\"" + timeId + "\"" +
                  " value=\"" + timeString + "\">&#160;");
    }

  }

}
