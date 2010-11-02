package org.sakaiproject.chat2.model.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.chat2.model.ChatChannel;
import org.sakaiproject.chat2.model.ChatManager;
import org.sakaiproject.chat2.model.PresenceObserver;
import org.sakaiproject.chat2.model.RoomObserver;
import org.sakaiproject.courier.api.CourierService;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.DirectRefreshDelivery;

public class ChatRestListener implements RoomObserver, PresenceObserver {

	   private static final String IFRAME_ROOM_USERS = "Presence";

	   /** Our logger. */
	   private static Log LOG = LogFactory.getLog(ChatRestListener.class);

	   /**   The work-horse of chat   */
	   private ChatManager chatManager;

	   /** The id of the session. needed for adding messages to the courier because that runs in the notification thread */
	   private String sessionId = "";
	
	   /** Channel for this listener */
	   private ChatChannel channel = null;

	   /** CourierService. */
	   protected CourierService m_courierService = null;
	
	public ChatRestListener(ChatManager chatManager, CourierService courier, String sessionId, ChatChannel channel) {
		this.chatManager = chatManager;
		this.m_courierService = courier;
		this.sessionId = sessionId;
		this.channel = channel;
	}
	   
	public void receivedMessage(String roomId, Object message) {

		if (channel != null) {
			  if (!roomId.equals(channel.getId())) {
				  LOG.error("Incorrect channelId: room = " + roomId + " channelId = " + channel.getId());
				  return;
			  }
		
			  String address = sessionId + roomId;
			  
			  if (SessionManager.getSession(sessionId) == null) {
			      LOG.debug("received msg expired session " + sessionId + " " + channel);
			      m_courierService.clear(address);
			  } else {
			      m_courierService.deliver(new ChatRestDelivery(address, "Monitor", message, false, chatManager));
			  }
	      }

	}

	public void roomDeleted(String roomId) {

		if (!roomId.equals(channel.getId())) {
			LOG.error("Incorrect channelId: room = " + roomId + " channelId = " + channel.getId());
			return;
		}
		
		  resetCurrentChannel();
		  m_courierService.clear(sessionId+roomId);
	}

	public void userJoined(String location, String user) {
	      m_courierService.deliver(new DirectRefreshDelivery(sessionId+location, IFRAME_ROOM_USERS));
	}

	public void userLeft(String location, String user) {
		if (channel != null && SessionManager.getSession(sessionId) == null) {
			if (!location.equals(channel.getId())) {
				LOG.error("Incorrect channelId: room = " + location + " channelId = " + channel.getId());
				return;
			}
		
		   resetCurrentChannel();
		   m_courierService.clear(sessionId+location);
       }
       else
    	   m_courierService.deliver(new DirectRefreshDelivery(sessionId+location, IFRAME_ROOM_USERS));
		
	}

	// this removes the current channel but doesn't add a new one. sort of
    // half of setCurrentChannel.
    protected void resetCurrentChannel() {
    	/*
      String channelId = oldChannel.getChatChannel().getId();
      String address = sessionId+channelId;
      PresenceObserverHelper observer = presenceChannelObservers.get(channelId);
      if (observer != null) {
         observer.endObservation();
         observer.removePresence();
         getChatManager().removeRoomListener(this, channelId);
      }
      
      m_courierService.clear(address);
      presenceChannelObservers.remove(channelId);
      channels.remove(channelId);
      tools.remove(address);
      currentChannel = null;
*/
      // System.out.println("resetcurrent channel " + presenceChannelObservers.size() + " " + channels.size() + " " + tools.size() );

   }
   

}
