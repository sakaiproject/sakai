/*
 * Copyright (c) 2003-2022 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitemanage.impl;

import lombok.extern.slf4j.Slf4j;
import lombok.Setter;
import java.time.Instant;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitemanage.api.UnpublishingSiteScheduleService;

@Slf4j
public class PublishingSiteScheduleServiceImpl implements org.sakaiproject.sitemanage.api.PublishingSiteScheduleService {
    @Setter private ScheduledInvocationManager scheduledInvocationManager;
    @Setter private SiteService siteService;
    @Setter private SecurityService securityService;
    private final String GROUPMEMEBERSHIP = "site.upd.grp.mbrshp";
    private final String SITEUPDATE = "site.upd";
    private final String READANNOUNCEMENTS = "annc.read";

    public void schedulePublishing(Instant when, String siteId){
        removeScheduledPublish(siteId);   //remove any existing one first
        scheduledInvocationManager.createDelayedInvocation(when, "org.sakaiproject.sitemanage.api.PublishingSiteScheduleService", siteId);
    }

    public void removeScheduledPublish(String siteId){
        scheduledInvocationManager.deleteDelayedInvocation("org.sakaiproject.sitemanage.api.PublishingSiteScheduleService", siteId);
    }

    public void execute(String siteId){
        SecurityAdvisor advisor = (userId, function, reference) -> {
            if(function.equals(GROUPMEMEBERSHIP) || function.equals(SITEUPDATE) || function.equals(READANNOUNCEMENTS)){
                return SecurityAdvisor.SecurityAdvice.ALLOWED;  //allow only three special permissions for publishing
            } else {
                return SecurityAdvisor.SecurityAdvice.PASS;
            }
        };
        securityService.pushAdvisor(advisor);   //add special authorization for the operation
        try{
            Site gettingPublished = siteService.getSite(siteId);
            gettingPublished.setPublished(true);
            siteService.save(gettingPublished);
        } catch (IdUnusedException | PermissionException i){
            log.error("Unable to schedule publishing for site: " + siteId);
        } finally {
            securityService.popAdvisor(advisor);   //remove special authorization
        }
    }
}