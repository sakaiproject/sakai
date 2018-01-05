/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-app/src/java/org/sakaiproject/tool/messageforums/ui/DiscussionAreaBean.java $
 * $Id: DiscussionAreaBean.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.tool.messageforums.ui;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.faces.context.FacesContext;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.UserPreferencesManager;
import org.sakaiproject.component.cover.ComponentManager;

/**
 * 
 * @author <a href="mailto:wagnermr@iupui.edu">Michelle Wagner</a>
 *
 */
@Slf4j
public class DiscussionAreaBean 
{
	 private static UserPreferencesManager userPreferencesManager = ComponentManager.get(UserPreferencesManager.class);

	 private Area area;
	 private int numPendingMsgs;
	 private SimpleDateFormat datetimeFormat = ourDateFormat();

	 private SimpleDateFormat ourDateFormat() {
	     SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	     df.setTimeZone(userPreferencesManager.getTimeZone());
	     return df;
	 }

	 public DiscussionAreaBean(Area area)
	 {
	    if(log.isDebugEnabled())
	    {
	      log.debug("DiscussionAreaBean(DiscussionArea " + area + ")");
	    }
	    
		this.area = area;
	 }
	 
	 /**
	   * Returns whether the forum is moderated or not
	   * @return
	   */
	  public String getModerated()
	  {
		  log.debug("getModerated()");
		  if (area == null || area.getModerated() == null || 
			  area.getModerated().booleanValue() == false)
		  {
			  return Boolean.FALSE.toString();
		  }

		  return Boolean.TRUE.toString();
	  }
	  
	  /**
	   * Set the "moderated" setting for the forum
	   * @param moderated
	   */
	  public void setModerated(String moderated)
	  {
		  log.debug("setModerated()");
		  if (moderated.equals(Boolean.TRUE.toString()))
		  {
			  area.setModerated(Boolean.valueOf(true));
		  }
		  else
		  {
			  area.setModerated(Boolean.valueOf(false));
		  }
	  }
	  
	  /**
	   * Returns whether the forum is postFirst or not
	   * @return
	   */
	  public String getPostFirst()
	  {
		  log.debug("getPostFirst()");
		  if (area == null || area.getPostFirst() == null || 
			  area.getPostFirst().booleanValue() == false)
		  {
			  return Boolean.FALSE.toString();
		  }

		  return Boolean.TRUE.toString();
	  }
	  
	  /**
	   * Set the "postFirst" setting for the forum
	   * @param postFirst
	   */
	  public void setPostFirst(String postFirst)
	  {
		  log.debug("setPostFirst()");
		  if (postFirst.equals(Boolean.TRUE.toString()))
		  {
			  area.setPostFirst(Boolean.valueOf(true));
		  }
		  else
		  {
			  area.setPostFirst(Boolean.valueOf(false));
		  }
	  }
	  
	  /**
	   * Returns whether or not the forum automatically marks messages in topics as read.
	   */
	  public String getAutoMarkThreadsRead() {
		  log.debug("getAutoMarkThreadsRead()");
		  if (area == null || area.getAutoMarkThreadsRead() == null || !area.getAutoMarkThreadsRead())
		  {
			  return Boolean.FALSE.toString();
		  }
		  else
		  {
			  return Boolean.TRUE.toString();
		  }
	  }
	  
	  /**
	   * Set the automatically mark topics as read value on the forum.
	   */
	  public void setAutoMarkThreadsRead(String autoMarkThreadsRead) {
		  log.debug("setMarkThreadsRead(String)");
		  if (autoMarkThreadsRead.equals(Boolean.TRUE.toString()))
		  {
			  area.setAutoMarkThreadsRead(Boolean.valueOf(true));
		  }
		  else
		  {
			  area.setAutoMarkThreadsRead(Boolean.valueOf(false));
		  }
	  }
	  
	  /**
	   * 
	   * @return
	   */
	  public Area getArea()
	  {
		  return area;
	  }
	  
