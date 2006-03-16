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

package org.sakaiproject.calendar.tool;

//import java.util.Enumeration;
import java.util.Hashtable;


public class CalendarDB {
    

  private Hashtable events;
  private static final String[] time = {"8:00 am", "9:00 am", "10:00 am", "11:00 am", "12:00 pm", 
					"1:00 pm", "2:00 pm", "3:00 pm", "4:00 pm", "5:00 pm", "6:00 pm",
					"7:00 pm", "8:00 pm" };
  public static final int rows = 12;
  
  
// Event class  
  
  public class Event {

  String hour;
  String description;


  public Event (String hourx) {
    hour = hourx;
    description = "";

  }

  public String getHour () {
    return hour;
  }


  public String getDescription () {
    if (description.equals("")) return "";
    else return description;
  }

  public void setDescription (String descr) {
    description = descr;
  }
 
}

// End of event class 

  public CalendarDB () {   
   events = new Hashtable (rows);
   for (int i=0; i < rows; i++) {
     events.put (time[i], new Event(time[i]));
   }
  }

  public int getRows () {
    return rows;
  }

  public Event getEvent (int index) {
    return (Event)events.get(time[index]);
  }

  public int getIndex (String tm) {
    for (int i=0; i<rows; i++)
      if(tm.equals(time[i])) return i;
    return -1;
  }

  public void addEvent (String tm, String desc) {
    int index = getIndex (tm);
    if (index >= 0) {
      String descr = desc;
      Event e = (Event)events.get(time[index]);
      e.setDescription (descr);
    }
  }

}



