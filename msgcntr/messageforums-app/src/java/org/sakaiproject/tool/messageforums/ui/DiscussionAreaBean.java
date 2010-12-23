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
 *       http://www.osedu.org/licenses/ECL-2.0
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.api.app.messageforums.Area;

/**
 * 
 * @author <a href="mailto:wagnermr@iupui.edu">Michelle Wagner</a>
 *
 */
public class DiscussionAreaBean 
{
	 private static final Log LOG = LogFactory.getLog(DiscussionAreaBean.class);
	 
	 private Area area;
	 private int numPendingMsgs;
	 private SimpleDateFormat datetimeFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
	 
	 public DiscussionAreaBean(Area area)
	 {
	    if(LOG.isDebugEnabled())
	    {
	      LOG.debug("DiscussionAreaBean(DiscussionArea " + area + ")");
	    }
	    
		this.area = area;
	 }
	 
	 /**
	   * Returns whether the forum is moderated or not
	   * @return
	   */
	  public String getModerated()
	  {
		  LOG.debug("getModerated()");
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
		  LOG.debug("setModerated()");
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
		  LOG.debug("getPostFirst()");
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
		  LOG.debug("setPostFirst()");
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
		  LOG.debug("getAutoMarkThreadsRead()");
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
		  LOG.debug("setMarkThreadsRead(String)");
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
		  LOG.debug("getAvailabilityRestricted()");
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
		  LOG.debug("setAvailabilityRestricted()");
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
		  LOG.debug("getAvailability()");
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
		  LOG.debug("setAvailability()");
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
		  if(!"".equals(openDateStr) && openDateStr != null){
			  try{
				  Date openDate = (Date) datetimeFormat.parse(openDateStr);
				  area.setOpenDate(openDate);
			  }catch (ParseException e) {
				  LOG.error("Couldn't convert open date", e);
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
		  if(!"".equals(closeDateStr) && closeDateStr != null){
			  try{
				  Date CloseDate = (Date) datetimeFormat.parse(closeDateStr);
				  area.setCloseDate(CloseDate);
			  }catch (ParseException e) {
				  LOG.error("Couldn't convert Close date", e);
			}
		  }else{
			  area.setCloseDate(null);
		  }
	  }
}
