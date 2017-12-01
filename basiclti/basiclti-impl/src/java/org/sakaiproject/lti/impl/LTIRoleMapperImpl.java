/**
 * Copyright (c) 2009-2016 The Apereo Foundation
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
package org.sakaiproject.lti.impl;

import java.util.Map;
import java.util.AbstractMap;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.tsugi.basiclti.BasicLTIConstants;
import org.tsugi.basiclti.BasicLTIUtil;

import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.lti.api.LTIException;
import org.sakaiproject.lti.api.LTIRoleMapper;
import org.sakaiproject.user.api.User;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

/**
 *  @author Adrian Fish <a.fish@lancaster.ac.uk>
 */
@Slf4j
public class LTIRoleMapperImpl implements LTIRoleMapper {
    /**
     *  Injected from Spring, see components.xml
     */
    private SiteService siteService = null;
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }
    
    private ServerConfigurationService serverConfigurationService;
    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public Map.Entry<String, String> mapLTIRole(Map payload, User user, Site site, boolean trustedConsumer) throws LTIException {

        // Check if the user is a member of the site already
        boolean userExistsInSite = false;
        try {
            Member member = site.getMember(user.getId());
            if (member != null && BasicLTIUtil.equals(member.getUserEid(), user.getEid())) {
                userExistsInSite = true;
                return new AbstractMap.SimpleImmutableEntry(userRole(payload), member.getRole().getId());
            }
        } catch (Exception e) {
            log.warn(e.getLocalizedMessage(), e);
            throw new LTIException( "launch.site.invalid", "siteId="+site.getId(), e);
        }

        if (log.isDebugEnabled()) {
            log.debug("userExistsInSite={}", userExistsInSite);
        }

        // If not a member of the site, and we are a trusted consumer, error
        // Otherwise, add them to the site
        if (!userExistsInSite && trustedConsumer) {
            throw new LTIException( "launch.site.user.missing", "user_id="+user.getId()+ ", siteId="+site.getId(), null);
        }

        String ltiRole = null;

        if (trustedConsumer) {
            // If the launch is from a trusted consumer, just return the user's
            // role in the site. No need to map.
            Member member = site.getMember(user.getId());
            return new AbstractMap.SimpleImmutableEntry(ltiRole, member.getRole().getId());
        } else {
            ltiRole = userRole(payload);
        }

        if (log.isDebugEnabled()) {
            log.debug("ltiRole={}", ltiRole);
        }

        try {
            site = siteService.getSite(site.getId());
            Set<Role> roles = site.getRoles();

            //BLTI-151 see if we can directly map the incoming role to the list of site roles
            String newRole = null;
            if (log.isDebugEnabled()) {
                log.debug("Incoming ltiRole: {}", ltiRole);
            }
            for (Role r : roles) {
                String roleId = r.getId();

                if (BasicLTIUtil.equalsIgnoreCase(roleId, ltiRole)) {
                    newRole = roleId;
                    if (log.isDebugEnabled()) {
                        log.debug("Matched incoming role to role in site: {}", roleId);
                    }
                    break;
                }
            }

            //if we haven't mapped a role, check against the standard roles and fallback
            if (BasicLTIUtil.isBlank(newRole)) {

                if (log.isDebugEnabled()) {
                    log.debug("No match, falling back to determine role");
                }

		String maintainRole = site.getMaintainRole();

		if (maintainRole == null) {
		    maintainRole = serverConfigurationService.getString("lti.role.mapping.Instructor", null);
		}

		boolean isInstructor = ltiRole.indexOf("instructor") >= 0;
		if (isInstructor && maintainRole != null) {
		   newRole = maintainRole;
		}else{
		   newRole=serverConfigurationService.getString("lti.role.mapping.Student", null);
		}
              

                if (log.isDebugEnabled()) {
                    log.debug("Determined newRole as: {}", newRole);
                }
            }
            if (newRole == null) {
                log.warn("Could not find Sakai role, role={} user={} site={}", ltiRole, user.getId(), site.getId());
                throw new LTIException( "launch.role.missing", "siteId="+site.getId(), null);

            }

            return new AbstractMap.SimpleImmutableEntry(ltiRole, newRole);
        } catch (Exception e) {
            log.warn("Could not map role role={} user={} site={}", ltiRole, user.getId(), site.getId());
            log.warn(e.getLocalizedMessage(), e);
            throw new LTIException( "map.role", "siteId="+site.getId(), e);
        }
    }

	private String userRole(Map payload) {
		String ltiRole;
		ltiRole = (String) payload.get(BasicLTIConstants.ROLES);
		if (ltiRole == null) {
		    ltiRole = "";
		} else {
		    ltiRole = ltiRole.toLowerCase();
		}
		return ltiRole;
	}
}
