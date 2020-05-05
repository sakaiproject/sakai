/**
 * Copyright (c) 2003-2019 The Apereo Foundation
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
package org.sakaiproject.assignment.tool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.MultiGroupRecord;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;

/**
 * Delegate for AssignmentAction. Handles functions related to the range and group settings of the assignment.
 *
 * @author plukasew
 */
@Slf4j
@RequiredArgsConstructor
class RangeAndGroupsDelegate
{
	private static final String NEW_ASSIGNMENT_RANGE = "new_assignment_range";
	private static final String NEW_ASSIGNMENT_GROUPS = "new_assignment_groups";
	private static final String NEW_ASSIGNMENT_GROUP_SUBMIT = "new_assignment_group_submit";

	private static final String NAME_ASSIGN_TO = "assignTo";
	// "assign to" choices
	private static final String VALUE_ASSIGN_TO_INDIVIDUALS = "individuals";
	private static final String VALUE_ASSIGN_TO_INDIVIDUALS_FROM_GROUPS = "individualsFromGroups";
	private static final String VALUE_ASSIGN_TO_GROUPS = "groups";

	private final AssignmentService assignmentService;
	private final ResourceLoader rb;
	private final SiteService siteService;
	private final SecurityService securityService;
	private final FormattedText formattedText;

	void buildInstructorNewEditAssignmentContextGroupCheck(Context context, Assignment asn)
	{
		if (!asn.getIsGroup())
		{
			return;
		}

		List<MultiGroupRecord> dupes = defaultMultipleGroupCheck(asn);
		if (!dupes.isEmpty())
		{
			context.put("multipleGroupUsers", formattedText.escapeHtml(formatDuplicateMemberships(dupes)));
		}
	}

	void setAssignmentFormContext(SessionState state, Context context, String contextString, AssignmentAction asnAct)
	{
		String range = StringUtils.trimToNull((String) state.getAttribute(NEW_ASSIGNMENT_RANGE));
		Collection<Group> groupsAllowAddAssignment = assignmentService.getGroupsAllowAddAssignment(contextString);
		if (range == null)
		{
			if (assignmentService.allowAddSiteAssignment(contextString))
			{
				range = Assignment.Access.SITE.toString();
			}
			else if (groupsAllowAddAssignment.size() > 0)
			{
				range = Assignment.Access.GROUP.toString();
			}
		}

		// Determine the assignTo value from the range and group submit settings. We'll do the opposite when reading it back in.
		String fromRange = Assignment.Access.GROUP.toString().equals(range) ? VALUE_ASSIGN_TO_INDIVIDUALS_FROM_GROUPS : VALUE_ASSIGN_TO_INDIVIDUALS;
		String gs = (String) state.getAttribute(NEW_ASSIGNMENT_GROUP_SUBMIT);
		String assignToValue = "1".equals(gs) ? VALUE_ASSIGN_TO_GROUPS : fromRange;
		context.put("name_AssignTo", NAME_ASSIGN_TO);
		context.put("value_AssignTo_Individuals", VALUE_ASSIGN_TO_INDIVIDUALS);
		context.put("value_AssignTo_IndividualsFromGroups", VALUE_ASSIGN_TO_INDIVIDUALS_FROM_GROUPS);
		context.put("value_AssignTo_Groups", VALUE_ASSIGN_TO_GROUPS);
		context.put("value_AssignTo", assignToValue);

		if (groupsAllowAddAssignment.size() > 0)
		{
			List<Group> groupList = new ArrayList<>(groupsAllowAddAssignment);
			asnAct.sortGroupList(groupList, state);
			context.put("groupsList", groupList);
			context.put("assignmentGroups", state.getAttribute(NEW_ASSIGNMENT_GROUPS));
		}

		context.put("allowGroupAssignmentsInGradebook", Boolean.TRUE);
	}

