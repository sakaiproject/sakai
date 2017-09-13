/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.chat2.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 
 * Class used to transfer data through jGroups
 *
 */
@Data
public class TransferableChatMessage implements Serializable {

	public enum MessageType {
	    CHAT,
	    HEARTBEAT,
	    CLEAR,
	    REMOVE;
	}
	
	public MessageType type;
	public String id;
	public String owner;
	public String siteId;
	public String channelId;
	public String content;
	public long timestamp;

	public TransferableChatMessage(ChatMessage msg) {
		this(MessageType.CHAT, msg.getId(), msg.getOwner(), msg.getChatChannel().getContext(), msg.getChatChannel().getId(), msg.getBody());
	}
	
	public TransferableChatMessage(MessageType type, String id) {
		this(type, id, null, null, null, null);
	}
	
	public TransferableChatMessage(MessageType type, String id, String channelId) {
		this(type, id, null, null, channelId, null);
	}

	public TransferableChatMessage(MessageType type, String id, String owner, String siteId, String channelId, String content) {
		this.type = type;
		this.id = id;
		this.owner = owner;
		this.siteId = siteId;
		this.channelId = channelId;
		this.content = content;
		this.timestamp = (new Date()).getTime();
	}
	
	public static TransferableChatMessage HeartBeat(String channelId, String sessionKey){
		return new TransferableChatMessage(MessageType.HEARTBEAT, sessionKey, channelId);
	}
	
	public ChatMessage toChatMessage() {
		return toChatMessage(null);
	}
	public ChatMessage toChatMessage(ChatChannel channel) {
		ChatMessage message = new ChatMessage();

		message.setId(id);
        message.setChatChannel(channel);
        message.setOwner(owner);
        message.setRawBody(content);
        message.setMessageDate(new Date(timestamp));
        
        return message;
	}

	public void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(type.toString());
		out.writeObject(id);
		out.writeObject(owner);
		out.writeObject(siteId);
		out.writeObject(channelId);
		out.writeObject(content);
		out.writeObject(timestamp);
	}

	public void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		this.type = MessageType.valueOf((String) in.readObject());
		this.id = (String) in.readObject();
		this.owner = (String) in.readObject();
		this.siteId = (String) in.readObject();
		this.channelId = (String) in.readObject();
		this.content = (String) in.readObject();
		this.timestamp = (Long) in.readObject();
	}
}
