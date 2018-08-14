package org.sakaiproject.sitestats.api.event.detailed.forums;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Data for a message posted in Forums
 * @author plukasew
 */
public class MessageData implements MsgForumsData
{
	public final TopicData topic;
	public final Optional<MessageData> conversation;
	public final String title;
	public final String author;
	public final Instant creationDate;
	public final boolean repliedTo;

	/**
	 * Data for a message posted in Forums
	 * @param topic the forum topic
	 * @param conversation the forum thread
	 * @param title the title of the message
	 * @param author the author of the message
	 * @param creationDate the time the message was posted
	 * @param repliedTo true if this is a message that was replied to by the user being tracked
	 */
	public MessageData(TopicData topic, MessageData conversation, String title, String author, Date creationDate, boolean repliedTo)
	{
		this.topic = topic;
		this.conversation = Optional.ofNullable(conversation);
		this.title = title;
		this.author = author;  // this should be uuid, but forums Message objects only store the display id string
		this.creationDate = creationDate != null ? creationDate.toInstant() : Instant.EPOCH;
		this.repliedTo = repliedTo;
	}

	/**
	 * Contains just a TopicData object indicating that we wanted a MessageData object but there was a problem
	 * with the topic.
	 */
	public static final class MessageDataTopicError implements MsgForumsData
	{
		public final TopicData topic;

		/**
		 * Constructor
		 * @param topic the forum topic
		 */
		public MessageDataTopicError(TopicData topic)
		{
			this.topic = topic;
		}
	}
}