	boolean setNewOrEditedAssignmentParameters(RunData data, SessionState state, String siteId)
	{
		String assignTo = data.getParameters().getString(NAME_ASSIGN_TO);
		// reading assignTo back in could be one of three values
		// need to translate it into range/groupAssignment pair where groupAssignment implies groups for the range value
		String range = Assignment.Access.SITE.toString();
		boolean groupAssignment = false;
		if (VALUE_ASSIGN_TO_GROUPS.equals(assignTo))
		{
			range = Assignment.Access.GROUP.toString();
			groupAssignment = true;
		}
		else if (VALUE_ASSIGN_TO_INDIVIDUALS_FROM_GROUPS.equals(assignTo))
		{
			range = Assignment.Access.GROUP.toString();
		}
		state.setAttribute(NEW_ASSIGNMENT_GROUP_SUBMIT, groupAssignment ? "1" : "0");

		state.setAttribute(NEW_ASSIGNMENT_RANGE, range);
		if (Assignment.Access.GROUP.toString().equals(range))
		{
			List<String> groupChoice
				= new ArrayList<>(Arrays.asList(ArrayUtils.nullToEmpty(
					data.getParameters().getStrings("selectedGroups"))));
			String assignmentId
				= AssignmentReferenceReckoner.reckoner().reference(
					data.getParameters().getString("assignmentId")).reckon().getId();
			try {
				Assignment assignment = assignmentService.getAssignment(assignmentId);
				if (assignment.getIsGroup()) {
					// If this assignment has any group submissions, ensure the group id is not removed
					// from the list. The html form will not submit disabled fields, so it can happen.
					assignment.getSubmissions().stream().filter(as -> as.getUserSubmission()).forEach(as -> {

						if (!groupChoice.contains(as.getGroupId())) {
							groupChoice.add(as.getGroupId());
						}
					});
				}
			} catch (Exception e) {
				log.warn("Failed to retrieve assignment with id {}, {}", assignmentId, e.getMessage());
			}

			if (!groupChoice.isEmpty()) {
				state.setAttribute(NEW_ASSIGNMENT_GROUPS, groupChoice);
			} else {
				state.setAttribute(NEW_ASSIGNMENT_GROUPS, null);
				VelocityPortletPaneledAction.addAlert(state, rb.getString("java.alert.youchoosegroup"));
			}
		}
		else
		{
			state.removeAttribute(NEW_ASSIGNMENT_GROUPS);
		}

		// check groups for duplicate members
		if (groupAssignment)
		{
			List<String> groupIds = Arrays.asList(ArrayUtils.nullToEmpty(data.getParameters().getStrings("selectedGroups")));
			List<MultiGroupRecord> dupes = assignmentService.checkAssignmentForUsersInMultipleGroups(siteId, groupsFromIds(siteId, groupIds));
			alertDuplicateMemberships(dupes, state);
		}

		return groupAssignment;
	}

	void resetAssignment(SessionState state)
	{
		state.removeAttribute(NEW_ASSIGNMENT_GROUP_SUBMIT);
		state.removeAttribute(NEW_ASSIGNMENT_RANGE);
		state.removeAttribute(NEW_ASSIGNMENT_GROUPS);
	}

	RangeAndGroupSettings postSaveAssignmentSettings(SessionState state, SiteService siteService, String siteId)
	{
		boolean isGroupSubmit = "1".equals((String) state.getAttribute(NEW_ASSIGNMENT_GROUP_SUBMIT));
		if (isGroupSubmit && !siteService.allowUpdateSite(siteId))
		{
			VelocityPortletPaneledAction.addAlert(state, rb.getFormattedMessage("group.editsite.nopermission"));
		}

		// set group property
		String range = (String) state.getAttribute(NEW_ASSIGNMENT_RANGE);

		Collection<Group> groups = Collections.emptyList();
		try
		{
			Site site = siteService.getSite(siteId);
			Collection<String> groupChoice = (Collection) state.getAttribute(NEW_ASSIGNMENT_GROUPS);
			if (Assignment.Access.GROUP.toString().equals(range) && (groupChoice == null || groupChoice.isEmpty()))
			{
				// show alert if no group is selected for the group access assignment
				VelocityPortletPaneledAction.addAlert(state, rb.getString("java.alert.youchoosegroup"));
			}
			else if (groupChoice != null)
			{
				groups = groupChoice.stream().map(g -> site.getGroup(g)).filter(Objects::nonNull).collect(Collectors.toList());
			}
		}
		catch (Exception e)
		{
			log.warn("{}:post_save_assignment {}", this, e.getMessage());
		}

		return new RangeAndGroupSettings(isGroupSubmit, range, groups);
	}

