package org.sakaiproject.lti.impl;

import java.util.Map;
import java.util.AbstractMap;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
public class LTIRoleMapperImpl implements LTIRoleMapper {

	private static Log M_log = LogFactory.getLog(LTIRoleMapperImpl.class);

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
            M_log.warn(e.getLocalizedMessage(), e);
            throw new LTIException( "launch.site.invalid", "siteId="+site.getId(), e);
        }

        if (M_log.isDebugEnabled()) {
            M_log.debug("userExistsInSite=" + userExistsInSite);
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

        if (M_log.isDebugEnabled()) {
            M_log.debug("ltiRole=" + ltiRole);
        }

        try {
            site = siteService.getSite(site.getId());
            Set<Role> roles = site.getRoles();

            //BLTI-151 see if we can directly map the incoming role to the list of site roles
            String newRole = null;
            if (M_log.isDebugEnabled()) {
                M_log.debug("Incoming ltiRole:" + ltiRole);
            }
            for (Role r : roles) {
                String roleId = r.getId();

                if (BasicLTIUtil.equalsIgnoreCase(roleId, ltiRole)) {
                    newRole = roleId;
                    if (M_log.isDebugEnabled()) {
                        M_log.debug("Matched incoming role to role in site:" + roleId);
                    }
                    break;
                }
            }

            //if we haven't mapped a role, check against the standard roles and fallback
            if (BasicLTIUtil.isBlank(newRole)) {

                if (M_log.isDebugEnabled()) {
                    M_log.debug("No match, falling back to determine role");
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
              

                if (M_log.isDebugEnabled()) {
                    M_log.debug("Determined newRole as: " + newRole);
                }
            }
            if (newRole == null) {
                M_log.warn("Could not find Sakai role, role=" + ltiRole+ " user=" + user.getId() + " site=" + site.getId());
                throw new LTIException( "launch.role.missing", "siteId="+site.getId(), null);

            }

            return new AbstractMap.SimpleImmutableEntry(ltiRole, newRole);
        } catch (Exception e) {
            M_log.warn("Could not map role role=" + ltiRole + " user="+ user.getId() + " site=" + site.getId());
            M_log.warn(e.getLocalizedMessage(), e);
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
