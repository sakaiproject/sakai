package org.sakaiproject.api.app.messageforums;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

/**
 * This class represents a saved recipient for a draft message. It can represent a user, a group, a site role, a combination of those, or all participants.
 * It can be thought of as a more compact version of MembershipItem.
 *
 * @author plukasew
 */
@Entity @Table(name = "MFR_DRAFT_RECIPIENT_T", indexes = { @Index(name = "MFR_DRAFT_REC_MSG_ID_I", columnList = "DRAFT_ID" ) } )
@NamedQuery(name = "findDraftRecipientsByMessageId", query = "from DraftRecipient d where d.draftId = :id")
@Getter @Setter
public class DraftRecipient
{
	public static final String ALL_PARTICIPANTS_ID = "all_participants";

	@Id @Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "draft_recipient_sequence")
    @SequenceGenerator(name = "draft_recipient_sequence", sequenceName = "MFR_DRAFT_RECIPIENT_S")
	private Long id;

	@Column(name = "TYPE", nullable = false)
	private int type; // we borrow the types from MembershipItem

	@Column(name = "RECIPIENT_ID", nullable = false)
	private String recipientId;
	
	@Column(name = "DRAFT_ID", nullable = false)
	private long draftId;
	
	@Column(name = "BCC", nullable = false) @ColumnDefault("0")
	private boolean bcc;

	public DraftRecipient()
	{
		type = MembershipItem.TYPE_NOT_SPECIFIED;
		recipientId = "";
		draftId = -1L;
		bcc = false;
	}

	public static DraftRecipient from(MembershipItem item, long draftMsgId, boolean bcc)
	{
		DraftRecipient dr = new DraftRecipient();
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