	  /**
	   * returns boolean moderated status for area
	   * @return
	   */
	  public boolean isAreaModerated()
	  {
		  return area.getModerated().booleanValue();
	  }
	  
	  /**
	   * Returns number of msgs pending in moderated topics in which
	   * user has moderate perm
	   * @return
	   */
	  public int getNumPendingMsgs()
	  {
		  return numPendingMsgs;
	  }
	  
	  /**
	   * Set num of pending msgs in area
	   *
	   */
	  public void setNumPendingMsgs(int numPendingMsgs)
	  {
		  this.numPendingMsgs = numPendingMsgs;
	  }
	  
	  public String getAvailabilityRestricted()
	  {
		  log.debug("getAvailabilityRestricted()");
		  if (area == null || area.getAvailabilityRestricted() == null || 
			  area.getAvailabilityRestricted().booleanValue() == false)
		  {
			  return Boolean.FALSE.toString();
		  }

		  return Boolean.TRUE.toString();
	  }
	  
	  /**
	   * Set the "availabilityRestricted" setting for the forum
	   * @param restricted
	   */
	  public void setAvailabilityRestricted(String restricted)
	  {
		  log.debug("setAvailabilityRestricted()");
		  if (restricted.equals(Boolean.TRUE.toString()))
		  {
			  area.setAvailabilityRestricted(Boolean.valueOf(true));
		  }
		  else
		  {
			  area.setAvailabilityRestricted(Boolean.valueOf(false));
		  }
	  }
	  
	  public String getAvailability()
	  {
		  log.debug("getAvailability()");
		  if (area == null || area.getAvailability() == null || 
			  area.getAvailability().booleanValue() == false)
		  {
			  return Boolean.FALSE.toString();
		  }

		  return Boolean.TRUE.toString();
	  }
	  
	  /**
	   * Set the "Availability" setting for the area
	   * @param restricted
	   */
	  public void setAvailability(String restricted)
	  {
		  log.debug("setAvailability()");
		  if (restricted.equals(Boolean.TRUE.toString()))
		  {
			  area.setAvailability(Boolean.valueOf(true));
		  }
		  else
		  {
			  area.setAvailability(Boolean.valueOf(false));
		  }
	  }

	  
	  public String getOpenDate(){
		  if(area == null || area.getOpenDate() == null){
			  return "";
		  }else{
			  StringBuilder dateTimeOpenDate = new StringBuilder( datetimeFormat.format( area.getOpenDate() ) );			
			  return dateTimeOpenDate.toString();
		  }
	  }	  
	  
	  public void setOpenDate(String openDateStr){
		  if (StringUtils.isNotBlank(openDateStr)) {
			  try{
				  String hiddenOpenDate = (String)FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("openDateISO8601");
				  Date openDate = (Date) datetimeFormat.parse(hiddenOpenDate);				
				  area.setOpenDate(openDate);
			  }catch (ParseException e) {
				  log.error("Couldn't convert open date", e);
			}
		  }else{
			  area.setOpenDate(null);
		  }
	  }
	  
	  public String getCloseDate(){
		  if(area == null || area.getCloseDate() == null){
			  return "";
		  }else{
			  StringBuilder dateTimeCloseDate = new StringBuilder( datetimeFormat.format( area.getCloseDate() ) );
			  return dateTimeCloseDate.toString();
		  }
	  }	  
	  
	  public void setCloseDate(String closeDateStr){
		  if (StringUtils.isNotBlank(closeDateStr)) {
			  try{
				  String hiddenCloseDate = (String)FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("closeDateISO8601");
				  Date CloseDate = (Date) datetimeFormat.parse(hiddenCloseDate);
				  area.setCloseDate(CloseDate);
			  }catch (ParseException e) {
				  log.error("Couldn't convert Close date", e);
			}
		  }else{
			  area.setCloseDate(null);
		  }
	  }
}
