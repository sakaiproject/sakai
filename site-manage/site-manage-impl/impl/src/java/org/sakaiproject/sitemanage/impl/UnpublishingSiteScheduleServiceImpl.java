package org.sakaiproject.sitemanage.impl;

import lombok.extern.slf4j.Slf4j;
import lombok.Setter;
import java.time.Instant;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitemanage.api.UnpublishingSiteScheduleService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

@Slf4j
public class UnpublishingSiteScheduleServiceImpl implements org.sakaiproject.sitemanage.api.UnpublishingSiteScheduleService {
    @Setter private ScheduledInvocationManager scheduledInvocationManager;
    @Setter private SiteService siteService;
    @Setter private EventTrackingService eventTrackingService;
    @Setter private SessionManager sessionManager;
    private final String GROUPMEMEBERSHIP = "site.upd.grp.mbrshp";
    private final String SITEUPDATE = "site.upd";
    private final String READANNOUNCEMENTS = "annc.read";

    public void scheduleUnpublishing(Instant when, String siteId){
        removeScheduledUnpublish(siteId);   //remove any existing one first
        scheduledInvocationManager.createDelayedInvocation(when, "org.sakaiproject.sitemanage.api.UnpublishingSiteScheduleService", siteId);
    }

    public void removeScheduledUnpublish(String siteId){
        scheduledInvocationManager.deleteDelayedInvocation("org.sakaiproject.sitemanage.api.UnpublishingSiteScheduleService", siteId);
    }

    public void execute(String siteId){
        Session session = null;
        try {
            session = sessionManager.getCurrentSession();
            session.setUserEid("admin");
            session.setUserId("admin");
            Site gettingUnpublished = siteService.getSite(siteId);
            if (gettingUnpublished.isPublished()) {
              eventTrackingService.post(eventTrackingService.newEvent(siteService.EVENT_SITE_UNPUBLISH, gettingUnpublished.getReference(), true));
            }
            gettingUnpublished.setPublished(false);
            siteService.save(gettingUnpublished);
        } catch (IdUnusedException | PermissionException i) {
            log.error("Unable to schedule unpublishing for site: " + siteId);
        } finally {
            if (session != null) {
                session.clear();
            }
        }
    }
}
