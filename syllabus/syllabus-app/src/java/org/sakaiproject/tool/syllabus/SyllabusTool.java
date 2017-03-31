/**********************************************************************************
 * $URL$
 * $Id$
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
package org.sakaiproject.tool.syllabus;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.api.app.syllabus.SyllabusAttachment;
import org.sakaiproject.api.app.syllabus.SyllabusData;
import org.sakaiproject.api.app.syllabus.SyllabusItem;
import org.sakaiproject.api.app.syllabus.SyllabusManager;
import org.sakaiproject.api.app.syllabus.SyllabusService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;

import com.sun.faces.util.MessageFactory;

//sakai2 - no need to import org.sakaiproject.jsf.ToolBean here as sakai does.

/**
 * @author cwen TODO To change the template for this generated type comment go to Window - Preferences -
 *         Java - Code Style - Code Templates
 */
//sakai2 - doesn't implement ToolBean as sakai does.
public class SyllabusTool
{
  private static final int MAX_REDIRECT_LENGTH = 512; // according to HBM file
  private static final int MAX_TITLE_LENGTH = 256;    // according to HBM file
  private boolean mainEdit = false;
  private static final String SESSION_ATTACHMENT_DATA_ID = "syllabysAttachDataId";
  //used for the UI to know which data ID to have opened by default (i.e. if you added/removed an attachment on the main page)
  private String openDataId;
  
  public class DecoratedSyllabusEntry
  {
    protected SyllabusData in_entry = null;
    protected String orig_title;
    protected Date orig_startDate, orig_endDate;
    protected Boolean orig_isLinkCalendar;
    protected String orig_status;
    private String draftStatus = SyllabusData.ITEM_DRAFT;
    protected boolean selected = false;
    
    protected boolean posted = false;

    protected boolean justCreated = false;
    
    protected ArrayList attachmentList = null;
    
    public DecoratedSyllabusEntry(SyllabusData en)
    {
      in_entry = en;
      //b/c of pass by reference, we need to clone the values we want to check
      //against
      this.orig_title = en.getTitle();
      this.orig_startDate = en.getStartDate() == null ? null : (Date) en.getStartDate().clone();
      this.orig_endDate = en.getEndDate() == null ? null : (Date) en.getEndDate().clone();
      this.orig_isLinkCalendar= en.isLinkCalendar();
      this.orig_status = en.getStatus();
    }

    public SyllabusData getEntry()
    {
      return in_entry;
    }

    public boolean isJustCreated()
    {
      return justCreated;
    }

    public boolean isSelected()
    {
      return selected;
    }

    public void setSelected(boolean b)
    {
      selected = b;
    }
    
    public boolean isPosted()
    {
      return SyllabusData.ITEM_POSTED.equals(getEntry().getStatus());
    }

    public void setPosted(boolean b)
    {
    	getEntry().setStatus(b ? SyllabusData.ITEM_POSTED : SyllabusData.ITEM_DRAFT);
    }

    public void setJustCreated(boolean b)
    {
      justCreated = b;
    }

    public String processListRead()
    {
      //logger.info(this + ".processListRead() in SyllabusTool.");
      
      attachments.clear();

      SyllabusData sd = syllabusManager.getSyllabusData(in_entry.getSyllabusId().toString());
      Set tempAttach = syllabusManager.getSyllabusAttachmentsForSyllabusData(sd);
      
      Iterator iter = tempAttach.iterator();
      while(iter.hasNext())
      {
        oldAttachments.add((SyllabusAttachment)iter.next());
      }
      
      allAttachments.clear();
      for(int i=0; i<oldAttachments.size(); i++)
      {
        allAttachments.add((SyllabusAttachment)oldAttachments.get(i));
      }
      
      syllabusService.readSyllabus(sd);
      
      entry = this;

      entries.clear();

      return "read";
    }

    public String processDownMove()
    {
      downOnePlace(this.getEntry());
      dontUpdateEntries = true;
      return "main_edit";
    }

    public String processUpMove()
    {
      upOnePlace(this.getEntry());
      dontUpdateEntries = true;
      return "main_edit";
    }
    
    public ArrayList getAttachmentList()
    {
    	if(attachmentList == null){
    		attachmentList = new ArrayList();
    		Set tempList = syllabusManager.getSyllabusAttachmentsForSyllabusData(in_entry);

    		Iterator iter = tempList.iterator();
    		while(iter.hasNext())
    		{
    			SyllabusAttachment sa = (SyllabusAttachment)iter.next();
    			attachmentList.add(sa);
    		}
    	}
      
      return attachmentList;
    }
    
    public void setAttachmentList(ArrayList attachmentList)
    {
      this.attachmentList = attachmentList;
    }
    public String getStatus(){
		return in_entry.getStatus();
	}
    public boolean getTitleChanged(){
    	//Title Changed?
    	if((in_entry.getTitle() == null && orig_title != null)
    		|| (in_entry.getTitle() != null && orig_title == null)
    		|| (in_entry.getTitle() != null && orig_title != null
    			&& (!in_entry.getTitle().equals(orig_title)))){
    		return true;
    	}else{
    		return false;
    	}
    }
    
    public boolean getStartTimeChanged(){
    	//Start Time
    	if((in_entry.getStartDate() == null && orig_startDate != null)
        		|| (in_entry.getStartDate() != null && orig_startDate == null)
        		|| (in_entry.getStartDate() != null && orig_startDate != null
        			&& (!in_entry.getStartDate().equals(orig_startDate)))){
    		return true;
    	}else{
    		return false;
    	}
    }
    
    public boolean getEndTimeChanged(){
    	//End Time
    	if((in_entry.getEndDate() == null && orig_endDate != null)
        		|| (in_entry.getEndDate() != null && orig_endDate == null)
        		|| (in_entry.getEndDate() != null && orig_endDate != null
        			&& (!in_entry.getEndDate().equals(orig_endDate)))){
    		return true;
    	}else{
    		return false;
    	}
    }
    
    public boolean getPostToCalendarChanged(){
    	//posted to cal:
    	if(in_entry.isLinkCalendar() != orig_isLinkCalendar){
    		return true;
    	}else{
    		return false;
    	}
    }
    
    public boolean getStatusChanged(){
    	//draft status:
    	if((in_entry.getStatus() == null && orig_status != null)
        		|| (in_entry.getStatus() != null && orig_status == null)
        		|| (in_entry.getStatus() != null && orig_status != null
        			&& (!in_entry.getStatus().equals(orig_status)))){
    		return true;
    	}else{
    		return false;
    	}
    }
    
    
    public boolean hasChanged(){
    	if(getTitleChanged() || getStartTimeChanged()
    			|| getEndTimeChanged() || getPostToCalendarChanged()
    			|| getStatusChanged()){
    		return true;
    	}else{
    		return false;
    	}
    }
    
    public String validateInput(){
    	//Title
    	if(in_entry.getTitle() == null || in_entry.getTitle().trim().equals(""))
        {
    		return MessageFactory.getMessage(FacesContext.getCurrentInstance(),
					"empty_title_validate", null).getSummary();
        }else  if(in_entry.getStartDate() != null 
        		&& in_entry.getEndDate() != null 
        		&& in_entry.getStartDate().after(in_entry.getEndDate())){
        	return MessageFactory.getMessage(FacesContext.getCurrentInstance(),
					"invalid_dates", null).getSummary();
        }
    	return "";
    }
    
    public boolean getStartAndEndDatesSameDay(){
    	if(in_entry.getStartDate() != null && in_entry.getEndDate() != null){
    		java.util.Calendar cal1 = java.util.Calendar.getInstance();
    		java.util.Calendar cal2 = java.util.Calendar.getInstance();
    		cal1.setTime(in_entry.getStartDate());
    		cal2.setTime(in_entry.getEndDate());
    		return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
    				cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR);
    	}else{
    		return false;
    	}
    }

