package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import org.apache.commons.lang3.StringUtils;

/**
 * This is a minimal class representing an unidentified user, containing only the display ID (EID) and display name.
 * @author bjones86
 */
@Getter
@EqualsAndHashCode
public class GbUnidentifiedUser implements GbUserBase, Serializable, Comparable<GbUnidentifiedUser>
{
	private static final long serialVersionUID = 5006775569057810645L;

	private final String displayId;
	private final String displayName;

	public GbUnidentifiedUser( final String displayId, final String displayName )
	{
		this.displayId = displayId;
		this.displayName = displayName;
	}

	/**
	 * Unidentified users are always considered invalid (they have no UUID).
	 * @return false
	 */
	public boolean isValid()
	{
		return false;
	}

	@Override
	public int compareTo( GbUnidentifiedUser other )
	{
		String prop1 = displayId;
		String prop2 = other.displayId;
		if( (StringUtils.isBlank( prop1 ) && StringUtils.isBlank( prop2 )) || StringUtils.equalsIgnoreCase( prop1, prop2 ) )
		{
			prop1 = displayName;
			prop2 = other.displayName;
		}

		return StringUtils.compareIgnoreCase( prop1, prop2 );
	}
}
