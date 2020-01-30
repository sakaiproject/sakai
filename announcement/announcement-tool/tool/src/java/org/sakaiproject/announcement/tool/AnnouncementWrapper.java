/**
 * Copyright (c) 2003-2020 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.announcement.tool;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.sakaiproject.announcement.api.AnnouncementChannel;
import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementMessageHeader;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.message.api.MessageHeader;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.user.api.ContextualUserDisplayService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.api.FormattedText;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Decorator for the "Message" class. It adds various properties to the decorated real Announcement message.
 */
public class AnnouncementWrapper implements AnnouncementMessage
{
	private boolean enforceMaxNumberOfChars;

	private AnnouncementMessage announcementMesssage;

	private boolean editable;

	private String channelDisplayName;

	private int maxNumberOfChars;

	private String range;
	
	private String authorDisplayName;
	
	public AnnouncementMessage getMessage()
	{
		return this.announcementMesssage;
	}

	/**
	 * Constructor
	 * 
	 * @param message
	 *        The message to be wrapped.
	 * @param currentChannel
	 *        The channel in which the message is contained.
	 * @param hostingChannel
	 *        The channel into which the message is being merged.
	 * @param maxNumberOfChars
	 *        The maximum number of characters that will be returned by getTrimmedBody().
	 */
	public AnnouncementWrapper(AnnouncementMessage message, AnnouncementChannel currentChannel,
			AnnouncementChannel hostingChannel, AnnouncementActionState.DisplayOptions options, String range)
	{
		if (options != null)
		{
			this.maxNumberOfChars = options.getNumberOfCharsPerAnnouncement();
			this.enforceMaxNumberOfChars = options.isEnforceNumberOfCharsPerAnnouncement();
		}
		else
		{
			// default settings from DisplayOptions class
			this.maxNumberOfChars = Integer.MAX_VALUE;
			this.enforceMaxNumberOfChars = false;
		}
		this.announcementMesssage = message;

		// This message is editable only if the site matches.
		this.editable = currentChannel.getReference().equals(hostingChannel.getReference());

		Site site = null;

		try
		{
			site = SiteService.getSite(currentChannel.getContext());
		}
		catch (IdUnusedException e)
		{
			// No site available.
		}

		if (site != null)
		{
			this.channelDisplayName = site.getTitle();
		}
		else
		{
			this.channelDisplayName = "";
		}

		// TODO Let's not retrieve the service for each and every message....
		ContextualUserDisplayService contextualUserDisplayService = (ContextualUserDisplayService) ComponentManager.get("org.sakaiproject.user.api.ContextualUserDisplayService");
		User author = message.getAnnouncementHeader().getFrom();
		if ((site != null) && (!this.editable) && (contextualUserDisplayService != null))
		{
			this.authorDisplayName = contextualUserDisplayService.getUserDisplayName(author, site.getReference());
		}
		if (this.authorDisplayName == null)
		{
			this.authorDisplayName = author.getDisplayName();
		}

		if (range != null)
		{
			this.range = range;
		}
	}

	/**
	 * Constructor
	 * 
	 * @param announcementWrapper
	 *        The message to be wrapped.
	 */
	public AnnouncementWrapper(AnnouncementWrapper mWrapper)
	{
		this.maxNumberOfChars = mWrapper.maxNumberOfChars;
		this.enforceMaxNumberOfChars = mWrapper.enforceMaxNumberOfChars;
		this.announcementMesssage = mWrapper.getMessage();
		
		this.channelDisplayName = mWrapper.channelDisplayName;
		this.range = mWrapper.range;
	}

