/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/portal/trunk/portal-util/util/src/java/org/sakaiproject/portal/util/PortalSiteHelper.java $
 * $Id: PortalSiteHelper.java 21708 2007-02-18 21:59:28Z ian@caret.cam.ac.uk $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
 
package org.sakaiproject.chat2.tool;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.chat2.model.ChatChannel;

public class DecoratedChatChannel {

   private ChatChannel chatChannel;
   private ChatTool chatTool;
   private int filterParamPast;
   private int filterParamLast;
   private int filterParamNone = 0;
   private boolean directEdit = false;
   private boolean newChannel = false;
   private Date startDate = null;
   private Date endDate = null;
   private String startDateString=null;
   private String endDateString=null;
   private final String HIDDEN_START_DATE_FIELD = "startDateISO8601";
   private final String HIDDEN_END_DATE_FIELD = "endDateISO8601";
   private static final Log log = LogFactory.getLog(DecoratedChatChannel.class);

   public DecoratedChatChannel(ChatTool chatTool, ChatChannel chatChannel)
   {
      this.chatTool = chatTool;
      this.chatChannel = chatChannel;
      // populate the chat channel dates
      if (chatChannel != null) {
          setStartDate(chatChannel.getStartDate());
          setEndDate(chatChannel.getEndDate());
      }
   }
   
   public DecoratedChatChannel(ChatTool chatTool, ChatChannel chatChannel, boolean newChannel)
   {
      this(chatTool, chatChannel);
      this.newChannel = newChannel;
   }
   
   public String processActionEnterRoom()
   {
      return chatTool.processActionEnterRoom(this);
   }
   
   /**
    * This method will edit the room from the page listing all the rooms
    * @return string to navigate
    */
   public String processActionEditRoom()
   {
      directEdit = false;
      return chatTool.processActionEditRoom(this);
   }

   /**
    * This method will edit the room directly from the main chat page
    * @return string to navigate
    */
   public String processActionEditRoomDirect()
   {
      //Setting this to false to the UI doesn't change too much
      //directEdit = true;
      directEdit = false;
      return chatTool.processActionEditRoom(this);
   }
   
   public String processActionDeleteRoom()
   {
      return chatTool.processActionDeleteRoomConfirm(this);
   }
   
   public String processActionDeleteRoomMessages()
   {
      return chatTool.processActionDeleteRoomMessagesConfirm(this);
   }
   
   public String processActionSetAsDefaultRoom() {
      return chatTool.processActionSetAsDefaultRoom(this);
   }
   
   public ChatChannel getChatChannel()
   {
      return chatChannel;
   }
   
   public ChatTool getChatTool()
   {
      return chatTool;
   }
   
   public boolean getCanDelete() {
      return chatTool.getCanRemoveChannel(chatChannel);
   }

   public boolean getCanDeleteMessages() {
      return chatTool.getCanRemoveChannelMessages(chatChannel);
   }
   
   public boolean getCanEdit() {
      return chatTool.getCanEditChannel(chatChannel);
   }

   public boolean getCanRead() {
      return chatTool.getCanRead(chatChannel);
   }
   
   /**
    * Returns the bundle message with key "enter_the_chat_room", inserting the chat channel's title
    * @return message
    */
   public String getEnterChatText() {
      return chatTool.getMessageFromBundle("enter_the_chat_room", new Object[]{chatChannel.getTitle()});
   }
   
   public String getSetAsDefaultText() {
      return chatTool.getMessageFromBundle("set_as_default", new Object[]{chatChannel.getTitle()});
   }

   public int getFilterParamLast() {
      return filterParamLast;
   }

   public void setFilterParamLast(int filterParamLast) {
      this.filterParamLast = filterParamLast;
   }

   public int getFilterParamPast() {
      return filterParamPast;
   }

   public void setFilterParamPast(int filterParamPast) {
      this.filterParamPast = filterParamPast;
   }

   public int getFilterParamNone() {
      return filterParamNone;
   }

   public void setFilterParamNone(int filterParamNone) {
      this.filterParamNone = filterParamNone;
   }

   public boolean isDirectEdit() {
      return directEdit;
   }

   public void setDirectEdit(boolean directEdit) {
      this.directEdit = directEdit;
   }

   public boolean isNewChannel() {
      return newChannel;
   }

   public void setNewChannel(boolean newChannel) {
      this.newChannel = newChannel;
   }
   
   public int getNumberChannelMessages() {
      return chatTool.countChannelMessages(chatChannel);
   }

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;		
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public void setStartDateString(String startDateString) {
		this.startDateString = startDateString;
		if (StringUtils.isEmpty(startDateString)) {
			this.startDate = null;
		} else {
			String hiddenopenDate = JsfUtil.getStringFromParam(HIDDEN_START_DATE_FIELD);
			Calendar tempDate = JsfUtil.convertISO8601StringToCalendar(hiddenopenDate);
				
			if (tempDate != null) {
				this.startDate = tempDate.getTime();
			}else{
				this.startDate = null;
			}

		}
	}
	
	public String getStartDateString() {
		if (startDate == null) {
			return "";
		} else {
			return JsfUtil.convertCalendarToISO8601String(startDate);
		}
	}

	public void setEndDateString(String endDateString) {
		this.endDateString = endDateString;
		if (StringUtils.isEmpty(endDateString)) {
			this.endDate = null;
		} else {
			String hiddenendDate = JsfUtil.getStringFromParam(HIDDEN_END_DATE_FIELD);
			Calendar tempDate = JsfUtil.convertISO8601StringToCalendar(hiddenendDate);
			if (tempDate != null) {
				this.endDate = tempDate.getTime();
			}else{
				this.endDate = null;
			}
		}
	}
	
	public String getEndDateString() {
		if (endDate == null) {
			return "";
		} else {
			return JsfUtil.convertCalendarToISO8601String(endDate);
		}
	}
	
}
