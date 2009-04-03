/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/chat/trunk/chat-tool/tool/src/java/org/sakaiproject/chat/tool/ChatDelivery.java $
 * $Id: ChatDelivery.java 14062 2006-08-27 03:44:18Z csev@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
import org.sakaiproject.chat2.model.ChatMessage;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
//import org.sakaiproject.chat.cover.ChatService;
//import org.sakaiproject.entity.api.Reference;
//import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.cover.UsageSessionService;
//import org.sakaiproject.exception.IdUnusedException;
//import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.chat2.model.ChatChannel;
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
public class AlertDelivery extends BaseDelivery
{	
   
   /**
    * Construct.
    * 
    * @param address
    *        The address.
    * @param elementId
    *        The elementId.
    */
   public AlertDelivery(String address)
   {
      super(address, null);

   } // ChatDelivery
   
   /**
    * Construct.
    * 
    * @param address
    *        The address.
    * @param elementId
    *        The elementId.
    */
   public AlertDelivery(String address, String id)
   {
      super(address, id);

   } // ChatDelivery

	/**
	 * Compose a javascript message for delivery to the browser client window.
	 * 
	 * @return The javascript message to send to the browser client window.
	 */
	public String compose()
	{
      if(getElement() == null)
         return "alert(\"AlertDelivery\");";
      return "alert(" + getElement() +  ");";

	} // compose

	/**
	 * Display.
	 */
	public String toString()
	{
		return super.toString() + " : alert delivery";

	} // toString

	/**
	 * Are these the same?
	 * 
	 * @return true if obj is the same Delivery as this one.
	 */
	public boolean equals(Object obj)
	{
		return super.equals(obj);
	}
}
