/**
 * $URL: https://source.sakaiproject.org/svn/basiclti/trunk/basiclti-portlet/src/java/org/sakaiproject/blti/ProviderServlet.java $
 * $Id: ProviderServlet.java 310276 2014-06-17 09:29:09Z a.fish@lancaster.ac.uk $
 *
 * Copyright (c) 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.lti.impl;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.lti.api.LTIException;
import org.sakaiproject.lti.api.LTIRoleMapper;
import org.sakaiproject.lti.api.SiteMembershipUpdater;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;

/**
 *  @author Adrian Fish <a.fish@lancaster.ac.uk>
 */
@Slf4j
public class SiteMembershipUpdaterImpl implements SiteMembershipUpdater {
    /**
     *  Injected from Spring, see components.xml
     */
    private LTIRoleMapper roleMapper = null;
    public void setRoleMapper(LTIRoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    /**
     *  Injected from Spring, see components.xml
     */
    private SiteService siteService = null;
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

	private void pushAdvisor() {

		// setup a security advisor
		SecurityService.pushAdvisor(new SecurityAdvisor() {
			public SecurityAdvice isAllowed(String userId, String function,
					String reference) {
				return SecurityAdvice.ALLOWED;
			}
		});
	}

	private void popAdvisor() {
		SecurityService.popAdvisor();
	}

    public Site addOrUpdateSiteMembership(Map payload, boolean trustedConsumer, User user, Site site) throws LTIException {

        Map.Entry<String, String> roleTuple = roleMapper.mapLTIRole(payload, user, site, trustedConsumer);
        String userrole = roleTuple.getKey();
        String newRole = roleTuple.getValue();

        try {
            Role currentRoleObject = site.getUserRole(user.getId());
            String currentRole = null;
            if (currentRoleObject != null) {
                currentRole = currentRoleObject.getId();
            }

            if (!newRole.equals(currentRole)) {
                site.addMember(user.getId(), newRole, true, false);
                if (currentRole == null) {
                    log.info("Added role={} user={} site={} LMS Role={}", newRole, user.getId(), site.getId(), userrole);
                } else {
                    log.info("Old role={} New role={} user={} site={} LMS Role={}", currentRole, newRole, user.getId(), site.getId(), userrole);
                }

                pushAdvisor();
                String tool_id = (String) payload.get("tool_id");
                try {
                    siteService.save(site);
                    log.info("Site saved role={} user={} site={}", newRole, user.getId(), site.getId());

                } catch (Exception e) {
                    throw new LTIException("launch.site.save", "siteId="+ site.getId() + " tool_id=" + tool_id, e);
                } finally {
                    popAdvisor();
                }

            }
        } catch (Exception e) {
            log.warn("Could not add user to site role={} user={} site={}", userrole, user.getId(), site.getId());
            log.warn(e.getLocalizedMessage(), e);
            throw new LTIException( "launch.join.site", "siteId="+site.getId(), e);

        }

        return site;
    }
}
