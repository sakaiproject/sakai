/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.mailarchive.cover;

import org.sakaiproject.component.cover.ComponentManager;

/**
 * <p>
 * MailArchiveService is a static Cover for the {@link org.sakaiproject.mailarchive.api.MailArchiveService MailArchiveService}; see that interface for usage details.
 * </p>
 */
public class MailArchiveService
{
	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.mailarchive.api.MailArchiveService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.mailarchive.api.MailArchiveService) ComponentManager
						.get(org.sakaiproject.mailarchive.api.MailArchiveService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.mailarchive.api.MailArchiveService) ComponentManager
					.get(org.sakaiproject.mailarchive.api.MailArchiveService.class);
		}
	}

	private static org.sakaiproject.mailarchive.api.MailArchiveService m_instance = null;

	public static java.lang.String APPLICATION_ID = org.sakaiproject.mailarchive.api.MailArchiveService.APPLICATION_ID;

	public static java.lang.String REFERENCE_ROOT = org.sakaiproject.mailarchive.api.MailArchiveService.REFERENCE_ROOT;

	public static java.lang.String SECURE_MAIL_ROOT = org.sakaiproject.mailarchive.api.MailArchiveService.SECURE_MAIL_ROOT;

	public static java.lang.String SECURE_MAIL_READ = org.sakaiproject.mailarchive.api.MailArchiveService.SECURE_MAIL_READ;

	public static java.lang.String SECURE_MAIL_ADD = org.sakaiproject.mailarchive.api.MailArchiveService.SECURE_MAIL_ADD;

	public static java.lang.String SECURE_MAIL_REMOVE_OWN = org.sakaiproject.mailarchive.api.MailArchiveService.SECURE_MAIL_REMOVE_OWN;

	public static java.lang.String SECURE_MAIL_REMOVE_ANY = org.sakaiproject.mailarchive.api.MailArchiveService.SECURE_MAIL_REMOVE_ANY;

	public static java.lang.String SECURE_MAIL_UPDATE_OWN = org.sakaiproject.mailarchive.api.MailArchiveService.SECURE_MAIL_UPDATE_OWN;

	public static java.lang.String SECURE_MAIL_UPDATE_ANY = org.sakaiproject.mailarchive.api.MailArchiveService.SECURE_MAIL_UPDATE_ANY;

	public static java.lang.String SECURE_MAIL_READ_DRAFT = org.sakaiproject.mailarchive.api.MailArchiveService.SECURE_MAIL_READ_DRAFT;

	public static java.lang.String HEADER_OUTER_CONTENT_TYPE = org.sakaiproject.mailarchive.api.MailArchiveService.HEADER_OUTER_CONTENT_TYPE;

	public static java.lang.String HEADER_INNER_CONTENT_TYPE = org.sakaiproject.mailarchive.api.MailArchiveService.HEADER_INNER_CONTENT_TYPE;

	public static java.lang.String SECURE_READ = org.sakaiproject.mailarchive.api.MailArchiveService.SECURE_READ;

	public static java.lang.String SECURE_ADD = org.sakaiproject.mailarchive.api.MailArchiveService.SECURE_ADD;

	public static java.lang.String SECURE_REMOVE_OWN = org.sakaiproject.mailarchive.api.MailArchiveService.SECURE_REMOVE_OWN;

	public static java.lang.String SECURE_REMOVE_ANY = org.sakaiproject.mailarchive.api.MailArchiveService.SECURE_REMOVE_ANY;

	public static java.lang.String SECURE_UPDATE_OWN = org.sakaiproject.mailarchive.api.MailArchiveService.SECURE_UPDATE_OWN;

	public static java.lang.String SECURE_UPDATE_ANY = org.sakaiproject.mailarchive.api.MailArchiveService.SECURE_UPDATE_ANY;

	public static java.lang.String SECURE_READ_DRAFT = org.sakaiproject.mailarchive.api.MailArchiveService.SECURE_READ_DRAFT;

	public static java.lang.String REF_TYPE_CHANNEL = org.sakaiproject.mailarchive.api.MailArchiveService.REF_TYPE_CHANNEL;

	public static java.lang.String REF_TYPE_MESSAGE = org.sakaiproject.mailarchive.api.MailArchiveService.REF_TYPE_MESSAGE;
	
	public static java.lang.String HEADER_CONTENT_TYPE = org.sakaiproject.mailarchive.api.MailArchiveService.HEADER_CONTENT_TYPE;
	public static java.lang.String HEADER_RETURN_PATH = org.sakaiproject.mailarchive.api.MailArchiveService.HEADER_RETURN_PATH;
	public static java.lang.String HEADER_CONTENT_TRANSFER_ENCODING = org.sakaiproject.mailarchive.api.MailArchiveService.HEADER_CONTENT_TRANSFER_ENCODING;
        public static java.lang.String HEADER_SUBJECT = org.sakaiproject.mailarchive.api.MailArchiveService.HEADER_SUBJECT;

	public static org.sakaiproject.mailarchive.api.MailArchiveChannel getMailArchiveChannel(java.lang.String param0)
			throws org.sakaiproject.exception.IdUnusedException, org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.mailarchive.api.MailArchiveService service = getInstance();
		if (service == null) return null;

		return service.getMailArchiveChannel(param0);
	}

	public static org.sakaiproject.mailarchive.api.MailArchiveChannelEdit addMailArchiveChannel(java.lang.String param0)
			throws org.sakaiproject.exception.IdUsedException, org.sakaiproject.exception.IdInvalidException,
			org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.mailarchive.api.MailArchiveService service = getInstance();
		if (service == null) return null;

		return service.addMailArchiveChannel(param0);
	}

	public static java.util.List getChannels()
	{
		org.sakaiproject.mailarchive.api.MailArchiveService service = getInstance();
		if (service == null) return null;

		return service.getChannels();
	}

	public static boolean allowGetChannel(java.lang.String param0)
	{
		org.sakaiproject.mailarchive.api.MailArchiveService service = getInstance();
		if (service == null) return false;

		return service.allowGetChannel(param0);
	}

	public static boolean allowAddChannel(java.lang.String param0)
	{
		org.sakaiproject.mailarchive.api.MailArchiveService service = getInstance();
		if (service == null) return false;

		return service.allowAddChannel(param0);
	}

	public static org.sakaiproject.message.api.MessageChannelEdit addChannel(java.lang.String param0)
			throws org.sakaiproject.exception.IdUsedException, org.sakaiproject.exception.IdInvalidException,
			org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.mailarchive.api.MailArchiveService service = getInstance();
		if (service == null) return null;

		return service.addChannel(param0);
	}

	public static boolean allowEditChannel(java.lang.String param0)
	{
		org.sakaiproject.mailarchive.api.MailArchiveService service = getInstance();
		if (service == null) return false;

		return service.allowEditChannel(param0);
	}

	public static org.sakaiproject.message.api.MessageChannelEdit editChannel(java.lang.String param0)
			throws org.sakaiproject.exception.IdUnusedException, org.sakaiproject.exception.PermissionException,
			org.sakaiproject.exception.InUseException
	{
		org.sakaiproject.mailarchive.api.MailArchiveService service = getInstance();
		if (service == null) return null;

		return service.editChannel(param0);
	}

	public static void commitChannel(org.sakaiproject.message.api.MessageChannelEdit param0)
	{
		org.sakaiproject.mailarchive.api.MailArchiveService service = getInstance();
		if (service == null) return;

		service.commitChannel(param0);
	}

	public static void cancelChannel(org.sakaiproject.message.api.MessageChannelEdit param0)
	{
		org.sakaiproject.mailarchive.api.MailArchiveService service = getInstance();
		if (service == null) return;

		service.cancelChannel(param0);
	}

	public static boolean allowRemoveChannel(java.lang.String param0)
	{
		org.sakaiproject.mailarchive.api.MailArchiveService service = getInstance();
		if (service == null) return false;

		return service.allowRemoveChannel(param0);
	}

	public static void removeChannel(org.sakaiproject.message.api.MessageChannelEdit param0)
			throws org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.mailarchive.api.MailArchiveService service = getInstance();
		if (service == null) return;

		service.removeChannel(param0);
	}

	public static java.lang.String channelReference(java.lang.String param0, java.lang.String param1)
	{
		org.sakaiproject.mailarchive.api.MailArchiveService service = getInstance();
		if (service == null) return null;

		return service.channelReference(param0, param1);
	}

	public static java.lang.String messageReference(java.lang.String param0, java.lang.String param1, java.lang.String param2)
	{
		org.sakaiproject.mailarchive.api.MailArchiveService service = getInstance();
		if (service == null) return null;

		return service.messageReference(param0, param1, param2);
	}

	public static java.lang.String messageReference(java.lang.String param0, java.lang.String param1)
	{
		org.sakaiproject.mailarchive.api.MailArchiveService service = getInstance();
		if (service == null) return null;

		return service.messageReference(param0, param1);
	}

	public static void cancelMessage(org.sakaiproject.message.api.MessageEdit param0)
	{
		org.sakaiproject.mailarchive.api.MailArchiveService service = getInstance();
		if (service == null) return;

		service.cancelMessage(param0);
	}

	public static java.util.List getMessages(java.lang.String param0, org.sakaiproject.time.api.Time param1, int param2,
			boolean param3, boolean param4, boolean param5) throws org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.mailarchive.api.MailArchiveService service = getInstance();
		if (service == null) return null;

		return service.getMessages(param0, param1, param2, param3, param4, param5);
	}

	public static java.util.List getChannelIds(java.lang.String param0)
	{
		org.sakaiproject.mailarchive.api.MailArchiveService service = getInstance();
		if (service == null) return null;

		return service.getChannelIds(param0);
	}

	public static org.sakaiproject.message.api.Message getMessage(org.sakaiproject.entity.api.Reference param0)
			throws org.sakaiproject.exception.IdUnusedException, org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.mailarchive.api.MailArchiveService service = getInstance();
		if (service == null) return null;

		return service.getMessage(param0);
	}

	public static org.sakaiproject.message.api.MessageChannel getChannel(java.lang.String param0)
			throws org.sakaiproject.exception.IdUnusedException, org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.mailarchive.api.MailArchiveService service = getInstance();
		if (service == null) return null;

		return service.getChannel(param0);
	}

	public static java.lang.String merge(java.lang.String param0, org.w3c.dom.Element param1, java.lang.String param2,
			java.lang.String param3, java.util.Map param4, java.util.HashMap param5, java.util.Set param6)
	{
		org.sakaiproject.mailarchive.api.MailArchiveService service = getInstance();
		if (service == null) return null;

		return service.merge(param0, param1, param2, param3, param4, param5, param6);
	}

	public static java.lang.String getLabel()
	{
		org.sakaiproject.mailarchive.api.MailArchiveService service = getInstance();
		if (service == null) return null;

		return service.getLabel();
	}

	public static java.lang.String archive(java.lang.String param0, org.w3c.dom.Document param1, java.util.Stack param2,
			java.lang.String param3, java.util.List param4)
	{
		org.sakaiproject.mailarchive.api.MailArchiveService service = getInstance();
		if (service == null) return null;

		return service.archive(param0, param1, param2, param3, param4);
	}
}
