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
 *       http://www.osedu.org/licenses/ECL-2.0
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.fileupload.FileItem;
import org.sakaiproject.api.app.syllabus.SyllabusAttachment;
import org.sakaiproject.api.app.syllabus.SyllabusData;
import org.sakaiproject.api.app.syllabus.SyllabusItem;
import org.sakaiproject.api.app.syllabus.SyllabusManager;
import org.sakaiproject.api.app.syllabus.SyllabusService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.FormattedText;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.faces.util.MessageFactory;

//sakai2 - no need to import org.sakaiproject.jsf.ToolBean here as sakai does.

/**
 * @author cwen TODO To change the template for this generated type comment go to Window - Preferences -
 *         Java - Code Style - Code Templates
 */
//sakai2 - doesn't implement ToolBean as sakai does.
public class SyllabusTool
{
  public class DecoratedSyllabusEntry
  {
    protected SyllabusData in_entry = null;

    protected boolean selected = false;

    protected boolean justCreated = false;
    
    protected ArrayList attachmentList = new ArrayList();

    public DecoratedSyllabusEntry(SyllabusData en)
    {
      in_entry = en;
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
      return "main_edit";
    }

    public String processUpMove()
    {
      upOnePlace(this.getEntry());
      return "main_edit";
    }
    
    public ArrayList getAttachmentList()
    {
      Set tempList = syllabusManager.getSyllabusAttachmentsForSyllabusData(in_entry);

      Iterator iter = tempList.iterator();
      while(iter.hasNext())
      {
        SyllabusAttachment sa = (SyllabusAttachment)iter.next();
        attachmentList.add(sa);
      }
      
      return attachmentList;
    }
    
    public void setAttachmentList(ArrayList attachmentList)
    {
      this.attachmentList = attachmentList;
    }
  }

  protected SyllabusManager syllabusManager;

  protected SyllabusItem syllabusItem;

  protected ArrayList entries;

  protected String userId;

  protected DecoratedSyllabusEntry entry = null;

  protected Log logger = LogFactory.getLog(SyllabusTool.class);

  protected String filename = null;

  protected String siteId = null;

  protected String editAble = null;

  protected String title = null;
  
  private boolean displayNoEntryMsg = false;
  
  private boolean displayTitleErroMsg = false;
  
  private boolean displayEvilTagMsg=false;
  
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
  
  private ContentHostingService contentHostingService;
  
