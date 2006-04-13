/**
 * 
 */
package uk.ac.cam.caret.sakai.rwiki.service.message.api.dao;

import java.util.List;

import uk.ac.cam.caret.sakai.rwiki.service.message.api.model.Trigger;

/**
 * @author ieb
 */
public interface TriggerDao
{
	Trigger createTrigger(String pageName, String pageSpace,
			String triggerSpec, String user);

	List findByUser(String user);

	List findBySpace(String space);

	List findByPage(String space, String page);

	void update(Object o);

}
