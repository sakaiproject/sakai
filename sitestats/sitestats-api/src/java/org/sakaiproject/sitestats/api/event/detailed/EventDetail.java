package org.sakaiproject.sitestats.api.event.detailed;

import java.io.Serializable;

import lombok.Getter;

/**
 * Represents public facing information about a reference.
 * Can take the form of simple textual information, or a link to, say, an entity
 *
 * @author bbailla2, bjones86
 */
public class EventDetail implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * enum indicating how a EventDetail is to be displayed
	 */
	public enum DetailType
	{
		TEXT, LINK
	}

	// how this EventDetail is to be displayed
	@Getter private final DetailType type;

	// the key for the key-value pair
	@Getter private final String key;

	// text to display to user
	@Getter private final String displayValue;

	// if type=LINK, this will store the href
	@Getter private String url;

	private EventDetail(String key, String displayValue)
	{
		type = DetailType.TEXT;
		this.key = key;
		this.displayValue = displayValue;
	}

	private EventDetail(DetailType type, String key, String displayValue)
	{
		this.type = type;
		this.key = key;
		this.displayValue = displayValue;
	}

	/**
	 * @param key they key for this key-value pair
	 * @param displayValue the user facing text
	 * @return a EventDetail instance with type=TEXT
	 */
	public static EventDetail newText(String key, String displayValue)
	{
		return new EventDetail(key, displayValue);
	}

	/**
	 * @param key the key for this key-value pair
	 * @param displayValue the user facing text in the link
	 * @param url the href of the link
	 * @return a EventDetail instance with type=LINK
	 */
	public static EventDetail newLink(String key, String displayValue, String url)
	{
		EventDetail instance = new EventDetail(DetailType.LINK, key, displayValue);
		instance.url = url;
		return instance;
	}
}
