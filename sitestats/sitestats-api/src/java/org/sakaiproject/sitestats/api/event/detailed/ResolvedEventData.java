package org.sakaiproject.sitestats.api.event.detailed;

/**
 * Common empty interface to tag the various data objects returned by ref resolvers.
 *
 * @author plukasew, bjones86
 */
public interface ResolvedEventData
{
	public static final class Error implements ResolvedEventData {} // ref does not contain expected data or other general error state
	public static final class PermissionError implements ResolvedEventData {} // user does not have permission to retrieve data for this event
	public static final class NoDetails implements ResolvedEventData {} // particular ref cannot provide further details

	public static final Error ERROR = new Error();
	public static final PermissionError PERM_ERROR = new PermissionError();
	public static final NoDetails NO_DETAILS = new NoDetails();
}
