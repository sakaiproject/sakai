package org.sakaiproject.sitestats.api.event.detailed.content;

import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;

/**
 * Interface to tag the various classes that can be resolved from a Content (Resources) event reference
 * @author plukasew
 */
public interface ContentData extends ResolvedEventData
{
	// a deleted resource (no additional info available)
	public static final class Deleted implements ContentData {}
	public static final Deleted DELETED = new Deleted();
}