	void postSaveAssignmentGroupLocking(SessionState state, boolean post, RangeAndGroupSettings settings, Collection<String> aOldGroups, String siteId, String assignmentReference)
	{
		List<String> lockedGroupsReferences = new ArrayList<>();
		if (post && settings.isGroupSubmit && !settings.groups.isEmpty())
		{
			for (Group group : settings.groups)
			{
				// Prior to SAK-41172 the string concatenation:
				// 'group.getReference() + "/assignment/" + a.getId()'
				// was used to create the reference for a lock
				// this was simplified to the assignment reference
				lockedGroupsReferences.add(group.getReference());
				log.debug("Adding group to lock list: {}", group.getReference());

				if (!aOldGroups.contains(group.getReference()) || group.getLockForReference(assignmentReference) == AuthzGroup.RealmLockMode.NONE)
				{
					log.debug("locking group: {}", group.getReference());
					group.setLockForReference(assignmentReference, AuthzGroup.RealmLockMode.ALL);
					log.debug("locked group: {}", group.getReference());

					try
					{
						siteService.save(group.getContainingSite());
					}
					catch (IdUnusedException e)
					{
						log.warn("Cannot find site with id {}", siteId);
						VelocityPortletPaneledAction.addAlert(state, rb.getFormattedMessage("options_cannotFindSite", siteId));
					}
					catch (PermissionException e)
					{
						log.warn("Do not have permission to edit site with id {}", siteId);
						VelocityPortletPaneledAction.addAlert(state, rb.getFormattedMessage("options_cannotEditSite", siteId));
					}
				}
			}
		}

		if (post && !aOldGroups.isEmpty())
		{
			try
			{
				Site site = siteService.getSite(siteId);

				for (String reference : aOldGroups)
				{
					if (!lockedGroupsReferences.contains(reference))
					{
						log.debug("Not contains: {}", reference);
						Group group = site.getGroup(reference);
						if (group != null)
						{
							group.setLockForReference(assignmentReference, AuthzGroup.RealmLockMode.NONE);
							siteService.save(group.getContainingSite());
						}
					}
				}
			}
			catch (IdUnusedException e)
			{
				log.warn(".post_save_assignment: Cannot find site with id {}", siteId);
				VelocityPortletPaneledAction.addAlert(state, rb.getFormattedMessage("options_cannotFindSite", siteId));
			}
			catch (PermissionException e)
			{
				log.warn(".post_save_assignment: Do not have permission to edit site with id {}", siteId);
				VelocityPortletPaneledAction.addAlert(state, rb.getFormattedMessage("options_cannotEditSite", siteId));
			}
		}
	}

	void doEditAssignment(SessionState state, Assignment a)
	{
		// group setting
		String range = a.getTypeOfAccess() == Assignment.Access.SITE ? Assignment.Access.SITE.toString() : Assignment.Access.GROUP.toString();
		state.setAttribute(NEW_ASSIGNMENT_RANGE, range);
		state.setAttribute(NEW_ASSIGNMENT_GROUPS, a.getGroups());
		state.setAttribute(NEW_ASSIGNMENT_GROUP_SUBMIT, a.getIsGroup() ? "1" : "0");
	}

	void initializeAssignment(SessionState state)
	{
		state.removeAttribute(NEW_ASSIGNMENT_RANGE);
		state.removeAttribute(NEW_ASSIGNMENT_GROUPS);
	}

	/**
	 * The default check for users that are in multiple groups. Checks all the groups eligible to submit to the given
	 * assignment.
	 *
	 * @param asn the assignment
	 * @return list of MultiGroupRecords containing users who are in multiple groups, and the specific groups they are
	 * in
	 */
	List<MultiGroupRecord> defaultMultipleGroupCheck(Assignment asn)
	{
		return assignmentService.checkAssignmentForUsersInMultipleGroups(asn.getContext(), groupsFromRefs(asn.getContext(), asn.getGroups()));
	}

