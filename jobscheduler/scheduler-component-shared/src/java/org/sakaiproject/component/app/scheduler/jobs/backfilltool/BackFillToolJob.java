package org.sakaiproject.component.app.scheduler.jobs.backfilltool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;

import java.util.List;

/**
 * This job walks sites in the system and adds a tool to them.
 */
public class BackFillToolJob implements Job {

    private final Log log = LogFactory.getLog(BackFillToolJob.class);

    private SiteService siteService;

    private SessionManager sessionManager;

    private ToolManager toolManager;

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setToolManager(ToolManager toolManager) {
        this.toolManager = toolManager;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        Session session = sessionManager.getCurrentSession();
        try {
            session.setUserEid("admin");
            session.setUserId("admin");

            // We might just be wanting to process one site, this makes it
            String siteId = context.getMergedJobDataMap().getString("site.id");
            String type = context.getMergedJobDataMap().getString("type");
            int interval = 0;
            if (!context.getMergedJobDataMap().getString("interval").isEmpty()) {
                interval = context.getMergedJobDataMap().getIntValue("interval");
            }

            if (siteId == null || siteId.isEmpty()) {
                // Configurable properties can't be null, but we want to allow a null type when searching for sites.
                if (type != null && type.isEmpty()) {
                    type = null;
                }
                processSites(context, type, interval);
            } else {
                try {
                    Site site = siteService.getSite(siteId);
                    if (type == null || type.isEmpty() || type.equals(site.getType())) {
                        if (updateSite(context, site)) {
                            log.info("Updated site: "+ siteId);
                        } else {
                            log.info("Site wasn't updated: "+ siteId);
                        }
                    } else {
                        log.error("Both site ID and type specified but site doesn't match.");
                    }
                } catch (IdUnusedException e) {
                    throw new JobExecutionException("Failed to find site: " + siteId, e);
                }
            }

        } finally {
            session.clear();
        }
    }

    /**
     * This processes all sites looking for extra roles to add back.
     *
     * @param context  The job execution context.
     * @param type     If not null only search for sites of this type.
     * @param interval The interval to log process at (number of sites processed).
     */
    protected void processSites(JobExecutionContext context, String type, int interval) throws JobExecutionException {
        // We have to use the SiteService to iterate as it's the site that knows it's
        // type and so allows us to guess at the template that was used.
        // We only get the IDs so that we can save the sites.
        List<String> siteIds = siteService.getSiteIds(SiteService.SelectionType.ANY, type, null, null, SiteService.SortType.NONE, null);

        boolean skipUserSites = context.getMergedJobDataMap().getBoolean("skip.user.sites");

        int updated = 0, examined = 0, special = 0, user = 0;
        for (String siteId : siteIds) {
            Site site;
            // Skip special
            if (siteService.isSpecialSite(siteId)) {
                special++;
                continue;
            }
            // Skip user
            if (siteService.isUserSite(siteId)) {
                user++;
                if (skipUserSites) {
                    continue;
                }
            }
            try {
                site = siteService.getSite(siteId);
            } catch (IdUnusedException e) {
                log.warn("Couldn't load site: " + siteId);
                continue;
            }
            examined++;
            // Log progress
            if (interval != 0 && examined % interval == 0) {
                log.info("Processed: " + examined);
            }

            boolean saved = updateSite(context, site);

            if (saved) {
                updated++;
            }
        }
        log.info(String.format("Complete: Examined %d, Updated %d, Special %d, User %d",
                examined, updated, special, user));
    }

    protected boolean updateSite(JobExecutionContext context, Site site) throws JobExecutionException {
        // The toolId we are wanting to add to the site.
        String toolId = context.getMergedJobDataMap().getString("tool");
        if (toolId == null || toolId.isEmpty()) {
            throw new JobExecutionException("tool isn't set in job data.");
        }
        Tool tool = toolManager.getTool(toolId);
        if (tool == null) {
            throw new JobExecutionException("Failed to find tool with id: "+ toolId);
        }
        ToolConfiguration toolConfiguration = site.getToolForCommonId(toolId);
        if (toolConfiguration == null) {
            // Add the tool to the site.
            SitePage page = site.addPage();
            page.setTitle(tool.getTitle());
            page.addTool(tool);
            try {
                siteService.save(site);
                return true;
            } catch (IdUnusedException|PermissionException e) {
                log.warn("Failed to save site: "+ site.getId(), e);
            }
        } else {
            log.debug(String.format("Site: %s already has tool: %s on page: %s",
                site.getId(), toolId, toolConfiguration.getContainingPage().getId()));
        }
        return false;

    }
}
