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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.sakaiproject.api.app.syllabus.SyllabusAttachment;
import org.sakaiproject.api.app.syllabus.SyllabusData;
import org.sakaiproject.api.app.syllabus.SyllabusItem;
import org.sakaiproject.api.app.syllabus.SyllabusManager;
import org.sakaiproject.api.app.syllabus.SyllabusService;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResourceEdit;
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
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.DateFormatterUtil;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;

import com.sun.faces.util.MessageFactory;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

//sakai2 - no need to import org.sakaiproject.jsf.ToolBean here as sakai does.

/**
 * @author cwen TODO To change the template for this generated type comment go to Window - Preferences -
 *         Java - Code Style - Code Templates
 */
//sakai2 - doesn't implement ToolBean as sakai does.
@Slf4j
public class SyllabusTool
{
  private static final int MAX_REDIRECT_LENGTH = 512; // according to HBM file
  private static final int MAX_TITLE_LENGTH = 256;    // according to HBM file
  private boolean mainEdit = false;
  private static final String SESSION_ATTACHMENT_DATA_ID = "syllabysAttachDataId";
  //used for the UI to know which data ID to have opened by default (i.e. if you added/removed an attachment on the main page)
  private String openDataId;
  private static final String HIDDEN_START_ISO_DATE = "dataStartDateISO8601";
  private static final String HIDDEN_END_ISO_DATE = "dataEndDateISO8601";
  private static final String DATEPICKER_DATE_FORMAT = "yyyy-MM-dd";
  private static final String DATEPICKER_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
  private FormattedText formattedText;
  
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
    
    protected List<SyllabusAttachment> attachmentList = null;
    private String startDateString;
    private String endDateString;
    private Integer relativePosition = 0;
    
