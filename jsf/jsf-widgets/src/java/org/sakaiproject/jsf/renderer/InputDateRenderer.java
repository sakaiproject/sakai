
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
 *       http://www.opensource.org/licenses/ECL-2.0
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
import java.util.Locale;
import java.util.Map;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.sakaiproject.jsf.util.ConfigurationResource;
import org.sakaiproject.jsf.util.LocaleUtil;
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
  private static final boolean inputTimeSeconds = true; //todo

  /** Bean to get resources for specific locale. */
  private class ConfigurationResourceBean
  {
    // calendar popup configuration
    private final String cursorStyle;
    private final String clickAlt;
    private final String calendarPath;
    private final String calendarIcon;
    // input date and time configuration, from global resources
    private final boolean inputMonthFirst;
    private final String dateFormatString;
    private final SimpleDateFormat dateFormat;
    private final SimpleDateFormat timeFormat;
    private final SimpleDateFormat dateTimeFormat;
    private final String dateHint;
    private final String timeHint;

    public ConfigurationResourceBean(final Locale locale)
    {
      ConfigurationResource cr = new ConfigurationResource(locale);
      String resources = cr.get("resources");
      cursorStyle = cr.get("picker_style");
      calendarPath = "/" + resources + "/" + cr.get("inputDatePopup");
      calendarIcon = "/" + resources + "/" + cr.get("inputDateImage");
      clickAlt = cr.get("date_pick_alt");
      inputMonthFirst = Boolean.valueOf(cr.get("inputMonthFirst"));
      boolean inputTimeColon = Boolean.valueOf(cr.get("inputTimeColon"));
      boolean inputTime24 = Boolean.valueOf(cr.get("inputTime24"));
      dateHint = cr.get("inputDateHint");
      timeHint = cr.get("inputTimeHint");

      String timeFormatString;
      if (inputMonthFirst)
      {
        dateFormatString = cr.get("inputMonthFirstFormat");
      }
      else
      {
        dateFormatString = cr.get("inputDayFirstFormat");
      }
      if (inputTimeColon)
      {
        if (inputTime24)
        {
          timeFormatString = cr.get("inputTimeColonFormat24");
        }
        else
        {
          timeFormatString = cr.get("inputTimeColonFormatAMPM");
        }
      }
      else
      {
        if (inputTime24)
        {
          timeFormatString = cr.get("inputTimeDotFormat24");
        }
        else
        {
          timeFormatString = cr.get("inputTimeDotFormatAMPM");
        }
      }
      dateFormat = new SimpleDateFormat(dateFormatString, locale);
      timeFormat = new SimpleDateFormat(timeFormatString, locale);
      dateTimeFormat =
        new SimpleDateFormat(dateFormatString + " " + timeFormatString, locale);
    }

    public String getCursorStyle()
    {
      return cursorStyle;
    }

    public String getClickAlt()
    {
      return clickAlt;
    }

    public String getCalendarPath()
    {
      return calendarPath;
    }

    public String getCalendarIcon()
    {
      return calendarIcon;
    }

    public boolean isInputMonthFirst()
    {
      return inputMonthFirst;
    }

    public String getDateFormatString()
    {
      return dateFormatString;
    }

    public SimpleDateFormat getDateFormat()
    {
      return dateFormat;
    }

    public SimpleDateFormat getTimeFormat()
    {
      return timeFormat;
    }

    public SimpleDateFormat getDateTimeFormat()
    {
      return dateTimeFormat;
    }

    public String getDateHint()
    {
      return dateHint;
    }

    public String getTimeHint()
    {
      return timeHint;
    }
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
    ConfigurationResourceBean crb = new ConfigurationResourceBean(LocaleUtil.getLocale(context));
    Date date = null;
    if(dateStr==null && timeStr!=null)
    {
      try
      {
        date = crb.getTimeFormat().parse(timeStr);
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
        date = crb.getDateFormat().parse(dateStr);
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
        date = crb.getDateTimeFormat().parse(dateStr + " " + timeStr);
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
    ConfigurationResourceBean crb = new ConfigurationResourceBean(LocaleUtil.getLocale(context));
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
          date = crb.getDateTimeFormat().parse((String) value);
        }
        catch (ParseException ex)
        {
          // leave alone
        }
      }
    }

    if (date !=null)
    {
      dateString = crb.getDateFormat().format(date);
      timeString = crb.getTimeFormat().format(date);
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
      writer.write("<i>&#160;" + crb.getDateHint() + "&#160;</i>" );
      String dateId = clientId + "_date" ;

      String type ="text";
      writer.write("<input type=\"" + type + "\"" +
                   " size=\"" + crb.getDateFormatString().length() + "\"" +
                   " name=\"" + dateId + "\"" +
                   " id=\"" + dateId + "\"" +
                   " value=\"" + dateString + "\">&#160;");

      // script creates unique javascript popup calendar object
      String calRand = "cal" + ("" + Math.random()).substring(2);
      String calendar;
      if (crb.isInputMonthFirst())
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
      writer.write("  style=\"" + crb.getCursorStyle() + "\" ");
      writer.write("  src=\"" + crb.getCalendarIcon() + "\"\n");
      writer.write("  border=\"0\"\n");
      writer.write("  onclick=");
      writer.write("\"javascript:" + calScript +
                   calRand + ".popup('','" + crb.getCalendarPath() + "');\"\n");
      writer.write("  alt=\"" + crb.getClickAlt() + "\"\n");
      writer.write(" />&#160;&#160;\n");
    }

    /////////////////////////////////////////////////////////
    //  RENDER TIME INPUT
    /////////////////////////////////////////////////////////
    if (showTime)
    {
      writer.write("<i>&#160;" + crb.getTimeHint() + "&#160;</i>" );
      String timeId = clientId + "_time" ;
      writer.write("<input type=\"text\"" +
                   " size=\"" + (crb.getDateFormatString().length() + 1) + "\"" +
                  " name=\"" + timeId + "\"" +
                  " id=\"" + timeId + "\"" +
                  " value=\"" + timeString + "\">&#160;");
    }

  }

}
