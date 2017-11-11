/**
 * Copyright (c) 2008-2017 The Apereo Foundation
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
package org.sakaiproject.profile2.tool.entityprovider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityURLRedirect;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Redirectable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Sampleable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

/**
 * Entity provider for the Roster tool
 */
@Slf4j
public class RosterEntityProvider extends AbstractEntityProvider implements
		AutoRegisterEntityProvider, ActionsExecutable, Outputable,
		Describeable, Sampleable, Redirectable {

	private static final String OFFICIAL_IMAGES_PARAM = "officialImages";

	public final static String ENTITY_PREFIX = "roster";

	public final static String PROFILE_PREFIX = "profile";

	@Setter
	private AuthzGroupService authzGroupService;

	@Setter
	private SiteService siteService;

	@Setter
	private ProfileLogic profileLogic;

	@Override
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	@Override
	public String[] getHandledOutputFormats() {
		return new String[] { Formats.XML, Formats.JSON };
	}

	@EntityURLRedirect("/{prefix}/user/{id}")
	public String redirectUserAccount(Map<String, String> vars) {
		return PROFILE_PREFIX + "/" + vars.get("id")
				+ vars.get(TemplateParseUtil.DOT_EXTENSION);
	}

	@Override
	public Object getSampleEntity() {
		return new RosterItem();
	}

	/**
	 * site/siteId
	 */
	@EntityCustomAction(action = "site", viewKey = EntityView.VIEW_LIST)
	public List<RosterItem> getRosterForSite(EntityView view,
			Map<String, Object> params) {

		// get siteId
		String siteId = view.getPathSegment(2);

		// check siteId supplied
		if (StringUtils.isBlank(siteId)) {
			throw new IllegalArgumentException(
					"siteId must be set in order to get the roster for a site, via the URL /roster/site/siteId");
		}

		// get site
		Site site;
		try {
			site = siteService.getSiteVisit(siteId);
		} catch (IdUnusedException e) {
			throw new EntityNotFoundException("Invalid siteId: " + siteId,
					siteId);
		} catch (PermissionException e) {
			throw new EntityNotFoundException("No access to site: " + siteId,
					siteId);
		}

		String paramValue = (String) params.get(OFFICIAL_IMAGES_PARAM);
		boolean officialImage = Boolean.valueOf(paramValue);

		// Get the member set
		Set<Member> members = site.getMembers();
		return convertToRosterItems(members, officialImage);
	}


	/**
	 * site/siteId
	 */
	@EntityCustomAction(action = "group", viewKey = EntityView.VIEW_LIST)
	public List<RosterItem> getGroupForSite(EntityView view,
			Map<String, Object> params) {

		String siteId = view.getPathSegment(2);
		String groupId = view.getPathSegment(3);

		// check siteId and groupId supplied
		if (StringUtils.isBlank(siteId) || StringUtils.isBlank(groupId)) {
			throw new IllegalArgumentException(
					"siteId and groupId must be set in order to get the roster, via the URL /roster/group/{siteId}/{groupId/groupName}");
		}

		// get site
		Site site;
		try {
			site = siteService.getSiteVisit(siteId);
		} catch (IdUnusedException e) {
			throw new EntityNotFoundException("Invalid siteId: " + siteId,
					siteId);
		} catch (PermissionException e) {
			throw new EntityNotFoundException("No access to site: " + siteId,
					siteId);
		}

		String paramValue = (String) params.get(OFFICIAL_IMAGES_PARAM);
		boolean officialImage = Boolean.valueOf(paramValue);

		// get members in site, create list

		// find the group by group id first
		Group group = site.getGroup(groupId);
		if (group == null) {
			// try to search by title
			Collection<Group> groups = site.getGroups();
			for (Group g : groups) {
				if (groupId.equalsIgnoreCase(g.getTitle())) {
					group = g;
					break;
				}
			}

			// still could not find the group
			if (group == null) {
			    throw new EntityNotFoundException("Invalid group: "+ groupId, siteId);
			}
		}

		// Get the member set
		Set<Member> members = group.getMembers();
		return convertToRosterItems(members, officialImage);
	}

	/**
	 * Utility method to convert members to a list of RosterItem. Returned list may be smaller than
	 * the supplied collection due to deleted/inactive members.
	 *
	 * @param members The members to be converted, can contain users who aren't found.
	 * @param officialImage If <code>true</code> official images are used.
	 * @return A sorted list of RosterItems for the members.
	 */
	private List<RosterItem> convertToRosterItems(Collection<Member> members, boolean officialImage) {
		List<RosterItem> rosterList = new ArrayList<>();
		for (Member member : members) {
			if (member.isActive()) {
				Person person = profileLogic.getPerson(member.getUserId());
				if (person != null) {
					RosterItem item = new RosterItem(person, officialImage);
					rosterList.add(item);
				}
			}
		}
		Collections.sort(rosterList);
		return rosterList;
	}

	@Data
	public static class RosterItem implements Comparable<RosterItem> {
		private String displayName;
		private String imageUrl;

		public RosterItem() {
		}

		public RosterItem(Person person, boolean useOfficialImages) {
			displayName = person.getDisplayName();

			imageUrl = person.getProfile().getImageUrl();
			if (useOfficialImages) {
				imageUrl += "official";
			}
		}

		@Override
		public int compareTo(RosterItem o) {
			return displayName.compareToIgnoreCase(o.displayName);
		}

	}

}