    public DecoratedSyllabusEntry(SyllabusData en)
    {
      in_entry = en;
      //b/c of pass by reference, we need to clone the values we want to check
      //against
      this.orig_title = en.getTitle();
      this.orig_startDate = en.getStartDate() == null ? null : (Date) en.getStartDate().clone();
      this.orig_endDate = en.getEndDate() == null ? null : (Date) en.getEndDate().clone();
      this.orig_isLinkCalendar= en.getLinkCalendar();
      this.orig_status = en.getStatus();
      this.startDateString = en.getStartDate() == null ? "" : DateFormatterUtil.format(en.getStartDate(), DATEPICKER_DATETIME_FORMAT, rb.getLocale());
      this.endDateString = en.getEndDate() == null ? "" : DateFormatterUtil.format(en.getEndDate(), DATEPICKER_DATETIME_FORMAT, rb.getLocale());
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
 
    public void setRelativePosition(Integer pos) {
      relativePosition = pos;
    }
	
    public Integer getRelativePosition() {
      return relativePosition;
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
      //log.info(this + ".processListRead() in SyllabusTool.");
      
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
    
    public List<SyllabusAttachment> getAttachmentList()
    {
    	if(attachmentList == null){
    		attachmentList = new ArrayList<>();
    		Set tempList = syllabusManager.getSyllabusAttachmentsForSyllabusData(in_entry);

    		Iterator iter = tempList.iterator();
    		while(iter.hasNext())
    		{
    			SyllabusAttachment sa = (SyllabusAttachment)iter.next();
    			attachmentList.add(sa);
    		}
    	}
    	
      Collections.sort(attachmentList);
      return attachmentList;
    }
    
    public void setAttachmentList(List attachmentList)
    {
      this.attachmentList = attachmentList;
    }
    public String getStatus(){
		return in_entry.getStatus();
	}
    public boolean getTitleChanged(){
		//Title Changed?
		return (in_entry.getTitle() == null && orig_title != null)
			   || (in_entry.getTitle() != null && orig_title == null)
			   || (in_entry.getTitle() != null && orig_title != null
				&& (!in_entry.getTitle().equals(orig_title)));
    }
    
    public boolean getStartTimeChanged(){
		//Start Time
		return (in_entry.getStartDate() == null && orig_startDate != null)
			   || (in_entry.getStartDate() != null && orig_startDate == null)
			   || (in_entry.getStartDate() != null && orig_startDate != null
					&& (!in_entry.getStartDate().equals(orig_startDate)));
    }
    
    public boolean getEndTimeChanged(){
		//End Time
		return (in_entry.getEndDate() == null && orig_endDate != null)
			   || (in_entry.getEndDate() != null && orig_endDate == null)
			   || (in_entry.getEndDate() != null && orig_endDate != null
					&& (!in_entry.getEndDate().equals(orig_endDate)));
    }
    
    public boolean getPostToCalendarChanged(){
		//posted to cal:
		return !Objects.equals(in_entry.getLinkCalendar(), orig_isLinkCalendar);
    }
    
    public boolean getStatusChanged(){
		//draft status:
		return (in_entry.getStatus() == null && orig_status != null)
			   || (in_entry.getStatus() != null && orig_status == null)
			   || (in_entry.getStatus() != null && orig_status != null
					&& (!in_entry.getStatus().equals(orig_status)));
    }
    
    
    public boolean hasChanged(){
		return getTitleChanged() || getStartTimeChanged()
			   || getEndTimeChanged() || getPostToCalendarChanged()
			   || getStatusChanged();
    }
    
    public String validateInput(){
    	//Title
    	if(in_entry.getTitle() == null || in_entry.getTitle().trim().equals(""))
        {
    		return MessageFactory.getMessage(FacesContext.getCurrentInstance(),
					"empty_title_validate", (Object) null).getSummary();
        }else  if(in_entry.getStartDate() != null 
        		&& in_entry.getEndDate() != null 
        		&& in_entry.getStartDate().after(in_entry.getEndDate())){
        	return MessageFactory.getMessage(FacesContext.getCurrentInstance(),
					"invalid_dates", (Object) null).getSummary();
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

	public String getStartDateString() {
		return this.startDateString;
	}

	public void setStartDateString(String startDateString) {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String startISODate = params.get(this.getRelativePosition() + HIDDEN_START_ISO_DATE);
		if(DateFormatterUtil.isValidISODate(startISODate)){
			getEntry().setStartDate(DateFormatterUtil.parseISODate(startISODate));
		} else {
			getEntry().setStartDate(null);
		}
		this.startDateString = DateFormatterUtil.format(getEntry().getStartDate(), DATEPICKER_DATETIME_FORMAT, rb.getLocale());
	}

	public String getEndDateString() {
		return this.endDateString;
	}

	public void setEndDateString(String endDateString) {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String endISODate = params.get(this.getRelativePosition() + HIDDEN_END_ISO_DATE);
		if(DateFormatterUtil.isValidISODate(endISODate)){
			getEntry().setEndDate(DateFormatterUtil.parseISODate(endISODate));
		} else {
			getEntry().setEndDate(null);
		}
		
		this.endDateString = DateFormatterUtil.format(getEntry().getEndDate(), DATEPICKER_DATETIME_FORMAT, rb.getLocale());
	}
  }

  protected SyllabusManager syllabusManager;

  protected SyllabusItem syllabusItem;

  protected ArrayList entries;

  protected String userId;

  protected DecoratedSyllabusEntry entry = null;
  
  protected BulkSyllabusEntry bulkEntry = null;

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

  @Setter
  private ArrayList attachments = new ArrayList();

  @Getter @Setter
  private boolean attachCaneled = false;

  @Getter @Setter
  private String removeAttachId = null;

  @Getter @Setter
  private ArrayList oldAttachments = new ArrayList();

  @Setter
  private ArrayList allAttachments = new ArrayList();

  private List filePickerList;
  
  private String currentRediredUrl = null;
  
  private final String httpPrefix = "http://";
  
  private final String httpsPrefix = "https://";

  private ContentHostingService contentHostingService;
  
  private ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.syllabus.bundle.Messages");
  
  private CalendarService calendarService;
  private Boolean calendarExistsForSite = null;
  private Map<String, Boolean> calendarExistCache = new HashMap<>();
  
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
	  mobileSession = session.getAttribute("is_wireless_device") != null && ((Boolean) session.getAttribute("is_wireless_device"))?"true":"false";
	  formattedText = ComponentManager.get(FormattedText.class);
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
      //log.info(this + ".getEntries() in SyllabusTool");

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
                int i = 0;
                while (iter.hasNext())
                {
                    SyllabusData en = (SyllabusData) iter.next();
                    if (isAddOrEdit())
                    {
                        DecoratedSyllabusEntry den = new DecoratedSyllabusEntry(en);
                        den.setRelativePosition(i++);
                        entries.add(den);
                    }
                    else
                    {
                        if (en.getStatus().equalsIgnoreCase(SyllabusData.ITEM_POSTED))
                        {
                            DecoratedSyllabusEntry den = new DecoratedSyllabusEntry(en);
                            den.setRelativePosition(i++);
                            entries.add(den);
                        }
                    }
                }
            }
        }
      }
      catch (Exception e)
      {
        log.info(this + ".getEntries() in SyllabusTool " + e);
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
            int i = 0;
            while (iter.hasNext())
            {
              SyllabusData en = (SyllabusData) iter.next();
              if (isAddOrEdit())
              {
                DecoratedSyllabusEntry den = new DecoratedSyllabusEntry(en);
                den.setRelativePosition(i++);
                entries.add(den);
              }
              else
              {
                if (en.getStatus().equalsIgnoreCase(SyllabusData.ITEM_POSTED))
                {
                  DecoratedSyllabusEntry den = new DecoratedSyllabusEntry(en);
                  den.setRelativePosition(i++);
                  entries.add(den);
                }
              }
            }
          }
        }
      }
      catch (Exception e)
      {
        log.info(this + ".getEntries() in SyllabusTool for redirection" + e);
        FacesContext.getCurrentInstance().addMessage(
            null,
            MessageFactory.getMessage(FacesContext.getCurrentInstance(),
                "error_general", (new Object[] { e.toString() })));
      }
    }
	this.displayNoEntryMsg = entries == null || entries.isEmpty();

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
    			log.error(e.getMessage(), e);
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

  public void setSyllabusItem(SyllabusItem syllabusItem)
  {
    this.syllabusItem = syllabusItem;
  }

  public String getFilename()
  {
    //log.info(this + ".getFilename() in SyllabusTool");
    return filename;
  }

  public void setFilename(String filename)
  {
    //log.info(this + ".setFilename() in SyllabusTool");
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
		  log.info(this + "IdUnusedException getting site title for syllabus: " + e);
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
    //log.info(this + ".processDeleteCancel() in SyllabusTool.");

	  //we want to keep the changes, so set this flag 
	  dontUpdateEntries = true;
  
    return "main_edit";
  }

  public String processDelete()
      throws org.sakaiproject.exception.PermissionException
  {
    //log.info(this + ".processDelete() in SyllabusTool");

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
              SyllabusData saved = syllabusManager.saveSyllabus(den.getEntry());
              if(posted && statusChanged){
            	  //went from draft to post:
            	  syllabusService.postChangeSyllabus(saved);
              }
              if(!posted && statusChanged){
            	  //went from post to draft
            	  syllabusService.draftChangeSyllabus(saved);
              }
          }
         
        }
      }
      Placement currentPlacement = ToolManager.getCurrentPlacement();
      syllabusItem = syllabusManager.getSyllabusItemByContextId(currentPlacement.getContext());
      
      entries.clear();
      entry = null;

      return "main";
    }
    catch (Exception e)
    {
      log.info(this + ".processDelete: " + e);
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
    //log.info(this + ".processEditCancel() in SyllabusTool ");

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
      log.error(this + ".processEditCancel - " + e);
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
    //log.info(this + ".processEditSave() in SyllabusTool");

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
    			cleanedText  =  formattedText.processFormattedText(entry.getEntry().getAsset(), alertMsg);
    			if (cleanedText != null)
    			{
        			entry.getEntry().setAsset(cleanedText);
    			}
    			if (alertMsg.length() > 0)
    			{
    			  log.debug("Syllabus content sanitized: {}", alertMsg);
    			}
    		 }
    		catch (Exception e)
    		{
    			log.warn(this + " " + cleanedText, e);
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
      log.info(this + ".processEditSave in SyllabusTool: " + e);
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
	  return "main_edit";
  }
  	public String processEditBulkPost() throws PermissionException{
  		return processEditBulk(true);
  	}
	public String processEditBulkDraft() throws PermissionException{
		return processEditBulk(false);
	}

    private String processEditBulk(boolean post) throws PermissionException {
        String status = post ? SyllabusData.ITEM_POSTED : SyllabusData.ITEM_DRAFT;
        alertMessage = null;

        if (bulkEntry != null) {
            boolean addByDate = "1".equals(bulkEntry.getAddByDate());
            boolean addByItem = "1".equals(bulkEntry.getAddByItems());
            boolean addSingleItem = "1".equals(bulkEntry.getAddSingleItem());

            // check options only one should be true
            if (!BooleanUtils.oneHot(new Boolean[] {addSingleItem, addByItem, addByDate})) {
                alertMessage = rb.getString("bulk_to_many_options_selected");
                return "edit_bulk";
            }

            // check that a title was entered
            if (StringUtils.isBlank(bulkEntry.getTitle())) {
                alertMessage = rb.getString("empty_title_validate");
                return "edit_bulk";
            }

            int bulkItems = 0;
            if (addByItem) {
                //check that bulk items is a valid number and no more than 100
                bulkItems = NumberUtils.toInt(bulkEntry.getBulkItems());
                if (bulkItems < 2 || bulkItems > 20) {
                    alertMessage = rb.getString("bulk_items_invalid");
                    return "edit_bulk";
                }
            }

            if (addByDate) {
                // check start date
                if (bulkEntry.getStartDate() == null) {
                    alertMessage = rb.getString("start_date_required");
                    return "edit_bulk";
                }
                // check end date
                if (bulkEntry.getEndDate() == null) {
                    alertMessage = rb.getString("end_date_required");
                    return "edit_bulk";
                }
                // check end date
                if (bulkEntry.getStartTime() == null) {
                    alertMessage = rb.getString("start_time_required");
                    return "edit_bulk";
                }
                // end date after start date?
                if (bulkEntry.getStartDate().after(bulkEntry.getEndDate())) {
                    alertMessage = rb.getString("invalid_dates");
                    return "edit_bulk";
                }
                // check day of week
                if (!(bulkEntry.isMonday()
                        || bulkEntry.isTuesday()
                        || bulkEntry.isWednesday()
                        || bulkEntry.isThursday()
                        || bulkEntry.isFriday()
                        || bulkEntry.isSaturday()
                        || bulkEntry.isSunday())) {
                    alertMessage = rb.getString("dayOfWeekRequired");
                    return "edit_bulk";
                }
            }

            int initPosition = syllabusManager.findLargestSyllabusPosition(syllabusItem) + 1;

            if (addSingleItem) {
                SyllabusData syllabusDataObj = syllabusManager.createSyllabusDataObject(bulkEntry.getTitle(), initPosition, null, "no", status, "none", null, null, false, null, null, syllabusItem);
                syllabusManager.addSyllabusToSyllabusItem(syllabusItem, syllabusDataObj, false);
                entry = new DecoratedSyllabusEntry(syllabusDataObj);
                entries.clear();
                return "read";
            }

            if (addByItem) {
                //add by bulk items
                for (int i = 1; i <= bulkItems; i++) {
                    SyllabusData syllabusDataObj = syllabusManager.createSyllabusDataObject(bulkEntry.getTitle() + " - " + i, initPosition, null, "no", status, "none", null, null, false, null, null, syllabusItem);
                    syllabusManager.addSyllabusToSyllabusItem(syllabusItem, syllabusDataObj, false);
                    initPosition++;
                }
                return "main_edit";
            }

            if (addByDate) {
                //ok let's loop through the date span
                //break out if past 1 year (don't want to have a DOS attack)
                java.util.Calendar cal = java.util.Calendar.getInstance();
                java.util.Calendar calStartTime = java.util.Calendar.getInstance();
                java.util.Calendar calEndTime = java.util.Calendar.getInstance();
                java.util.Calendar calYear = java.util.Calendar.getInstance();
                cal.setTime(bulkEntry.getStartDate());
                calStartTime.setTime(bulkEntry.getStartTime());
                if (bulkEntry.getEndTime() != null) {
                    calEndTime.setTime(bulkEntry.getEndTime());
                }
                cal.set(java.util.Calendar.HOUR_OF_DAY, calStartTime.get(java.util.Calendar.HOUR_OF_DAY));
                cal.set(java.util.Calendar.MINUTE, calStartTime.get(java.util.Calendar.MINUTE));
                cal.set(java.util.Calendar.SECOND, calStartTime.get(java.util.Calendar.SECOND));
                calYear.setTime(bulkEntry.getStartDate());
                calYear.add(java.util.Calendar.YEAR, 1);
                //one extra precaution
                int i = 1;
                while (!cal.getTime().after(bulkEntry.getEndDate()) && !cal.getTime().after(calYear.getTime()) && i < 366) {
                    if ((bulkEntry.isMonday() && cal.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.MONDAY)
                            || bulkEntry.isTuesday() && cal.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.TUESDAY
                            || bulkEntry.isWednesday() && cal.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.WEDNESDAY
                            || bulkEntry.isThursday() && cal.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.THURSDAY
                            || bulkEntry.isFriday() && cal.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.FRIDAY
                            || bulkEntry.isSaturday() && cal.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.SATURDAY
                            || bulkEntry.isSunday() && cal.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.SUNDAY) {
                        Date startDate = cal.getTime();
                        Date endDate = null;
                        if (bulkEntry.getEndTime() != null) {
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
                        SyllabusData syllabusDataObj = syllabusManager.createSyllabusDataObject(bulkEntry.getTitle() + " - " + i, initPosition, null, "no", status, "none", startDate, endDate, bulkEntry.isLinkCalendar(), null, null, syllabusItem);
                        syllabusManager.addSyllabusToSyllabusItem(syllabusItem, syllabusDataObj, false);
                        i++;
                        initPosition++;
                    }
                    cal.add(java.util.Calendar.DAY_OF_WEEK, 1);
                }
                return "main_edit";
            }
        }
        return null;
  }
  
  public String processEditPost() throws PermissionException
  {
    //log.info(this + ".processEditPost() in SyllabusTool");

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
    			cleanedText  =  formattedText.processFormattedText(entry.getEntry().getAsset(), alertMsg);
    			if (cleanedText != null)
    			{
					entry.getEntry().setAsset(cleanedText);
				}
    			if (alertMsg.length() > 0)
    			{
					log.debug("Syllabus content sanitized: {}", alertMsg);
    			}
    		 }
    		catch (Exception e)
    		{
    			log.warn(this + " " + cleanedText, e);
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
      log.info(this + ".processEditPost in SyllabusTool: " + e);
      FacesContext.getCurrentInstance().addMessage(
          null,
          MessageFactory.getMessage(FacesContext.getCurrentInstance(),
              "error_general", (new Object[] { e.toString() })));
    }

    return null;
  }

  public String processListDelete() throws PermissionException
  {
    //log.info(this + ".processListDelete() in SyllabusTool");

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
                  "error_delete_select", (Object) null));

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
    							"error_invalid_entry_item", Integer.toString(entry.getEntry().getPosition())).getSummary();
        			}
        			//invalid entry:
        			FacesContext.getCurrentInstance().addMessage(
        					null,
        					MessageFactory.getMessage(FacesContext.getCurrentInstance(),
        							"error_invalid_entry", itemTitle, validate));
        			return null;
        		}
        	}
        	
        	return "delete_confirm";
        }
      }
    }
    catch (Exception e)
    {
      log.info(this + ".processListDelete in SyllabusTool: " + e);
      FacesContext.getCurrentInstance().addMessage(
          null,
          MessageFactory.getMessage(FacesContext.getCurrentInstance(),
              "error_general", (new Object[] { e.toString() })));
    }

    return null;
  }

  public String processListNew() throws PermissionException
  {
    //log.info(this + ".processListNew() in SyllabusTool");

    try
    {
      if (!syllabusService.checkPermission(SyllabusService.SECURE_ADD_ITEM))
      {
        return "permission_error";
      }
      else
      {
        int initPosition = syllabusManager.findLargestSyllabusPosition(syllabusItem) + 1;
        SyllabusData en = syllabusManager.createSyllabusDataObject(null, initPosition, null, null, SyllabusData.ITEM_DRAFT, "none", null, null, Boolean.FALSE, null, null, syllabusItem);
        en.setView("no");

        entry = new DecoratedSyllabusEntry(en);
        entry.setJustCreated(true);

        entries.clear();

        return "edit";
      }
    }
    catch (Exception e)
    {
      log.info(this + ".processListNew in SyllabusTool: " + e);
      FacesContext.getCurrentInstance().addMessage(
          null,
          MessageFactory.getMessage(FacesContext.getCurrentInstance(),
              "error_general", (new Object[] { e.toString() })));

      return null;
    }
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
      log.info(this + ".processListNewBulk in SyllabusTool: " + e);
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
	      log.info(this + ".processListEditBulk in SyllabusTool: " + e);
	      FacesContext.getCurrentInstance().addMessage(
	          null,
	          MessageFactory.getMessage(FacesContext.getCurrentInstance(),
	              "error_general", (new Object[] { e.toString() })));

	      return null;
	    }
  }

  public String processReadCancel()
  {
    //log.info(this + ".processReadCancel() in SyllabusTool");

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
      log.error(this + ".processReadCancel - " + e);
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
    //log.info(this + ".processReadSave() in SyllabusTool");

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
    			cleanedText  =  formattedText.processFormattedText(entry.getEntry().getAsset(), alertMsg);
    			if (cleanedText != null) 
    			{
					entry.getEntry().setAsset(cleanedText);
				}
    			if (alertMsg.length() > 0)
    			{
					log.debug("Syllabus content sanitized: {}", alertMsg);
    			}
    		 }
    		catch (Exception e)
    		{
    			log.warn(this + " " + cleanedText, e);
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
          SyllabusData saved = syllabusManager.saveSyllabus(getEntry().getEntry());
          
          for(int i=0; i<attachments.size(); i++)
          {
            syllabusManager.addSyllabusAttachToSyllabusData(saved,
                (SyllabusAttachment)attachments.get(i));            
          }
          //update calendar attachments
          if(saved.getCalendarEventIdStartDate() != null){
        	  syllabusManager.addCalendarAttachments(saved.getSyllabusItem().getContextId(), saved.getCalendarEventIdStartDate(), attachments);
          }
          if(saved.getCalendarEventIdEndDate() != null){
        	  syllabusManager.addCalendarAttachments(saved.getSyllabusItem().getContextId(), saved.getCalendarEventIdEndDate(), attachments);
          }
          
          syllabusService.draftChangeSyllabus(saved);
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
      log.info(this + ".processReadSave in SyllabusTool: " + e);
      FacesContext.getCurrentInstance().addMessage(
          null,
          MessageFactory.getMessage(FacesContext.getCurrentInstance(),
              "error_general", (new Object[] { e.toString() })));
    }

    return null;
  }

  public String processReadPost() throws PermissionException
  {
    //log.info(this + ".processReadPost() in SyllabusTool");

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
				cleanedText  =  formattedText.processFormattedText(entry.getEntry().getAsset(), alertMsg);
				if (cleanedText != null)
				{
					entry.getEntry().setAsset(cleanedText);
				}
    			if (alertMsg.length() > 0)
    			{
					log.debug("Syllabus content sanitized: {}", alertMsg);
    			}
    		 }
    		catch (Exception e)
    		{
    			log.warn(this + " " + cleanedText, e);
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
          SyllabusData saved = syllabusManager.saveSyllabus(getEntry().getEntry());

          syllabusService.postChangeSyllabus(saved);
          
          for(int i=0; i<attachments.size(); i++)
          {
            syllabusManager.addSyllabusAttachToSyllabusData(saved,
                (SyllabusAttachment)attachments.get(i));            
          }
          //update calendar attachments
          if(saved.getCalendarEventIdStartDate() != null){
        	  syllabusManager.addCalendarAttachments(saved.getSyllabusItem().getContextId(), saved.getCalendarEventIdStartDate(), attachments);
          }
          if(saved.getCalendarEventIdEndDate() != null){
        	  syllabusManager.addCalendarAttachments(saved.getSyllabusItem().getContextId(), saved.getCalendarEventIdEndDate(), attachments);
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
      log.info(this + ".processReadPost in SyllabusTool: " + e);
      FacesContext.getCurrentInstance().addMessage(
          null,
          MessageFactory.getMessage(FacesContext.getCurrentInstance(),
              "error_general", (new Object[] { e.toString() })));
    }

    return null;
  }

  public void downOnePlace(SyllabusData en)
  {
    //log.info(this + ".downOnePlace() in SyllabusTool");

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
    //log.info(this + ".upOnePlace() in SyllabusTool");

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
			cleanedText  =  formattedText.processFormattedText(entry.getEntry().getAsset(), alertMsg);
			if (cleanedText != null)
			{
				entry.getEntry().setAsset(cleanedText);
			}
			if (alertMsg.length() > 0)
			{
				log.debug("Syllabus content sanitized: {}", alertMsg);
			}
		 }
		catch (Exception e)
		{
			log.warn(this + " " + cleanedText, e);
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
			cleanedText  =  formattedText.processFormattedText(entry.getEntry().getAsset(), alertMsg);
			if (cleanedText != null)
			{
				entry.getEntry().setAsset(cleanedText);
			}
			if (alertMsg.length() > 0)
			{
				log.debug("Syllabus content sanitized: {}", alertMsg);
			}
		 }
		catch (Exception e)
		{
			log.warn(this + " " + cleanedText, e);
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
     * fu.parseRequest(req); } catch(Exception e) { log.error(e.getMessage(), e); }
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
        return "edit_redirect";
      }
    }
    catch (Exception e)
    {
      log.info(this + ".processRedirect in SyllabusTool: " + e);
      FacesContext.getCurrentInstance().addMessage(
          null,
          MessageFactory.getMessage(FacesContext.getCurrentInstance(),
              "error_general", (new Object[] { e.toString() })));
    }
    return null;
  }

  public String processEditCancelRedirect()
  {
    //log.info(this + ".processEditCancelRedirect() in SyllabusTool ");
    
	entries.clear();
    entry = null;

    return "main";
  }

  public String processEditSaveRedirect() throws PermissionException
  {
    //log.info(this + ".processEditSaveRedirect() in SyllabusTool");

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
      log.warn("Could not save redirect [{}], {}", currentRediredUrl, e.toString());
      FacesContext.getCurrentInstance().addMessage(null, MessageFactory.getMessage(FacesContext.getCurrentInstance(), "error_general", e.toString()));
    }

    return null;
  }

  public String processCreateAndEdit()
  {
    //log.info(this + ".processCreateAndEdit() in SyllabusTool");

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
      log.info(this + ".processCreateAndEdit() in SyllabusTool: " + e);
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
      log.info(this + ".sendNotification() in SyllabusTool.");
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
        log.error(this + ".processUpload() in SyllabusTool", e);
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
      Reference ref;
      
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

  public String processDeleteAttach() {
    ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
    String attachId = null;
    
    Map paramMap = context.getRequestParameterMap();
    Iterator itr = paramMap.keySet().iterator();
    while(itr.hasNext()) {
      Object key = itr.next();
      if( key instanceof String && "syllabus_current_attach".equals((String) key)) {
          attachId = (String) paramMap.get(key);
          break;
      }
    }

    removeAttachId = attachId;

    if (StringUtils.isNotBlank(removeAttachId)) {
      return "remove_attach_confirm";
    } else {
      return null;
    }

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
          if((thisAttach.getSyllabusAttachId()).toString().equals(removeAttachId))
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
        log.error(this + ".processRemoveAttach() - " + e);
      }
      
      removeAttachId = null;
      return "edit";
    }
    else
    {
      String id = null;
      try
      {
        SyllabusAttachment sa = syllabusManager.getSyllabusAttachment(removeAttachId);
        id = sa.getAttachmentId();
        boolean deleted = false;
        
        for(int i=0; i<attachments.size(); i++)
        {
          SyllabusAttachment thisAttach = (SyllabusAttachment)attachments.get(i);
          if((thisAttach.getSyllabusAttachId()).toString().equals(removeAttachId))
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
            if((thisAttach.getSyllabusAttachId()).toString().equals(removeAttachId))
            {
              oldAttachments.remove(i);
              break;
            }
          }
        }
        
        syllabusManager.removeSyllabusAttachmentObject(sa);
        removeCalendarAttachment(entry.getEntry(), sa);

        allAttachments.clear();
        for(int i=0; i<attachments.size(); i++)
        {
          allAttachments.add((SyllabusAttachment)attachments.get(i));
        }
        for(int i=0; i<oldAttachments.size(); i++)
        {
          allAttachments.add((SyllabusAttachment)oldAttachments.get(i));
        }

        if(id.toLowerCase().startsWith("/attachment")) contentHostingService.removeResource(id);
      }
      catch(Exception e)
      {
        log.warn("Attempting to remove the syllabus attachment [{}:{}], {}", removeAttachId, id, e.toString());
      }

      removeAttachId = null;
      return "read";
    }
  }
  
  public String processRemoveAttachCancel()
  {
    removeAttachId = null;
    if(entry.justCreated == true)
    {
      return "edit";
    }
    else
    {
      return "read";
    }
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
      Reference ref;
      
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

  public List getPrepareRemoveAttach() {
    List removedAttachments = new ArrayList();
    if(StringUtils.isNotBlank(removeAttachId)) {
      removedAttachments.add(syllabusManager.getSyllabusAttachment(removeAttachId));
    }
    return removedAttachments;
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
      log.error(this + ".processAddAttachRedirect - " + e);
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
   * 	Return the print JSP and do not just send the user to another URL without warning the user
   */
  public String getPrintFriendlyUrl()
  {
    return ServerConfigurationService.getToolUrl() + Entity.SEPARATOR
      + ToolManager.getCurrentPlacement().getId() + Entity.SEPARATOR + "printFriendly";
  }

    /**
     * @return
     * Get id attribute of syllabus
     */
    public String getSyllabusDataId()
    {
	String rv = "";
	DecoratedSyllabusEntry entry = getEntry();
	boolean alert = true;
	if (entry != null)
	    {
		SyllabusData syllabusData = entry.getEntry();
		if (syllabusData != null)
		    {
			Long id = syllabusData.getSyllabusId();
			if (id != null) 
			    {
				rv = id.toString();
				alert = false;
			    }
		    }
	    }
	  
	if (alert)
	    {
		setAlertMessage(rb.getString("refresh"));
	    }
	  
	return rv;
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
				  rv = DateFormatterUtil.format(rvDate, DATEPICKER_DATETIME_FORMAT, rb.getLocale());
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
   * @param date
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
					Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
					String startISODate = params.get(HIDDEN_START_ISO_DATE);
					if(DateFormatterUtil.isValidISODate(startISODate)){
					    syllabusData.setStartDate(DateFormatterUtil.parseISODate(startISODate));
					} else {
						syllabusData.setStartDate(null);
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
				  rv = DateFormatterUtil.format(rvDate, DATEPICKER_DATETIME_FORMAT, rb.getLocale());
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
   * @param date
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
					Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
					String endISODate = params.get(HIDDEN_END_ISO_DATE);
					if(DateFormatterUtil.isValidISODate(endISODate)){
					    syllabusData.setEndDate(DateFormatterUtil.parseISODate(endISODate));
					} else {
						syllabusData.setEndDate(null);
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
			  rv = syllabusData.getLinkCalendar();
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

		  Site site;
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
			  log.warn("Exception thrown while getting site", e);
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
  
  public class BulkSyllabusEntry {
	  public final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
	  private String title = "";
	  private Date startDate = null;
	  private Date endDate = null;
	  private boolean monday, tuesday, wednesday, thursday, friday, saturday, sunday = false;
	  private boolean linkCalendar;
	  private Date startTime;
	  private Date endTime;
	  private String bulkItems;
	  @Getter @Setter private String addSingleItem = "1";
	  private String addByItems;
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
			rv = DateFormatterUtil.format(getStartDate(), DATEPICKER_DATE_FORMAT, rb.getLocale());
		}
		return rv;
	  }
  
	public void setStartDateString(String date)
	{
		if(date == null || "".equals(date)){
			setStartDate(null);
		}else{
			Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
			String startISODate = params.get(HIDDEN_START_ISO_DATE);
			if(DateFormatterUtil.isValidISODate(startISODate)){
				setStartDate(DateFormatterUtil.parseISODate(startISODate));
			} else {
				setStartDate(null);
			}
		}
	}
	
	public String getEndDateString()
	  {
		String rv = "";
		if(getEndDate() != null){
			rv = DateFormatterUtil.format(getEndDate(), DATEPICKER_DATE_FORMAT, rb.getLocale());
		}
		return rv;
	  }

	public void setEndDateString(String date)
	{
		if(date == null || "".equals(date)){
			setEndDate(null);
		}else{
			Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
			String endISODate = params.get(HIDDEN_END_ISO_DATE);
			if(DateFormatterUtil.isValidISODate(endISODate)){
				setEndDate(DateFormatterUtil.parseISODate(endISODate));
			} else {
				setEndDate(null);
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

	public boolean isDisplayCalendarError() {
		return displayCalendarError;
	}

	public void setDisplayCalendarError(boolean displayCalendarError) {
		this.displayCalendarError = displayCalendarError;
	}
}
