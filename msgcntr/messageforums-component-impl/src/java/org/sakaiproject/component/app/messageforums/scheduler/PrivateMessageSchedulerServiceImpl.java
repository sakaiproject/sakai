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
package org.sakaiproject.component.app.messageforums.scheduler;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.api.app.messageforums.SynopticMsgcntrManager;
import org.sakaiproject.api.app.messageforums.scheduler.PrivateMessageSchedulerService;
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.api.privacy.PrivacyManager;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateMessageRecipientImpl;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Statement;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Verb.SAKAI_VERB;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.api.app.messageforums.DraftRecipient;
import org.sakaiproject.api.app.messageforums.HiddenGroup;
import org.sakaiproject.api.app.messageforums.MembershipItem;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class PrivateMessageSchedulerServiceImpl implements PrivateMessageSchedulerService {

	@Setter
	private PrivateMessageManager prtMsgManager;
	@Setter
	private ScheduledInvocationManager scheduledInvocationManager;
	@Setter
	private SynopticMsgcntrManager synopticMsgcntrManager;
	@Setter
	private EventTrackingService eventTrackingService;
	@Setter
	private MessageForumsMessageManager messageManager;
	@Setter
	private UserDirectoryService userDirectoryService;
	@Setter
	private AuthzGroupService authzGroupService;
	@Setter
	private SessionManager sessionManager;
	@Setter
	private SiteService siteService;
	@Setter
	private ServerConfigurationService serverConfigurationService;
	@Setter
	private PrivacyManager privacyManager;

	private static final String MESSAGECENTER_BUNDLE = "org.sakaiproject.api.app.messagecenter.bundle.Messages";
	private ResourceLoader rl;

	public void init() {
		log.debug("PrivateMessageSchedulerService init()");
		rl = new ResourceLoader(MESSAGECENTER_BUNDLE);
	}

	public void destroy() {
		log.debug("PrivateMessageSchedulerService destroy()");
	}

	@Override
	public void scheduleDueDateReminder(Long messageId) {
		removeScheduledReminder(messageId);

		PrivateMessage pvtMsg = (PrivateMessage) prtMsgManager.getMessageById(messageId);
		Instant reminderDate = pvtMsg.getScheduledDate().toInstant();
		scheduledInvocationManager.createDelayedInvocation(reminderDate,
				"org.sakaiproject.api.app.messageforums.scheduler.PrivateMessageSchedulerService",
				messageId.toString());
	}

	@Override
	public void removeScheduledReminder(Long messageId) {
		if (messageId != null)
			scheduledInvocationManager.deleteDelayedInvocation(
					"org.sakaiproject.api.app.messageforums.scheduler.PrivateMessageSchedulerService",
					messageId.toString());
	}

	@Override
	public void execute(String opaqueContext) {

		Session session = sessionManager.getCurrentSession();
		session.setUserEid("admin");
		session.setUserId("admin");

		try {
			Long messageId = Long.parseLong(opaqueContext);
			PrivateMessage pvtMsg = (PrivateMessage) prtMsgManager.getMessageById(messageId);

			Map<User, Boolean> recipients = getRecipients(pvtMsg);

			pvtMsg.setScheduler(false);
			pvtMsg.setDraft(false);

			prtMsgManager.sendPrivateMessage(pvtMsg, recipients, pvtMsg.getExternalEmail(), false);

			// if you are sending a reply
			Message replying = pvtMsg.getInReplyTo();
			if (replying != null) {
				replying = prtMsgManager.getMessageById(replying.getId());
				if (replying != null) {
					prtMsgManager.markMessageAsRepliedForUser((PrivateMessage) replying, pvtMsg.getCreatedBy());
				}
			}

			synopticMsgcntrManager.incrementSynopticToolInfo(recipients.keySet(), pvtMsg, false);

			LRS_Statement statement = null;
			try {
				statement = prtMsgManager.getStatementForUserSentPvtMsg(pvtMsg.getTitle(), SAKAI_VERB.shared, pvtMsg);
			} catch (Exception e) {
				log.warn(e.getMessage(), e);
			}
			Event event = eventTrackingService.newEvent(replying != null ? DiscussionForumService.EVENT_MESSAGES_RESPONSE : DiscussionForumService.EVENT_MESSAGES_ADD,
					prtMsgManager.getEventMessage(pvtMsg, DiscussionForumService.MESSAGES_TOOL_ID, pvtMsg.getAuthorId(),
							((PrivateMessageRecipientImpl) pvtMsg.getRecipients().get(0)).getContextId()),
					((PrivateMessageRecipientImpl) pvtMsg.getRecipients().get(0)).getContextId(), true,
					NotificationService.NOTI_OPTIONAL, statement);
			eventTrackingService.post(event);
		} finally {
			session.clear();
			session.setUserEid(null);
			session.setUserId(null);
		}
	}

	/**
	 * get recipients
	 * 
	 * @return a set of recipients (User objects)
	 */
	private Map<User, Boolean> getRecipients(PrivateMessage pvtMsg) {

		Map<User, Boolean> returnSet = null;
		/** get List of unfiltered course members */
		List allCourseUsers = convertMemberMapToList(getAllCourseUsersAsMap(pvtMsg));

		Map courseMemberMap = null;
		try {
			courseMemberMap = getFilteredCourseMembers(
					true, getHiddenGroupIds(prtMsgManager
							.getPrivateMessageArea(pvtMsg.getRecipients().get(0).getContextId()).getHiddenGroups()),
					allCourseUsers, pvtMsg);
		} catch (UserNotDefinedException e) {
			log.warn(e.getMessage(), e);
			return returnSet;
		}
		List<MembershipItem> members = convertMemberMapToList(courseMemberMap);
		SelectedLists selectedLists = populateDraftRecipients(pvtMsg.getId(), messageManager, members, members);

		Map<User, Boolean> composeToSet = getRecipientsHelper(selectedLists.to, allCourseUsers, Boolean.FALSE, courseMemberMap);
		Map<User, Boolean> composeBccSet = getRecipientsHelper(selectedLists.bcc, allCourseUsers, Boolean.TRUE, courseMemberMap);

		returnSet = new HashMap<>();

		returnSet.putAll(composeBccSet);
		// remove all duplicates by doing this first:
		for (User user : composeToSet.keySet()) {
			if (returnSet.containsKey(user)) {
				returnSet.remove(user);
			}
		}
		// now add them all back
		returnSet.putAll(composeToSet);

		messageManager.deleteDraftRecipientsByMessageId(pvtMsg.getId());

		return returnSet;
	}

	private Map<User, Boolean> getRecipientsHelper(List selectedList, List allCourseUsers, boolean bcc, Map courseMemberMap) {

		Map<User, Boolean> returnSet = new HashMap<>();

		for (String selectedItem : (List<String>) selectedList) {

			/** lookup item in map */
			MembershipItem item = (MembershipItem) courseMemberMap.get(selectedItem);
			if (item == null) {
				log.warn("getRecipients() could not resolve uuid: " + selectedItem);
			} else {
				if (MembershipItem.TYPE_ALL_PARTICIPANTS == item.getType()) {
					for (MembershipItem member : (List<MembershipItem>) allCourseUsers) {
						returnSet.put(member.getUser(), bcc);
					}
					// if all users have been selected we may as well return and ignore any other
					// entries
					return returnSet;
				} else if (MembershipItem.TYPE_ROLE == item.getType()) {
					for (MembershipItem member : (List<MembershipItem>) allCourseUsers) {
						if (member.getRole().equals(item.getRole())) {
							returnSet.put(member.getUser(), bcc);
						}
					}
				} else if (MembershipItem.TYPE_GROUP == item.getType()
						|| MembershipItem.TYPE_MYGROUPS == item.getType()) {
					for (MembershipItem member : (List<MembershipItem>) allCourseUsers) {
						Set groupMemberSet = item.getGroup().getMembers();
						for (Member m : (Set<Member>) groupMemberSet) {
							if (m.getUserId() != null && m.getUserId().equals(member.getUser().getId())) {
								returnSet.put(member.getUser(), bcc);
							}
						}
					}
				} else if (MembershipItem.TYPE_USER == item.getType()
						|| MembershipItem.TYPE_MYGROUPMEMBERS == item.getType()) {
					returnSet.put(item.getUser(), bcc);
				} else if (MembershipItem.TYPE_MYGROUPROLES == item.getType()) {
					for (MembershipItem member : (List<MembershipItem>) allCourseUsers) {
						Set groupMemberSet = item.getGroup().getMembers();
						for (Member m : (Set<Member>) groupMemberSet) {
							if (m.getUserId() != null && m.getUserId().equals(member.getUser().getId())
									&& member.getRole().equals(item.getRole())) {
								returnSet.put(member.getUser(), bcc);
							}
						}
					}
				} else {
					log.warn("getRecipients() could not resolve membership type: " + item.getType());
				}
			}
		}

		return returnSet;
	}

	private List<String> getHiddenGroupIds(Set hiddenGroups) {
		return CollectionUtils.emptyIfNull((Set<HiddenGroup>) hiddenGroups).stream().map(HiddenGroup::getGroupId)
				.collect(Collectors.toList());
	}

	public Map<String, MembershipItem> getAllCourseUsersAsMap(PrivateMessage pvtMsg) {
		Map<String, MembershipItem> userMap = new HashMap<>();
		String realmId = "/site/" + pvtMsg.getRecipients().get(0).getContextId();

		AuthzGroup realm = null;
		try {
			realm = authzGroupService.getAuthzGroup(realmId);
		} catch (GroupNotDefinedException e) {
			log.error(e.getMessage(), e);
			throw new IllegalStateException("AuthzGroup realm == null!");
		}

		Set users = realm.getMembers();
		List userIds = getRealmIdList(users);
		List<User> userList = userDirectoryService.getUsers(userIds);
		Map<String, User> userMMap = getuserMap(userList);
		if (users == null)
			throw new IllegalStateException("Could not obtain members from realm!");

		for (Iterator userIterator = users.iterator(); userIterator.hasNext();) {
			Member member = (Member) userIterator.next();
			String userId = member.getUserId();
			Role userRole = member.getRole();
			User user = null;

			if (realm.getMember(userId) != null && realm.getMember(userId).isActive() && userMMap.containsKey(member.getUserId())) {
				user = getUserFromList(member.getUserId(), userList);
			}
			if (user != null && !"admin".equals(userId)) {
				MembershipItem memberItem = MembershipItem.makeMembershipItem(user.getSortName(),
						MembershipItem.TYPE_USER, null, userRole, user);
				userMap.put(memberItem.getId(), memberItem);
			}
		}
		return userMap;
	}

	private List<String> getRealmIdList(Set realmUsers) {
		List ret = new ArrayList();
		Iterator it = realmUsers.iterator();
		while (it.hasNext()) {
			Member mem = (Member) it.next();
			ret.add(mem.getUserId());
		}
		return ret;
	}

	private Map<String, User> getuserMap(List userList) {
		Map<String, User> ret = new HashMap<String, User>();
		for (int i = 0; i < userList.size(); i++) {
			User tu = (User) userList.get(i);
			ret.put(tu.getId(), tu);
		}
		return ret;
	}

	private User getUserFromList(String userId, List<User> userList) {
		User u = null;
		for (int i = 0; i < userList.size(); i++) {
			User tu = (User) userList.get(i);
			if (userId.equals(tu.getId()))
				return tu;
		}

		return u;
	}

	public List<MembershipItem> convertMemberMapToList(Map<String, MembershipItem> memberMap) {

		MembershipItem[] membershipArray = new MembershipItem[memberMap.size()];
		membershipArray = (MembershipItem[]) memberMap.values().toArray(membershipArray);
		Arrays.sort(membershipArray);

		return Arrays.asList(membershipArray);
	}

	public Map getFilteredCourseMembers(boolean filterFerpa, List<String> hiddenGroups, List allCourseUsers,
			PrivateMessage pvtMsg) throws UserNotDefinedException {

		String contextId = "/site/" + pvtMsg.getRecipients().get(0).getContextId();
		Set membershipRoleSet = new HashSet();

		if (prtMsgManager.isAllowToFieldRoles(userDirectoryService.getUser(pvtMsg.getAuthorId()), contextId)
				|| prtMsgManager.isAllowToFieldMyGroupRoles(userDirectoryService.getUser(pvtMsg.getAuthorId()),
						contextId)) {
			/** generate set of roles which has members */
			for (Iterator i = allCourseUsers.iterator(); i.hasNext();) {
				MembershipItem item = (MembershipItem) i.next();
				if (item.getRole() != null) {
					membershipRoleSet.add(item.getRole());
				}
			}
		}

		/** filter member map */
		Map<String, MembershipItem> memberMap = getAllCourseMembers(filterFerpa, true, true, hiddenGroups, pvtMsg);

		Set<String> viewableUsersForTA = new HashSet<String>();
		if (prtMsgManager.isSectionTA(userDirectoryService.getUser(pvtMsg.getAuthorId()), contextId)) {
			viewableUsersForTA = getFellowSectionMembers(pvtMsg);
		}

		for (Iterator i = memberMap.entrySet().iterator(); i.hasNext();) {

			Map.Entry entry = (Map.Entry) i.next();
			MembershipItem item = (MembershipItem) entry.getValue();

			if (MembershipItem.TYPE_ROLE == item.getType() || MembershipItem.TYPE_MYGROUPROLES == item.getType()) {
				/** if no member belongs to role, filter role */
				if (!membershipRoleSet.contains(item.getRole())) {
					i.remove();
				}
			} else if (MembershipItem.TYPE_GROUP == item.getType()) {
				/** if no member belongs to group, filter group */
				if (item.getGroup().getMembers().size() == 0) {
					i.remove();
				}
			} else {
				if (!item.isViewable() && !prtMsgManager.isInstructor(userDirectoryService.getUser(pvtMsg.getAuthorId()), contextId) && (!prtMsgManager.isSectionTA(userDirectoryService.getUser(pvtMsg.getAuthorId()), contextId) || !viewableUsersForTA.contains(item.getUser().getId()))) {
					i.remove();
				}
			}
		}

		return memberMap;
	}

	public Map<String, MembershipItem> getAllCourseMembers(boolean filterFerpa, boolean includeRoles,
			boolean includeAllParticipantsMember, List<String> hiddenGroups, PrivateMessage pvtMsg)
			throws UserNotDefinedException {
		AuthzGroup realm;
		Map<String, MembershipItem> returnMap = new HashMap<>();
		Site site;
		String siteId = pvtMsg.getRecipients().get(0).getContextId();
		String siteReference = "/site/" + pvtMsg.getRecipients().get(0).getContextId();
		User user = userDirectoryService.getUser(pvtMsg.getAuthorId());
		try {
			realm = authzGroupService.getAuthzGroup(siteReference);
			site = siteService.getSite(siteId);
		} catch (IdUnusedException iue) {
			log.warn("Attempted to access site {} but it was not found: {}", siteId, iue.toString());
			return returnMap;
		} catch (GroupNotDefinedException gnde) {
			log.warn("Attempted to access authz site realm {} but it was not found: {}", siteReference,
					gnde.toString());
			return returnMap;
		}

		if (prtMsgManager.isAllowToFieldAllParticipants(user, siteReference) && includeAllParticipantsMember) {
			// add all participants
			MembershipItem memberAll = MembershipItem.makeMembershipItem(rl.getString("all_participants_desc"),
					MembershipItem.TYPE_ALL_PARTICIPANTS);
			returnMap.put(memberAll.getId(), memberAll);
		}

		if (prtMsgManager.isAllowToFieldGroups(user, siteReference)) {
			boolean viewHiddenGroups = prtMsgManager.isAllowToViewHiddenGroups(user, siteReference);
			for (Group currentGroup : site.getGroups()) {
				// only show groups the user has access to
				if (viewHiddenGroups || !hiddenGroups.contains(currentGroup.getTitle())) {
					MembershipItem member = MembershipItem.makeMembershipItem(
							rl.getFormattedMessage("participants_group_desc", currentGroup.getTitle()),
							MembershipItem.TYPE_GROUP, currentGroup, null, null);
					if (!isGroupAlreadyInMap(returnMap, member)) {
						returnMap.put(member.getId(), member);
					}
				}
			}
		}

		if (prtMsgManager.isAllowToFieldRoles(user, siteReference) && (includeRoles && realm != null)) {
			Set<Role> roles = realm.getRoles();
			for (Role role : roles) {
				String roleId = role.getId();
				if (StringUtils.isNotBlank(roleId)) {
					roleId = roleId.substring(0, 1).toUpperCase() + roleId.substring(1);
				}

				MembershipItem member = MembershipItem.makeMembershipItem(
						rl.getFormattedMessage("participants_role_desc", roleId), MembershipItem.TYPE_ROLE, null,
						role, null);
				returnMap.put(member.getId(), member);
			}
		}

		if (prtMsgManager.isAllowToFieldUsers(user, siteReference)) {
			realm.getMembers().forEach(member -> addUsertoMemberItemMap(returnMap, realm, member.getUserId(),
					member.getRole(), MembershipItem.TYPE_USER));
		}

		if (prtMsgManager.isAllowToFieldMyGroups(user, siteReference)) {
			for (Group group : site.getGroupsWithMember(user.getId())) {
				MembershipItem member = MembershipItem.makeMembershipItem(
						rl.getFormattedMessage("participants_group_desc", group.getTitle()),
						MembershipItem.TYPE_MYGROUPS, group, null, null);
				if (!isGroupAlreadyInMap(returnMap, member)) {
					returnMap.put(member.getId(), member);
				}
			}
		}

		if (prtMsgManager.isAllowToFieldMyGroupMembers(user, siteReference)) {
			for (Group group : site.getGroupsWithMember(user.getId())) {
				Set<Member> groupMembers = group.getMembers();
				for (Member groupMember : groupMembers) {
					addUsertoMemberItemMap(returnMap, realm, groupMember.getUserId(), groupMember.getRole(),
							MembershipItem.TYPE_MYGROUPMEMBERS);
				}
			}
		}

		if (prtMsgManager.isAllowToFieldMyGroupRoles(user, siteReference)) {
			for (Group group : site.getGroupsWithMember(user.getId())) {
				for (Role role : group.getRoles()) {
					String roleId = role.getId();
					if (StringUtils.isNotBlank(roleId)) {
						roleId = roleId.substring(0, 1).toUpperCase() + roleId.substring(1);
					}
					MembershipItem member = MembershipItem.makeMembershipItem(
							rl.getFormattedMessage("group_role_desc", group.getTitle(), roleId),
							MembershipItem.TYPE_MYGROUPROLES, group, role, null);

					if (!isGroupAlreadyInMap(returnMap, member)) {
						returnMap.put(member.getId(), member);
					}
				}
			}
		}

		// set FERPA status for all items in map - allCourseUsers
		// needed by PrivacyManager to determine status

		return setPrivacyStatus(convertMemberMapToList(getAllCourseUsersAsMap(pvtMsg)), returnMap, pvtMsg);
	}

	private boolean isGroupAlreadyInMap(Map<String, MembershipItem> returnMap, MembershipItem membershipItem) {
		Predicate<MembershipItem> ifGroupHasAMembership = m -> (m.getType() == MembershipItem.TYPE_GROUP
				|| m.getType() == MembershipItem.TYPE_MYGROUPS)
				&& StringUtils.equals(m.getName(), membershipItem.getName());
		return returnMap.values().stream().anyMatch(ifGroupHasAMembership);
	}

	private void addUsertoMemberItemMap(Map<String, MembershipItem> returnMap, AuthzGroup realm, String userId,
			Role userRole, Integer memberItemType) {
		if (!isUserAlreadyInMap(returnMap, userId)) {
			Member member = realm.getMember(userId);
			if (member != null && member.isActive() && !"admin".equals(userId)) {
				try {
					User user = userDirectoryService.getUser(userId);
					String name = user.getSortName();
					if (serverConfigurationService.getBoolean("msg.displayEid", true)) {
						name = name + " (" + user.getDisplayId() + ")";
					}
					MembershipItem memberItem = MembershipItem.makeMembershipItem(name, memberItemType, null, userRole,
							user);
					returnMap.put(memberItem.getId(), memberItem);
				} catch (UserNotDefinedException e) {
					log.warn("User {} not defined", userId);
				}
			}
		}
	}

	private boolean isUserAlreadyInMap(Map<String, MembershipItem> returnMap, String userId) {
		Predicate<MembershipItem> ifUserHasAMembership = m -> m.getUser() != null
				&& StringUtils.equals(m.getUser().getId(), userId);
		return returnMap.values().stream().anyMatch(ifUserHasAMembership);
	}

	private Map setPrivacyStatus(List allCourseUsers, Map courseUserMap, PrivateMessage pvtMsg) {

		List userIds = new ArrayList();
		Map results = new HashMap();

		Collection userCollection = courseUserMap.values();

		for (Iterator usersIter = allCourseUsers.iterator(); usersIter.hasNext();) {
			MembershipItem memberItem = (MembershipItem) usersIter.next();

			if (memberItem.getUser() != null) {
				userIds.add(memberItem.getUser().getId());
			}
		}

		// set privacy status
		Set memberSet = null;

		memberSet = privacyManager.findViewable(("/site/" + pvtMsg.getRecipients().get(0).getContextId()),
				new HashSet(userIds));

		/**
		 * look through the members again to pick out Member objects corresponding to
		 * only those who are visible (as well as current user)
		 */
		for (Iterator userIterator = userCollection.iterator(); userIterator.hasNext();) {
			MembershipItem memberItem = (MembershipItem) userIterator.next();

			if (memberItem.getUser() != null) {
				memberItem.setViewable(memberSet.contains(memberItem.getUser().getId()));
			} else {
				// want groups to be displayed
				memberItem.setViewable(true);
			}

			results.put(memberItem.getId(), memberItem);
		}

		return results;
	}

	private Set<String> getFellowSectionMembers(PrivateMessage pvtMsg) {
		Set<String> fellowMembers = new HashSet<String>();
		try {
			Collection<Group> groups = siteService.getSite(pvtMsg.getRecipients().get(0).getContextId())
					.getGroupsWithMember(pvtMsg.getAuthorId());
			if (groups != null) {
				for (Group group : groups) {
					Set<Member> groupMembers = group.getMembers();
					if (groupMembers != null) {
						for (Member groupMember : groupMembers) {
							fellowMembers.add(groupMember.getUserId());
						}
					}
				}
			}
		} catch (IdUnusedException e) {
			log.warn("Unable to retrieve site to determine current user's fellow section members.");
		}

		return fellowMembers;
	}

	public SelectedLists populateDraftRecipients(long draftId, MessageForumsMessageManager msgMan,
			List<MembershipItem> totalComposeToList, List<MembershipItem> totalComposeBccList) {
		List<DraftRecipient> draftRecipients = msgMan.findDraftRecipientsByMessageId(draftId);
		Map<Boolean, List<DraftRecipient>> drMap = draftRecipients.stream()
				.collect(Collectors.partitioningBy(dr -> dr.isBcc()));

		ConversionResult toResult = draftRecipientsToMembershipIds(drMap.get(Boolean.FALSE), totalComposeToList);
		ConversionResult bccResult = draftRecipientsToMembershipIds(drMap.get(Boolean.TRUE), totalComposeBccList);

		return new SelectedLists(toResult.membershipIds, bccResult.membershipIds);
	}

	private ConversionResult draftRecipientsToMembershipIds(List<DraftRecipient> draftRecipients,
			List<MembershipItem> memberships) {
		List<String> ids = new ArrayList<>(draftRecipients.size());
		List<DraftRecipient> notFound = new ArrayList<>();

		for (DraftRecipient dr : draftRecipients) {
			int type = dr.getType();
			switch (type) {
			case MembershipItem.TYPE_ALL_PARTICIPANTS:
				process(memberships.stream().filter(m -> m.getType() == type).findAny(), ids, notFound, dr);
				break;
			case MembershipItem.TYPE_GROUP:
			case MembershipItem.TYPE_MYGROUPS:
				process(memberships.stream()
						.filter(m -> m.getType() == type && m.getGroup().getId().equals(dr.getRecipientId())).findAny(),
						ids, notFound, dr);
				break;
			case MembershipItem.TYPE_ROLE:
				process(memberships.stream()
						.filter(m -> m.getType() == type && m.getRole().getId().equals(dr.getRecipientId())).findAny(),
						ids, notFound, dr);
				break;
			case MembershipItem.TYPE_USER:
			case MembershipItem.TYPE_MYGROUPMEMBERS:
				process(memberships.stream()
						.filter(m -> m.getType() == type && m.getUser().getId().equals(dr.getRecipientId())).findAny(),
						ids, notFound, dr);
				break;
			case MembershipItem.TYPE_MYGROUPROLES:
				String[] grouproleIds = dr.getRecipientId().split("\\+\\+\\+");
				if (grouproleIds.length == 2) {
					String groupId = grouproleIds[0];
					String roleId = grouproleIds[1];
					process(memberships.stream().filter(m -> m.getType() == type && m.getGroup().getId().equals(groupId)
							&& m.getRole().getId().equals(roleId)).findAny(), ids, notFound, dr);
				}
				break;
			default:
				notFound.add(dr);
				break;
			}
		}

		return new ConversionResult(ids, notFound);
	}

	private void process(Optional<MembershipItem> item, List<String> ids, List<DraftRecipient> notFound,
			DraftRecipient dr) {
		if (item.isPresent()) {
			ids.add(item.get().getId());
		} else {
			notFound.add(dr);
		}
	}

	public static final class SelectedLists {
		public final List<String> to;
		public final List<String> bcc;

		public SelectedLists(List<String> toList, List<String> bccList) {
			to = toList;
			bcc = bccList;
		}
	}

	private static final class ConversionResult {
		public final List<String> membershipIds;
		public final List<DraftRecipient> notFound;

		public ConversionResult(List<String> mIds, List<DraftRecipient> drs) {
			membershipIds = mIds;
			notFound = drs;
		}
	}
}
