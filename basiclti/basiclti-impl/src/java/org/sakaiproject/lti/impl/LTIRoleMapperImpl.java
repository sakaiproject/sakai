/**
 * Copyright (c) 2009-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	http://opensource.org/licenses/ecl2
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
import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

import org.tsugi.basiclti.BasicLTIConstants;
import org.tsugi.basiclti.BasicLTIUtil;
import org.sakaiproject.basiclti.util.SakaiBLTIUtil;

import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.lti.api.LTIException;
import org.sakaiproject.lti.api.LTIRoleMapper;
import org.sakaiproject.user.api.User;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.exception.IdUnusedException;

import lombok.Setter;

/**
 *  @author Adrian Fish <a.fish@lancaster.ac.uk>
 */
@Slf4j
public class LTIRoleMapperImpl implements LTIRoleMapper {
	/**
	 *  Injected from Spring, see components.xml
	 */
	@Setter private SiteService siteService;

	@Setter private ServerConfigurationService serverConfigurationService;

	public Map.Entry<String, String> mapLTIRole(Map payload, User user, Site site, boolean trustedConsumer, String inboundMapStr) throws LTIException {

		// Remember payload is ether an LTI 1.1 launch or simulated data from an LTI 1.1 launch
		String ltiRole = (String) payload.get(BasicLTIConstants.ROLES);
		if ( StringUtils.isBlank(ltiRole) ) ltiRole = BasicLTIConstants.MEMBERSHIP_ROLE_LEARNER;

		String extSakaiRole = (String) payload.get("ext_sakai_role");

		// Get the roles from the site
		Set<Role> roles = null;
		Set<String> siteRoles = new HashSet<String> ();

		try {
			site = siteService.getSite(site.getId());
			roles = site.getRoles();

			for (Role r : roles) {
				String roleId = r.getId();
				siteRoles.add(roleId);
			}
		} catch (IdUnusedException e) {
			log.warn("Could not map role role={} user={} site={}", ltiRole, user.getId(), site.getId());
			log.warn(e.getLocalizedMessage(), e);
			throw new LTIException( "map.role", "siteId="+site.getId(), e);
		}

		log.debug("ltiRole={} extSakaiRole={} site={} siteRoles={}", ltiRole, extSakaiRole, site.getId(), siteRoles);

		// See if we can take the role from the calling Sakai site...
		if (trustedConsumer && StringUtils.isNotBlank(extSakaiRole) && siteRoles.contains(extSakaiRole) ) {
			log.debug("Trusted Consumer selected sakaiExtRole={}", extSakaiRole);
			return new AbstractMap.SimpleImmutableEntry(ltiRole, extSakaiRole);
		}

		// Check if inbound mapping will find a role
		String sakaiRole = SakaiBLTIUtil.mapInboundRole(ltiRole, siteRoles, inboundMapStr);
		if (BasicLTIUtil.isNotBlank(sakaiRole)) {
			log.debug("sakaiRole from SakaiBLTIUtil.mapInboundRole={}", sakaiRole);
			return new AbstractMap.SimpleImmutableEntry(ltiRole, sakaiRole);
		}

		// Check if the incoming IMS Role matches a role in the site (not likely)
		for (String matchRole : siteRoles) {
			if (BasicLTIUtil.equalsIgnoreCase(matchRole, ltiRole)) {
				log.debug("Matched incoming role={} to role in site: {}", ltiRole, matchRole);
				return new AbstractMap.SimpleImmutableEntry(ltiRole, matchRole);
			}

			// http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor#TeachingAssistant
			for(String piece : ltiRole.split("#") ) {
				if ( piece.startsWith("http://") || piece.startsWith("https://") ) continue;
				if (BasicLTIUtil.equalsIgnoreCase(sakaiRole, piece)) {
					log.debug("Matched incoming role={} to site role: {}", piece, sakaiRole);
					return new AbstractMap.SimpleImmutableEntry(ltiRole, sakaiRole);
				}
			}
		}

		// Begin fallback checks...
		String maintainRole = site.getMaintainRole();
		if (maintainRole == null) {
			maintainRole = serverConfigurationService.getString("lti.role.mapping.Instructor", null);
		}

		String joinRole = site.getJoinerRole();
		if (joinRole == null) {
			joinRole = serverConfigurationService.getString("lti.role.mapping.Student", null);
		}

		boolean isInstructor = ltiRole.toLowerCase().indexOf("instructor") >= 0;
		if (isInstructor && maintainRole != null) {
			sakaiRole = maintainRole;
		} else if ( joinRole != null ) {
			sakaiRole = joinRole;
		}

		if ( StringUtils.isNotBlank(sakaiRole) ) {
			log.debug("Fallback ltiRole={} to sakaiRole=", ltiRole, sakaiRole);
			return new AbstractMap.SimpleImmutableEntry(ltiRole, sakaiRole);
		}

		log.warn("Could not find Sakai role, role={} user={} site={}", ltiRole, user.getId(), site.getId());
		throw new LTIException( "launch.role.missing", "siteId="+site.getId(), null);
	}

}
