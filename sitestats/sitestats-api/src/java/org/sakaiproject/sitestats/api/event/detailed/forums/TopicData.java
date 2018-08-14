package org.sakaiproject.sitestats.api.event.detailed.forums;

import java.util.Optional;

/**
 * Data for a forum topic
 * @author plukasew
 */
public final class TopicData implements MsgForumsData
{
	public final Optional<ForumData> forum;
	public final String title;
	public final Optional<String> entityUrl;
	public final boolean deleted;
	public final boolean anon;
	public final boolean userPermittedToReadMsgs;

	/**
	 * Constructor
	 * @param forum the forum (may be null if the topic was deleted)
	 * @param title the title of the topic
	 * @param entityUrl the url of the topic (may be null if the topic was deleted)
	 * @param deleted whether the topic has been deleted
	 * @param anon whether the topic is anonymous
	 * @param userPermittedToReadMsgs whether the current user is permitted to read the messages in this topic
	 */
	public TopicData(ForumData forum, String title, String entityUrl, boolean deleted, boolean anon, boolean userPermittedToReadMsgs)
	{
		this.forum = Optional.ofNullable(forum); // deleted topics have no forum
		this.title = title;
		this.entityUrl = Optional.ofNullable(entityUrl); // deleted topics have no entity url
		this.deleted = deleted;
		this.anon = anon;
		this.userPermittedToReadMsgs = userPermittedToReadMsgs;
	}
}
