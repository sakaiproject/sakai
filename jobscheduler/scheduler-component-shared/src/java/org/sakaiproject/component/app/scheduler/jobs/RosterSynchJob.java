/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

//package org.sakaiproject.component.app.scheduler.jobs;
//
//import java.sql.Connection;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.quartz.Job;
//import org.quartz.JobExecutionContext;
//import org.quartz.JobExecutionException;
//import org.sakaiproject.exception.IdUnusedException;
//import org.sakaiproject.exception.InUseException;
//import org.sakaiproject.exception.PermissionException;
//import org.sakaiproject.service.legacy.authzGroup.RealmEdit;
//import org.sakaiproject.service.legacy.authzGroup.cover.RealmService;
//import org.sakaiproject.service.legacy.resource.ResourceProperties;
//import org.sakaiproject.service.legacy.site.Site;
//import org.sakaiproject.service.legacy.site.cover.SiteService;
//
//public class RosterSynchJob implements Job
//{
//
//  private static final Log LOG = LogFactory.getLog(RosterSynchJob.class);
//
//  /**
//   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
//   */
//  public void execute(JobExecutionContext arg0) throws JobExecutionException
//  {
//    LOG.info(this + ": Starting rosterSync()");    
//    
//    try
//    {
//      Connection conn = getOncourseConnection();
//      Statement statement = null;
//      ResultSet result = null;
//      Map propertyCriteria = new HashMap();
//      propertyCriteria.put(PROP_SITE_ONCOURSE_COURSE_ID, "2005-5-");
//
//      List linkedSites = SiteService.getSites(
//          org.sakaiproject.service.legacy.site.SiteService.SelectionType.ANY,
//          null, null, propertyCriteria,
//          org.sakaiproject.service.legacy.site.SiteService.SortType.NONE, null);
//
//      Iterator linkedSitesIterator = linkedSites.iterator();
//      while (linkedSitesIterator.hasNext())
//      {
//        Site site = (Site) linkedSitesIterator.next();
//        String siteId = site.getId();
//        ResourceProperties properties = site.getProperties();
//        String oncourseCourseId = properties
//            .getProperty(PROP_SITE_ONCOURSE_COURSE_ID);
//
//        LOG.info(this + ": Considering realm: /site/" + siteId);
//        RealmEdit realmEdit = null;
//
//        try
//        {
//          realmEdit = AuthzGroupService.editRealm("/site/" + siteId);
//        }
//        catch (IdUnusedException e)
//        {
//          LOG.info(this + ": IdUnusedException: " + e);
//        }
//        catch (PermissionException e)
//        {
//          LOG.info(this + ": PermissionException: " + e);
//        }
//        catch (InUseException e)
//        {
//          LOG.info(this + ": InUseException: " + e);
//        }
//
//        if (realmEdit != null)
//        {
//          statement = conn.createStatement();
//          result = null;
//          String sql = "SELECT USER_ID,IS_AUTHOR FROM COURSE_USER WHERE COURSE_ID = "
//              + "'" + oncourseCourseId + "'";
//
//          LOG.info(this + ": " + sql);
//
//          result = statement.executeQuery(sql);
//
//          if (result != null)
//          {
//            realmEdit.removeUsers();
//            while (result.next())
//            {
//              String username = result.getString("USER_ID");
//              String role = "access";
//              if (result.getString("IS_AUTHOR").equals("1"))
//              {
//                role = "maintain";
//              }
//
//              LOG.info(this + ": adding user '" + username
//                  + "' with role '" + role + "'");
//
//              realmEdit.addUserRole(username, role, true, false);
//            }
//
//            AuthzGroupService.commitEdit(realmEdit);
//          }
//        }
//      }
//    }
//    catch (SQLException e)
//    {
//      LOG.info(this + ": SQLException: " + e);
//    }
//
//    LOG.info(this + ": Stopping rosterSync()");
//  }
//
//}