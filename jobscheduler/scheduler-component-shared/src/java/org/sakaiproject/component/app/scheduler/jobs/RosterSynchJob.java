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
//import org.sakaiproject.service.legacy.realm.RealmEdit;
//import org.sakaiproject.service.legacy.realm.cover.RealmService;
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
//          realmEdit = RealmService.editRealm("/site/" + siteId);
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
//            RealmService.commitEdit(realmEdit);
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