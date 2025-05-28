package org.sakaiproject.sitemanage.impl;

import lombok.extern.slf4j.Slf4j;
import lombok.Setter;
import java.time.Instant;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

@Setter
@Slf4j
public class PublishingSiteScheduleServiceImpl implements org.sakaiproject.sitemanage.api.PublishingSiteScheduleService {
    private ScheduledInvocationManager scheduledInvocationManager;
    private SiteService siteService;
    private EventTrackingService eventTrackingService;
    private SessionManager sessionManager;

    @Override
    public void schedulePublishing(Instant when, String siteId){
        removeScheduledPublish(siteId);   //remove any existing one first
        scheduledInvocationManager.createDelayedInvocation(when, "org.sakaiproject.sitemanage.api.PublishingSiteScheduleService", siteId);
    }

    @Override
    public void removeScheduledPublish(String siteId){
        scheduledInvocationManager.deleteDelayedInvocation("org.sakaiproject.sitemanage.api.PublishingSiteScheduleService", siteId);
    }

    @Override
    public void execute(String siteId){
        Session session = null;
        try{
            session = sessionManager.getCurrentSession();
            session.setUserEid("admin");
            session.setUserId("admin");
            Site gettingPublished = siteService.getSite(siteId);
            if (!gettingPublished.isPublished()) {
              eventTrackingService.post(eventTrackingService.newEvent(SiteService.EVENT_SITE_PUBLISH, gettingPublished.getReference(), true));
              gettingPublished.setPublished(true);
              siteService.save(gettingPublished);
              log.info("Scheduled publishing completed for site: {}", siteId);
            }
            else {
                log.info("Site {} is already published, skipping publishing", siteId);
            }
        } catch (IdUnusedException | PermissionException i){
            log.error("Unable to schedule publishing for site: {}", siteId);
        } finally {
            if (session != null) {
              session.clear();
            }
        }
    }
}
