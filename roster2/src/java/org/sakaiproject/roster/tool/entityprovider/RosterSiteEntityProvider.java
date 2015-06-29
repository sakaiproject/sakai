/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational
* Community License, Version 2.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of the
* License at:
*
* http://opensource.org/licenses/ecl2.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.roster.tool.entityprovider;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.roster.api.RosterData;
import org.sakaiproject.roster.api.RosterFunctions;
import org.sakaiproject.roster.api.RosterMember;
import org.sakaiproject.roster.api.RosterMemberComparator;
import org.sakaiproject.roster.api.SakaiProxy;
import org.sakaiproject.sitestats.api.SitePresenceTotal;
import org.sakaiproject.user.api.User;

import lombok.Setter;

/**
 * <code>EntityProvider</code> to allow Roster to access site, membership, and
 * enrollment data for the current user. The provider respects Roster
 * permissions, so shouldn't expose any data the current user should not have
 * access to.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
public class RosterSiteEntityProvider extends AbstractEntityProvider implements
		AutoRegisterEntityProvider, ActionsExecutable, Outputable {

	@SuppressWarnings("unused")
	private static final Log log = LogFactory.getLog(RosterSiteEntityProvider.class);
	
	public final static String ENTITY_PREFIX		= "roster-membership";
	public final static String DEFAULT_ID			= ":ID:";
	
	public final static String ERROR_INVALID_SITE	= "Invalid site ID";
	
	// key passed as parameters
	public final static String KEY_GROUP_ID						= "groupId";
	public final static String KEY_ROLE_ID						= "roleId";
	public final static String KEY_USER_ID						= "userId";
	public final static String KEY_PAGE                         = "page";
	public final static String KEY_ENROLLMENT_SET_ID			= "enrollmentSetId";
	public final static String KEY_ENROLLMENT_STATUS			= "enrollmentStatus";

    @Setter
	private SakaiProxy sakaiProxy;
	
	/**
	 * {@inheritDoc}
	 */
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}
	
	@EntityCustomAction(action = "get-membership", viewKey = EntityView.VIEW_SHOW)
	public Object getMembership(EntityReference reference, Map<String, Object> parameters) {

        String userId = developerHelperService.getCurrentUserId();

        if (userId == null) {
            throw new EntityException("You must be logged in to get the memberships", reference.getReference());
        }

        String siteId = reference.getId();

		if (null == siteId || DEFAULT_ID.equals(siteId)) {
			throw new EntityException(ERROR_INVALID_SITE, reference.getReference());
		}

		String groupId = null;
		if (parameters.containsKey(KEY_GROUP_ID)) {
			groupId = parameters.get(KEY_GROUP_ID).toString();
		}

		String roleId = null;
		if (parameters.containsKey(KEY_ROLE_ID)) {
			roleId = parameters.get(KEY_ROLE_ID).toString();
		}

		String enrollmentSetId = null;
		if (parameters != null && parameters.containsKey(KEY_ENROLLMENT_SET_ID)) {
			enrollmentSetId = parameters.get(KEY_ENROLLMENT_SET_ID).toString();
		}

        if (groupId != null && enrollmentSetId != null) {
			throw new EntityException("You can't specify a groupId AND an enrollmentSetId. One or the other, not both.", reference.getReference());
        }

		String enrollmentStatus = null;
		if (parameters != null && parameters.containsKey(KEY_ENROLLMENT_STATUS)) {
			enrollmentStatus = parameters.get(KEY_ENROLLMENT_STATUS).toString();
		}

		int page = 0;
		if (parameters.containsKey(KEY_PAGE)) {
            String pageString = parameters.get(KEY_PAGE).toString();
            try {
			    page = Integer.parseInt(pageString);
            } catch (NumberFormatException nfe) {
                log.error("Invalid page number " + pageString + " supplied. The first page will be returned ...");
            }
		}

		List<RosterMember> membership
            = sakaiProxy.getMembership(userId, siteId, groupId, roleId, enrollmentSetId, enrollmentStatus);

		if (null == membership) {
			throw new EntityException("Unable to retrieve membership", reference.getReference());
		}

        int pageSize = 10;
        int start  = page * pageSize;
        int membershipsSize = membership.size();

        if (log.isDebugEnabled()) {
            log.debug("start: " + start);
            log.debug("memberships.size(): " + membershipsSize);
        }

        if (start >= membershipsSize) {
            return "{\"status\": \"END\"}";
        } else {
            int end = start + pageSize;

            if (log.isDebugEnabled()) {
                log.debug("end: " + end);
            }

            if (end >= membershipsSize) {
                end = membershipsSize;
            }

		    List<RosterMember> subList = membership.subList(start, end);

            RosterData data = new RosterData();
            data.setMembers(subList);
            data.setMembersTotal(membershipsSize);

            boolean showVisits = sakaiProxy.getShowVisits();

            Map<String, SitePresenceTotal> sitePresenceTotals = new HashMap<String, SitePresenceTotal>();
            
            if (showVisits) {
                sitePresenceTotals = sakaiProxy.getPresenceTotalsForSite(siteId);
            }

            boolean viewSiteVisits
                = developerHelperService.isUserAllowedInEntityReference("/user/" + userId
                                                    , RosterFunctions.ROSTER_FUNCTION_VIEWSITEVISITS
                                                    , "/site/" + siteId);

            Map<String, Integer> roleCounts = new HashMap<String, Integer>();

            for (RosterMember member : membership) {
                if (showVisits && viewSiteVisits) {
                    String memberUserId = member.getUserId();
                    if (sitePresenceTotals.containsKey(memberUserId)) {
                        SitePresenceTotal spt = sitePresenceTotals.get(memberUserId);
                        member.setTotalSiteVisits(spt.getTotalVisits());
                        member.setLastVisitTime(spt.getLastVisitTime().getTime());
                    }
                }
                String memberRoleId = member.getRole();
                if (!roleCounts.containsKey(memberRoleId)) {
                    roleCounts.put(memberRoleId, 1);
                } else {
                    roleCounts.put(memberRoleId, roleCounts.get(memberRoleId) + 1);
                }
            }

            data.setRoleCounts(roleCounts);

		    return data;
        }
	}

    @EntityCustomAction(action = "get-user", viewKey = EntityView.VIEW_SHOW)
	public Object getUser(EntityReference reference, Map<String, Object> parameters) {

        String siteId = reference.getId();

		if (null == siteId || DEFAULT_ID.equals(siteId)) {
			throw new EntityException(ERROR_INVALID_SITE, reference.getReference());
		}

		String userId = null;
		if (parameters.containsKey(KEY_USER_ID)) {
			userId = parameters.get(KEY_USER_ID).toString();
		}

		if (null == userId) {
			throw new EntityException("No user id supplied", reference.getReference());
		}

		String enrollmentSetId = null;
		if (parameters != null && parameters.containsKey(KEY_ENROLLMENT_SET_ID)) {
			enrollmentSetId = parameters.get(KEY_ENROLLMENT_SET_ID).toString();
		}

		List<RosterMember> membership = new ArrayList<RosterMember>();

        RosterMember member = sakaiProxy.getMember(siteId, userId, enrollmentSetId);
		
		if (null == member) {
			throw new EntityException("Unable to retrieve membership", reference.getReference());
		}

        membership.add(member);

        RosterData data = new RosterData();
        data.setMembers(membership);
        data.setMembersTotal(1);
        Map<String, Integer> roleCounts = new HashMap<String, Integer>(1);
        roleCounts.put(member.getRole(), 1);
        data.setRoleCounts(roleCounts);

        return data;
	}
			
	@EntityCustomAction(action = "get-site", viewKey = EntityView.VIEW_SHOW)
	public Object getSite(EntityReference reference) {
		
		if (null == reference.getId() || DEFAULT_ID.equals(reference.getId())) {
			throw new EntityException(ERROR_INVALID_SITE, reference.getReference());
		}
		
		return sakaiProxy.getRosterSite(reference.getId());
	}

	@EntityCustomAction(action = "get-search-index", viewKey = EntityView.VIEW_SHOW)
	public Object getSearchIndex(EntityReference reference) {
		
        String siteId = reference.getId();

		if (null == siteId || DEFAULT_ID.equals(siteId)) {
			throw new EntityException(ERROR_INVALID_SITE, reference.getReference());
		}

        return sakaiProxy.getSearchIndex(siteId);
	}
		
    /*
	@EntityCustomAction(action = "get-enrollment", viewKey = EntityView.VIEW_SHOW)
	public Object getEnrollment(EntityReference reference, Map<String, Object> parameters) {
		
		if (null == reference.getId() || DEFAULT_ID.equals(reference.getId())) {
			throw new EntityException(ERROR_INVALID_SITE, reference.getReference());
		}
		
		String enrollmentSetId = null;
		if (parameters != null && parameters.containsKey(KEY_ENROLLMENT_SET_ID)) {
			enrollmentSetId = parameters.get(KEY_ENROLLMENT_SET_ID).toString();
		}
		
		return sakaiProxy.getEnrollmentMembership(reference.getId(), enrollmentSetId, null);
	}
    */

	/**
	 * {@inheritDoc}
	 */
	public String[] getHandledOutputFormats() {
		return new String[] { Formats.JSON };
	}
}
