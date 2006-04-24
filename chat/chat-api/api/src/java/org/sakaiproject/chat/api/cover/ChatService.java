/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.service.legacy.chat.cover;

import org.sakaiproject.service.framework.component.cover.ComponentManager;

/**
* <p>ChatService is a static Cover for the {@link org.sakaiproject.service.legacy.chat.ChatService ChatService};
* see that interface for usage details.</p>
* 
* @author University of Michigan, Sakai Software Development Team
* @version $Revision$
*/
public class ChatService
{
	/**
	 * Access the component instance: special cover only method.
	 * @return the component instance.
	 */
	public static org.sakaiproject.service.legacy.chat.ChatService getInstance()
	{
		if (ComponentManager.CACHE_SINGLETONS)
		{
			if (m_instance == null) m_instance = (org.sakaiproject.service.legacy.chat.ChatService) ComponentManager.get(org.sakaiproject.service.legacy.chat.ChatService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.service.legacy.chat.ChatService) ComponentManager.get(org.sakaiproject.service.legacy.chat.ChatService.class);
		}
	}
	private static org.sakaiproject.service.legacy.chat.ChatService m_instance = null;

	public static java.lang.String SERVICE_NAME = org.sakaiproject.service.legacy.chat.ChatService.SERVICE_NAME;
	public static java.lang.String REFERENCE_ROOT = org.sakaiproject.service.legacy.chat.ChatService.REFERENCE_ROOT;
	public static java.lang.String SECURE_READ = org.sakaiproject.service.legacy.chat.ChatService.SECURE_READ;
	public static java.lang.String SECURE_ADD = org.sakaiproject.service.legacy.chat.ChatService.SECURE_ADD;
	public static java.lang.String SECURE_REMOVE_OWN = org.sakaiproject.service.legacy.chat.ChatService.SECURE_REMOVE_OWN;
	public static java.lang.String SECURE_REMOVE_ANY = org.sakaiproject.service.legacy.chat.ChatService.SECURE_REMOVE_ANY;
	public static java.lang.String SECURE_UPDATE_OWN = org.sakaiproject.service.legacy.chat.ChatService.SECURE_UPDATE_OWN;
	public static java.lang.String SECURE_UPDATE_ANY = org.sakaiproject.service.legacy.chat.ChatService.SECURE_UPDATE_ANY;
	public static java.lang.String SECURE_READ_DRAFT = org.sakaiproject.service.legacy.chat.ChatService.SECURE_READ_DRAFT;
	public static java.lang.String REF_TYPE_CHANNEL = org.sakaiproject.service.legacy.chat.ChatService.REF_TYPE_CHANNEL;
	public static java.lang.String REF_TYPE_CHANNEL_GROUPS = org.sakaiproject.service.legacy.chat.ChatService.REF_TYPE_CHANNEL_GROUPS;
	public static java.lang.String REF_TYPE_MESSAGE = org.sakaiproject.service.legacy.chat.ChatService.REF_TYPE_MESSAGE;

	public static org.sakaiproject.service.legacy.chat.ChatChannel getChatChannel(java.lang.String param0) throws org.sakaiproject.exception.IdUnusedException, org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.service.legacy.chat.ChatService service = getInstance();
		if (service == null)
			return null;

