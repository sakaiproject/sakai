package org.sakaiproject.api.app.messageforums;

/**
 * This class represents a saved recipient for a draft message. It can represent a user, a group, a site role, a combination of those, or all participants.
 * It can be thought of as a more compact version of MembershipItem.
 *
 * @author plukasew
 */
public interface DraftRecipient
{
	public static final String ALL_PARTICIPANTS_ID = "all_participants";

	public Long getId();

	public void setId(Long id);

	public int getType();

	public void setType(int value);

	public String getRecipientId();

	public void setRecipientId(String value);

	public long getDraftId();

	public void setDraftId(long value);

	public boolean isBcc();

	public void setBcc(boolean value);
}