	public String getDraftStatus() {
		return draftStatus;
	}
  }

  protected SyllabusManager syllabusManager;

  protected SyllabusItem syllabusItem;

  protected ArrayList entries;

  protected String userId;

  protected DecoratedSyllabusEntry entry = null;
  
  protected BulkSyllabusEntry bulkEntry = null;

  protected Logger logger = LoggerFactory.getLogger(SyllabusTool.class);

  protected String filename = null;

  protected String siteId = null;

  protected String editAble = null;

  protected String title = null;
  
  private boolean displayNoEntryMsg = false;
  
  private boolean displayTitleErroMsg = false;
  
  private boolean displayEvilTagMsg=false;
  
  private boolean displayDateError=false;
  
  private boolean displayCalendarError=false;
  
  private boolean dontUpdateEntries = false;
  
  private String evilTagMsg=null;
  
  private SyllabusService syllabusService;
  
  private ArrayList attachments = new ArrayList();
  
  private boolean attachCaneled = false;
  
  private String removeAttachId = null;
  
  private ArrayList oldAttachments = new ArrayList();
  
  private ArrayList allAttachments = new ArrayList();
  
  private ArrayList prepareRemoveAttach = new ArrayList();
  
  private List filePickerList;
  
  private String currentRediredUrl = null;
  
  private final String httpPrefix = "http://";
  
  private final String httpsPrefix = "https://";

  private boolean openInNewWindow = false;

  private ContentHostingService contentHostingService;
  
  private ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.syllabus.bundle.Messages");
  
  private CalendarService calendarService;
  private Boolean calendarExistsForSite = null;
  private Map<String, Boolean> calendarExistCache = new HashMap<String, Boolean>();
  
  private String alertMessage = null;
  
  public String getAlertMessage() {
	return (alertMessage == null || alertMessage.length() == 0) ? null:alertMessage;
  }
	
  public void setAlertMessage(String alertMessage) {
	  this.alertMessage = alertMessage;
  }

  protected String mobileSession = "false";
  
  public String getMobileSession()
  {
  	return mobileSession;
  }
  
  public SyllabusTool()
  {
	  Session session = SessionManager.getCurrentSession();
	  mobileSession = session.getAttribute("is_wireless_device") != null && ((Boolean) session.getAttribute("is_wireless_device")).booleanValue()?"true":"false";
  }

  public boolean getdisplayNoEntryMsg()
  {
    return this.displayNoEntryMsg;
  }

  public ArrayList getEntries() throws PermissionException
  {
    if (userId == null) userId = UserDirectoryService.getCurrentUser().getId();
    //sakai2 - use Placement to get context instead of getting currentSitePageId from PortalService in sakai.
    Placement placement = ToolManager.getCurrentPlacement();
	String currentSiteId = placement.getContext();


    if ((entries == null) || (entries.isEmpty())
        || ((currentSiteId != null) && (!currentSiteId.equals(siteId))))
    {
      //logger.info(this + ".getEntries() in SyllabusTool");

      siteId = currentSiteId;
      try
      {
        if (entries == null)
          entries = new ArrayList();
        else
          entries.clear();
        
                
        syllabusItem = getSyllabusItem();            

        if (syllabusItem != null) {
            Set tempEntries = syllabusManager
            .getSyllabiForSyllabusItem(syllabusItem);

            if (tempEntries != null)
            {
                Iterator iter = tempEntries.iterator();
                while (iter.hasNext())
                {
                    SyllabusData en = (SyllabusData) iter.next();
                    if (isAddOrEdit())
                    {
                        DecoratedSyllabusEntry den = new DecoratedSyllabusEntry(en);
                        entries.add(den);
                    }
                    else
                    {
                        if (en.getStatus().equalsIgnoreCase(SyllabusData.ITEM_POSTED))
                        {
                            DecoratedSyllabusEntry den = new DecoratedSyllabusEntry(en);
                            entries.add(den);
                        }
                    }
                }
            }
        }
      }
      catch (Exception e)
      {
        logger.info(this + ".getEntries() in SyllabusTool " + e);
        FacesContext.getCurrentInstance().addMessage(
            null,
            MessageFactory.getMessage(FacesContext.getCurrentInstance(),
                "error_general", (new Object[] { e.toString() })));
      }
    }
    else
    {
      try
      {
        siteId = currentSiteId;
        if ((userId != null) && (siteId != null))
        {
          syllabusItem = syllabusManager.getSyllabusItemByContextId(siteId);
        }

        boolean getFromDbAgain = true;
        if(dontUpdateEntries){
        	getFromDbAgain = false;
        	//reset to false:
        	dontUpdateEntries = false;
        }else{
	        for(int i=0; i<entries.size(); i++)
	        {
	          DecoratedSyllabusEntry thisDecEn = (DecoratedSyllabusEntry) entries.get(i);
	          if(thisDecEn.isSelected())
	          {
	            getFromDbAgain = false;
	            break;
	          }
	        }
        }
        
        if(getFromDbAgain)
        {
          entries.clear();
          Set tempEntries = syllabusManager
          .getSyllabiForSyllabusItem(syllabusItem);
          
          if (tempEntries != null)
          {
            Iterator iter = tempEntries.iterator();
            while (iter.hasNext())
            {
              SyllabusData en = (SyllabusData) iter.next();
              if (isAddOrEdit())
              {
                DecoratedSyllabusEntry den = new DecoratedSyllabusEntry(en);
                entries.add(den);
              }
              else
              {
                if (en.getStatus().equalsIgnoreCase(SyllabusData.ITEM_POSTED))
                {
                  DecoratedSyllabusEntry den = new DecoratedSyllabusEntry(en);
                  entries.add(den);
                }
              }
            }
          }
        }
      }
      catch (Exception e)
      {
        logger.info(this + ".getEntries() in SyllabusTool for redirection" + e);
        FacesContext.getCurrentInstance().addMessage(
            null,
            MessageFactory.getMessage(FacesContext.getCurrentInstance(),
                "error_general", (new Object[] { e.toString() })));
      }
    }
    if (entries == null || entries.isEmpty())
    {
      this.displayNoEntryMsg = true;
    }
    else
    {
      this.displayNoEntryMsg = false;
    }

    //Check if the instructor added an attachment to an item:
    //Clear out list first, then call get attachment(), which will check the 
    //session to see if the file picker selected any
    attachments = new ArrayList();
    attachments = getAttachments();
    if(attachments.size() > 0){
    	//user selected attachments, let's find which item it is for and add 
    	ToolSession session = SessionManager.getCurrentToolSession();
    	if(session.getAttribute(SESSION_ATTACHMENT_DATA_ID) != null){
    		String dataIdStr = (String) session.getAttribute(SESSION_ATTACHMENT_DATA_ID);
    		try{
    			Long dataId = Long.parseLong(dataIdStr);
    			//find data entry:
    			for(DecoratedSyllabusEntry entry : (List<DecoratedSyllabusEntry>) entries){
    				if(entry.getEntry().getSyllabusId().equals(dataId)){
    					boolean added = false;
    					for(int i=0; i<attachments.size(); i++)
    					{
    						syllabusManager.addSyllabusAttachToSyllabusData(entry.getEntry(), (SyllabusAttachment)attachments.get(i));
    						added = true;
    					}
    					//update the calendar data for this item since the attachments have chnaged:
    					if(added){
    						 //update calendar attachments
    				          if(entry.getEntry().getCalendarEventIdStartDate() != null
    				        		  && !"".equals(entry.getEntry().getCalendarEventIdStartDate())){
    				        	  syllabusManager.addCalendarAttachments(siteId, entry.getEntry().getCalendarEventIdStartDate(), attachments);
    				          }
    				          if(entry.getEntry().getCalendarEventIdEndDate() != null
    				        		  && !"".equals(entry.getEntry().getCalendarEventIdEndDate())){
    				        	  syllabusManager.addCalendarAttachments(siteId, entry.getEntry().getCalendarEventIdEndDate(), attachments);
    				          }
    					}
    					break;
    				}
    			}
    		}catch(Exception e){
    			logger.error(e.getMessage(), e);
    		}
    	}
    	session.removeAttribute(SESSION_ATTACHMENT_DATA_ID);
    }
    
    //Registramos el evento de que se ha accedido a Syllabus
    //Register the event when the syllabus is accessed
    Event event = EventTrackingService.newEvent("syllabus.read","/syllabus/"+currentSiteId+"/1", false, 0);
    EventTrackingService.post(event);
    
    return entries;
  }

  public DecoratedSyllabusEntry getEntry()
  {
    return entry;
  }

  public ArrayList getSelectedEntries()
  {
    ArrayList rv = new ArrayList();

    if ((entry != null) && (entry.isSelected()))
    {
      rv.add(entry);
    }
    else
    {
      for (int i = 0; i < entries.size(); i++)
      {
        DecoratedSyllabusEntry den = (DecoratedSyllabusEntry) entries.get(i);
        if (den.isSelected() || den.hasChanged())
        {
          rv.add(den);
        }
      }
    }
    return rv;
  }

  public SyllabusManager getSyllabusManager()
  {
    return syllabusManager;
  }

  public void setSyllabusManager(SyllabusManager syllabusManager)
  {
    this.syllabusManager = syllabusManager;
  }

  public SyllabusItem getSyllabusItem() throws PermissionException
  {
    //sakai2 - use Placement to get context instead of getting currentSitePageId from PortalService in sakai.
    Placement placement = ToolManager.getCurrentPlacement();
    String currentSiteId = placement.getContext();
    String currentUserId = UserDirectoryService.getCurrentUser().getId();

    if((syllabusItem != null) && (syllabusItem.getContextId().equals(currentSiteId))
            && (syllabusItem.getUserId().equals(currentUserId)))
    {
        return syllabusItem;
    }

    syllabusItem = syllabusManager.getSyllabusItemByContextId(currentSiteId);

    if (syllabusItem == null)
    {
        if (isAddOrEdit())
        {
            syllabusItem = syllabusManager.createSyllabusItem(currentUserId,
                    currentSiteId, null);
        }
    }

    return syllabusItem;
  }

  public String getOpenInNewWindowAsString () throws PermissionException {
	  SyllabusItem syItem = getSyllabusItem();
	  if (syItem != null) {
		return (syItem.isOpenInNewWindow()) ? "true" : null;
	  }
	  return null;
  }

  public void setSyllabusItem(SyllabusItem syllabusItem)
  {
    this.syllabusItem = syllabusItem;
  }

  public String getFilename()
  {
    //logger.info(this + ".getFilename() in SyllabusTool");
    return filename;
  }

  public void setFilename(String filename)
  {
    //logger.info(this + ".setFilename() in SyllabusTool");
    this.filename = filename;
  }

  public String getUserId()
  {
    return userId;
  }

  public void setUserId(String userId)
  {
    this.userId = userId;
  }

  public String getSiteId()
  {
	  if(siteId == null){
		  Placement placement = ToolManager.getCurrentPlacement();
		  if(placement != null){
			  siteId = placement.getContext();
		  }
	  }
    return siteId;
  }

  public void setSiteId(String siteId)
  {
    this.siteId = siteId;
  }

  /**
   * Returns the tool reset url
   */
  public String getResetUrl()
  {
	  return ServerConfigurationService.getToolUrl() + "-reset/" +
	  			ToolManager.getCurrentPlacement().getId() + "/?panel=Main";	
  }
  
  public String getSiteTitle(){
	  String siteTitle = "";
	  
	  Placement placement = ToolManager.getCurrentPlacement();
	  String currentSiteId = placement.getContext();
	  try {
		  Site site = SiteService.getSite(currentSiteId);
		  siteTitle = site.getTitle();
	  } 
	  catch (IdUnusedException e) {
		  logger.info(this + "IdUnusedException getting site title for syllabus: " + e);
	}
	  
	  return siteTitle;
  }
  
  //Convenience methods for JSF
  public boolean isAddItem() {
	  return syllabusService.checkPermission(SyllabusService.SECURE_ADD_ITEM);
  }
  
  public boolean isBulkAddItem() {
	  return syllabusService.checkPermission(SyllabusService.SECURE_BULK_ADD_ITEM);
  }
  
  public boolean isBulkEdit() {
	  return syllabusService.checkPermission(SyllabusService.SECURE_BULK_EDIT_ITEM);
  }
  
  public boolean isRedirect() {
	  return syllabusService.checkPermission(SyllabusService.SECURE_REDIRECT);
  }

  public boolean isAddOrEdit() {
	  return syllabusService.checkAddOrEdit();
			  
  }

 
  //testing the access to control the "create/edit"
  //button showing up or not on main page.
  public String getEditAble()
  {
    if (isAddItem() || isBulkAddItem() || isBulkEdit())
    {
      editAble = "true";
    }
    else
    {
      editAble = null;
    }
    return editAble;
  }

  public void setEditAble(String editAble)
  {
    this.editAble = editAble;
  }
  
  public void setDisplayTitleErroMsg(boolean displayTitleErroMsg)
  {
    this.displayTitleErroMsg = displayTitleErroMsg;
  }
  
  public boolean getDisplayTitleErroMsg()
  {
    return displayTitleErroMsg;
  }

  /* testing fileUpload
   * public FileUpload getFileUpload() { return fileUpload; } public void setFileUpload(FileUpload
   * fileUpload) { this.fileUpload = fileUpload; }
   */

  public boolean getDisplayEvilTagMsg()
  {
	return displayEvilTagMsg;
  }

  public void setDisplayEvilTagMsg(boolean displayEvilTagMsg) 
  {
	this.displayEvilTagMsg = displayEvilTagMsg;
  }

  public String getEvilTagMsg() 
  {
	return evilTagMsg;
  }

  public void setEvilTagMsg(String evilTagMsg) 
  {
	this.evilTagMsg = evilTagMsg;
  }
  public String processMainEditCancel(){
	  entries.clear();
	  entry = null;
	  
	  return null;
  }
  
  public String processDeleteCancel()
  {
    //logger.info(this + ".processDeleteCancel() in SyllabusTool.");

	  //we want to keep the changes, so set this flag 
	  dontUpdateEntries = true;
  
    return "main_edit";
  }

  public String processDelete()
      throws org.sakaiproject.exception.PermissionException
  {
    //logger.info(this + ".processDelete() in SyllabusTool");

    ArrayList selected = getSelectedEntries();
    try
    {
      if (!isAddOrEdit())
      {
        return "permission_error";
      }
      else
      {
        Set dataSet = syllabusManager.getSyllabiForSyllabusItem(syllabusItem);
        for (int i = 0; i < selected.size(); i++)
        {
          DecoratedSyllabusEntry den = (DecoratedSyllabusEntry) selected.get(i);
          if(den.isSelected()){
        	  //Delete item
        	  syllabusService.deletePostedSyllabus(den.getEntry());
        	  //Set syllabusAttachments = den.getEntry().getAttachments();
        	  Set syllabusAttachments = syllabusManager.getSyllabusAttachmentsForSyllabusData(den.getEntry());
        	  //den.getEntry().getAttachments();
        	  Iterator iter = syllabusAttachments.iterator();
        	  while(iter.hasNext())
        	  {
        		  SyllabusAttachment attach = (SyllabusAttachment)iter.next();
        		  String id = attach.getAttachmentId();

        		  syllabusManager.removeSyllabusAttachSyllabusData(den.getEntry(), attach);  
        		  if(id.toLowerCase().startsWith("/attachment"))
        			  contentHostingService.removeResource(id);
        	  }
        	  syllabusManager.removeCalendarEvents(den.getEntry());
        	  syllabusManager.removeSyllabusFromSyllabusItem(syllabusItem, den
        			  .getEntry());
          }else{
        	  //update item:
        	  boolean posted = SyllabusData.ITEM_POSTED.equals(den.getEntry().getStatus());
        	  //make sure calendar settings are set correctly:
        	  if(den.getEntry().getLinkCalendar()){
        		  if(den.getEntry().getStartDate() == null && den.getEntry().getEndDate() == null){
        			  //can't post to calendar if dates are null
        			  den.getEntry().setLinkCalendar(false);
        		  }else if(!posted){
        			  //can't post to calendar if the item is in draft
        			  den.getEntry().setLinkCalendar(false);
        		  }
        	  }
        	  boolean statusChanged = den.getStatusChanged();
        	  //this will update the calendar if it's posted and inCalendar is selected
              syllabusManager.saveSyllabus(den.getEntry());
              if(posted && statusChanged){
            	  //went from draft to post:
            	  syllabusService.postChangeSyllabus(den.getEntry());
              }
              if(!posted && statusChanged){
            	  //went from post to draft
            	  syllabusService.draftChangeSyllabus(den.getEntry());
              }
          }
         
        }
      }
      Placement currentPlacement = ToolManager.getCurrentPlacement();
      syllabusItem = syllabusManager.getSyllabusItemByContextId(currentPlacement.getContext());
      
      entries.clear();
      entry = null;

      return "main_edit";
    }
    catch (Exception e)
    {
      logger.info(this + ".processDelete: " + e);
      FacesContext.getCurrentInstance().addMessage(
          null,
          MessageFactory.getMessage(FacesContext.getCurrentInstance(),
              "error_general", (new Object[] { e.toString() })));
    }

    entries.clear();
    entry = null;

    return null;
  }

  public String processEditCancel()
  {
    //logger.info(this + ".processEditCancel() in SyllabusTool ");

    try
    {
      if (entry != null)
      {
        for(int i=0; i <attachments.size(); i++)
        {
          String id = ((SyllabusAttachment)attachments.get(i)).getAttachmentId();
          syllabusManager.removeSyllabusAttachmentObject((SyllabusAttachment)attachments.get(i));
          removeCalendarAttachment(entry.getEntry(), (SyllabusAttachment)attachments.get(i));
          if(id.toLowerCase().startsWith("/attachment"))
            contentHostingService.removeResource(id);
        }
        syllabusManager.removeSyllabusDataObject(entry.getEntry());
      }
    }
    catch(Exception e)
    {
      logger.error(this + ".processEditCancel - " + e);
      e.printStackTrace();
    }
    displayTitleErroMsg = false;
    displayEvilTagMsg=false;
    displayDateError=false;
    displayCalendarError = false;
    entries.clear();
    entry = null;
    attachments.clear();

    return "main_edit";
  }


  public String processEditSave() throws PermissionException
  {
    //logger.info(this + ".processEditSave() in SyllabusTool");

    try
    {
      displayTitleErroMsg = false;
      displayEvilTagMsg=false;
      displayDateError=false;
      displayCalendarError = false;
      alertMessage = null;
      
      if (!(isAddItem() || isBulkAddItem() || isBulkEdit()))
      {
        return "permission_error";
      }
      else
      {
        if(entry.getEntry().getTitle() == null)
        {
        	alertMessage = rb.getString("empty_title_validate");
          return "edit";          
        }
        else if(entry.getEntry().getTitle().trim().equals(""))
        {
        	alertMessage = rb.getString("empty_title_validate");
          return "edit";
        }
        if(entry.getEntry().getAsset()!=null)
        {
        	StringBuilder alertMsg = new StringBuilder();
        	String cleanedText = null;
    		try
    		{
    			cleanedText  =  FormattedText.processFormattedText(entry.getEntry().getAsset(), alertMsg);
    			if (cleanedText != null)
    			{
        			entry.getEntry().setAsset(cleanedText);
    			}
    			if (alertMsg.length() > 0)
    			{
    				alertMessage = rb.getString("empty_content_validate") + " " + alertMsg.toString();
    			  return "edit";
    			}
    		 }
    		catch (Exception e)
    		{
    			logger.warn(this + " " + cleanedText, e);
    		}
        }
        if(entry.getEntry().getStartDate() != null 
        		&& entry.getEntry().getEndDate() != null 
        		&& entry.getEntry().getStartDate().after(entry.getEntry().getEndDate())){
        	alertMessage = rb.getString("invalid_dates");
        	return "edit";
        }
        //calendar can not be posted to when it's saved as a draft
        entry.getEntry().setLinkCalendar(false);
        if (entry.justCreated == true)
        {
          syllabusManager.addSyllabusToSyllabusItem(syllabusItem, getEntry()
              .getEntry());
          for(int i=0; i<attachments.size(); i++)
          {
            syllabusManager.addSyllabusAttachToSyllabusData(getEntry().getEntry(), 
                (SyllabusAttachment)attachments.get(i));            
          }
          syllabusService.draftNewSyllabus(getEntry().getEntry());
        }
      }
 
      entries.clear();
      entry = null;
      attachments.clear();

      return "main_edit";
    }
    catch (Exception e)
    {
      logger.info(this + ".processEditSave in SyllabusTool: " + e);
      FacesContext.getCurrentInstance().addMessage(
          null,
          MessageFactory.getMessage(FacesContext.getCurrentInstance(),
              "error_general", (new Object[] { e.toString() })));
    }

    return null;
  }


  public String processEditBulkCancel()
  {
	  bulkEntry = null;
	  alertMessage = null;
	  if(mainEdit){
		  return "main_edit";
	  }else{
		  return "main";
	  }
  }
  	public String processEditBulkPost() throws PermissionException{
  		return processEditBulk(true);
  	}
	public String processEditBulkDraft() throws PermissionException{
		return processEditBulk(false);
	}
	
	private String processEditBulk(boolean post) throws PermissionException{
		try{
			String status = post ? SyllabusData.ITEM_POSTED : SyllabusData.ITEM_DRAFT;
			alertMessage = null;
			boolean addByDate = "1".equals(bulkEntry.getAddByDate());
			int bulkItems = -1;
			if(bulkEntry != null){
				//check title:
				if(bulkEntry.getTitle() == null || bulkEntry.getTitle().trim().isEmpty())
				{
					alertMessage = rb.getString("empty_title_validate");
				}else if(addByDate){
					//add by date
					
					//check start date
					if(bulkEntry.getStartDate() == null){
						alertMessage = rb.getString("start_date_required");
					}else 
					//check end date
					if(bulkEntry.getEndDate() == null){
						alertMessage = rb.getString("end_date_required");
					}else 
					//check end date
					if(bulkEntry.getStartTime() == null){
						alertMessage = rb.getString("start_time_required");
					}else
					//check day of week
					if(!(bulkEntry.isMonday() || bulkEntry.isTuesday() || bulkEntry.isWednesday() || bulkEntry.isThursday() 
							|| bulkEntry.isFriday() || bulkEntry.isSaturday() || bulkEntry.isSunday())){
						alertMessage = rb.getString("dayOfWeekRequired");
					}else
					//end time after start time?
					if(bulkEntry.getStartDate().after(bulkEntry.getEndDate())){
						alertMessage = rb.getString("invalid_dates");
					}
				}else{
					//check that bulk items is a valid number and no more than 100
					try{
						bulkItems = Integer.parseInt(bulkEntry.getBulkItems());
						if(bulkItems > 100 || bulkItems < 1){
							alertMessage = rb.getString("bulk_items_invalid");
						}
					}catch (Exception e) {
						alertMessage = rb.getString("bulk_items_invalid");	
					}
				}
				if(alertMessage != null){
					return "edit_bulk";
				}else{
					int initPosition = syllabusManager.findLargestSyllabusPosition(
							syllabusItem).intValue() + 1;
					if(addByDate){
						//ok let's loop through the date span
						//break out if past 1 year (don't want to have a DOS attack)
						java.util.Calendar cal = java.util.Calendar.getInstance();
						java.util.Calendar calStartTime = java.util.Calendar.getInstance();
						java.util.Calendar calEndTime = java.util.Calendar.getInstance();
						java.util.Calendar calYear = java.util.Calendar.getInstance();
						cal.setTime(bulkEntry.getStartDate());
						calStartTime.setTime(bulkEntry.getStartTime());
						if(bulkEntry.getEndTime() != null){
							calEndTime.setTime(bulkEntry.getEndTime());
						}
						cal.set(java.util.Calendar.HOUR_OF_DAY, calStartTime.get(java.util.Calendar.HOUR_OF_DAY));
						cal.set(java.util.Calendar.MINUTE, calStartTime.get(java.util.Calendar.MINUTE));
						cal.set(java.util.Calendar.SECOND, calStartTime.get(java.util.Calendar.SECOND));
						calYear.setTime(bulkEntry.getStartDate());
						calYear.add(java.util.Calendar.YEAR, 1);
						//one extra precaution
						int i = 1;
						while(!cal.getTime().after(bulkEntry.getEndDate()) && !cal.getTime().after(calYear.getTime()) && i < 366){
							if((bulkEntry.isMonday() && cal.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.MONDAY)
									|| bulkEntry.isTuesday() && cal.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.TUESDAY
									|| bulkEntry.isWednesday() && cal.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.WEDNESDAY
									|| bulkEntry.isThursday() && cal.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.THURSDAY
									|| bulkEntry.isFriday() && cal.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.FRIDAY
									|| bulkEntry.isSaturday() && cal.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.SATURDAY
									|| bulkEntry.isSunday() && cal.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.SUNDAY){
								Date startDate = cal.getTime();
								Date endDate = null;
								if(bulkEntry.getEndTime() != null){
									//set to end time
									cal.set(java.util.Calendar.HOUR_OF_DAY, calEndTime.get(java.util.Calendar.HOUR_OF_DAY));
									cal.set(java.util.Calendar.MINUTE, calEndTime.get(java.util.Calendar.MINUTE));
									cal.set(java.util.Calendar.SECOND, calEndTime.get(java.util.Calendar.SECOND));
									endDate = cal.getTime();
									//reset to start time
									cal.set(java.util.Calendar.HOUR_OF_DAY, calStartTime.get(java.util.Calendar.HOUR_OF_DAY));
									cal.set(java.util.Calendar.MINUTE, calStartTime.get(java.util.Calendar.MINUTE));
									cal.set(java.util.Calendar.SECOND, calStartTime.get(java.util.Calendar.SECOND));
								}
								SyllabusData syllabusDataObj = syllabusManager.createSyllabusDataObject(bulkEntry.getTitle() + " - " + i,
										new Integer(initPosition), null, "no", status, "none", startDate, endDate, bulkEntry.isLinkCalendar(), null, null, syllabusItem);
								syllabusManager.addSyllabusToSyllabusItem(syllabusItem, syllabusDataObj, false);
								i++;
								initPosition++;
							}
							cal.add(java.util.Calendar.DAY_OF_WEEK, 1);
						}
					}else if(bulkItems > 0 && bulkItems <= 100){
						//add by bulk items
						for(int i = 1; i <= bulkItems; i++){
							syllabusManager.addSyllabusToSyllabusItem(syllabusItem, syllabusManager.createSyllabusDataObject(bulkEntry.getTitle() + " - " + i,
									new Integer(initPosition), null, "no", status, "none", null, null, false, null, null, syllabusItem), false);
							initPosition++;
						}
					}
					if(mainEdit){
						return "main_edit";
					}else{
						return "main";
					}
				}
			  }
		}catch (Exception e)
		{
			logger.info(this + ".processEditBulkPost in SyllabusTool: " + e);
			FacesContext.getCurrentInstance().addMessage(
					null,
					MessageFactory.getMessage(FacesContext.getCurrentInstance(),
							"error_general", (new Object[] { e.toString() })));
		}
		return null;
  }
  
  public String processEditPost() throws PermissionException
  {
    //logger.info(this + ".processEditPost() in SyllabusTool");

    try
    { 
      displayTitleErroMsg = false;
      displayEvilTagMsg=false;
      displayDateError=false;
      displayCalendarError = false;
      alertMessage = null;
      if (!(isAddItem() || isBulkEdit()))
      {
        return "permission_error";
      }
      else
      {
        if(entry.getEntry().getTitle() == null)
        {
          alertMessage = rb.getString("empty_title_validate");
          return "edit";          
        }
        else if(entry.getEntry().getTitle().trim().equals(""))
        {
          alertMessage = rb.getString("empty_title_validate");
          return "edit";
        }
        if(entry.getEntry().getAsset()!=null)
        {
        	StringBuilder alertMsg = new StringBuilder();
        	String cleanedText = null;
        	try
    		{
    			cleanedText  =  FormattedText.processFormattedText(entry.getEntry().getAsset(), alertMsg);
    			if (cleanedText != null)
    			{
					entry.getEntry().setAsset(cleanedText);
				}
    			if (alertMsg.length() > 0)
    			{
					alertMessage = rb.getString("empty_content_validate") + " " + alertMsg.toString();
					return "edit";
    			}
    		 }
    		catch (Exception e)
    		{
    			logger.warn(this + " " + cleanedText, e);
    		}
        }
        if(entry.getEntry().getStartDate() != null 
        		&& entry.getEntry().getEndDate() != null 
        		&& entry.getEntry().getStartDate().after(entry.getEntry().getEndDate())){
        	alertMessage = rb.getString("invalid_dates");
        	return "edit";
        }
        if(entry.getEntry().getLinkCalendar() && entry.getEntry().getStartDate() == null && entry.getEntry().getEndDate() == null){
        	alertMessage = rb.getString("invalid_calendar");
        	return "edit";
        }
        if (entry.justCreated == true)
        {
          getEntry().getEntry().setStatus(SyllabusData.ITEM_POSTED);
          syllabusManager.addSyllabusToSyllabusItem(syllabusItem, getEntry()
              .getEntry());
          //syllabusManager.saveSyllabusItem(syllabusItem);
          for(int i=0; i<attachments.size(); i++)
          {
            syllabusManager.addSyllabusAttachToSyllabusData(getEntry().getEntry(), 
                (SyllabusAttachment)attachments.get(i));            
          }
          //update calendar attachments
          if(getEntry().getEntry().getCalendarEventIdStartDate() != null
        		  && !"".equals(getEntry().getEntry().getCalendarEventIdStartDate())){
        	  syllabusManager.addCalendarAttachments(siteId, getEntry().getEntry().getCalendarEventIdStartDate(), attachments);
          }
          if(getEntry().getEntry().getCalendarEventIdEndDate() != null
        		  && !"".equals(getEntry().getEntry().getCalendarEventIdEndDate())){
        	  syllabusManager.addCalendarAttachments(siteId, getEntry().getEntry().getCalendarEventIdEndDate(), attachments);
          }
          
          syllabusService.postNewSyllabus(getEntry().getEntry());
          
          entries.clear();
          entry = null;
          attachments.clear();

          return "main_edit";
        }
      }
    }
    catch (Exception e)
    {
      logger.info(this + ".processEditPost in SyllabusTool: " + e);
      e.printStackTrace();
      FacesContext.getCurrentInstance().addMessage(
          null,
          MessageFactory.getMessage(FacesContext.getCurrentInstance(),
              "error_general", (new Object[] { e.toString() })));
    }

    return null;
  }

  public String processListDelete() throws PermissionException
  {
    //logger.info(this + ".processListDelete() in SyllabusTool");

    try
    {
      if (!isAddOrEdit())
      {
        return "permission_error";
      }
      else
      {
        ArrayList selected = getSelectedEntries();
        if (selected.isEmpty())
        {
          FacesContext.getCurrentInstance().addMessage(
              null,
              MessageFactory.getMessage(FacesContext.getCurrentInstance(),
                  "error_delete_select", null));

          return null;
        }else{
        	//verify valid modifications:
        	for(DecoratedSyllabusEntry entry : (ArrayList<DecoratedSyllabusEntry>) selected){
        		String validate = entry.validateInput();
        		if(!"".equals(validate)){
        			String itemTitle = entry.getEntry().getTitle();
        			if(itemTitle == null || "".equals(itemTitle.trim())){
        				//title is null, so just point to the item #
        				itemTitle = MessageFactory.getMessage(FacesContext.getCurrentInstance(),
    							"error_invalid_entry_item", new String[]{Integer.toString(entry.getEntry().getPosition())}).getSummary();
        			}
        			//invalid entry:
        			FacesContext.getCurrentInstance().addMessage(
        					null,
        					MessageFactory.getMessage(FacesContext.getCurrentInstance(),
        							"error_invalid_entry", new String[]{itemTitle, validate}));
        			return null;
        		}
        	}
        	
        	return "delete_confirm";
        }
      }
    }
    catch (Exception e)
    {
      logger.info(this + ".processListDelete in SyllabusTool: " + e);
      FacesContext.getCurrentInstance().addMessage(
          null,
          MessageFactory.getMessage(FacesContext.getCurrentInstance(),
              "error_general", (new Object[] { e.toString() })));
    }

    return null;
  }

  public String processListNew() throws PermissionException
  {
    //logger.info(this + ".processListNew() in SyllabusTool");

    try
    {
      if (!syllabusService.checkPermission(SyllabusService.SECURE_ADD_ITEM))
      {
        return "permission_error";
      }
      else
      {
        int initPosition = syllabusManager.findLargestSyllabusPosition(
            syllabusItem).intValue() + 1;
        SyllabusData en = syllabusManager.createSyllabusDataObject(null,
            new Integer(initPosition), null, null, SyllabusData.ITEM_DRAFT, "none", null, null, Boolean.FALSE, null, null);
        en.setView("no");

        entry = new DecoratedSyllabusEntry(en);
        entry.setJustCreated(true);

        entries.clear();

        return "edit";
      }
    }
    catch (Exception e)
    {
      logger.info(this + ".processListNew in SyllabusTool: " + e);
      FacesContext.getCurrentInstance().addMessage(
          null,
          MessageFactory.getMessage(FacesContext.getCurrentInstance(),
              "error_general", (new Object[] { e.toString() })));

      return null;
    }
  }
  
  public String processListNewBulkMainEdit() throws PermissionException
  {
	  mainEdit = true;
	  return processListNewBulk();
  }
  
  public String processListNewBulkMain() throws PermissionException
  {
	  mainEdit = false;
	  return processListNewBulk();
  }
  
  public String processListNewBulk() throws PermissionException
  {
    try
    {
      if (!syllabusService.checkPermission(SyllabusService.SECURE_BULK_EDIT_ITEM))
      {
        return "permission_error";
      }
      else
      {
        bulkEntry = new BulkSyllabusEntry();

        return "edit_bulk";
      }
    }
    catch (Exception e)
    {
      logger.info(this + ".processListNewBulk in SyllabusTool: " + e);
      FacesContext.getCurrentInstance().addMessage(
          null,
          MessageFactory.getMessage(FacesContext.getCurrentInstance(),
              "error_general", (new Object[] { e.toString() })));

      return null;
    }
  }
  
  public String processListEditBulk() throws PermissionException
  {
	  try
	    {
	      if (!syllabusService.checkPermission(SyllabusService.SECURE_BULK_EDIT_ITEM))
	      {
	        return "permission_error";
	      }
	      else
	      {
	     //   bulkEntry = new BulkSyllabusEntry();

	        return "main_edit_bulk";
	      }
	    }
	    catch (Exception e)
	    {
	      logger.info(this + ".processListEditBulk in SyllabusTool: " + e);
	      FacesContext.getCurrentInstance().addMessage(
	          null,
	          MessageFactory.getMessage(FacesContext.getCurrentInstance(),
	              "error_general", (new Object[] { e.toString() })));

	      return null;
	    }
  }

  public String processReadCancel()
  {
    //logger.info(this + ".processReadCancel() in SyllabusTool");

    try
    {
      for(int i=0; i <attachments.size(); i++)
      {
        String id = ((SyllabusAttachment)attachments.get(i)).getAttachmentId();
        syllabusManager.removeSyllabusAttachmentObject((SyllabusAttachment)attachments.get(i));
        removeCalendarAttachment(entry.getEntry(), (SyllabusAttachment)attachments.get(i));
        if(id.toLowerCase().startsWith("/attachment"))
          contentHostingService.removeResource(id);
      }
    }
    catch(Exception e)
    {
      logger.error(this + ".processReadCancel - " + e);
      e.printStackTrace();
    }
    
    displayTitleErroMsg = false;
    displayEvilTagMsg=false;
    displayDateError=false;
    displayCalendarError = false;    		
    entries.clear();
    entry = null;
    attachments.clear();
    oldAttachments.clear();

    return "main_edit";
  }

  public String processReadSave() throws PermissionException
  {
    //logger.info(this + ".processReadSave() in SyllabusTool");

    try
    {
      displayTitleErroMsg = false;
      displayEvilTagMsg=false;	
      displayDateError=false;
      displayCalendarError = false;
      alertMessage = null;
      if (!isAddOrEdit())
      {
        return "permission_error";
      }
      else
      {
        if(entry.getEntry().getTitle() == null)
        {
        	alertMessage = rb.getString("empty_title_validate");
          return "read";          
        }
        else if(entry.getEntry().getTitle().trim().equals(""))
        {
        	alertMessage = rb.getString("empty_title_validate");
          return "read";
        }
        if(entry.getEntry().getAsset()!=null)
        {
        	StringBuilder alertMsg = new StringBuilder();
        	String cleanedText = null;
        	try
    		{
    			cleanedText  =  FormattedText.processFormattedText(entry.getEntry().getAsset(), alertMsg);
    			if (cleanedText != null) 
    			{
					entry.getEntry().setAsset(cleanedText);
				}
    			if (alertMsg.length() > 0)
    			{
    				alertMessage = rb.getString("empty_content_validate") + " " + alertMsg.toString();
					return "edit";
    			}
    		 }
    		catch (Exception e)
    		{
    			logger.warn(this + " " + cleanedText, e);
    		}
        }
        if(entry.getEntry().getStartDate() != null 
        		&& entry.getEntry().getEndDate() != null 
        		&& entry.getEntry().getStartDate().after(entry.getEntry().getEndDate())){
        	alertMessage = rb.getString("invalid_dates");
        	return "edit";
        }
        //calendar can not be posted to when its a draft
        entry.getEntry().setLinkCalendar(false);
       if (entry.justCreated == false)
        {
          getEntry().getEntry().setStatus(SyllabusData.ITEM_DRAFT);
          syllabusManager.saveSyllabus(getEntry().getEntry());
          
          for(int i=0; i<attachments.size(); i++)
          {
            syllabusManager.addSyllabusAttachToSyllabusData(getEntry().getEntry(), 
                (SyllabusAttachment)attachments.get(i));            
          }
          //update calendar attachments
          if(getEntry().getEntry().getCalendarEventIdStartDate() != null){
        	  syllabusManager.addCalendarAttachments(getEntry().getEntry().getSyllabusItem().getContextId(), getEntry().getEntry().getCalendarEventIdStartDate(), attachments);
          }
          if(getEntry().getEntry().getCalendarEventIdEndDate() != null){
        	  syllabusManager.addCalendarAttachments(getEntry().getEntry().getSyllabusItem().getContextId(), getEntry().getEntry().getCalendarEventIdEndDate(), attachments);
          }
          
          syllabusService.draftChangeSyllabus(getEntry().getEntry());
        }
      }
      
      entries.clear();
      entry = null;
      attachments.clear();
      oldAttachments.clear();

      return "main_edit";
    }
    catch (Exception e)
    {
      logger.info(this + ".processReadSave in SyllabusTool: " + e);
      FacesContext.getCurrentInstance().addMessage(
          null,
          MessageFactory.getMessage(FacesContext.getCurrentInstance(),
              "error_general", (new Object[] { e.toString() })));
    }

    return null;
  }

  public String processReadPost() throws PermissionException
  {
    //logger.info(this + ".processReadPost() in SyllabusTool");

    try
    {
      displayTitleErroMsg = false;
      displayEvilTagMsg=false;	
      displayDateError=false;
      displayCalendarError = false;
      alertMessage = null;
      if (!isAddOrEdit())
      {
        return "permission_error";
      }
      else
      {
        if(entry.getEntry().getTitle() == null)
        {
        	alertMessage = rb.getString("empty_title_validate");
          return "read";          
        }
        else if(entry.getEntry().getTitle().trim().equals(""))
        {
        	alertMessage = rb.getString("empty_title_validate");
          return "read";
        }
        if(entry.getEntry().getAsset()!=null)
        {
        	StringBuilder alertMsg = new StringBuilder();
        	String cleanedText = null;
        	try
    		{
				cleanedText  =  FormattedText.processFormattedText(entry.getEntry().getAsset(), alertMsg);
				if (cleanedText != null)
				{
					entry.getEntry().setAsset(cleanedText);
				}
    			if (alertMsg.length() > 0)
    			{
    				alertMessage = rb.getString("empty_content_validate") + " " + alertMsg.toString();
					return "edit";
    			}
    		 }
    		catch (Exception e)
    		{
    			logger.warn(this + " " + cleanedText, e);
    		}
        }
        if(entry.getEntry().getStartDate() != null 
        		&& entry.getEntry().getEndDate() != null 
        		&& entry.getEntry().getStartDate().after(entry.getEntry().getEndDate())){
        	alertMessage = rb.getString("invalid_dates");
        	return "read";
        }
        if(entry.getEntry().getLinkCalendar() && entry.getEntry().getStartDate() == null && entry.getEntry().getEndDate() == null){
        	alertMessage = rb.getString("invalid_calendar");
        	return "read";
        }
        if (entry.justCreated == false)
        {
          getEntry().getEntry().setStatus(SyllabusData.ITEM_POSTED);
          syllabusManager.saveSyllabus(getEntry().getEntry());

          syllabusService.postChangeSyllabus(getEntry().getEntry());
          
          for(int i=0; i<attachments.size(); i++)
          {
            syllabusManager.addSyllabusAttachToSyllabusData(getEntry().getEntry(), 
                (SyllabusAttachment)attachments.get(i));            
          }
          //update calendar attachments
          if(getEntry().getEntry().getCalendarEventIdStartDate() != null){
        	  syllabusManager.addCalendarAttachments(getEntry().getEntry().getSyllabusItem().getContextId(), getEntry().getEntry().getCalendarEventIdStartDate(), attachments);
          }
          if(getEntry().getEntry().getCalendarEventIdEndDate() != null){
        	  syllabusManager.addCalendarAttachments(getEntry().getEntry().getSyllabusItem().getContextId(), getEntry().getEntry().getCalendarEventIdEndDate(), attachments);
          }
               
          entries.clear();
          entry = null;
          attachments.clear();
          oldAttachments.clear();

          return "main_edit";
        }
      }
    }
    catch (Exception e)
    {
      logger.info(this + ".processReadPost in SyllabusTool: " + e);
      FacesContext.getCurrentInstance().addMessage(
          null,
          MessageFactory.getMessage(FacesContext.getCurrentInstance(),
              "error_general", (new Object[] { e.toString() })));
    }

    return null;
  }

  public void downOnePlace(SyllabusData en)
  {
    //logger.info(this + ".downOnePlace() in SyllabusTool");

    SyllabusData swapData = null;
    Iterator iter = syllabusManager.getSyllabiForSyllabusItem(syllabusItem)
        .iterator();
    int i = 0;
    while (iter.hasNext())
    {
      SyllabusData data = (SyllabusData) iter.next();
      if (en.equals(data))
      {
        if (iter.hasNext()) swapData = (SyllabusData) iter.next();
        break;
      }
      i++;
    }

    if (swapData != null){
        syllabusManager.swapSyllabusDataPositions(syllabusItem, en, swapData);
        
        //reorder array to show to the user it was updated:
        ArrayList firstPart = new ArrayList(entries.subList(0, i));
        if(entries.size() > i + 1){
        	firstPart.add(entries.get(i+1));
        }
        firstPart.add(entries.get(i));
        if(entries.size() > i + 2){
        	firstPart.addAll(entries.subList(i+2, entries.size()));
        }
        entries = firstPart;
        
    }

//    entries.clear();
//    entry = null;
  }

  public void upOnePlace(SyllabusData en)
  {
    //logger.info(this + ".upOnePlace() in SyllabusTool");

    SyllabusData swapData = null;
    Iterator iter = syllabusManager.getSyllabiForSyllabusItem(syllabusItem)
        .iterator();
    int i = 0;
    while (iter.hasNext())
    {
      SyllabusData data = (SyllabusData) iter.next();
      if (en.equals(data))
      {
        break;
      }
      else
      {
        swapData = data;
      }
      i++;
    }

    if (swapData != null){
        syllabusManager.swapSyllabusDataPositions(syllabusItem, en, swapData);
        //reorder array to show to the user it was updated:
        if(i > 0){
        	ArrayList firstPart = new ArrayList(entries.subList(0, i-1));
        	if(entries.size() > i){
        		firstPart.add(entries.get(i));
        		firstPart.add(entries.get(i-1));
        	}
        	if(entries.size() > i + 1){
        		firstPart.addAll(entries.subList(i+1, entries.size()));
        	}
        	entries = firstPart;
        }
    }

//    entries.clear();
//    entry = null;
  }

  public String processEditPreview()
  {
	displayTitleErroMsg = false;
    displayEvilTagMsg=false;
    displayDateError=false;
    displayCalendarError = false;
    alertMessage = null;
    if(entry.getEntry().getTitle() == null)
    {
    	alertMessage = rb.getString("empty_title_validate");
      return "edit";          
    }
    if(entry.getEntry().getTitle().trim().equals(""))
    {
    	alertMessage = rb.getString("empty_title_validate");
      return "edit";
    }
    if(entry.getEntry().getAsset()!=null)
    {
    	StringBuilder alertMsg = new StringBuilder();
    	String cleanedText = null;
    	try
		{
			cleanedText  =  FormattedText.processFormattedText(entry.getEntry().getAsset(), alertMsg);
			if (cleanedText != null)
			{
				entry.getEntry().setAsset(cleanedText);
			}
			if (alertMsg.length() > 0)
			{
				alertMessage = rb.getString("empty_content_validate") + " " + alertMsg.toString();
				return "edit";
			}
		 }
		catch (Exception e)
		{
			logger.warn(this + " " + cleanedText, e);
		}
    } 
    if(entry.getEntry().getStartDate() != null 
    		&& entry.getEntry().getEndDate() != null 
    		&& entry.getEntry().getStartDate().after(entry.getEntry().getEndDate())){
    	alertMessage = rb.getString("invalid_dates");
    	return "edit";
    }
    if(entry.getEntry().getLinkCalendar() && entry.getEntry().getStartDate() == null && entry.getEntry().getEndDate() == null){
    	alertMessage = rb.getString("invalid_calendar");
    	return "edit";
    }
    return "preview";
    
  }

  public String processEditPreviewBack()
  {
    return "edit";
  }

  public String processReadPreview()
  {
	displayTitleErroMsg = false;
    displayEvilTagMsg=false;  
    displayDateError=false;
    displayCalendarError = false;
    alertMessage = null;
    if(entry.getEntry().getTitle() == null)
    {
    	alertMessage = rb.getString("empty_title_validate");
      return "read";          
    }
    if(entry.getEntry().getTitle().trim().equals(""))
    {
    	alertMessage = rb.getString("empty_title_validate");
      return "read";
    }
    if(entry.getEntry().getAsset()!=null)
    {
    	StringBuilder alertMsg = new StringBuilder();
    	String cleanedText = null;
    	try
		{
			cleanedText  =  FormattedText.processFormattedText(entry.getEntry().getAsset(), alertMsg);
			if (cleanedText != null)
			{
				entry.getEntry().setAsset(cleanedText);
			}
			if (alertMsg.length() > 0)
			{
				alertMessage = rb.getString("empty_content_validate") + " " + alertMsg.toString();
				return "edit";
			}
		 }
		catch (Exception e)
		{
			logger.warn(this + " " + cleanedText, e);
		}
    }    
    if(entry.getEntry().getStartDate() != null 
    		&& entry.getEntry().getEndDate() != null 
    		&& entry.getEntry().getStartDate().after(entry.getEntry().getEndDate())){
    	alertMessage = rb.getString("invalid_dates");
    	return "read";
    }
    if(entry.getEntry().getLinkCalendar() && entry.getEntry().getStartDate() == null && entry.getEntry().getEndDate() == null){
    	alertMessage = rb.getString("invalid_calendar");
    	return "read";
    }
    return "read_preview";
    
  }

  public String processReadPreviewBack()
  {
    return "read";
  }

  public String processEditUpload()
  {
    //TODO let the filter work and upload...
    /*
     * try { FacesContext fc = FacesContext.getCurrentInstance(); ExternalContext exFc =
     * fc.getExternalContext(); HttpServletRequest currentRequest = (HttpServletRequest)
     * exFc.getRequest(); String[] fileNames ={filename}; org.apache.commons.fileupload.FileUploadBase
     * fu = new org.apache.commons.fileupload.DiskFileUpload(); HttpServletRequest req =
     * HttpServletRequestFactory.createValidHttpServletRequest(fileNames); java.util.List itemList =
     * fu.parseRequest(req); } catch(Exception e) { e.printStackTrace(); }
     */

    filename = null;
    return "edit";
  }

  public String processReadUpload()
  {
    //TODO let the filter work and upload...
    filename = null;
    return "read";
  }

  public String processRedirect() throws PermissionException
  {
    try
    {
      if (!syllabusService.checkPermission(SyllabusService.SECURE_REDIRECT))
      {
        return "permission_error";
      }
      else
      {
    	currentRediredUrl = syllabusItem.getRedirectURL();
    	openInNewWindow = syllabusItem.isOpenInNewWindow();
        return "edit_redirect";
      }
    }
    catch (Exception e)
    {
      logger.info(this + ".processRedirect in SyllabusTool: " + e);
      FacesContext.getCurrentInstance().addMessage(
          null,
          MessageFactory.getMessage(FacesContext.getCurrentInstance(),
              "error_general", (new Object[] { e.toString() })));
    }
    return null;
  }

  public String processEditCancelRedirect()
  {
    //logger.info(this + ".processEditCancelRedirect() in SyllabusTool ");
    
	entries.clear();
    entry = null;

    return "main";
  }

  public String processEditSaveRedirect() throws PermissionException
  {
    //logger.info(this + ".processEditSaveRedirect() in SyllabusTool");

    try
    {
      if (!syllabusService.checkPermission(SyllabusService.SECURE_REDIRECT))
      {
        return "permission_error";
      }
      else
      {
    	// can currentRedirectURL ever be null?
      	currentRediredUrl = currentRediredUrl.replaceAll("\"", ""); 
      	FacesMessage errorMsg = null;
      	if (currentRediredUrl.length() > MAX_REDIRECT_LENGTH) {
          errorMsg = MessageFactory.getMessage(FacesContext.getCurrentInstance(), 
   			     "error_redirect_too_long");
      	} else {
      		try {
      			// an empty redirect URL will effectively remove the redirect
      			if (currentRediredUrl.trim().length() > 0) {
      				// validate the input string to be a valid URL.
      				URL ignore = new URL(currentRediredUrl);
      			}
      			String origURL = syllabusItem.getRedirectURL();
    	    	syllabusItem.setRedirectURL(currentRediredUrl.trim());
                syllabusItem.setOpenInNewWindow(openInNewWindow);
    	        syllabusManager.saveSyllabusItem(syllabusItem);
    	        if(((origURL == null || origURL.isEmpty())
    	    			&& !currentRediredUrl.trim().isEmpty())
    	    		|| (origURL != null && !origURL.isEmpty()
        	    			&& currentRediredUrl.trim().isEmpty())){
    	    		//the URL went from empty to set, or visa versa
    	    		//we need to update the calendar events too
    	        	syllabusManager.updateAllCalendarEvents(syllabusItem.getSurrogateKey());
    	    	}
    	
    	        entries.clear();
    	        entry = null;
      		} catch (MalformedURLException ex) {
      			errorMsg = MessageFactory.getMessage(FacesContext.getCurrentInstance(), 
  			     "error_redirect_ivalid", new Object[] {ex.getMessage()});
      		}
      	}
      	if (errorMsg != null) {
      	   FacesContext.getCurrentInstance().addMessage("redirectForm:urlValue", errorMsg);
      	   return "edit_redirect";
      	}
      }

     return "main";
    }
    catch (Exception e)
    {
      logger.info(this + ".processEditSaveRedirect in SyllabusTool: " + e);
      FacesContext.getCurrentInstance().addMessage(
          null,
          MessageFactory.getMessage(FacesContext.getCurrentInstance(),
              "error_general", (new Object[] { e.toString() })));
    }

    return null;
  }

  public String processCreateAndEdit()
  {
    //logger.info(this + ".processCreateAndEdit() in SyllabusTool");

    try
    {
      if (!(isAddItem() || isBulkAddItem() || isBulkEdit()))
      {
        return "permission_error";
      }
      else
      {
//      syllabusManager.saveSyllabusItem(syllabusItem);

        entries.clear();
        entry = null;
        attachments.clear();
        oldAttachments.clear();
      }

      return "main_edit";
    }
    catch (Exception e)
    {
      logger.info(this + ".processCreateAndEdit() in SyllabusTool: " + e);
      FacesContext.getCurrentInstance().addMessage(
          null,
          MessageFactory.getMessage(FacesContext.getCurrentInstance(),
              "error_general", (new Object[] { e.toString() })));
    }
    return null;
  }

  public String processStudentView()
  {
    return "main";
  }

  public String getTitle()
  {
    ////return SiteService.findTool(PortalService.getCurrentToolId()).getTitle();
    Placement placement = ToolManager.getCurrentPlacement();
    return SiteService.findTool(placement.getToolId()).getTitle();

  }
  