  public SyllabusTool()
  {
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
                        
        Set tempEntries = syllabusManager
            .getSyllabiForSyllabusItem(syllabusItem);

        if (tempEntries != null)
        {
          Iterator iter = tempEntries.iterator();
          while (iter.hasNext())
          {
            SyllabusData en = (SyllabusData) iter.next();
            if (this.checkAccess())
            {
              DecoratedSyllabusEntry den = new DecoratedSyllabusEntry(en);
              entries.add(den);
            }
            else
            {
              if (en.getStatus().equals("Posted"))
              {
                DecoratedSyllabusEntry den = new DecoratedSyllabusEntry(en);
                entries.add(den);
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
                "error_permission", (new Object[] { e.toString() })));
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
        for(int i=0; i<entries.size(); i++)
        {
          DecoratedSyllabusEntry thisDecEn = (DecoratedSyllabusEntry) entries.get(i);
          if(thisDecEn.isSelected())
          {
            getFromDbAgain = false;
            break;
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
              if (this.checkAccess())
              {
                DecoratedSyllabusEntry den = new DecoratedSyllabusEntry(en);
                entries.add(den);
              }
              else
              {
                if (en.getStatus().equals("Posted"))
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
                "error_permission", (new Object[] { e.toString() })));
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
        if (den.isSelected())
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
    try
    {
      if((syllabusItem != null) && (syllabusItem.getContextId().equals(currentSiteId))
          && (syllabusItem.getUserId().equals(currentUserId)))
      {
        return syllabusItem;
      }

      syllabusItem = syllabusManager.getSyllabusItemByContextId(currentSiteId);

      if (syllabusItem == null)
      {
        if (!this.checkAccess())
        {
        	throw new PermissionException(currentUserId, SiteService.SECURE_UPDATE_SITE, currentSiteId); 
        }
        else
        {
          syllabusItem = syllabusManager.createSyllabusItem(currentUserId,
              currentSiteId, null);
        }
      }
    }
    catch (Exception e)
    {
      logger.info(this + ".getSyllabusItem() in SyllabusTool " + e);
      FacesContext.getCurrentInstance().addMessage(
          null,
          MessageFactory.getMessage(FacesContext.getCurrentInstance(),
              "error_permission", (new Object[] { e.toString() })));
    }
    return syllabusItem;
  }

  public void setSyllabusItem(SyllabusItem syllabusItem)
  {
    this.syllabusItem = syllabusItem;
  }

  public void setLogger(Log logger)
  {
    this.logger = logger;
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
  
  //testing the access to control the "create/edit"
  //button showing up or not on main page.
  public String getEditAble()
  {
    if (checkAccess())
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

  public String processDeleteCancel()
  {
    //logger.info(this + ".processDeleteCancel() in SyllabusTool.");

    entries.clear();
    entry = null;
  
    return "main_edit";
  }

  public String processDelete()
      throws org.sakaiproject.exception.PermissionException
  {
    //logger.info(this + ".processDelete() in SyllabusTool");

    ArrayList selected = getSelectedEntries();
    try
    {
      if (!this.checkAccess())
      {
        return "permission_error";
      }
      else
      {
        Set dataSet = syllabusManager.getSyllabiForSyllabusItem(syllabusItem);
        for (int i = 0; i < selected.size(); i++)
        {
          DecoratedSyllabusEntry den = (DecoratedSyllabusEntry) selected.get(i);
          
//          if(den.getEntry().getStatus().equalsIgnoreCase("Posted"))
//          {
            syllabusService.deletePostedSyllabus(den.getEntry());
//          }
          
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
          syllabusManager.removeSyllabusFromSyllabusItem(syllabusItem, den
              .getEntry());
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
              "error_permission", (new Object[] { e.toString() })));
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
      if (!this.checkAccess())
      {
        return "permission_error";
      }
      else
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
        if(entry.getEntry().getAsset()!=null)
        {
        	StringBuilder alertMsg = new StringBuilder();
        	String errorMsg= null;
    		try
    		{
    			errorMsg =  FormattedText.processFormattedText(entry.getEntry().getAsset(), alertMsg);
    			if (alertMsg.length() > 0)
    			{
    			  evilTagMsg =alertMsg.toString();
    			  displayEvilTagMsg=true;
    			  return "edit";
    			}
    		 }
    		catch (Exception e)
    		{
    			logger.warn(this + " " + errorMsg,e);
    		}
        }
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
              "error_permission", (new Object[] { e.toString() })));
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
      if (!this.checkAccess())
      {
        return "permission_error";
      }
      else
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
        if(entry.getEntry().getAsset()!=null)
        {
        	StringBuilder alertMsg = new StringBuilder();
        	String errorMsg= null;
        	try
    		{
    			errorMsg =  FormattedText.processFormattedText(entry.getEntry().getAsset(), alertMsg);
    			if (alertMsg.length() > 0)
    			{
					evilTagMsg =alertMsg.toString();
					displayEvilTagMsg=true;
					return "edit";
    			}
    		 }
    		catch (Exception e)
    		{
    			logger.warn(this + " " + errorMsg,e);
    		}
        }
        if (entry.justCreated == true)
        {
          getEntry().getEntry().setStatus("Posted");
          syllabusManager.addSyllabusToSyllabusItem(syllabusItem, getEntry()
              .getEntry());
          //syllabusManager.saveSyllabusItem(syllabusItem);
          for(int i=0; i<attachments.size(); i++)
          {
            syllabusManager.addSyllabusAttachToSyllabusData(getEntry().getEntry(), 
                (SyllabusAttachment)attachments.get(i));            
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
              "error_permission", (new Object[] { e.toString() })));
    }

    return null;
  }

  public String processListDelete() throws PermissionException
  {
    //logger.info(this + ".processListDelete() in SyllabusTool");

    try
    {
      if (!this.checkAccess())
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
        }
        return "delete_confirm";
      }
    }
    catch (Exception e)
    {
      logger.info(this + ".processListDelete in SyllabusTool: " + e);
      FacesContext.getCurrentInstance().addMessage(
          null,
          MessageFactory.getMessage(FacesContext.getCurrentInstance(),
              "error_permission", (new Object[] { e.toString() })));
    }

    return null;
  }

  public String processListNew() throws PermissionException
  {
    //logger.info(this + ".processListNew() in SyllabusTool");

    try
    {
      if (!this.checkAccess())
      {
        return "permission_error";
      }
      else
      {
        int initPosition = syllabusManager.findLargestSyllabusPosition(
            syllabusItem).intValue() + 1;
        SyllabusData en = syllabusManager.createSyllabusDataObject(null,
            new Integer(initPosition), null, null, "Draft", "none");
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
              "error_permission", (new Object[] { e.toString() })));

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
      if (!this.checkAccess())
      {
        return "permission_error";
      }
      else
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
        if(entry.getEntry().getAsset()!=null)
        {
        	StringBuilder alertMsg = new StringBuilder();
        	String errorMsg= null;
        	try
    		{
    			errorMsg =  FormattedText.processFormattedText(entry.getEntry().getAsset(), alertMsg);
    			if (alertMsg.length() > 0)
    			{
					evilTagMsg =alertMsg.toString();
					displayEvilTagMsg=true;
					return "edit";
    			}
    		 }
    		catch (Exception e)
    		{
    			logger.warn(this + " " + errorMsg,e);
    		}
        }
        if (entry.justCreated == false)
        {
          getEntry().getEntry().setStatus("Draft");
          syllabusManager.saveSyllabus(getEntry().getEntry());
          
          for(int i=0; i<attachments.size(); i++)
          {
            syllabusManager.addSyllabusAttachToSyllabusData(getEntry().getEntry(), 
                (SyllabusAttachment)attachments.get(i));            
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
              "error_permission", (new Object[] { e.toString() })));
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
      if (!this.checkAccess())
      {
        return "permission_error";
      }
      else
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
        if(entry.getEntry().getAsset()!=null)
        {
        	StringBuilder alertMsg = new StringBuilder();
        	String errorMsg= null;
        	try
    		{
    			errorMsg =  FormattedText.processFormattedText(entry.getEntry().getAsset(), alertMsg);
    			if (alertMsg.length() > 0)
    			{
					evilTagMsg =alertMsg.toString();
					displayEvilTagMsg=true;
					return "edit";
    			}
    		 }
    		catch (Exception e)
    		{
    			logger.warn(this + " " + errorMsg,e);
    		}
        }
        if (entry.justCreated == false)
        {
          getEntry().getEntry().setStatus("Posted");
          syllabusManager.saveSyllabus(getEntry().getEntry());

          syllabusService.postChangeSyllabus(getEntry().getEntry());
          
          for(int i=0; i<attachments.size(); i++)
          {
            syllabusManager.addSyllabusAttachToSyllabusData(getEntry().getEntry(), 
                (SyllabusAttachment)attachments.get(i));            
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
              "error_permission", (new Object[] { e.toString() })));
    }

    return null;
  }