	/**
	 * Checks for users that are in multiple groups eligible to submit to the given assignment.
	 *
	 * @param asn the assignment
	 * @param state the state
	 * @return list of users that are in multiple groups and the groups they are in
	 */
	Collection<MultiGroupRecord> checkAssignmentForUsersInMultipleGroups(Assignment asn, SessionState state)
	{
		List<MultiGroupRecord> dupes = defaultMultipleGroupCheck(asn);
		alertDuplicateMemberships(dupes, state);

		return dupes;
	}

	/**
	 * Checks for members of the submission group that are in other groups eligible to submit to the given assignment.
	 *
	 * @param asn the assignment
	 * @param submissionGroup the submission's group
	 * @param state the state
	 * @param showAlert if true, show an alert banner listing the names of the users in multiple groups
	 * @return list of submission group members that are in multiple groups and the groups they are in
	 */
	Collection<MultiGroupRecord> checkSubmissionForUsersInMultipleGroups(Assignment asn, Group submissionGroup, SessionState state, boolean showAlert)
	{
		if (submissionGroup == null || securityService.isSuperUser()) // don't check this for admin users / short circuit if no group given
		{
			return Collections.emptyList();
		}

		String siteId = asn.getContext();
		List<MultiGroupRecord> dupes = assignmentService.checkSubmissionForUsersInMultipleGroups(siteId, submissionGroup, groupsFromRefs(siteId, asn.getGroups()));
		if (showAlert)
		{
			alertDuplicateNames(dupes, state);
		}

		return dupes;
	}

	void buildStudentViewSubmissionContext(SessionState state, Context context, String userId, Assignment asn, AssignmentAction asnAct)
	{
		if (!asn.getIsGroup())
		{
			return;
		}

		Site site = getSite(asn);
		context.put("site", site);
		List<Group> groups = getGroupsWithUser(userId, asn, site);
		checkSubmissionForUsersInMultipleGroups(asn, groups.stream().findAny().orElse(null), state, true);
		context.put("group_size", String.valueOf(groups.size()));
		asnAct.sortGroupList(groups, state);
		context.put("groups", groups.iterator());
		if (state.getAttribute(AssignmentAction.VIEW_SUBMISSION_GROUP) != null)
		{
			context.put("selectedGroup", (String) state.getAttribute(AssignmentAction.VIEW_SUBMISSION_GROUP));
		}
		if (state.getAttribute(AssignmentAction.VIEW_SUBMISSION_ORIGINAL_GROUP) != null)
		{
			context.put("originalGroup", (String) state.getAttribute(AssignmentAction.VIEW_SUBMISSION_ORIGINAL_GROUP));
		}
	}

	void buildStudentViewAssignmentContext(SessionState state, String userId, Assignment asn)
	{
		if (asn.getIsGroup() && !validateUserGroups(state, userId, asn))
		{
			VelocityPortletPaneledAction.addAlert(state, rb.getString("group.error.message"));
		}
	}

	void buildInstructorGradeAssignmentContext(SessionState state, Context context, Assignment asn)
	{
		Collection<String> dupes = checkAssignmentForUsersInMultipleGroups(asn, state).stream().map(mgr -> formattedText.escapeHtml(mgr.user.getDisplayName())).collect(Collectors.toList());
		if (!dupes.isEmpty())
		{
			context.put("usersinmultiplegroups", dupes);
		}
	}

	/**
	 * Determines if any of this user's group members are in multiple groups
	 *
	 * @param state the state
	 * @param userId the user whose groups to check
	 * @param asn the assignment
	 * @return false if any of the members of the user's group are in multiple groups
	 */
	boolean validateUserGroups(SessionState state, String userId, Assignment asn)
	{
		Site site = getSite(asn);

		// finding any of the user's groups is sufficient, if they are in multiple groups the check
		// will fail no matter which of their groups is used
		Optional<Group> userGroup = getGroupsWithUser(userId, asn, site).stream().findAny();
		if (userGroup.isPresent())
		{
			return checkSubmissionForUsersInMultipleGroups(asn, userGroup.get(), state, true).isEmpty();
		}

		// user is not in any assignment groups, if they are an instructor this is probably the Student View feature so let them through
		return assignmentService.allowAddAssignment(site.getId()) || assignmentService.allowUpdateAssignmentInContext(site.getId());
	}

