/**
 * 
 */
package uk.ac.cam.caret.sakai.rwiki.tool.bean;

import java.util.List;

import uk.ac.cam.caret.sakai.rwiki.service.message.api.MessageService;

/**
 * @author ieb
 */
public class PresenceBean
{
	private String pageName;

	private String pageSpace;

	private MessageService messageService;

	/**
	 * @return Returns the pageName.
	 */
	public String getPageName()
	{
		return pageName;
	}

	/**
	 * @param pageName
	 *        The pageName to set.
	 */
	public void setPageName(String pageName)
	{
		this.pageName = pageName;
	}

	/**
	 * @return Returns the pageSpace.
	 */
	public String getPageSpace()
	{
		return pageSpace;
	}

	/**
	 * @param pageSpace
	 *        The pageSpace to set.
	 */
	public void setPageSpace(String pageSpace)
	{
		this.pageSpace = pageSpace;
	}

	/**
	 * @return Returns the messageService.
	 */
	public MessageService getMessageService()
	{
		return messageService;
	}

	/**
	 * @param messageService
	 *        The messageService to set.
	 */
	public void setMessageService(MessageService messageService)
	{
		this.messageService = messageService;
	}

	/**
	 * returns a list of users on the page, ordered by last seen
	 * 
	 * @return
	 */
	public List getPagePresence()
	{
		return messageService.getUsersOnPage(pageSpace, pageName);
	}

	/**
	 * returns a list of users in the space, ordered by last seen
	 * 
	 * @return
	 */
	public List getSpacePresence()
	{
		return messageService.getUsersInSpaceOnly(pageSpace, pageName);
	}

	public List getPageMessages()
	{
		return messageService.getMessagesInPage(pageSpace, pageName);
	}

	public List getSpaceMessages()
	{
		return messageService.getMessagesInSpace(pageSpace);
	}

}
