package org.sakaiproject.assignment.api;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import lombok.Value;

/**
 * Immutable object pairing a user with groups they belong to
 * @author plukasew
 */
public final class MultiGroupRecord implements Serializable
{
	public final AsnUser user;
	public final List<AsnGroup> groups;

	public MultiGroupRecord(AsnUser user, List<AsnGroup> groups)
	{
		this.user = user;
		this.groups = Collections.unmodifiableList(groups);
	}

	@Value
	public static final class AsnUser implements Serializable
	{
		String id, displayId, displayName;
	}

	@Value
	public static final class AsnGroup implements Serializable
	{
		String id, title;
	}
}
