/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.chat.tool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.chat.api.ChatMessage;
import org.sakaiproject.chat.cover.ChatService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.message.api.MessageChannel;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.BaseDelivery;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;

/**
 * <p>
 * ChatDelivery is a Delivery that causes a chat message to be appended to a table of chat messages in the HTML element identified by the address and elementID.
 * </p>
 */
public class ChatDelivery extends BaseDelivery
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(ChatDelivery.class);

	/** The messageId. */
	protected String m_messageId = null;

	protected boolean m_beepOnDelivery = false;

	/**
	 * Construct.
	 * 
	 * @param address
	 *        The address.
	 * @param elementId
	 *        The elementId.
	 */
	public ChatDelivery(String address, String elementId, String messageID, boolean beepOnDelivery)
	{
		super(address, elementId);
		m_messageId = messageID;
		m_beepOnDelivery = beepOnDelivery;

	} // ChatDelivery

	/**
	 * Set the Message Id that this delivery is in reference to.
	 * 
	 * @param id
	 *        The message Id that this delivery is in reference to.
	 */
	public void setMessage(String id)
	{
		m_messageId = id;

	} // setMessage

	/**
	 * Access the Message Id that this delivery is in reference to.
	 * 
	 * @return The Message Id that this delivery is in reference to.
	 */
	public String getMessage()
	{
		return m_messageId;

	} // getMessage

	/**
	 * Compose a javascript message for delivery to the browser client window.
	 * 
	 * @return The javascript message to send to the browser client window.
	 */
	public String compose()
	{
		if (M_log.isDebugEnabled()) M_log.debug("compose() element: " + m_elementId + ", message: " + m_messageId);

		// generate a string of JavaScript commands to update the message log

		Reference ref = EntityManager.newReference(m_messageId);
		ChatMessage msg = (ChatMessage) ref.getEntity();
		User sender = null;
		if (msg != null)
		{
			sender = msg.getHeader().getFrom();
		}
		User myself = UserDirectoryService.getCurrentUser();

		MessageChannel channel = null;
		try
		{
			String channelRef = ChatService.channelReference(ref.getContext(), ref.getContainer());
			channel = ChatService.getChannel(channelRef);
		}
		catch (PermissionException e)
		{
		}
		catch (IdUnusedException e)
		{
		}

		// We may not have a usage session
		UsageSession session = UsageSessionService.getSession();
		String browserId = UsageSession.UNKNOWN;
		if ( session != null ) 
		{
			browserId = session.getBrowserId();
		}

		String retval = null;

		// if we don't know we can do the DOM based refresh, or we are missing channel or msg (could have been a message delete)
		// trigger a panel refresh
		boolean browserSupportsDomRefresh = !browserId.equals(UsageSession.UNKNOWN);
		
		// TODO: temporary workaround - IE is not liking our DOM refresh, so don't use it there till we get it working -ggolden
		if (browserId.equals(UsageSession.WIN_IE)) browserSupportsDomRefresh = false;

		if (!browserSupportsDomRefresh || channel == null || msg == null)
		{
			retval = "try { " + m_elementId + ".location.replace(addAuto(" + m_elementId + ".location));} catch (error) {}";
		}
		
		// otherwise setup for a browser-side javascript DOM modification to insert the message
		else
		{
			String msgbody = Web.escapeJsQuoted(Web.escapeHtmlFormattedText(msg.getBody()));

			// is the user we are delivering to allowed to remove this message?
			boolean removeable = false;
			if (channel.allowRemoveMessage(msg)) removeable = true;

			retval = "try { " + m_elementId + ".appendMessage('" + sender.getDisplayName() + "', '" + sender.getId() + "', '"
					+ new Boolean(removeable) + "', '" + msg.getHeader().getDate().toStringLocalDate() + "', '"
					+ msg.getHeader().getDate().toStringLocalTimeZ() + "', '" + msgbody + "','" + msg.getHeader().getId()
					+ "'); } catch (error) {} ";
		}

		if (m_beepOnDelivery && (sender != null) && sender.compareTo(myself) != 0)
		{
			retval += "beep = true;";
		}

		return retval;

	} // compose

	/**
	 * Display.
	 */
	public String toString()
	{
		return super.toString() + " : " + m_messageId;

	} // toString

	/**
	 * Are these the same?
	 * 
	 * @return true if obj is the same Delivery as this one.
	 */
	public boolean equals(Object obj)
	{
		if (!super.equals(obj)) return false;

		ChatDelivery cob = (ChatDelivery) obj;
		if (StringUtil.different(cob.getMessage(), getMessage())) return false;

		return true;
	}
}
