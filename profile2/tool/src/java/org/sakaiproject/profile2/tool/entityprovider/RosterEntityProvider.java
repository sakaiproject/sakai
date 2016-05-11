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

		// get members in site, create list
		List<RosterItem> rosterList = new ArrayList<RosterItem>();

		try {
			// Get the member set
			Set<Member> members = authzGroupService.getAuthzGroup(
					site.getReference()).getMembers();
			for (Member member : members) {
				if (member.isActive()) {
					Person person = profileLogic.getPerson(member.getUserId());
					RosterItem item = new RosterItem(person, officialImage);
					rosterList.add(item);
				}
			}
		} catch (GroupNotDefinedException e) {
			log.error("getUsersInAllSections: " + e.getMessage(), e);
		}

		Collections.sort(rosterList);
		return rosterList;
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
		List<RosterItem> rosterList = new ArrayList<RosterItem>();

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
				log.warn("Group " + groupId + " not found");
				return rosterList;
			}
		}

		// Get the member set
		Set<Member> members = group.getMembers();
		for (Member member : members) {
			if (member.isActive()) {
				Person person = profileLogic.getPerson(member.getUserId());
				RosterItem item = new RosterItem(person, officialImage);
				rosterList.add(item);
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