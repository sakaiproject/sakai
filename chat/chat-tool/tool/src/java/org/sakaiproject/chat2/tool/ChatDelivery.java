/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/chat/trunk/chat-tool/tool/src/java/org/sakaiproject/chat/tool/ChatDelivery.java $
 * $Id: ChatDelivery.java 14062 2006-08-27 03:44:18Z csev@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.chat2.tool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringEscapeUtils;
import org.sakaiproject.chat2.model.ChatMessage;
import org.sakaiproject.chat2.model.ChatManager;
import org.sakaiproject.chat2.tool.ChatTool;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.chat2.model.ChatChannel;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.user.api.ContextualUserDisplayService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
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
	private static Log logger = LogFactory.getLog(ChatDelivery.class);

	/** The message.  it could be a string id or the actual message. */
	protected Object m_message = null;
   
   protected ChatManager chatManager = null;

	protected boolean m_beepOnDelivery = false;

   protected String placementId = "";
   
   private ContextualUserDisplayService contextualUserDisplayService;
   
	/**
	 * Construct.
	 * 
	 * @param address
	 *        The address.
	 * @param elementId
	 *        The elementId.
	 */
	public ChatDelivery(String address, String elementId, Object message, String placementId, boolean beepOnDelivery, ChatManager chatManager)
	{
		super(address, elementId);
		m_message = message;
		m_beepOnDelivery = beepOnDelivery;
      this.chatManager = chatManager;
      this.placementId = placementId;
	} // ChatDelivery

	public ChatMessage getMessage() {
      if(m_message instanceof String) {
         m_message = chatManager.getMessage((String)m_message);
      }
      if(m_message instanceof ChatMessage)
         return (ChatMessage)m_message;
      return null;
   }

   public void setMessage(Object message) {
      this.m_message = message;
   }

   /**
	 * Compose a javascript message for delivery to the browser client window.
    * This function happens in the client connection thread instead of in the event notification thread
	 * 
	 * @return The javascript message to send to the browser client window.
	 */
	public String compose()
	{
      ChatMessage message = null;
      
      // assume the message is being delivered, so clear timeout
      ChatTool.setTimeout(getAddress(),null);

      if(m_message instanceof ChatMessage) {
         message = (ChatMessage)m_message;
      } else if(m_message instanceof String) {
         message = chatManager.getMessage((String)m_message);
      } else {
         return "";
      }
		if (logger.isDebugEnabled()) logger.debug("compose() element: " + m_elementId + ", message: " + message.getId());

		// generate a string of JavaScript commands to update the message log
		
      User sender = null;
      try {
         sender = UserDirectoryService.getUser(message.getOwner());
      } catch(UserNotDefinedException e) {
         logger.error(e);
      }
		User myself = UserDirectoryService.getCurrentUser();

		ChatChannel channel = message.getChatChannel();

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
		
		if (!browserSupportsDomRefresh || channel == null)
		{
			retval = "try { this.location.replace(addAuto(this.location));} catch (error) {}";
		}
		
		// otherwise setup for a browser-side javascript DOM modification to insert the message
		else
		{
			String msgbody = Web.escapeJsQuoted(Web.escapeHtmlFormattedText(message.getBody()));
			msgbody = msgbody.replace('\n',' ').replace('\r',' ');
         
         Time messageTime = TimeService.newTime(message.getMessageDate().getTime());

         StringBuilder retvalBuf = new StringBuilder();
			retvalBuf.append( "try { appendMessage('" );
			
			String displayName = getUserDisplayName(sender, channel.getContext());
			retvalBuf.append( Web.escapeJsQuoted(displayName) );
			 
			//	retvalBuf.append( StringEscapeUtils.escapeJavaScript(contextualUserDisplayService.getUserDisplayId(sender, channel.getContext())) );
			retvalBuf.append( "', '" );
			retvalBuf.append( sender.getId() );
			retvalBuf.append( "', '" );
			retvalBuf.append( String.valueOf(chatManager.getCanDelete(message, placementId)).toString() );
			retvalBuf.append( "', '" );
			retvalBuf.append( messageTime.toStringLocalDate() );
			retvalBuf.append( "', '" );
			retvalBuf.append( messageTime.toStringLocalTimeZ() );
			retvalBuf.append( "', '" );
			retvalBuf.append( messageTime.toString() );
			retvalBuf.append( "', '" );
			retvalBuf.append( msgbody );
			retvalBuf.append( "','" );
			retvalBuf.append( message.getId() );
			retvalBuf.append( "'); } catch (error) {alert(error);} " );
			
			retval = retvalBuf.toString();
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
		return super.toString() + " : " + m_message;

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
		if (StringUtil.different(cob.getMessage().getId(), getMessage().getId() )) return false;

		return true;
	}
	
	private String getUserDisplayName(User u, String context) {
		contextualUserDisplayService = (ContextualUserDisplayService) ComponentManager.get("org.sakaiproject.user.api.ContextualUserDisplayService");
		
		if (contextualUserDisplayService == null) {
			return u.getDisplayName(); 
		} else {
		  	  String ret = contextualUserDisplayService.getUserDisplayName(u, "/site/" + context);
	    	  if (ret == null)
	    		  ret = u.getDisplayName();
	    	  return ret;
	      
		}
	}
}