		return service.getChatChannel(param0);
	}

	public static org.sakaiproject.service.legacy.chat.ChatChannelEdit addChatChannel(java.lang.String param0) throws org.sakaiproject.exception.IdUsedException, org.sakaiproject.exception.IdInvalidException, org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.service.legacy.chat.ChatService service = getInstance();
		if (service == null)
			return null;

		return service.addChatChannel(param0);
	}

	public static java.util.List getChannels()
	{
		org.sakaiproject.service.legacy.chat.ChatService service = getInstance();
		if (service == null)
			return null;

		return service.getChannels();
	}

	public static boolean allowGetChannel(java.lang.String param0)
	{
		org.sakaiproject.service.legacy.chat.ChatService service = getInstance();
		if (service == null)
			return false;

		return service.allowGetChannel(param0);
	}

	public static boolean allowAddChannel(java.lang.String param0)
	{
		org.sakaiproject.service.legacy.chat.ChatService service = getInstance();
		if (service == null)
			return false;

		return service.allowAddChannel(param0);
	}

	public static org.sakaiproject.service.legacy.message.MessageChannelEdit addChannel(java.lang.String param0) throws org.sakaiproject.exception.IdUsedException, org.sakaiproject.exception.IdInvalidException, org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.service.legacy.chat.ChatService service = getInstance();
		if (service == null)
			return null;

		return service.addChannel(param0);
	}

	public static boolean allowEditChannel(java.lang.String param0)
	{
		org.sakaiproject.service.legacy.chat.ChatService service = getInstance();
		if (service == null)
			return false;

		return service.allowEditChannel(param0);
	}

	public static org.sakaiproject.service.legacy.message.MessageChannelEdit editChannel(java.lang.String param0) throws org.sakaiproject.exception.IdUnusedException, org.sakaiproject.exception.PermissionException, org.sakaiproject.exception.InUseException
	{
		org.sakaiproject.service.legacy.chat.ChatService service = getInstance();
		if (service == null)
			return null;

		return service.editChannel(param0);
	}

	public static void commitChannel(org.sakaiproject.service.legacy.message.MessageChannelEdit param0)
	{
		org.sakaiproject.service.legacy.chat.ChatService service = getInstance();
		if (service == null)
			return;

		service.commitChannel(param0);
	}

	public static void cancelChannel(org.sakaiproject.service.legacy.message.MessageChannelEdit param0)
	{
		org.sakaiproject.service.legacy.chat.ChatService service = getInstance();
		if (service == null)
			return;

		service.cancelChannel(param0);
	}

	public static boolean allowRemoveChannel(java.lang.String param0)
	{
		org.sakaiproject.service.legacy.chat.ChatService service = getInstance();
		if (service == null)
			return false;

		return service.allowRemoveChannel(param0);
	}

	public static void removeChannel(org.sakaiproject.service.legacy.message.MessageChannelEdit param0) throws org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.service.legacy.chat.ChatService service = getInstance();
		if (service == null)
			return;

		service.removeChannel(param0);
	}

	public static java.lang.String channelReference(java.lang.String param0, java.lang.String param1)
	{
		org.sakaiproject.service.legacy.chat.ChatService service = getInstance();
		if (service == null)
			return null;

		return service.channelReference(param0, param1);
	}

	public static java.lang.String messageReference(java.lang.String param0, java.lang.String param1, java.lang.String param2)
	{
		org.sakaiproject.service.legacy.chat.ChatService service = getInstance();
		if (service == null)
			return null;

		return service.messageReference(param0, param1, param2);
	}

	public static java.lang.String messageReference(java.lang.String param0, java.lang.String param1)
	{
		org.sakaiproject.service.legacy.chat.ChatService service = getInstance();
		if (service == null)
			return null;

		return service.messageReference(param0, param1);
	}

	public static void cancelMessage(org.sakaiproject.service.legacy.message.MessageEdit param0)
	{
		org.sakaiproject.service.legacy.chat.ChatService service = getInstance();
		if (service == null)
			return;

		service.cancelMessage(param0);
	}

	public static java.util.List getMessages(java.lang.String param0, org.sakaiproject.service.legacy.time.Time param1, int param2, boolean param3, boolean param4, boolean param5) throws org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.service.legacy.chat.ChatService service = getInstance();
		if (service == null)
			return null;

		return service.getMessages(param0, param1, param2, param3, param4, param5);
	}

	public static java.util.List getChannelIds(java.lang.String param0)
	{
		org.sakaiproject.service.legacy.chat.ChatService service = getInstance();
		if (service == null)
			return null;

		return service.getChannelIds(param0);
	}

	public static org.sakaiproject.service.legacy.message.Message getMessage(org.sakaiproject.service.legacy.entity.Reference param0) throws org.sakaiproject.exception.IdUnusedException, org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.service.legacy.chat.ChatService service = getInstance();
		if (service == null)
			return null;

		return service.getMessage(param0);
	}

	public static org.sakaiproject.service.legacy.message.MessageChannel getChannel(java.lang.String param0) throws org.sakaiproject.exception.IdUnusedException, org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.service.legacy.chat.ChatService service = getInstance();
		if (service == null)
			return null;

		return service.getChannel(param0);
	}

	public static java.lang.String merge(java.lang.String param0, org.w3c.dom.Element param1, java.lang.String param2, java.lang.String param3, java.util.Map param4, java.util.HashMap param5, java.util.Set param6)
	{
		org.sakaiproject.service.legacy.chat.ChatService service = getInstance();
		if (service == null)
			return null;

		return service.merge(param0, param1, param2, param3, param4, param5, param6);
	}

	public static java.lang.String getLabel()
	{
		org.sakaiproject.service.legacy.chat.ChatService service = getInstance();
		if (service == null)
			return null;

		return service.getLabel();
	}

	public static java.lang.String archive(java.lang.String param0, org.w3c.dom.Document param1, java.util.Stack param2, java.lang.String param3, java.util.List param4)
	{
		org.sakaiproject.service.legacy.chat.ChatService service = getInstance();
		if (service == null)
			return null;

		return service.archive(param0, param1, param2, param3, param4);
	}
}



