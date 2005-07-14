/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
package org.sakaiproject.tool.syllabus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import javax.faces.context.FacesContext;

import org.sakaiproject.api.app.syllabus.SyllabusData;
import org.sakaiproject.api.app.syllabus.SyllabusItem;
import org.sakaiproject.api.app.syllabus.SyllabusManager;
import org.sakaiproject.api.app.syllabus.SyllabusService;
import org.sakaiproject.api.kernel.tool.Placement;
import org.sakaiproject.api.kernel.tool.cover.ToolManager;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.service.framework.log.Logger;
import org.sakaiproject.service.framework.portal.cover.PortalService;
import org.sakaiproject.service.framework.session.cover.UsageSessionService;
import org.sakaiproject.service.legacy.site.cover.SiteService;
import org.sakaiproject.service.legacy.user.cover.UserDirectoryService;

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
      logger.info(this + ".processListRead() in SyllabusTool.");

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
  }

  protected SyllabusManager syllabusManager;

  protected SyllabusItem syllabusItem;

  protected ArrayList entries;

  protected String userId;

  protected DecoratedSyllabusEntry entry = null;

  protected Logger logger = null;

  protected String filename = null;

  protected String siteId = null;

  protected String editAble = null;

  protected String title = null;
  
  private boolean displayNoEntryMsg = false;
  
  private boolean displayTitleErroMsg = false;
  
  private SyllabusService syllabusService;

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
      logger.info(this + ".getEntries() in SyllabusTool");

      siteId = currentSiteId;
      try
      {
        if (entries == null)
          entries = new ArrayList();
        else
          entries.clear();
        syllabusItem = syllabusManager.getSyllabusItemByContextId(siteId);
        if (syllabusItem == null)
        {
          if (!this.checkAccess())
          {
            throw new PermissionException(UsageSessionService
                .getSessionUserId(), "syllabus_access_athz", "");
          }
          else
          {
            syllabusItem = syllabusManager.createSyllabusItem(userId, siteId,
                null);
          }
        }

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
      syllabusItem = syllabusManager.getSyllabusItemByContextId(currentSiteId);

      if (syllabusItem == null)
      {
        if (!this.checkAccess())
        {
          throw new PermissionException(UsageSessionService.getSessionUserId(),
              "syllabus_access_athz", "");
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

  public void setLogger(Logger logger)
  {
    this.logger = logger;
  }

  public String getFilename()
  {
    logger.info(this + ".getFilename() in SyllabusTool");
    return filename;
  }

  public void setFilename(String filename)
  {
    logger.info(this + ".setFilename() in SyllabusTool");
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

  public String processDeleteCancel()
  {
    logger.info(this + ".processDeleteCancel() in SyllabusTool.");

    entries.clear();
    entry = null;
    syllabusItem = null;

    return "main_edit";
  }

  public String processDelete()
      throws org.sakaiproject.exception.PermissionException
  {
    logger.info(this + ".processDelete() in SyllabusTool");

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
          
          if(den.getEntry().getStatus().equalsIgnoreCase("Posted"))
          {
            syllabusService.deletePostedSyllabus(den.getEntry());
          }
          
          syllabusManager.removeSyllabusFromSyllabusItem(syllabusItem, den
              .getEntry());
        }
      }
      entries.clear();
      entry = null;
      syllabusItem = null;

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
    syllabusItem = null;

    return null;
  }

  public String processEditCancel()
  {
    logger.info(this + ".processEditCancel() in SyllabusTool ");

    if (entry != null){
      syllabusManager.removeSyllabusDataObject(entry.getEntry());      
    }
    
    displayTitleErroMsg = false;
    entries.clear();
    entry = null;
    syllabusItem = null;

    return "main_edit";
  }

  public String processEditSave() throws PermissionException
  {
    logger.info(this + ".processEditSave() in SyllabusTool");

    try
    {
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
        if (entry.justCreated == true)
        {
          syllabusManager.addSyllabusToSyllabusItem(syllabusItem, getEntry()
              .getEntry());
        }
      }

      displayTitleErroMsg = false;
      entries.clear();
      entry = null;
      syllabusItem = null;

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
    logger.info(this + ".processEditPost() in SyllabusTool");

    try
    {
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
        if (entry.justCreated == true)
        {
          getEntry().getEntry().setStatus("Posted");
          syllabusManager.addSyllabusToSyllabusItem(syllabusItem, getEntry()
              .getEntry());
          //syllabusManager.saveSyllabusItem(syllabusItem);
          syllabusService.postNewSyllabus(getEntry().getEntry());
          
          entries.clear();
          entry = null;
          syllabusItem = null;
          displayTitleErroMsg = false;
          
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
    logger.info(this + ".processListDelete() in SyllabusTool");

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
    logger.info(this + ".processListNew() in SyllabusTool");

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
    logger.info(this + ".processReadCancel() in SyllabusTool");

    displayTitleErroMsg = false;
    entries.clear();
    entry = null;
    syllabusItem = null;

    return "main_edit";
  }

  public String processReadSave() throws PermissionException
  {
    logger.info(this + ".processReadSave() in SyllabusTool");

    try
    {
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
        if (entry.justCreated == false)
        {
          getEntry().getEntry().setStatus("Draft");
          syllabusManager.saveSyllabus(getEntry().getEntry());
        }
      }
      displayTitleErroMsg = false;
      entries.clear();
      entry = null;
      syllabusItem = null;

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
    logger.info(this + ".processReadPost() in SyllabusTool");

    try
    {
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
        if (entry.justCreated == false)
        {
          getEntry().getEntry().setStatus("Posted");
          syllabusManager.saveSyllabus(getEntry().getEntry());

          syllabusService.postChangeSyllabus(getEntry().getEntry());
          
          displayTitleErroMsg = false;
          entries.clear();
          entry = null;
          syllabusItem = null;

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
    logger.info(this + ".downOnePlace() in SyllabusTool");

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
    syllabusItem = null;
  }

  public void upOnePlace(SyllabusData en)
  {
    logger.info(this + ".upOnePlace() in SyllabusTool");

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
    syllabusItem = null;
  }

  public String processEditPreview()
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
      return "preview";
    }
  }

  public String processEditPreviewBack()
  {
    return "edit";
  }

  public String processReadPreview()
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
      return "read_preview";
    }
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
    logger.info(this + ".processEditCancelRedirect() in SyllabusTool ");

    entries.clear();
    entry = null;
    syllabusItem = null;

    return "main_edit";
  }

  public String processEditSaveRedirect() throws PermissionException
  {
    logger.info(this + ".processEditSaveRedirect() in SyllabusTool");

    try
    {
      if (!this.checkAccess())
      {
        return "permission_error";
      }
      else
      {
        syllabusManager.saveSyllabusItem(syllabusItem);

        entries.clear();
        entry = null;
        syllabusItem = null;
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
    logger.info(this + ".processCreateAndEdit() in SyllabusTool");

    try
    {
      if (!this.checkAccess())
      {
        return "permission_error";
      }
      else
      {
        syllabusManager.saveSyllabusItem(syllabusItem);

        entries.clear();
        entry = null;
        syllabusItem = null;
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
    return SiteService.findTool(PortalService.getCurrentToolId()).getTitle();
  }
  
/*test send email.  private void sendNotification()
  {
    String realmName = "/site/" + siteId;
    try
    {
      Realm siteRealm = RealmService.getRealm(realmName);
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
}



