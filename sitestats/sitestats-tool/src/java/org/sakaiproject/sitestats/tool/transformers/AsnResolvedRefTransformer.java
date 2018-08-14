package org.sakaiproject.sitestats.tool.transformers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.sitestats.api.event.detailed.EventDetail;
import org.sakaiproject.sitestats.api.event.detailed.assignments.AssignmentData;
import org.sakaiproject.sitestats.api.event.detailed.assignments.AssignmentsData;
import org.sakaiproject.sitestats.api.event.detailed.assignments.GroupSubmissionData;
import org.sakaiproject.sitestats.api.event.detailed.assignments.SubmissionData;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;

/**
 * View-layer logic for presenting the data contained in the ResolvedEventData object,
 * default mechanism of presentation is a simple K/V list
 * @author plukasew
 */
@Slf4j
public class AsnResolvedRefTransformer
{
	private static final String BY_FOR = "de_asn_sub_by_for";
	private static final String INS = "de_asn_sub_by_ins";
	private static final String SUBMITTER = "de_asn_sub_submitter";
	private static final String SUBMITTER_DISPLAY = "de_asn_sub_submitter_display";
	private static final String UNKNOWN = "de_asn_sub_by_unknown";

	/**
	 * Transforms AssignmentData for presentation to the user
	 * @param data assignment data
	 * @param rl resource loader for i18n
	 * @return EventDetails for presentation
	 */
	public static List<EventDetail> transform(AssignmentsData data, ResourceLoader rl)
	{
		if (data instanceof AssignmentData)
		{
			AssignmentData asn = (AssignmentData) data;
			return Collections.singletonList(getAsnDetails(asn, rl));
		}
		else if (data instanceof GroupSubmissionData || data instanceof SubmissionData)
		{
			boolean isGroup = data instanceof GroupSubmissionData;
			AssignmentData asn = isGroup ? ((GroupSubmissionData) data).asn : ((SubmissionData) data).asn;
			List<EventDetail> details = new ArrayList<>(2);
			details.add(getAsnDetails(asn, rl));
			if (asn.anonymous)
			{
				return details;
			}

			String subDetails = isGroup ? getSubmitterDetails((GroupSubmissionData) data, rl) : getSubmitterDetails((SubmissionData) data, rl);
			details.add(EventDetail.newText(rl.getString(SUBMITTER), subDetails));
			return details;
		}

		return Collections.emptyList();
	}

	private static EventDetail getAsnDetails(AssignmentData asn, ResourceLoader rl)
	{
		String title = asn.anonymous ? rl.getString("de_asn_anon") : asn.title;
		if (!asn.anonymous && asn.deleted)
		{
			title += " " + rl.getString("de_asn_deleted");
		}
		return EventDetail.newText(rl.getString("de_asn"), title);
	}

	private static String getSubmitterDetails(GroupSubmissionData gsub, ResourceLoader rl)
	{
		String by = gsub.byInstructor ? rl.getString(INS) : getSubmitterDisplay(gsub.submitterId.orElse(""), rl);
		return rl.getFormattedMessage(BY_FOR, by, gsub.group);
	}

	private static String getSubmitterDetails(SubmissionData sub, ResourceLoader rl)
	{
		String submitter = getSubmitterDisplay(sub.submitterId, rl);
		if (sub.byInstructor)
		{
			return rl.getFormattedMessage(BY_FOR, rl.getString(INS), submitter);
		}

		return submitter;
	}

	private static Optional<User> getUser(String userId)
	{
		try
		{
			return Optional.of(Locator.getFacade().getUserDirectoryService().getUser(userId));
		}
		catch (UserNotDefinedException e)
		{
			log.warn("User not found", e);
			return Optional.empty();
		}
	}

	private static String getSubmitterDisplay(String userId, ResourceLoader rl)
	{
		String site = Locator.getFacade().getToolManager().getCurrentPlacement().getContext();
		return getUser(userId)
				.map(u -> rl.getFormattedMessage(SUBMITTER_DISPLAY, u.getDisplayName(site), u.getDisplayId(site)))
				.orElse(rl.getString(UNKNOWN));
	}
}