  public void downOnePlace(SyllabusData en)
  {
    //logger.info(this + ".downOnePlace() in SyllabusTool");

    SyllabusData swapData = null;
    Iterator iter = syllabusManager.getSyllabiForSyllabusItem(syllabusItem)
        .iterator();
    while (iter.hasNext())
    {
      SyllabusData data = (SyllabusData) iter.next();
      if (en.equals(data))
      {
        if (iter.hasNext()) swapData = (SyllabusData) iter.next();
        break;
      }
    }

    if (swapData != null)
        syllabusManager.swapSyllabusDataPositions(syllabusItem, en, swapData);

    entries.clear();
    entry = null;
  }

  public void upOnePlace(SyllabusData en)
  {
    //logger.info(this + ".upOnePlace() in SyllabusTool");

    SyllabusData swapData = null;
    Iterator iter = syllabusManager.getSyllabiForSyllabusItem(syllabusItem)
        .iterator();
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
    }

    if (swapData != null)
        syllabusManager.swapSyllabusDataPositions(syllabusItem, en, swapData);

    entries.clear();
    entry = null;
  }

  public String processEditPreview()
  {
	displayTitleErroMsg = false;
    displayEvilTagMsg=false;
    if(entry.getEntry().getTitle() == null)
    {
      displayTitleErroMsg = true;
      return "edit";          
    }
    if(entry.getEntry().getTitle().trim().equals(""))
    {
      displayTitleErroMsg = true;
      return "edit";
    }
    if(entry.getEntry().getAsset()!=null)
    {
    	StringBuilder alertMsg = new StringBuilder();
    	String errorMsg= null;
    	try
		{
			errorMsg =  FormattedText.processFormattedText(entry.getEntry().getAsset(), alertMsg);
			if (alertMsg.length() > 0)
			{
				evilTagMsg =alertMsg.toString();
				displayEvilTagMsg=true;
				return "edit";
			}
		 }
		catch (Exception e)
		{
			logger.warn(this + " " + errorMsg,e);
		}
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
    if(entry.getEntry().getTitle() == null)
    {
      displayTitleErroMsg = true;
      return "read";          
    }
    if(entry.getEntry().getTitle().trim().equals(""))
    {
      displayTitleErroMsg = true;
      return "read";
    }
    if(entry.getEntry().getAsset()!=null)
    {
    	StringBuilder alertMsg = new StringBuilder();
    	String errorMsg= null;
    	try
		{
			errorMsg =  FormattedText.processFormattedText(entry.getEntry().getAsset(), alertMsg);
			if (alertMsg.length() > 0)
			{
				evilTagMsg =alertMsg.toString();
				displayEvilTagMsg=true;
				return "edit";
			}
		 }
		catch (Exception e)
		{
			logger.warn(this + " " + errorMsg,e);
		}
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
      if (!this.checkAccess())
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
      logger.info(this + ".processRedirect in SyllabusTool: " + e);
      FacesContext.getCurrentInstance().addMessage(
          null,
          MessageFactory.getMessage(FacesContext.getCurrentInstance(),
              "error_permission", (new Object[] { e.toString() })));
    }
    return null;
  }

  public String processEditCancelRedirect()
  {
    //logger.info(this + ".processEditCancelRedirect() in SyllabusTool ");
    
	entries.clear();
    entry = null;

    return "main_edit";
  }

  public String processEditSaveRedirect() throws PermissionException
  {
    //logger.info(this + ".processEditSaveRedirect() in SyllabusTool");

    try
    {
      if (!this.checkAccess())
      {
        return "permission_error";
      }
      else
      {
      	currentRediredUrl = currentRediredUrl.replaceAll("\"", ""); 
    	  syllabusItem.setRedirectURL(currentRediredUrl);
        syllabusManager.saveSyllabusItem(syllabusItem);

        entries.clear();
        entry = null;
      }

      return "main_edit";
    }
    catch (Exception e)
    {
      logger.info(this + ".processEditSaveRedirect in SyllabusTool: " + e);
      FacesContext.getCurrentInstance().addMessage(
          null,
          MessageFactory.getMessage(FacesContext.getCurrentInstance(),
              "error_permission", (new Object[] { e.toString() })));
    }

    return null;
  }

  public String processCreateAndEdit()
  {
    //logger.info(this + ".processCreateAndEdit() in SyllabusTool");

    try
    {
      if (!this.checkAccess())
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
              "error_permission", (new Object[] { e.toString() })));
    }
    return null;
  }

  public String processStudentView()
  {
    return "main";
  }

  public boolean checkAccess()
  {
    //sakai2 - use Placement to get context instead of getting currentSitePageId from PortalService in sakai.
    Placement placement = ToolManager.getCurrentPlacement();
    String currentSiteId = placement.getContext();
    boolean allowOrNot = SiteService.allowUpdateSite(currentSiteId);
    return SiteService.allowUpdateSite(currentSiteId);
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
      UIComponent component = event.getComponent();
      Object newValue = event.getNewValue();
      Object oldValue = event.getOldValue();
      PhaseId phaseId = event.getPhaseId();
      Object source = event.getSource();
      
      if (newValue instanceof String) return "";
      if (newValue == null) return "";
      
      try
      {
        FileItem item = (FileItem) event.getNewValue();
        String fileName = item.getName();
        byte[] fileContents = item.get();
        
        ResourcePropertiesEdit props = contentHostingService.newResourceProperties();
        
        String tempS = fileName;
        //logger.info(tempS);
        int lastSlash = tempS.lastIndexOf("/") > tempS.lastIndexOf("\\") ? 
            tempS.lastIndexOf("/") : tempS.lastIndexOf("\\");
        if(lastSlash > 0)
          fileName = tempS.substring(lastSlash+1);
            
        ContentResource thisAttach = contentHostingService.addAttachmentResource(fileName, item.getContentType(), fileContents, props);
        
        SyllabusAttachment attachObj = syllabusManager.createSyllabusAttachmentObject(thisAttach.getId(), fileName);
        ////////revise        syllabusManager.addSyllabusAttachToSyllabusData(getEntry().getEntry(), attachObj);
        attachments.add(attachObj);
        
        String ss = thisAttach.getUrl();
        String fileWithWholePath = thisAttach.getUrl();
        
        ContentResource getAttach = contentHostingService.getResource(thisAttach.getId());
        
        String s = ss;
        
        if(entry.justCreated != true)
        {
          allAttachments.add(attachObj);
        }
      }
      catch (Exception e)
      {
        logger.error(this + ".processUpload() in SyllabusTool " + e);
        e.printStackTrace();
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
    if(entry.justCreated == true)
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
        
        ContentResource cr = contentHostingService.getResource(id);
        syllabusManager.removeSyllabusAttachmentObject(sa);
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
}