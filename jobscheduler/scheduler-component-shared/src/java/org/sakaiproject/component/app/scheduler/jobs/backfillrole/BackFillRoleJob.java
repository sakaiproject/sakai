package org.sakaiproject.component.app.scheduler.jobs.backfillrole;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.authz.api.*;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * This Job looks at the site realms. For each realm it attempts to work out the template realm it came from.
 * Then it looks for new roles in the template and copies them into the realm.
 *
 */
public class BackFillRoleJob  implements Job {

    private static final String TEMPLATE_PREFIX = "!site.template";

    private final Log log = LogFactory.getLog(BackFillRoleJob.class);

    private SiteService siteService;

    private AuthzGroupService authzService;

    private SessionManager sessionManager;

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setAuthzService(AuthzGroupService authzService) {
        this.authzService = authzService;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        Session session = sessionManager.getCurrentSession();
        try {
            session.setUserEid("admin");
            session.setUserId("admin");

            AuthzGroup defaultTemplate;

            try {
                defaultTemplate = authzService.getAuthzGroup(TEMPLATE_PREFIX);
            } catch (GroupNotDefinedException e) {
                throw new JobExecutionException("Couldn't find default template: "+ TEMPLATE_PREFIX + " giving up.", e);
            }

            // We might just be wanting to process one site, this makes it
            String siteId = context.getMergedJobDataMap().getString("siteId");
            String type = context.getMergedJobDataMap().getString("type");
            String role = context.getMergedJobDataMap().getString("role");
            int interval = 0;
            if (!context.getMergedJobDataMap().getString("interval").isEmpty()) {
                interval = context.getMergedJobDataMap().getIntValue("interval");
            }

            if (siteId != null) {
                try {
                    Site site = siteService.getSite(siteId);
                    if (type != null && !type.equals(site.getType())) {
                        log.error("Both site ID and type specified but site doesn't match.");
                    } else {
                        updateSite(defaultTemplate, site, role);
                    }
                } catch (IdUnusedException e) {
                    throw new JobExecutionException("Failed to find site: "+ siteId, e);
                }
            } else {
                processSites(defaultTemplate, type, interval, role);
            }

        } finally {
            session.clear();
        }
    }

    /**
     * This processes all sites looking for extra roles to add back.
     * @param defaultTemplate The default template we are using.
     * @param type If not null only search for sites of this type.
     * @param interval The interval to log process at (number of sites processed).
     * @param role Only update the specified role.
     */
    protected void processSites(AuthzGroup defaultTemplate, String type, int interval, String role) {
        // We have to use the SiteService to iterate as it's the site that knows it's
        // type and so allows us to guess at the template that was used.
        // We only get the IDs so that we can save the sites.
        List<String> siteIds = siteService.getSiteIds(SiteService.SelectionType.ANY, type, null, null, SiteService.SortType.NONE, null);

        int updated = 0, examined = 0, special = 0, user = 0;
        for (String siteId: siteIds) {
            Site site;
            // Skip special
            if (siteService.isSpecialSite(siteId)) {
                special++;
                continue;
            }
            // Skip user
            if (siteService.isUserSite(siteId)) {
                user++;
                continue;
            }
            try {
                site = siteService.getSite(siteId);
            } catch (IdUnusedException e) {
                log.warn("Couldn't load site: "+ siteId);
                continue;
            }
            examined++;
            // Log progress
            if (interval != 0 && examined % interval == 0) {
                log.info("Processed: "+ examined);
            }

            boolean saved = updateSite(defaultTemplate, site, role);

            if (saved) {
                updated++;
            }
        }
        log.info(String.format("Complete: Examined %d, Updated %d, Special %d, User %d",
                examined, updated, special, user));
    }

    protected boolean updateSite(AuthzGroup defaultTemplate, Site site, String role) {

        AuthzGroup template = defaultTemplate;
        String siteType = site.getType();
        if (siteType != null && !siteType.isEmpty()) {
            String templateId = TEMPLATE_PREFIX + "." + siteType;
            try {
                template = authzService.getAuthzGroup(templateId);
            } catch (GroupNotDefinedException e) {
                log.debug("Failed to find template realm: "+ templateId);
            }
        }

        boolean needSave = false;
        boolean saved = false;


        Set<Role> roles = template.getRoles();
        if (role != null) {
            Role singleRole = template.getRole(role);
            if (singleRole == null) {
                // If we can't find the role in the template then don't do anything.
                return false;
            } else {
                roles = Collections.singleton(singleRole);
            }
        }
        for (Role templateRole : roles) {
            Role siteRole = site.getRole(templateRole.getId());
            if (siteRole == null) {
                // Copy across role.
                try {
                    site.addRole(templateRole.getId(), templateRole);
                    log.debug(String.format("Copied %s to site %s", templateRole.getId(), site.getId()));
                    needSave = true;
                } catch (RoleAlreadyDefinedException e) {
                    log.warn(String.format("Role %s already exists in site %s", templateRole.getId(), site.getId()));
                }
            }
        }
        if (needSave) {
            try {
                siteService.save(site);
                saved = true;
            } catch (IdUnusedException e) {
                log.warn("Failed to save site as the ID is already used.", e);
            } catch (PermissionException e) {
                // We don't expect this to ever happen.
                log.error("No permission to save site.", e);
            }
        }
        return saved;
    }


}
