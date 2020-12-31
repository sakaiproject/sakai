package org.sakaiproject.tool.messageforums;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import org.sakaiproject.api.app.messageforums.DraftRecipient;
import org.sakaiproject.api.app.messageforums.MembershipItem;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;

public class DraftRecipientsDelegate
{
	@Getter
	private boolean displayDraftRecipientsNotFoundMsg;

	public DraftRecipientsDelegate()
	{
		displayDraftRecipientsNotFoundMsg = false;
	}

	public List<MembershipItem> getDraftRecipients(List<String> selectedList, Map<String, MembershipItem> courseMemberMap)
	{
		return courseMemberMap.entrySet().stream().filter(e -> selectedList.contains(e.getKey())).map(e -> e.getValue()).collect(Collectors.toList());
	}

	public SelectedLists populateDraftRecipients(long draftId, MessageForumsMessageManager msgMan, List<MembershipItem> totalComposeToList,
			List<MembershipItem> totalComposeBccList)
	{
		 List<DraftRecipient> draftRecipients = msgMan.findDraftRecipientsByMessageId(draftId);
		 Map<Boolean, List<DraftRecipient>> drMap = draftRecipients.stream().collect(Collectors.partitioningBy(dr -> dr.isBcc()));

		 ConversionResult toResult = draftRecipientsToMembershipIds(drMap.get(Boolean.FALSE), totalComposeToList);
		 ConversionResult bccResult = draftRecipientsToMembershipIds(drMap.get(Boolean.TRUE), totalComposeBccList);

		 displayDraftRecipientsNotFoundMsg = !toResult.notFound.isEmpty() || !bccResult.notFound.isEmpty();

		 return new SelectedLists(toResult.membershipIds, bccResult.membershipIds);
	}

	private ConversionResult draftRecipientsToMembershipIds(List<DraftRecipient> draftRecipients, List<MembershipItem> memberships)
	{
		List<String> ids = new ArrayList<>(draftRecipients.size());
		List<DraftRecipient> notFound = new ArrayList<>();

		for (DraftRecipient dr : draftRecipients)
		{
			int type = dr.getType();
			switch (type)
			{
				case MembershipItem.TYPE_ALL_PARTICIPANTS:
					process(memberships.stream().filter(m -> m.getType() == type).findAny(), ids, notFound, dr);
					break;
				case MembershipItem.TYPE_GROUP:
				case MembershipItem.TYPE_MYGROUPS:
					process(memberships.stream().filter(m -> m.getType() == type && m.getGroup().getId().equals(dr.getRecipientId())).findAny(),
							ids, notFound, dr);
					break;
				case MembershipItem.TYPE_ROLE:
					process(memberships.stream().filter(m -> m.getType() == type && m.getRole().getId().equals(dr.getRecipientId())).findAny(),
							ids, notFound, dr);
					break;
				case MembershipItem.TYPE_USER:
				case MembershipItem.TYPE_MYGROUPMEMBERS:
					process(memberships.stream().filter(m -> m.getType() == type && m.getUser().getId().equals(dr.getRecipientId())).findAny(),
							ids, notFound, dr);
					break;
				case MembershipItem.TYPE_MYGROUPROLES:
					String[] grouproleIds = dr.getRecipientId().split("\\+\\+\\+");
					if (grouproleIds.length == 2) {
						String groupId = grouproleIds[0];
						String roleId = grouproleIds[1];
						process(memberships.stream().filter(m -> m.getType() == type && m.getGroup().getId().equals(groupId) && m.getRole().getId().equals(roleId)).findAny(),
								ids, notFound, dr);
					}
					break;
				default:
					notFound.add(dr);
					break;
			}
		}

		return new ConversionResult(ids, notFound);
	}

	private void process(Optional<MembershipItem> item, List<String> ids, List<DraftRecipient> notFound, DraftRecipient dr)
	{
		if (item.isPresent())
		{
			ids.add(item.get().getId());
		}
		else
		{
			notFound.add(dr);
		}
	}

	public static final class SelectedLists
	{
		public final List<String> to, bcc;

		public SelectedLists(List<String> toList, List<String> bccList)
		{
			to = toList;
			bcc = bccList;
		}
	}

	private static final class ConversionResult
	{
		public final List<String> membershipIds;
		public final List<DraftRecipient> notFound;

		public ConversionResult(List<String> mIds, List<DraftRecipient> drs)
		{
			membershipIds = mIds;
			notFound = drs;
		}
	}
}