/*test send email.  private void sendNotification()
  {
    String realmName = "/site/" + siteId;
    try
    {
      AuthzGroup siteRealm = AuthzGroupService.getRealm(realmName);
      Set users = siteRealm.getUsers();
      
      if(entry.getEntry().getEmailNotification().equalsIgnoreCase("high"))
      {
        Iterator userIter = users.iterator();
        String userId;
        User thisUser;
        while(userIter.hasNext())
        {
          userId = (String) userIter.next();
          thisUser = UserDirectoryService.getUser(userId);
          if(thisUser.getEmail() != null)
          {
            if(!thisUser.getEmail().equalsIgnoreCase(""))
            {
              EmailService.send("cwen@iupui.edu", thisUser.getEmail(), entry.getEntry().getTitle(), 
                  entry.getEntry().getAsset(), null, null, null);
            }
          }
        }
      }
      else if(this.entry.in_entry.getEmailNotification().equalsIgnoreCase("low"))
      {
      }
      else
      {
      }
    }
    catch(Exception e)
    {
      logger.info(this + ".sendNotification() in SyllabusTool.");
      e.printStackTrace();
    }
//for test    EmailService.send("cwen@iupui.edu", "cwen@iupui.edu", entry.getEntry().getTitle(), 
//for test        entry.getEntry().getAsset(), null, null, null);
  }*/

  public SyllabusService getSyllabusService()
  {
    return syllabusService;
  }
  
  public void setSyllabusService(SyllabusService syllabusService)
  {
    this.syllabusService = syllabusService;
  }
  
  public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}
  
  public String processAddAttRead()
  {
    if(entry.getEntry().getTitle() == null)
    {
      displayTitleErroMsg = true;
      return "edit";          
    }
    else if(entry.getEntry().getTitle().trim().equals(""))
    {
      displayTitleErroMsg = true;
      return "edit";
    }
    else
    {
      displayTitleErroMsg = false;
      return "add_attach";
    }
  }

  public String processUpload(ValueChangeEvent event)
  {
    if(attachCaneled == false)
    {
      Object newValue = event.getNewValue();

      if (newValue instanceof String) return "";
      if (newValue == null) return "";

      FileItem item = (FileItem) event.getNewValue();
      try (InputStream inputStream = item.getInputStream())
      {
        String fileName = item.getName();

        ResourcePropertiesEdit props = contentHostingService.newResourceProperties();

        if (fileName != null) {
            filename = FilenameUtils.getName(filename);
        }

        ContentResourceEdit thisAttach = contentHostingService.addAttachmentResource(fileName);
        thisAttach.setContent(inputStream);
        thisAttach.setContentType(item.getContentType());
        thisAttach.getPropertiesEdit().addAll(props);
        contentHostingService.commitResource(thisAttach);
        
        SyllabusAttachment attachObj = syllabusManager.createSyllabusAttachmentObject(thisAttach.getId(), fileName);
        attachments.add(attachObj);

        if(entry.justCreated != true)
        {
          allAttachments.add(attachObj);
        }
      }
      catch (Exception e)
      {
        logger.error(this + ".processUpload() in SyllabusTool", e);
      }
      if(entry.justCreated == true)
      {
        return "edit";
      }
      else
      {
        return "read";
      }
    }
    return null;
  }
  
  public String processUploadConfirm()
  {
    //attachCaneled = false;
    if(this.entry.justCreated == true)
      return "edit";
    else
    {
      return "read";
    }
  }
  
  public String processUploadCancel()
  {
    //attachCaneled = true;
    if(this.entry.justCreated == true)
      return "edit";
    else
      return "read";
  }  
  
  public ArrayList getAttachments()
  {
    ToolSession session = SessionManager.getCurrentToolSession();
    if (session.getAttribute(FilePickerHelper.FILE_PICKER_CANCEL) == null &&
        session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS) != null) 
    {
      List refs = (List)session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
      Reference ref = null;
      
      for(int i=0; i<refs.size(); i++)
      {
        ref = (Reference) refs.get(i);
        SyllabusAttachment thisAttach = syllabusManager.createSyllabusAttachmentObject(
            ref.getId(), ref.getProperties().getProperty(ref.getProperties().getNamePropDisplayName()));
        
        attachments.add(thisAttach);
        
        if(entry != null && entry.justCreated != true)
        {
          allAttachments.add(thisAttach);
        }
      }
    }
    session.removeAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
    session.removeAttribute(FilePickerHelper.FILE_PICKER_CANCEL);
    if(filePickerList != null)
      filePickerList.clear();
    
    return attachments;
  }
  
  public void setAttachments(ArrayList attachments)
  {
    this.attachments = attachments;
  }
  
  public boolean getAttachCaneled()
  {
    return attachCaneled;
  }
  
  public void setAttachCaneled(boolean attachCaneled)
  {
    this.attachCaneled = attachCaneled;
  }
  
  public String processDeleteAttach()
  {
    ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
    String attachId = null;
    
    Map paramMap = context.getRequestParameterMap();
    Iterator itr = paramMap.keySet().iterator();
    while(itr.hasNext())
    {
      Object key = itr.next();
      if( key instanceof String)
      {
        String name =  (String)key;
        int pos = name.lastIndexOf("syllabus_current_attach");
        
        if(pos>=0 && name.length()==pos+"syllabus_current_attach".length())
        {
          attachId = (String)paramMap.get(key);
          break;
        }
      }
    }
    
    removeAttachId = attachId;
    
    if((removeAttachId != null) && (!removeAttachId.equals("")))
      return "remove_attach_confirm";
    else
      return null;
  }
  
  public String processRemoveAttach()
  {
      if (!isAddOrEdit())
      {
        return "permission_error";
      }
      else if(entry.justCreated == true)
    {
      try
      {
        SyllabusAttachment sa = syllabusManager.getSyllabusAttachment(removeAttachId);
        String id = sa.getAttachmentId();
        
        for(int i=0; i<attachments.size(); i++)
        {
          SyllabusAttachment thisAttach = (SyllabusAttachment)attachments.get(i);
          if(((Long)thisAttach.getSyllabusAttachId()).toString().equals(removeAttachId))
          {
            attachments.remove(i);
            break;
          }
        }
        
        syllabusManager.removeSyllabusAttachmentObject(sa);
        removeCalendarAttachment(entry.getEntry(), sa);
        if(id.toLowerCase().startsWith("/attachment"))
          contentHostingService.removeResource(id);
      }
      catch(Exception e)
      {
        logger.error(this + ".processRemoveAttach() - " + e);
        e.printStackTrace();
      }
      
      removeAttachId = null;
      prepareRemoveAttach.clear();
      return "edit";
    }
    else
    {
      try
      {
        SyllabusAttachment sa = syllabusManager.getSyllabusAttachment(removeAttachId);
        String id = sa.getAttachmentId();
        boolean deleted = false;
        
        for(int i=0; i<attachments.size(); i++)
        {
          SyllabusAttachment thisAttach = (SyllabusAttachment)attachments.get(i);
          if(((Long)thisAttach.getSyllabusAttachId()).toString().equals(removeAttachId))
          {
            attachments.remove(i);
            deleted = true;
            break;
          }
        }
        if(deleted == false)
        {
          for(int i=0; i<oldAttachments.size(); i++)
          {
            SyllabusAttachment thisAttach = (SyllabusAttachment)oldAttachments.get(i);
            if(((Long)thisAttach.getSyllabusAttachId()).toString().equals(removeAttachId))
            {
              oldAttachments.remove(i);
              break;
            }
          }
        }
        
        ContentResource cr = contentHostingService.getResource(id);
        syllabusManager.removeSyllabusAttachmentObject(sa);
        removeCalendarAttachment(entry.getEntry(), sa);
        if(id.toLowerCase().startsWith("/attachment"))
          contentHostingService.removeResource(id);
        
        allAttachments.clear();
        for(int i=0; i<attachments.size(); i++)
        {
          allAttachments.add((SyllabusAttachment)attachments.get(i));
        }
        for(int i=0; i<oldAttachments.size(); i++)
        {
          allAttachments.add((SyllabusAttachment)oldAttachments.get(i));
        }
        

      }
      catch(Exception e)
      {
        logger.error(this + ".processRemoveAttach() - " + e);
        e.printStackTrace();
      }

      removeAttachId = null;
      prepareRemoveAttach.clear();
      return "read";
    }
  }
  
  public String processRemoveAttachCancel()
  {
    removeAttachId = null;
    prepareRemoveAttach.clear();
    if(entry.justCreated == true)
    {
      return "edit";
    }
    else
    {
      return "read";
    }
  }

  public String getRemoveAttachId()
  {
    return removeAttachId;
  }

  public final void setRemoveAttachId(String removeAttachId)
  {
    this.removeAttachId = removeAttachId;
  }

  public final ArrayList getOldAttachments()
  {
    return oldAttachments;
  }

  public final void setOldAttachments(ArrayList oldAttachments)
  {
    this.oldAttachments = oldAttachments;
  }
  
  public String processAddAttWithOldItem()
  {
    if(entry.getEntry().getTitle() == null)
    {
      displayTitleErroMsg = true;
      return "read";          
    }
    else if(entry.getEntry().getTitle().trim().equals(""))
    {
      displayTitleErroMsg = true;
      return "read";
    }
    else
    {
      displayTitleErroMsg = false;
      return "add_attach";
    }
  }

  public final ArrayList getAllAttachments()
  {
    ToolSession session = SessionManager.getCurrentToolSession();
    if (session.getAttribute(FilePickerHelper.FILE_PICKER_CANCEL) == null &&
        session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS) != null) 
    {
      List refs = (List)session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
      Reference ref = null;
      
      for(int i=0; i<refs.size(); i++)
      {
        ref = (Reference) refs.get(i);
        SyllabusAttachment thisAttach = syllabusManager.createSyllabusAttachmentObject(
            ref.getId(), ref.getProperties().getProperty(ref.getProperties().getNamePropDisplayName()));
        
        attachments.add(thisAttach);
        
        if(entry.justCreated != true)
        {
          allAttachments.add(thisAttach);
        }
      }
    }
    session.removeAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
    session.removeAttribute(FilePickerHelper.FILE_PICKER_CANCEL);
    if(filePickerList != null)
      filePickerList.clear();

    return allAttachments;
  }

  public final void setAllAttachments(ArrayList allAttachments)
  {
    this.allAttachments = allAttachments;
  }

  public ArrayList getPrepareRemoveAttach()
  {
    if((removeAttachId != null) && (!removeAttachId.equals("")))
    {
      prepareRemoveAttach.add(syllabusManager.getSyllabusAttachment(removeAttachId));
    }
    
    return prepareRemoveAttach;
  }

  public final void setPrepareRemoveAttach(ArrayList prepareRemoveAttach)
  {
    this.prepareRemoveAttach = prepareRemoveAttach;
  }
  
  public String processAddAttachRedirect()
  {
    try
    {
      filePickerList = EntityManager.newReferenceList();
      ToolSession currentToolSession = SessionManager.getCurrentToolSession();
      currentToolSession.setAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS, filePickerList);
      ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
      context.redirect("sakai.filepicker.helper/tool");
      if(context.getRequestParameterMap().get("itemId") != null){
    	  currentToolSession.setAttribute(SESSION_ATTACHMENT_DATA_ID, context.getRequestParameterMap().get("itemId"));
      }
      return null;
    }
    catch(Exception e)
    {
      logger.error(this + ".processAddAttachRedirect - " + e);
      e.printStackTrace();
      return null;
    }
  }

  public String getCurrentRediredUrl() 
  {
	return currentRediredUrl;
  }

  public void setCurrentRediredUrl(String currentRediredUrl) 
  {
	this.currentRediredUrl = currentRediredUrl;
  }

  /**
   * @return
   * 	String url if using an external url 
   * 	If not included, also add http:// or https:// prefix.
   */
  public String getPrintFriendlyUrl()
  {
	  try {
		  SyllabusItem syItem = getSyllabusItem();
		  
		  if (syItem != null) {
			  currentRediredUrl = syItem.getRedirectURL();
		  }
		  else {
			  currentRediredUrl = "";
		  }
		  if (currentRediredUrl != null && !"".equals(currentRediredUrl)) {
			  if (currentRediredUrl.indexOf(httpPrefix) == -1 && currentRediredUrl.indexOf(httpsPrefix) == -1 ) {
				  if (ServerConfigurationService.getToolUrl().indexOf(httpsPrefix) != -1) {
					  return httpsPrefix + currentRediredUrl; 
				  }
				  else
					  return httpPrefix + currentRediredUrl;
			  }
			  return currentRediredUrl;
		  }
		  else {
			  return ServerConfigurationService.getToolUrl() + Entity.SEPARATOR
					+ ToolManager.getCurrentPlacement().getId() + Entity.SEPARATOR + "printFriendly";
		  }
	  }
	  catch (PermissionException e) {
	        logger.info(this + ".getRediredUrl() in SyllabusTool " + e);
	        FacesContext.getCurrentInstance().addMessage(
	            null,
	            MessageFactory.getMessage(FacesContext.getCurrentInstance(),
	                "error_permission", (new Object[] { e.toString() })));		  
	  }
	  // If here, we have a permission error getting redirected syllabus,
	  // so just return printFriendly url
	  return ServerConfigurationService.getToolUrl() + Entity.SEPARATOR
		+ ToolManager.getCurrentPlacement().getId() + Entity.SEPARATOR + "printFriendly";

  }
  
  /**
   * get title attribute of syllabus
   * @return
   */
  public String getSyllabusDataTitle()
  {
	  String rv = "";
	  DecoratedSyllabusEntry entry = getEntry();
	  boolean alert = true;
	  if (entry != null)
	  {
		  SyllabusData syllabusData = entry.getEntry();
		  if (syllabusData != null)
		  {
			  rv = syllabusData.getTitle();
			  alert = false;
		  }
	  }
	  
	  if (alert)
	  {
		  setAlertMessage(rb.getString("refresh"));
	  }
	  
	  return rv;
  }
  
  /**
   * set the title for saving
   * @param title
   */
  public void setSyllabusDataTitle(String title)
  {
      DecoratedSyllabusEntry entry = getEntry();
      if (entry != null)
      {
              SyllabusData syllabusData = entry.getEntry();
              if (syllabusData != null)
              {
                      syllabusData.setTitle(title);
              }
      }
  }
  
  /**
   * get Asset attribute of Syllabus
   * @return
   */
  public String getSyllabusDataAsset()
  {
	  String rv = "";
	  DecoratedSyllabusEntry entry = getEntry();
	  boolean alert = true;
	  
	  if (entry != null)
	  {
		  SyllabusData syllabusData = entry.getEntry();
		  if (syllabusData != null)
		  {
			  rv = syllabusData.getAsset();
			  alert = false;
		  }
	  }
	  
	  if (alert)
	  {
		  setAlertMessage(rb.getString("refresh"));
	  }
	  
	  return rv;
  }
  
  /**
   * set the asset for saving
   * @param asset
   */
  public void setSyllabusDataAsset(String asset)
  {
      DecoratedSyllabusEntry entry = getEntry();
      if (entry != null)
      {
              SyllabusData syllabusData = entry.getEntry();
              if (syllabusData != null)
              {
                      syllabusData.setAsset(asset);
              }
      }
  }
  
  /**
   * get view attribute of syllabus attribute
   * @return
   */
  public String getSyllabusDataView()
  {
	  String rv = "";
	  DecoratedSyllabusEntry entry = getEntry();
	  boolean alert = true;
	  
	  if (entry != null)
	  {
		  SyllabusData syllabusData = entry.getEntry();
		  if (syllabusData != null)
		  {
			  rv = syllabusData.getView();
			  alert = false;
		  }
	  }
	  
	  if (alert)
	  {
		  setAlertMessage(rb.getString("refresh"));
	  }
	  
	  return rv;
  }
  
  /**
   * set the view for saving
   * @param view
   */
  public void setSyllabusDataView(String view)
  {
      DecoratedSyllabusEntry entry = getEntry();
      if (entry != null)
      {
              SyllabusData syllabusData = entry.getEntry();
              if (syllabusData != null)
              {
                      syllabusData.setView(view);
              }
      }
  }
  
  /**
   * get view emailNotification of syllabus attribute
   * @return
   */
  public String getSyllabusDataEmailNotification()
  {
	  String rv = "";
	  DecoratedSyllabusEntry entry = getEntry();
	  boolean alert = true;
	  
	  if (entry != null)
	  {
		  SyllabusData syllabusData = entry.getEntry();
		  if (syllabusData != null)
		  {
			  rv = syllabusData.getEmailNotification();
			  alert = false;
		  }
	  }
	  
	  if (alert)
	  {
		  setAlertMessage(rb.getString("refresh"));
	  }
	  
	  return rv;
  }
  
  /**
   * set the email notification setting for saving
   * @param emailNotification
   */
  public void setSyllabusDataEmailNotification(String emailNotification)
  {
      DecoratedSyllabusEntry entry = getEntry();
      if (entry != null)
      {
              SyllabusData syllabusData = entry.getEntry();
              if (syllabusData != null)
              {
                      syllabusData.setEmailNotification(emailNotification);
              }
      }
  }
  
  /**
   * get Asset attribute of Syllabus
   * @return
   */
  public String getSyllabusDataStartDate()
  {
	  String rv = "";
	  DecoratedSyllabusEntry entry = getEntry();
	  boolean alert = true;

	  if (entry != null)
	  {
		  SyllabusData syllabusData = entry.getEntry();
		  if (syllabusData != null)
		  {
			  Date rvDate = syllabusData.getStartDate();
			  if(rvDate != null){
				  rv = SyllabusData.dateFormat.format(rvDate);
			  }
			  alert = false;
		  }
	  }

	  if (alert)
	  {
		  setAlertMessage(rb.getString("refresh"));
	  }

	  return rv;
  }
  
  /**
   * set the asset for saving
   * @param asset
   */
  public void setSyllabusDataStartDate(String date)
  {
	  DecoratedSyllabusEntry entry = getEntry();
	  if (entry != null)
	  {
		  SyllabusData syllabusData = entry.getEntry();
		  if (syllabusData != null)
		  {
			  if(date == null || "".equals(date)){
				  syllabusData.setStartDate(null);
			  }else{
				  try {
					  syllabusData.setStartDate(SyllabusData.dateFormat.parse(date));
				  } catch (ParseException e) {
					  //date won't be changed
				  }
			  }
		  }
	  }
  }
  
  /**
   * get Asset attribute of Syllabus
   * @return
   */
  public String getSyllabusDataEndDate()
  {
	  String rv = "";
	  DecoratedSyllabusEntry entry = getEntry();
	  boolean alert = true;

	  if (entry != null)
	  {
		  SyllabusData syllabusData = entry.getEntry();
		  if (syllabusData != null)
		  {
			  Date rvDate = syllabusData.getEndDate();
			  if(rvDate != null){
				  rv = SyllabusData.dateFormat.format(rvDate);
			  }
			  alert = false;
		  }
	  }

	  if (alert)
	  {
		  setAlertMessage(rb.getString("refresh"));
	  }

	  return rv;
  }
  
  /**
   * set the asset for saving
   * @param asset
   */
  public void setSyllabusDataEndDate(String date)
  {
	  DecoratedSyllabusEntry entry = getEntry();
	  if (entry != null)
	  {
		  SyllabusData syllabusData = entry.getEntry();
		  if (syllabusData != null)
		  {
			  if(date == null || "".equals(date)){
				  syllabusData.setEndDate(null);
			  }else{
				  try {
					  syllabusData.setEndDate(SyllabusData.dateFormat.parse(date));
				  } catch (ParseException e) {
					  //date won't be changed
				  }
			  }
		  }
	  }
  }
  
  public boolean getSyllabusDataLinkCalendar(){
	  boolean rv = false;
	  DecoratedSyllabusEntry entry = getEntry();
	  boolean alert = true;

	  if (entry != null)
	  {
		  SyllabusData syllabusData = entry.getEntry();
		  if (syllabusData != null)
		  {
			  rv = syllabusData.isLinkCalendar();
			  alert = false;
		  }
	  }

	  if (alert)
	  {
		  setAlertMessage(rb.getString("refresh"));
	  }

	  return rv;
  }

  public void setSyllabusDataLinkCalendar(boolean linkCalendar){
	  DecoratedSyllabusEntry entry = getEntry();
	  if (entry != null)
	  {
		  SyllabusData syllabusData = entry.getEntry();
		  if (syllabusData != null)
		  {
			  syllabusData.setLinkCalendar(linkCalendar);
		  }
	  }
  }

  public boolean isDisplayDateError() {
	  return displayDateError;
  }

  public void setDisplayDateError(boolean displayDateError) {
	  this.displayDateError = displayDateError;
  }
  public Boolean getCalendarExistsForSite(){
	  String siteContext = ToolManager.getCurrentPlacement().getContext();
	  if(calendarExistCache.containsKey(siteContext)){
		  return calendarExistCache.get(siteContext);
	  }else{

		  Site site = null;
		  try
		  {
			  site = SiteService.getSite(siteContext);
			  if (site.getToolForCommonId("sakai.schedule") != null)
			  {
				  calendarExistCache.put(siteContext, Boolean.TRUE);
				  return true;
			  }else{
				  calendarExistCache.put(siteContext, Boolean.FALSE);
				  return false;
			  }
		  }
		  catch (Exception e) {
			  logger.warn("Exception thrown while getting site", e);
		  }
		  return false;
	  }
  }

  public CalendarService getCalendarService() {
	  return calendarService;
  }

  public void setCalendarService(CalendarService calendarService) {
	  this.calendarService = calendarService;
  }

  private void removeCalendarAttachment(SyllabusData data, SyllabusAttachment attachment){
	//update calendar attachments
      if(data.getCalendarEventIdStartDate() != null
    		  && !"".equals(data.getCalendarEventIdStartDate())){
    	  syllabusManager.removeCalendarAttachments(siteId, data.getCalendarEventIdStartDate(), attachment);
      }
      if(getEntry().getEntry().getCalendarEventIdEndDate() != null
    		  && !"".equals(getEntry().getEntry().getCalendarEventIdEndDate())){
    	  syllabusManager.removeCalendarAttachments(siteId, data.getCalendarEventIdEndDate(), attachment);
      }
  }
  
  public class BulkSyllabusEntry{
	  public final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
	  public final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
	  private String title = "";
	  private Date startDate = null;
	  private Date endDate = null;
	  private boolean monday, tuesday, wednesday, thursday, friday, saturday, sunday = false;
	  private boolean linkCalendar;
	  private Date startTime;
	  private Date endTime;
	  private String bulkItems;
	  private String addByItems = "1";
	  private String addByDate;
	  
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public boolean isMonday() {
		return monday;
	}
	public void setMonday(boolean monday) {
		this.monday = monday;
	}
	public boolean isTuesday() {
		return tuesday;
	}
	public void setTuesday(boolean tuesday) {
		this.tuesday = tuesday;
	}
	public boolean isWednesday() {
		return wednesday;
	}
	public void setWednesday(boolean wednesday) {
		this.wednesday = wednesday;
	}
	public boolean isThursday() {
		return thursday;
	}
	public void setThursday(boolean thursday) {
		this.thursday = thursday;
	}
	public boolean isFriday() {
		return friday;
	}
	public void setFriday(boolean friday) {
		this.friday = friday;
	}
	public boolean isSaturday() {
		return saturday;
	}
	public void setSaturday(boolean saturday) {
		this.saturday = saturday;
	}
	public boolean isSunday() {
		return sunday;
	}
	public void setSunday(boolean sunday) {
		this.sunday = sunday;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public boolean isLinkCalendar() {
		return linkCalendar;
	}
	public void setLinkCalendar(boolean linkCalendar) {
		this.linkCalendar = linkCalendar;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
  
	//handle setting dates:
	public String getStartDateString()
	  {
		String rv = "";
		if(getStartDate() != null){
			rv = dateFormat.format(getStartDate());
		}
		return rv;
	  }
  
	public void setStartDateString(String date)
	{
		if(date == null || "".equals(date)){
			setStartDate(null);
		}else{
			try {
				setStartDate(dateFormat.parse(date));
			} catch (ParseException e) {
				//date won't be changed
			}
		}
	}
	
	public String getEndDateString()
	  {
		String rv = "";
		if(getEndDate() != null){
			rv = dateFormat.format(getEndDate());
		}
		return rv;
	  }

	public void setEndDateString(String date)
	{
		if(date == null || "".equals(date)){
			setEndDate(null);
		}else{
			try {
				setEndDate(dateFormat.parse(date));
			} catch (ParseException e) {
				//date won't be changed
			}
		}
	}
	
	public String getStartTimeString()
	  {
		String rv = "";
		if(getStartTime() != null){
			rv = timeFormat.format(getStartTime());
		}
		return rv;
	  }

	public void setStartTimeString(String time)
	{
		if(time == null || "".equals(time)){
			setStartTime(null);
		}else{
			try {
				setStartTime(timeFormat.parse(time));
			} catch (ParseException e) {
				//time won't be changed
			}
		}
	}
	
	public String getEndTimeString()
	  {
		String rv = "";
		if(getEndTime() != null){
			rv = timeFormat.format(getEndTime());
		}
		return rv;
	  }

	public void setEndTimeString(String time)
	{
		if(time == null || "".equals(time)){
			setEndTime(null);
		}else{
			try {
				setEndTime(timeFormat.parse(time));
			} catch (ParseException e) {
				//time won't be changed
			}
		}
	}
	public String getBulkItems() {
		return bulkItems;
	}
	public void setBulkItems(String bulkItems) {
		this.bulkItems = bulkItems;
	}
	public String getAddByItems() {
		return addByItems;
	}
	public void setAddByItems(String addByItems) {
		this.addByItems = addByItems;
	}
	public String getAddByDate() {
		return addByDate;
	}
	public void setAddByDate(String addByDate) {
		this.addByDate = addByDate;
	}
  }
  
public BulkSyllabusEntry getBulkEntry() {
	return bulkEntry;
}

public void setBulkEntry(BulkSyllabusEntry bulkEntry) {
	this.bulkEntry = bulkEntry;
}

public String getOpenDataId() {
	ToolSession session = SessionManager.getCurrentToolSession();
	if(session.getAttribute(SESSION_ATTACHMENT_DATA_ID) != null){
		return (String) session.getAttribute(SESSION_ATTACHMENT_DATA_ID);
	}else{
		return "";
	}
}

public void setOpenDataId(String openDataId) {
	this.openDataId = openDataId;
}

    public boolean isOpenInNewWindow() {
        return openInNewWindow;
    }

    public void setOpenInNewWindow(boolean openInNewWindow) {
        this.openInNewWindow = openInNewWindow;
    }

	public boolean isDisplayCalendarError() {
		return displayCalendarError;
	}

	public void setDisplayCalendarError(boolean displayCalendarError) {
		this.displayCalendarError = displayCalendarError;
	}
}
