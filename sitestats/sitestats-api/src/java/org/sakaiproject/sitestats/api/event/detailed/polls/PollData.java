package org.sakaiproject.sitestats.api.event.detailed.polls;

import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;

/**
 * Data for a poll
 * @author plukasew
 */
public class PollData implements ResolvedEventData
{
	public final String question;

	/**
	 * Constructor
	 * @param question the poll question
	 */
	public PollData(String question)
	{
		this.question = question;
	}
}
