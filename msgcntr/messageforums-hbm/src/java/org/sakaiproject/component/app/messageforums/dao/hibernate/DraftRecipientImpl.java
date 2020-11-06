package org.sakaiproject.component.app.messageforums.dao.hibernate;

import lombok.Getter;
import lombok.Setter;
import org.sakaiproject.api.app.messageforums.DraftRecipient;
import org.sakaiproject.api.app.messageforums.MembershipItem;

@Getter @Setter
public class DraftRecipientImpl implements DraftRecipient
{
	private Long id;
	private int type; // we borrow the types from MembershipItem
	private String recipientId;
	private long draftId;
	private boolean bcc;

	public DraftRecipientImpl()
	{
		type = MembershipItem.TYPE_NOT_SPECIFIED;
		recipientId = "";
		draftId = -1L;
		bcc = false;
	}

	public static DraftRecipientImpl from(MembershipItem item, long draftMsgId, boolean bcc)
	{
		DraftRecipientImpl dr = new DraftRecipientImpl();
		dr.draftId = draftMsgId;
		dr.bcc = bcc;
		int type = item.getType();
		dr.type = type;
		switch(type)
		{
			case MembershipItem.TYPE_ALL_PARTICIPANTS:
				dr.recipientId = DraftRecipient.ALL_PARTICIPANTS_ID;
				break;
			case MembershipItem.TYPE_ROLE:
				if (item.getRole() != null)
				{
					dr.recipientId = item.getRole().getId();
				}
				break;
			case MembershipItem.TYPE_GROUP:
			case MembershipItem.TYPE_MYGROUPS:
				if (item.getGroup() != null)
				{
					dr.recipientId = item.getGroup().getId();
				}
				break;
			case MembershipItem.TYPE_USER:
			case MembershipItem.TYPE_MYGROUPMEMBERS:
				if (item.getUser() != null)
				{
					dr.recipientId = item.getUser().getId();
				}
				break;
			case MembershipItem.TYPE_MYGROUPROLES:
				// we need to store both group and role ids
				if (item.getGroup() != null && item.getRole() != null)
				{
					dr.recipientId = item.getGroup().getId() + "+++" + item.getRole().getId();
				}
				break;
		}

		if (dr.recipientId.isEmpty())
		{
			dr.type = MembershipItem.TYPE_NOT_SPECIFIED;
		}

		return dr;
	}
}
