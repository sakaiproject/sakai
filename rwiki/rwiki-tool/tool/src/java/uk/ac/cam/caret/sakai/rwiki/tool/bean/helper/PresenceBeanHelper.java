/**
 * 
 */
package uk.ac.cam.caret.sakai.rwiki.tool.bean.helper;

import uk.ac.cam.caret.sakai.rwiki.service.message.api.MessageService;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.PresenceBean;

/**
 * @author ieb
 */
public class PresenceBeanHelper
{

	/**
	 * @param messageService
	 * @param currentUser
	 * @param currentPageName
	 * @param currentPageSpace
	 * @return
	 */
	public static PresenceBean createRealmBean(MessageService messageService,
			String currentPageName, String currentPageSpace)
	{
		PresenceBean pb = new PresenceBean();
		pb.setMessageService(messageService);
		pb.setPageName(currentPageName);
		pb.setPageSpace(currentPageSpace);
		return pb;
	}

}