	/**
	 * See if the given message was posted in the last N days, where N is the value of the maxDaysInPast parameter.
	 */
	static boolean isMessageWithinLastNDays(AnnouncementMessage message, int maxDaysInPast)
	{
		Instant postTime = message.getHeader().getInstant();
		Instant threshold = Instant.now().minus(Duration.ofDays(maxDaysInPast));

		return postTime.isAfter(threshold);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.chefproject.core.Message#getHeader()
	 */
	public MessageHeader getHeader()
	{
		return announcementMesssage.getHeader();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.chefproject.core.Message#getBody()
	 */
	public String getBody()
	{
		return announcementMesssage.getBody();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.chefproject.core.Message#getBody()
	 */
	public String getTrimmedBody()
	{
		if (this.enforceMaxNumberOfChars)
		{
			// trim the body, as formatted text
			String body = announcementMesssage.getBody();
			StringBuilder buf = new StringBuilder();
			body = ComponentManager.get(FormattedText.class).escapeHtmlFormattedTextSupressNewlines(body);
			boolean didTrim = ComponentManager.get(FormattedText.class).trimFormattedText(body, this.maxNumberOfChars, buf);
			if (didTrim)
			{
				if (buf.toString().length() != 0)
				{
					buf.append("...");
				}
			}

			return buf.toString();
		}
		else
		{
			return announcementMesssage.getBody();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.chefproject.core.Resource#getUrl()
	 */
	public String getUrl()
	{
		return announcementMesssage.getUrl();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.chefproject.core.Resource#getReference()
	 */
	public String getReference()
	{
		return announcementMesssage.getReference();
	}

	/**
	 * @inheritDoc
	 */
	public String getReference(String rootProperty)
	{
		return getReference();
	}

	/**
	 * @inheritDoc
	 */
	public String getUrl(String rootProperty)
	{
		return getUrl();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.chefproject.core.Resource#getId()
	 */
	public String getId()
	{
		return announcementMesssage.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.chefproject.core.Resource#getProperties()
	 */
	public ResourceProperties getProperties()
	{
		return announcementMesssage.getProperties();
	}
	
	/**
	 * returns the range string
	 * 
	 * @return
	 */
	public String getRange()
	{
		return range;
	}

	/**
	 * Set the range string
	 * 
	 * @return
	 */
	public void setRange(String range)
	{
		this.range = range;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.chefproject.core.Resource#toXml(org.w3c.dom.Document, java.util.Stack)
	 */
	public Element toXml(Document doc, Stack stack)
	{
		return announcementMesssage.toXml(doc, stack);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object arg0)
	{
		return announcementMesssage.compareTo(arg0);
	}

	/**
	 * Returns true if the message is editable.
	 */
	public boolean isEditable()
	{
		return editable;
	}

	/**
	 * Returns the string that is used to show the channel to the user.
	 */
	public String getChannelDisplayName()
	{
		return channelDisplayName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.chefproject.core.AnnouncementMessage#getAnnouncementHeader()
	 */
	public AnnouncementMessageHeader getAnnouncementHeader()
	{
		return announcementMesssage.getAnnouncementHeader();
	}
	
	public String getAuthorDisplayName()
	{
		return authorDisplayName;
	}

	/**
	 * Constructs a list of wrapped/decorated AnnouncementMessages when given a list of unwrapped/undecorated AnnouncementMessages.
	 * 
	 * @param messages
	 *        The list of messages.
	 * @param currentChannel
	 *        The current channel being processed.
	 * @param hostingChannel
	 *        The default channel of the page into which this list is being merged.
	 * @param maxNumberOfDaysInThePast
	 *        Messages over this limit will not be included in the list.
	 * @param maxCharsPerAnnouncement
	 *        The maximum number of characters that will be returned when getTrimmedBody() is called.
	 */
	static List<AnnouncementWrapper> wrapList(List<AnnouncementMessage> messages, AnnouncementChannel currentChannel, AnnouncementChannel hostingChannel,
			AnnouncementActionState.DisplayOptions options)
	{
		// 365 is the default in DisplayOptions
		int maxNumberOfDaysInThePast = (options != null) ? options.getNumberOfDaysInThePast() : 365;
		 

		List<AnnouncementWrapper> messageList = new ArrayList<>();

		Iterator<AnnouncementMessage> it = messages.iterator();

		while (it.hasNext())
		{
			AnnouncementMessage message = it.next();

			// See if the message falls within the filter window.
			// note: the default of enforceNumberOfDaysInThePastLimit is false
			if (options != null && options.isEnforceNumberOfDaysInThePastLimit() && !isMessageWithinLastNDays(message, maxNumberOfDaysInThePast))
			{
				continue;
			}

			messageList.add(new AnnouncementWrapper(message, currentChannel, hostingChannel, options,
					AnnouncementAction.getAnnouncementRange(message)));
		}

		return messageList;
	}

}
