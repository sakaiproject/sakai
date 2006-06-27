/**********************************************************************************
 *
 * Copyright (c) 2006 Universidade Fernando Pessoa
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
package org.sakaiproject.sitestats.tool.jsf;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.servlet.http.HttpServletRequest;


public class VBarChartComponent extends UIComponentBase {
	//private static Log	LOG							= LogFactory.getLog(VBarChartComponent.class);
	ResourceBundle		msgs						= ResourceBundle.getBundle("org.sakaiproject.sitestats.tool.bundle.Messages");

	private String[]	weekDays					= { "day_sun", "day_mon", "day_tue", "day_wed", "day_thu", "day_fri", "day_sat" };
	private String[]	months						= { "mo_jan", "mo_feb", "mo_mar", "mo_apr", "mo_may", "mo_jun", "mo_jul", "mo_ago", "mo_sep", "mo_oct", "mo_nov", "mo_dec" };
	private String		type;
	private List		column1;
	private List		column2;
	private List		column3;
	private Date		lastDate;
	private Calendar	lastDateCal						= Calendar.getInstance();

	private String		col1Image					= "vu1.png";
	private String		col2Image					= "vu2.png";
	private String		col3Image					= "vu3.png";

	//private int			realMaximumInnerAreaHeight	= 127;
	private int			maximumInnerAreaHeight		= 105;
	private String		weekColor					= "#EAEAEA";
	private String		weekEndColor				= "#AAAAAA";

	public void encodeBegin(FacesContext context) throws IOException {
		ResponseWriter writer = context.getResponseWriter();
		type = (String) getAttributes().get("type");
		lastDate = new Date(((Long) getAttributes().get("lastDate")).longValue());
		lastDateCal.setTime(lastDate);
		column1 = (List) getAttributes().get("column1");
		column2 = (List) getAttributes().get("column2");
		column3 = (List) getAttributes().get("column3");
		
		if(type.equals("week")){
			encodeWeekChart(writer);
		}else if(type.equals("month")){
			encodeMonthChart(writer);
		}else if(type.equals("year")){
			encodeYearChart(writer);
		}
	}

	// awstats like
	private void encodeWeekChart(ResponseWriter writer) throws IOException {
		int imgBarWidth = 12;
		String mainDiv = "div id=\"vbartbl\"";
		String table = "table width=\"340px\"";
		String tr = "tr valign=\"bottom\"";
		String td = "td valign=\"bottom\" height=\""+maximumInnerAreaHeight+"\"";
		String br = "br";
		String idiv = "div id=\"ivbartbl\"";
		String center = "center";
		String itable = "table cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"0\"";
		String itr = "tr id=\"ivbartbl\" valign=\"bottom\"";
		String itd = "td id=\"ivbartbl\" valign=\"bottom\"";


		writer.startElement("div style=\"width: 340px\"", this);
		writer.startElement(mainDiv, this);
		writer.startElement(table, this);

		int max = getMaximumValue();
		int visitsSize = column1.size();
		int activitySize = column2.size();
		int col3Size = column3.size();
		// <tr>: Data
		writer.startElement(tr, this);
		for(int i = 0; i < 7; i++){
			writer.startElement(td, this);
			writer.startElement(idiv, this);
			writer.startElement(center, this);
			writer.startElement(itable, this);
			writer.startElement(itr, this);

			// column1
			Integer nV = new Integer(0);
			writer.startElement(itd, this);
			if(i < visitsSize)
				nV = (Integer) (column1.get(i));
			int height = max == 0 ? 1 : (maximumInnerAreaHeight * nV.intValue()) / max;
			height = height == 0 ? 1 : height;
			String tooltip = msgs.getString("legend_visits")+": " + nV.toString();
			if(nV.intValue() > 0){
				writer.writeText(nV.toString(), null);
				writer.startElement(br, this);
				writer.endElement(br);
			}
			String element = "img align=\"bottom\" src=\"sitestats/images/" + col1Image + "\" height=\"" + height + "\" width=\"" + imgBarWidth + "\" alt='" + tooltip + "' title='" + tooltip + "'";
			writer.startElement(element, this);
			writer.endElement(element);
			writer.endElement(itd);

			// column2
			Integer nA = new Integer(0);
			writer.startElement(itd, this);
			if(i < activitySize)
				nA = (Integer) (column2.get(i));
			height = max == 0 ? 1 : (maximumInnerAreaHeight * nA.intValue()) / max;
			height = height == 0 ? 1 : height;
			tooltip = msgs.getString("legend_activity")+": " + nA.toString();
			if(nA.intValue() > 0){
				writer.writeText(nA.toString(), null);
				writer.startElement(br, this);
				writer.endElement(br);
			}
			element = "img align=\"bottom\" src=\"sitestats/images/" + col2Image + "\" height=\"" + height + "\" width=\"" + imgBarWidth + "\" alt='" + tooltip + "' title='" + tooltip + "'";
			writer.startElement(element, this);
			writer.endElement(element);
			writer.endElement(itd);

			// column3
			Integer n3 = new Integer(0);
			writer.startElement(itd, this);
			if(i < col3Size)
				n3 = (Integer) (column3.get(i));
			height = max == 0 ? 1 : (maximumInnerAreaHeight * n3.intValue()) / max;
			height = height == 0 ? 1 : height;
			tooltip = msgs.getString("legend_unique_visitors")+": " + n3.toString();
			if(n3.intValue() > 0){
				writer.writeText(n3.toString(), null);
				writer.startElement(br, this);
				writer.endElement(br);
			}
			element = "img align=\"bottom\" src=\"sitestats/images/" + col3Image + "\" height=\"" + height + "\" width=\"" + imgBarWidth + "\" alt='" + tooltip + "' title='" + tooltip + "'";
			writer.startElement(element, this);
			writer.endElement(element);
			writer.endElement(itd);

			writer.endElement(itr);
			writer.endElement(itable);
			writer.endElement(center);
			writer.endElement(idiv);
			writer.endElement(td);
		}
		writer.endElement(tr);

		// <tr>: Labels
		String td_week = "td bgcolor=\"" + weekColor + "\" valign=\"middle\" align=\"center\" height=\"10px\"";
		String td_weekend = "td bgcolor=\"" + weekEndColor + "\" valign=\"middle\" align=\"center\" height=\"10px\"";
		writer.startElement(tr, this);
		Calendar cal = (Calendar) lastDateCal.clone();
		cal.add(Calendar.DATE, -6);
		for(int i = 0; i < 7; i++){
			int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
			boolean isWeekEnd = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
			writer.startElement(isWeekEnd ? td_weekend : td_week, this);
			writer.writeText(msgs.getString(weekDays[dayOfWeek-1]), null);
			writer.endElement(isWeekEnd ? td_weekend : td_week);
			cal.add(Calendar.DATE, 1);
		}
		writer.endElement(tr);

		writer.endElement(table);
		writer.endElement(mainDiv);

		// <table>: Legend
		//encodeWeekChartLegend(writer, weekEndColor);
		encodeMonthYearChartLegend(writer, weekEndColor);
		writer.endElement("div style=\"width: 340px\"");
	}

	private void encodeMonthChart(ResponseWriter writer) throws IOException {
		int imgBarWidth = 2;
		String mainDiv = "div id=\"vbartbl_mo\"";
		String table = "table width=\"340px\"";
		String tr = "tr valign=\"bottom\"";
		String td = "td valign=\"bottom\" height=\""+maximumInnerAreaHeight+"\"";
		String idiv = "div id=\"ivbartbl\"";
		String center = "center";

		writer.startElement("div style=\"width: 340px\"", this);
		writer.startElement(mainDiv, this);
		writer.startElement(table, this);

		int max = getMaximumValue();
		int visitsSize = column1.size();
		int activitySize = column2.size();
		int col3Size = column3.size();
		// <tr>: Data
		writer.startElement(tr, this);
		for(int i = 0; i < 30; i++){
			writer.startElement(td, this);
			writer.startElement(idiv, this);
			writer.startElement(center, this);

			// column1
			Integer nV = new Integer(0);
			if(i < visitsSize)
				nV = (Integer) (column1.get(i));
			int height = max == 0 ? 1 : (maximumInnerAreaHeight * nV.intValue()) / max;
			height = height == 0 ? 1 : height;
			String tooltip = msgs.getString("legend_visits")+": " + nV.toString();
			String element = "img align=\"bottom\" src=\"sitestats/images/" + col1Image + "\" height=\"" + height + "\" width=\"" + imgBarWidth + "\" alt='" + tooltip + "' title='" + tooltip + "'";
			writer.startElement(element, this);
			writer.endElement(element);

			// column2
			Integer nA = new Integer(0);
			if(i < activitySize)
				nA = (Integer) (column2.get(i));
			height = max == 0 ? 1 : (maximumInnerAreaHeight * nA.intValue()) / max;
			height = height == 0 ? 1 : height;
			tooltip = msgs.getString("legend_activity")+": " + nA.toString();
			element = "img align=\"bottom\" src=\"sitestats/images/" + col2Image + "\" height=\"" + height + "\" width=\"" + imgBarWidth + "\" alt='" + tooltip + "' title='" + tooltip + "'";
			writer.startElement(element, this);
			writer.endElement(element);
			
			// column3
			Integer n3 = new Integer(0);
			if(i < col3Size)
				n3 = (Integer) (column3.get(i));
			height = max == 0 ? 1 : (maximumInnerAreaHeight * n3.intValue()) / max;
			height = height == 0 ? 1 : height;
			tooltip = msgs.getString("legend_unique_visitors")+": " + n3.toString();
			element = "img align=\"bottom\" src=\"sitestats/images/" + col3Image + "\" height=\"" + height + "\" width=\"" + imgBarWidth + "\" alt='" + tooltip + "' title='" + tooltip + "'";
			writer.startElement(element, this);
			writer.endElement(element);
			
			writer.endElement(center);
			writer.endElement(idiv);
			writer.endElement(td);
		}
		// right ruler
		drawRuler(writer, td, max);		
		writer.endElement(tr);

		// <tr>: Labels
		String td_week = "td bgcolor=\"" + weekColor + "\" valign=\"middle\" align=\"center\" height=\"10px\"";
		String td_weekend = "td bgcolor=\"" + weekEndColor + "\" valign=\"middle\" align=\"center\" height=\"10px\"";
		writer.startElement(tr, this);
		Calendar cal = (Calendar) lastDateCal.clone();
		cal.add(Calendar.DATE, -29);
		for(int i = 0; i < 30; i++){
			int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
			boolean isWeekEnd =  dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
			writer.startElement(isWeekEnd ? td_weekend : td_week, this);
			writer.writeText((cal.get(Calendar.DAY_OF_MONTH))+"", null);
			writer.endElement(isWeekEnd ? td_weekend : td_week);
			cal.add(Calendar.DATE, 1);
		}
		// right ruler label
		writer.startElement(td_week, this);
		writer.writeText(" ", null);
		writer.endElement(td_week);
		writer.endElement(tr);
		

		writer.endElement(table);
		writer.endElement(mainDiv);

		// <table>: Legend
		encodeMonthYearChartLegend(writer, weekEndColor);
		writer.endElement("div style=\"width: 340px\"");
	}

	private void encodeYearChart(ResponseWriter writer) throws IOException {
		int imgBarWidth = 4;
		String mainDiv = "div id=\"vbartbl_yr\"";
		String table = "table width=\"340px\"";
		String tr = "tr valign=\"bottom\"";
		String td = "td valign=\"bottom\" height=\""+maximumInnerAreaHeight+"\"";
		String idiv = "div id=\"ivbartbl\"";
		String center = "center";

		writer.startElement("div style=\"width: 340px\"", this);
		writer.startElement(mainDiv, this);
		writer.startElement(table, this);

		int max = getMaximumValue();
		int visitsSize = column1.size();
		int activitySize = column2.size();
		int col3Size = column3.size();
		// <tr>: Data
		writer.startElement(tr, this);
		for(int i = 0; i < 12; i++){
			writer.startElement(td, this);
			writer.startElement(idiv, this);
			writer.startElement(center, this);

			// column1
			Integer nV = new Integer(0);
			if(i < visitsSize)
				nV = (Integer) (column1.get(i));
			int height = max == 0 ? 1 : (maximumInnerAreaHeight * nV.intValue()) / max;
			height = height == 0 ? 1 : height;
			String tooltip = msgs.getString("legend_visits")+": " + nV.toString();
			String element = "img align=\"bottom\" src=\"sitestats/images/" + col1Image + "\" height=\"" + height + "\" width=\"" + imgBarWidth + "\" alt='" + tooltip + "' title='" + tooltip + "'";
			writer.startElement(element, this);
			writer.endElement(element);

			// column2
			Integer nA = new Integer(0);
			if(i < activitySize)
				nA = (Integer) (column2.get(i));
			height = max == 0 ? 1 : (maximumInnerAreaHeight * nA.intValue()) / max;
			height = height == 0 ? 1 : height;
			tooltip = msgs.getString("legend_activity")+": " + nA.toString();
			element = "img align=\"bottom\" src=\"sitestats/images/" + col2Image + "\" height=\"" + height + "\" width=\"" + imgBarWidth + "\" alt='" + tooltip + "' title='" + tooltip + "'";
			writer.startElement(element, this);
			writer.endElement(element);
			
			// column3
			Integer n3 = new Integer(0);
			if(i < col3Size)
				n3 = (Integer) (column3.get(i));
			height = max == 0 ? 1 : (maximumInnerAreaHeight * n3.intValue()) / max;
			height = height == 0 ? 1 : height;
			tooltip = msgs.getString("legend_unique_visitors")+": " + n3.toString();
			element = "img align=\"bottom\" src=\"sitestats/images/" + col3Image + "\" height=\"" + height + "\" width=\"" + imgBarWidth + "\" alt='" + tooltip + "' title='" + tooltip + "'";
			writer.startElement(element, this);
			writer.endElement(element);
			
			writer.endElement(center);
			writer.endElement(idiv);
			writer.endElement(td);
		}
		// right ruler
		drawRuler(writer, td, max);
		writer.endElement(tr);

		// <tr>: Labels
		String td_week = "td bgcolor=\"" + weekColor + "\" valign=\"middle\" align=\"center\" height=\"10px\"";
		String td_weekend = "td bgcolor=\"" + weekEndColor + "\" valign=\"middle\" align=\"center\" height=\"10px\"";
		writer.startElement(tr, this);
		Calendar cal = (Calendar) lastDateCal.clone();
		cal.add(Calendar.MONTH, -11);
		for(int i = 0; i < 12; i++){
			boolean isWeekEnd =  false;
			int month = cal.get(Calendar.MONTH);
			writer.startElement(isWeekEnd ? td_weekend : td_week, this);
			writer.writeText(msgs.getString(months[month]), null);
			writer.endElement(isWeekEnd ? td_weekend : td_week);
			cal.add(Calendar.MONTH, 1);
		}
		// right ruler label
		writer.startElement(td_week, this);
		writer.writeText(" ", null);
		writer.endElement(td_week);
		writer.endElement(tr);

		writer.endElement(table);
		writer.endElement(mainDiv);

		// <table>: Legend
		encodeMonthYearChartLegend(writer, weekEndColor);
		writer.endElement("div style=\"width: 340px\"");
	}

//	private void encodeWeekChartLegend(ResponseWriter writer, String borderColor) throws IOException {
//		String table = "table id=\"vbartbl\" width=\"100%\"";
//		String trS = "tr height=\"5px\"";
//		String trL1 = "tr";
//		String trL2 = "tr height=\"12px\"";
//		String tdLeft = "td style=\"text-align: left; white-space: nowrap; font-weight: bold; width: 50%;\"";
//		String tdRight = "td style=\"text-align: right; white-space: nowrap; font-weight: bold; width: 50%;\"";
//		String tdLegend = "td style=\"width: 100%\" colspan=\"2\"";
//
//		// calc dates if weekNumber != null
//		String iDate = new String();
//		String fDate = new String();
//		Calendar c = (Calendar) lastDateCal.clone();
//		// today
//		fDate = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH);
//		// 6 days ago
//		c.add(Calendar.DATE, -6);
//		iDate = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH);
//		
//
//		writer.startElement(table, this);
//
//		// tr: spacer line
//		writer.startElement(trS, this);
//		writer.startElement(tdLeft, this);
//		writer.endElement(tdLeft);
//		writer.startElement(tdRight, this);
//		writer.endElement(tdRight);
//		writer.endElement(trS);
//
//		
//		// tr: LINE1: START
//		writer.startElement(trL1, this);
//		// left lastDate
//		writer.startElement(tdLeft, this);
//		writer.writeText(iDate, null);
//		writer.endElement(tdLeft);
//		// right lastDate
//		writer.startElement(tdRight, this);
//		writer.writeText(fDate, null);
//		writer.endElement(tdRight);
//		// tr: LINE1 END
//		writer.endElement(trL1);
//		
//		
//
//		// tr: LINE2: START
//		writer.startElement(trL2, this);
//		writer.startElement(tdLegend, this);
//
//		// legend: column1
//		String img = "img src=\"sitestats/images/" + col1Image + "\" height=\"8\" width=\"12\" style=\"border: 1px solid " + borderColor + "; padding: 1px;\"";
//		writer.startElement(img, this);
//		writer.endElement(img);
//		writer.writeText(" " + msgs.getString("legend_visits"), null);
//
//		writer.write("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
//		
//		// legend: column2
//		img = "img src=\"sitestats/images/" + col2Image + "\" height=\"8\" width=\"12\" style=\"border: 1px solid " + borderColor + "; padding: 1px;\"";
//		writer.startElement(img, this);
//		writer.endElement(img);
//		writer.writeText(" " + msgs.getString("legend_activity"), null);
//		
//		writer.write("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
//
//		// legend: column3
//		img = "img src=\"sitestats/images/" + col3Image + "\" height=\"8\" width=\"12\" style=\"border: 1px solid " + borderColor + "; padding: 1px;\"";
//		writer.startElement(img, this);
//		writer.endElement(img);
//		writer.writeText(" " + msgs.getString("legend_unique_visitors"), null);
//
//		// tr: LINE2 END
//		writer.endElement(tdLegend);
//		writer.endElement(trL2);
//		
//
//		writer.endElement(table);
//	}

	private void encodeMonthYearChartLegend(ResponseWriter writer, String borderColor) throws IOException {
		String table = "table id=\"vbartbl_mo\" width=\"100%\"";
		String trS = "tr height=\"5px\"";
		String trL2 = "tr height=\"12px\"";
		String tdLeft = "td style=\"text-align: left; white-space: nowrap; font-weight: bold; width: 50%;\"";
		String tdRight = "td style=\"text-align: right; white-space: nowrap; font-weight: bold; width: 50%;\"";
		String tdLegend = "td style=\"width: 100%; font: 11px verdana, arial, helvetica, sans-serif;\" colspan=\"2\"";

		writer.startElement(table, this);

		// tr: spacer line
		writer.startElement(trS, this);
		writer.startElement(tdLeft, this);
		writer.endElement(tdLeft);
		writer.startElement(tdRight, this);
		writer.endElement(tdRight);
		writer.endElement(trS);
		
		// tr: LINE2: START
		writer.startElement(trL2, this);
		writer.startElement(tdLegend, this);

		// legend: column1
		String img = "img src=\"sitestats/images/" + col1Image + "\" height=\"8\" width=\"12\" style=\"border: 1px solid " + borderColor + "; padding: 1px;\"";
		writer.startElement(img, this);
		writer.endElement(img);
		writer.writeText(" " + msgs.getString("legend_visits"), null);

		writer.write("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		
		// legend: column2
		img = "img src=\"sitestats/images/" + col2Image + "\" height=\"8\" width=\"12\" style=\"border: 1px solid " + borderColor + "; padding: 1px;\"";
		writer.startElement(img, this);
		writer.endElement(img);
		writer.writeText(" " + msgs.getString("legend_activity"), null);
		
		writer.write("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");

		// legend: column3
		img = "img src=\"sitestats/images/" + col3Image + "\" height=\"8\" width=\"12\" style=\"border: 1px solid " + borderColor + "; padding: 1px;\"";
		writer.startElement(img, this);
		writer.endElement(img);
		writer.writeText(" " + msgs.getString("legend_unique_visitors"), null);

		// tr: LINE2 END
		writer.endElement(tdLegend);
		writer.endElement(trL2);
		

		writer.endElement(table);
	}

	private void drawRuler(ResponseWriter writer, String td, int max) throws IOException {
		String ldiv = "div class=\"legvbartbl\"";
		String p1 = "p style=\"height:25px\"";
		String p2 = "p style=\"height:25px\"";
		String p3 = "p style=\"height:20px\"";
		
		String browser = getBrowser();
		if(containsNoCaseSensitive(browser, "msie")){
			p1 = "p style=\"height:14px\"";
			p2 = "p style=\"height:14px\"";
			p3 = "p style=\"height:27px\"";
		}
		
		writer.startElement(td, this);
		writer.startElement(ldiv, this);
		boolean isSmallScale = max < 21; 
		int decimalPlaces = isSmallScale ? 1 : 0;
		double l1 = round(((double)max * (97.0)) / (double)maximumInnerAreaHeight, decimalPlaces); //92
		double l2 = round(((double)max * (63.0)) / (double)maximumInnerAreaHeight, decimalPlaces); //58
		double l3 = round(((double)max * (32.0)) / (double)maximumInnerAreaHeight, decimalPlaces); //27
		// line 1
		writer.startElement(p1, this);
		writer.writeText(isSmallScale ? Double.toString(l1) : Integer.toString((int)l1), null);
		writer.endElement(p1);
		// line 2
		writer.startElement(p2, this);
		writer.writeText(isSmallScale ? Double.toString(l2) : Integer.toString((int)l2), null);
		writer.endElement(p2);
		// line 3
		writer.startElement(p3, this);
		writer.writeText(isSmallScale ? Double.toString(l3) : Integer.toString((int)l3), null);
		writer.endElement(p3);
		writer.endElement(ldiv);
		writer.endElement(td);
	}
	
	private int getMaximumValue() {
		int max = 1;
		Iterator v = column1.iterator();
		while (v.hasNext()){
			int val = ((Integer) v.next()).intValue();
			if(val > max) max = val;
		}
		Iterator a = column2.iterator();
		while (a.hasNext()){
			int val = ((Integer) a.next()).intValue();
			if(val > max) max = val;
		}
		return max;
	}

	public String getFamily() {
		return "SiteStatsFamily";
	}
	
	private String getBrowser() {
		FacesContext context = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
		String useragent = request.getHeader("user-agent");
		useragent = useragent.toLowerCase();
		//LOG.info("UserAgent: "+useragent);
		return useragent;
	}
	
	private boolean containsNoCaseSensitive(String string, String substring){
		String str1 = string.toLowerCase();
		String str2 = substring.toLowerCase();
		if(str1.startsWith(str2) || str1.endsWith(str2) || str1.indexOf(str2) > 0)
			return true;
		return false;
	}
	
	private static double round(double val, int places) {
		long factor = (long) Math.pow(10, places);
		// Shift the decimal the correct number of places to the right.
		val = val * factor;
		// Round to the nearest integer.
		long tmp = Math.round(val);
		// Shift the decimal the correct number of places back to the left.
		return (double) tmp / factor;
	}
}
