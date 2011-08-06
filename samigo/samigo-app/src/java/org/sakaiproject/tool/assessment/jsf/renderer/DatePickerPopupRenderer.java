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
 *       http://www.osedu.org/licenses/ECL-2.0
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
import java.util.*;
import java.text.*;
/**
 * <p>Description: </p>
 * <p>Render the HTML code for a Tigris color picker popup.</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $id: $
 *
 * @todo add resource for strings
 */

public class DatePickerPopupRenderer extends Renderer
{
  // these should be coming from a resource
  // value in alt tags, these are not yet internationalized
  public static final String PREV_MONTH = "previous month";
  public static final String NEXT_MONTH = "next month";
  public static final String PREV_YEAR = "previous year";
  public static final String NEXT_YEAR = "next year";

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

  // warning: considerable hand modification...
  /* *** GENERATOR FILE: c:\Navigo\webapp\html\c.html*** */
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
    public void encodeEnd(FacesContext context, UIComponent component)
        throws IOException {
      // get the calendar for the Locale, so this will be localized
      DateFormatSymbols dfs = new DateFormatSymbols();
      String[] days = dfs.getShortWeekdays();
      String[] months = dfs.getMonths();
      String jan = months[Calendar.JANUARY];
      String feb = months[Calendar.FEBRUARY];
      String mar = months[Calendar.MARCH];
      String apr = months[Calendar.APRIL];
      String may = months[Calendar.MAY];
      String jun = months[Calendar.JUNE];
      String jul = months[Calendar.JULY];
      String aug = months[Calendar.AUGUST];
      String sep = months[Calendar.SEPTEMBER];
      String oct = months[Calendar.OCTOBER];
      String nov = months[Calendar.NOVEMBER];
      String dec = months[Calendar.DECEMBER];
      String mon = days[Calendar.MONDAY];
      String tue = days[Calendar.TUESDAY];
      String wed = days[Calendar.WEDNESDAY];
      String thu = days[Calendar.THURSDAY];
      String fri = days[Calendar.FRIDAY];
      //String sat = days[Calendar.SATURDAY];
      String sun = days[Calendar.SUNDAY];

      String contextPath = context.getExternalContext().getRequestContextPath();

      ResponseWriter writer = context.getResponseWriter();

      writer.write("<!--\n");
      writer.write("Title: Tigra Calendar\n");
      writer.write("URL: http://www.softcomplex.com/products/tigra_calendar/\n");
      writer.write("Version: 3.2\n");
      writer.write("Date: 10/14/2002 (mm/dd/yyyy)\n");
      writer.write("Feedback: feedback@softcCalendar calendar = new GregorianCalendar(pdt);omplex.com (specify product title in the subject)\n");
      writer.write("Note: Permission given to use this script in ANY kind of applications if\n");
      writer.write("   header lines are left unchanged.\n");
      writer.write("Note: Script consists of two files: calendar?.js and calendar.html\n");
      writer.write("About us: Our company provides offshore IT consulting services.\n");
      writer.write("    Contact us at sales@softcomplex.com if you have any programming task you\n");
      writer.write("    want to be handled by professionals. Our typical hourly rate is $20.\n");
      writer.write("-->\n");

      writer.write("<html>\n");
//      writer.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
//      writer.write(
//        "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN>");
//      writer.write("<html>\n");
      writer.write("<head>\n");
      writer.write("<title>Select Date, Please.</title>\n");
      writer.write("<style>\n");
      writer.write("	td {font-family: Tahoma, Verdana, sans-serif; font-size: 12px;}\n");
      writer.write("</style>\n");
      writer.write("<script type=\"text/javascript\">\n");
      writer.write("//<![CDATA[\n");
      writer.write("// months as they appear in the calendar's title\n");
      writer.write("// (These are dynamically built by locale DateFormatSymbols!)\n");
      writer.write("var ARR_MONTHS = " +
                       "[\"" + jan + "\", \"" + feb + "\", " +
                       "\"" + mar + "\", \"" + apr + "\", " +
                       "\"" + may + "\", \"" + jun + "\",\n");
      writer.write("		\"" + jul + "\", \"" + aug + "\", " +
                       "\"" + sep + "\", \"" + oct + "\", \"" +
                       nov + "\", \"" + dec + "\"];\n");
      writer.write("// week day titles as they appear on the calendar\n");
      writer.write("var ARR_WEEKDAYS = " +
                       "[\"" + sun + "\", \"" + mon + "\", \"" + tue + "\", " +
                       "\"" + wed + "\", \"" + thu + "\", \"" + fri + "\", " +
                       "\"Sa\"];\n");
      writer.write("// day week starts from (normally 0-Su or 1-Mo)\n");
      writer.write("var NUM_WEEKSTART = 1;\n");
      writer.write("// path to the directory where calendar images are stored. trailing slash req.\n");
      writer.write("var STR_ICONPATH = '" + contextPath + "/images/calendar/';\n");
      writer.write("\n");
      writer.write("var re_url = new RegExp('datetime=(\\-?\\d+)');\n");
      writer.write("var dt_current = (re_url.exec(String(window.location))\n");
      writer.write("	? new Date(new Number(RegExp.$1)) : new Date());\n");
      writer.write("var re_id = new RegExp('id=(\\d+)');\n");
      writer.write("var num_id = (re_id.exec(String(window.location))\n");
      writer.write("	? new Number(RegExp.$1) : 0);\n");
      writer.write("var obj_caller = (window.opener ? window.opener.calendars[num_id] : null);\n");
      writer.write("var root_url = new RegExp('root=(\\S+)');\n");
      writer.write("var root = (root_url.exec(String(window.location)) ? new String(RegExp.$1) : \"\")\n");
      writer.write("if (obj_caller && obj_caller.year_scroll) {\n");
      writer.write("	// get same date in the previous year\n");
      writer.write("	var dt_prev_year = new Date(dt_current);\n");
      writer.write("	dt_prev_year.setFullYear(dt_prev_year.getFullYear() - 1);\n");
      writer.write("	if (dt_prev_year.getDate() != dt_current.getDate())\n");
      writer.write("		dt_prev_year.setDate(0);\n");
      writer.write("\n");
      writer.write("	// get same date in the next year\n");
      writer.write("	var dt_next_year = new Date(dt_current);\n");
      writer.write("	dt_next_year.setFullYear(dt_next_year.getFullYear() + 1);\n");
      writer.write("	if (dt_next_year.getDate() != dt_current.getDate())\n");
      writer.write("		dt_next_year.setDate(0);\n");
      writer.write("}\n");
      writer.write("\n");
      writer.write("// get same date in the previous month\n");
      writer.write("var dt_prev_month = new Date(dt_current);\n");
      writer.write("dt_prev_month.setMonth(dt_prev_month.getMonth() - 1);\n");
      writer.write("if (dt_prev_month.getDate() != dt_current.getDate())\n");
      writer.write("	dt_prev_month.setDate(0);\n");
      writer.write("\n");
      writer.write("// get same date in the next month\n");
      writer.write("var dt_next_month = new Date(dt_current);\n");
      writer.write("dt_next_month.setMonth(dt_next_month.getMonth() + 1);\n");
      writer.write("if (dt_next_month.getDate() != dt_current.getDate())\n");
      writer.write("	dt_next_month.setDate(0);\n");
      writer.write("\n");
      writer.write("// get first day to display in the grid for current month\n");
      writer.write("var dt_firstday = new Date(dt_current);\n");
      writer.write("dt_firstday.setDate(1);\n");
      writer.write("dt_firstday.setDate(1 - (7 + dt_firstday.getDay() - NUM_WEEKSTART) % 7);\n");
      writer.write("\n");
      writer.write("// function passing selected date to calling window\n");
      writer.write("function set_datetime(n_datetime, b_close) {\n");
      writer.write("	if (!obj_caller) return;\n");
      writer.write("\n");
      writer.write("	var dt_datetime = obj_caller.prs_time(\n");
      writer.write("		(document.cal ? document.cal.time.value : ''),\n");
      writer.write("		new Date(n_datetime)\n");
      writer.write("	);\n");
      writer.write("\n");
      writer.write("	if (!dt_datetime) return;\n");
      writer.write("	if (b_close) {\n");
      writer.write("		window.close();\n");
      writer.write("		obj_caller.target.value = (document.cal\n");
      writer.write("			? obj_caller.gen_tsmp(dt_datetime)\n");
      writer.write("			: obj_caller.gen_date(dt_datetime)\n");
      writer.write("		);\n");
      writer.write("	}\n");
      writer.write("	else obj_caller.popup(dt_datetime.valueOf());\n");
      writer.write("}\n");
      writer.write("\n");
      writer.write("function set_datetime_update(n_datetime, with_root) {\n");
      writer.write("  if (!obj_caller) return;\n");
      writer.write("\n");
      writer.write("  var dt_datetime = obj_caller.prs_time(\n");
      writer.write("    (document.cal ? document.cal.time.value : ''),\n");
      writer.write("    new Date(n_datetime)\n");
      writer.write("  );\n");
      writer.write("\n");
      writer.write("  if (!dt_datetime) return;\n");
      writer.write("  obj_caller.popup(dt_datetime.valueOf(), with_root);\n");
      writer.write("}\n");
      writer.write("//]]> \n");
      writer.write("</script>\n");
      writer.write("</head>\n");
      writer.write("<body bgcolor=\"#FFFFFF\" marginheight=\"5\" marginwidth=\"5\" topmargin=\"5\" leftmargin=\"5\" rightmargin=\"5\">\n");
      // replaced these lines with document.write so dom balanced in XHTML
//        writer.write("<table class=\"clsOTable\" cellspacing=\"0\" border=\"0\" width=\"100%\">\n");
//        writer.write("<tr><td bgcolor=\"#4682B4\">\n");
//        writer.write("<table cellspacing=\"1\" cellpadding=\"3\" border=\"0\" width=\"100%\">\n");
//        writer.write("<tr><td colspan=\"7\"><table cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"100%\">\n");
//        writer.write("<tr>\n");
      writer.write("<script type=\"text/javascript\">\n");
//      writer.write("//<![CDATA[\n");
//      writer.write("alert('got here');\n");
//      writer.write("//<!--");
      writer.write("document.write('<table>');");// class=\"clsOTable\" cellspacing=\"0\" border=\"0\" width=\"100%\">');\n");
//      writer.write("document.write('<table class=\"clsOTable\" cellspacing=\"0\" border=\"0\" width=\"100%\">');\n");
      writer.write("document.write('<tr><td bgcolor=\"#4682B4\">');\n");
      writer.write("document.write('<table cellspacing=\"1\" cellpadding=\"3\" border=\"0\" width=\"100%\">');\n");
      writer.write("document.write('<tr><td colspan=\"7\"><table cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"100%\">');\n");
      writer.write("document.write('<tr>');\n");
      // following document.write() in original HTML/JavaScript was
      // nearly incomprehensible when generated into writer.write lines
      // broke up into shorter strings, hopefully more readable
      writer.write("document.write(\n");
      writer.write("'<td>'+" +
       //using JavaScript ? operator
       "(obj_caller && obj_caller.year_scroll?" +
           // first alternative
           "'<a href=\"javascript:set_datetime_update('" +
           "+dt_prev_year.valueOf()+', root)\">" +
           "<img src=\"'+STR_ICONPATH+'prev_year.gif\" " +
           "width=\"16\" height=\"16\" border=\"0\" alt=\"" +
           PREV_YEAR +
           "\"/></a>&#160;'" +
        ":" +
            // second alternative, a blank
            "'')+" +
        "'<a href=\"javascript:set_datetime_update(" +
        "'+dt_prev_month.valueOf()+', root)\">" +
        "<img src=\"'+STR_ICONPATH+'prev.gif\" " +
        "width=\"16\" height=\"16\" border=\"0\" alt=\"" +
         PREV_MONTH +
         "\"/></a></td>'+\n");
      writer.write("'<td align=\"center\" width=\"100%\">" +
         "<font color=\"#ffffff\">'+" +
         "ARR_MONTHS[dt_current.getMonth()]+' '+" +
         "dt_current.getFullYear() + '</font></td>'+\n");
      writer.write("'<td><a href=\"javascript:set_datetime_update('" +
       "+dt_next_month.valueOf()+', root)\">" +
       "<img src=\"'+STR_ICONPATH+'next.gif\" " +
       "width=\"16\" height=\"16\" border=\"0\" alt=\"" +
       NEXT_MONTH +
       "\"/></a>'+" + // ? operator, again...
       "(obj_caller && obj_caller.year_scroll?" +
           //first alternative
           "'&#160;<a href=\"javascript:set_datetime_update(" +
           "'+dt_next_year.valueOf()+', root)\">" +
           "<img src=\"'+STR_ICONPATH+'next_year.gif\" " +
           "width=\"16\" height=\"16\" border=\"0\" alt=\"" +
           NEXT_YEAR +
           "\"/></a>'" +
        ":" +
          //second alternative, blank
          "'')" +
        "+'</td>'\n");
      writer.write(");\n");
      writer.write("document.write('</tr>');\n");
      writer.write("document.write('</table></td></tr>');\n");
      writer.write("document.write('<tr>')\n");
      // replaced these lines with document.write so dom balanced in XHTML
//        writer.write("</script>\n");
//        writer.write("</tr>\n");
//        writer.write("</table></td></tr>\n");
//        writer.write("<tr>\n");
//        writer.write("<script type=\"text/javascript\">\n");
      writer.write("\n");
      writer.write("// print weekdays titles\n");
      writer.write("for (var n=0; n < 7; n++)\n");
      writer.write("	document.write('<td bgcolor=\"#87cefa\" align=\"center\"><font color=\"#ffffff\">'+ARR_WEEKDAYS[(NUM_WEEKSTART+n)%7]+'</font></td>');\n");
      writer.write("document.write('</tr>');\n");
      writer.write("\n");
      writer.write("// print calendar table\n");
      writer.write("var dt_current_day = new Date(dt_firstday);\n");
      writer.write("while (dt_current_day.getMonth() == dt_current.getMonth() ||\n");
      writer.write("	dt_current_day.getMonth() == dt_firstday.getMonth()) {\n");
      writer.write("	// print row heder\n");
      writer.write("	document.write('<tr>');\n");
      writer.write("	for (var n_current_wday=0; n_current_wday < 7; n_current_wday++) {\n");
      writer.write("		if (dt_current_day.getDate() == dt_current.getDate()  && \n");
      writer.write("			dt_current_day.getMonth() == dt_current.getMonth())\n");
      writer.write("			// print current date\n");
      writer.write("			document.write('<td bgcolor=\"#ffb6c1\" align=\"center\" width=\"14%\">');\n");
      writer.write("		else if (dt_current_day.getDay() == 0 || dt_current_day.getDay() == 6)\n");
      writer.write("			// weekend days\n");
      writer.write("			document.write('<td bgcolor=\"#dbeaf5\" align=\"center\" width=\"14%\">');\n");
      writer.write("		else\n");
      writer.write("			// print working days of current month\n");
      writer.write("			document.write('<td bgcolor=\"#ffffff\" align=\"center\" width=\"14%\">');\n");
      writer.write("\n");
      writer.write("		document.write('<a href=\"javascript:set_datetime('+dt_current_day.valueOf() +', true);\">');\n");
      writer.write("\n");
      writer.write("		if (dt_current_day.getMonth() == this.dt_current.getMonth())\n");
      writer.write("			// print days of current month\n");
      writer.write("			document.write('<font color=\"#000000\">');\n");
      writer.write("		else\n");
      writer.write("			// print days of other months\n");
      writer.write("			document.write('<font color=\"#606060\">');\n");
      writer.write("\n");
      writer.write("		document.write(dt_current_day.getDate()+'</font></font></a></td>');\n");
      writer.write("		dt_current_day.setDate(dt_current_day.getDate()+1);\n");
      writer.write("	}\n");
      writer.write("	// print row footer\n");
      writer.write("	document.write('</td></td></tr>');\n");
      writer.write("}\n");
      writer.write("if (obj_caller  &&  obj_caller.time_comp)\n");
      writer.write("	document.write('<form onsubmit=\"javascript:set_datetime('+dt_current.valueOf()+', true)\" name=\"cal\"><tr><td colspan=\"7\" bgcolor=\"#87CEFA\"><font color=\"White\" face=\"tahoma, verdana\" size=\"2\">Time: <input type=\"text\" name=\"time\" value=\"'+obj_caller.gen_time(this.dt_current)+'\" size=\"8\" maxlength=\"8\"/></font></td></tr></form>');\n");
      writer.write("document.write('</table></td></tr></table>');\n");
//      writer.write("//]]> \n");
      writer.write("</script>\n");
      // replaced these lines with document.write so DOM balanced in XHTML
//        writer.write("</tr></td>\n");
//        writer.write("</table></tr></td>\n");
//        writer.write("</table>\n");
      writer.write("</body>\n");
      writer.write("</html>\n");
      writer.write("\n");
  }
}
