package org.sakaiproject.sitestats.api.event.detailed;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * An immutable class to hold the parameters used for detailed events queries for user tracking.
 *
 * @author plukasew
 */
public final class TrackingParams implements Serializable
{
	public final String siteId;
	public final List<String> events;
	public final List<String> userIds;
	public final Instant startDate;
	public final Instant endDate;

	public static final Instant NO_DATE = Instant.EPOCH;

	public static final TrackingParams EMPTY_PARAMS = new TrackingParams("", Collections.<String>emptyList(),
			Collections.<String>emptyList(), NO_DATE, NO_DATE);

	/**
	 * Constructor requiring all parameters.
	 *
	 * @param siteId the site id
	 * @param events list of event IDs
	 * @param users list of user UUIDs
	 * @param start start of date range
	 * @param end end of date range
	 */
	public TrackingParams(String siteId, List<String> events, List<String> users, Instant start, Instant end)
	{
		this.siteId = Objects.requireNonNull(siteId);
		this.events = Collections.unmodifiableList(new ArrayList<>(events));
		userIds = Collections.unmodifiableList(new ArrayList<>(users));
		startDate = Objects.requireNonNull(start);
		endDate = Objects.requireNonNull(end);
	}
}