	void buildInstructorGradeSubmissionContextGroupCheck(Optional<Assignment> asnOpt, String groupId, SessionState state)
	{
		if (!asnOpt.isPresent() || !asnOpt.get().getIsGroup())
		{
			return;
		}

		Assignment asn = asnOpt.get();
		Collection<Group> groups = groupsFromIds(asn.getContext(), Collections.singletonList(groupId));
		if (groups.isEmpty())
		{
			log.error("Invalid group id {}", groupId);
		}

		checkSubmissionForUsersInMultipleGroups(asn, groups.stream().findAny().get(), state, true);
	}

	private Site getSite(Assignment asn)
	{
		try
		{
			return siteService.getSite(asn.getContext());
		}
		catch (IdUnusedException e)
		{
			log.error("Site not found for assignment: asnId {}, siteId {}", asn.getId(), asn.getContext(), e);
			throw new IllegalStateException("Assignment site must exist");
		}
	}

	private void alertDuplicates(String msg, SessionState state)
	{
		if (!msg.isEmpty())
		{
			String baseMessage = rb.getString("group.user.multiple.warning");
			VelocityPortletPaneledAction.addAlert(state, formattedText.escapeHtml(baseMessage + " " + msg));
		}
	}

	private void alertDuplicateNames(Collection<MultiGroupRecord> mgrs, SessionState state)
	{
		String msg = mgrs.stream().map(mgr -> mgr.user.getDisplayName()).collect(Collectors.joining(", "));
		alertDuplicates(msg, state);
	}

	private void alertDuplicateMemberships(Collection<MultiGroupRecord> mgrs, SessionState state)
	{
		String msg = formatDuplicateMemberships(mgrs);
		alertDuplicates(msg, state);
	}

	private String formatDuplicateMemberships(Collection<MultiGroupRecord> mgrs)
	{
		return mgrs.stream()
				.map(mgr -> String.format("%s (%s)", mgr.user.getDisplayName(), mgr.groups.stream().map(g -> g.getTitle()).collect(Collectors.joining(", "))))
				.collect(Collectors.joining(", "));
	}

	private List<Group> groupsFromIds(String siteId, Collection<String> groupIds)
	{
		return groupsFromIdsOrRefs(siteId, groupIds, Group::getId);
	}

	private List<Group> groupsFromRefs(String siteId, Collection<String> groupRefs)
	{
		return groupsFromIdsOrRefs(siteId, groupRefs, Group::getReference);
	}

	/**
	 * Returns the groups matching the given list of group ids or references
	 *
	 * @param siteId the site id
	 * @param groups the group ids or references
	 * @param accessor method reference determining which group property (id or reference) will be used for matching
	 * @return a list of Group objects
	 */
	private List<Group> groupsFromIdsOrRefs(String siteId, Collection<String> groups, Function<Group, String> accessor)
	{
		try
		{
			return siteService.getSite(siteId).getGroups().stream().filter(g -> groups.contains(accessor.apply(g))).collect(Collectors.toList());
		}
		catch (IdUnusedException e)
		{
			log.error("Group lookup failed. Unable to find site for id {}", siteId);
			return Collections.emptyList();
		}
	}

	/**
	 * Get groups containing a user for this assignment
	 *
	 * @param userId the user
	 * @param asn the assignment
	 * @param site the site
	 * @return collection of groups with the given member
	 */
	private List<Group> getGroupsWithUser(String userId, Assignment asn, Site site)
	{
		boolean isAdmin = securityService.isSuperUser();
		return asn.getGroups().stream().map(gref -> site.getGroup(gref)).filter(Objects::nonNull)
				.filter(g -> g.getMember(userId) != null || isAdmin) // allow admin to submit on behalf of groups
				.collect(Collectors.toList());
	}

	@RequiredArgsConstructor
	static class RangeAndGroupSettings
	{
		public final boolean isGroupSubmit;
		public final String range;
		public final Collection<Group> groups;
	}
}
