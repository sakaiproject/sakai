package org.sakaiproject.sitestats.api.event.detailed.samigo;

import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;

/**
 * Interface to tag the various classes that can be resolved from a Samigo event reference
 * @author plukasew
 */
public interface SamigoData extends ResolvedEventData
{
	// anonymous assessments provide no further details
	public static final class AnonymousAssessment implements SamigoData {}
	public static final AnonymousAssessment ANON_ASSESSMENT = new AnonymousAssessment();
}
