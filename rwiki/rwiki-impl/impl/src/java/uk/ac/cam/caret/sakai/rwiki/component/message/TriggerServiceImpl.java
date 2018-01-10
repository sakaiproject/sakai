/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package uk.ac.cam.caret.sakai.rwiki.component.message;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import uk.ac.cam.caret.sakai.rwiki.service.message.api.TriggerHandler;
import uk.ac.cam.caret.sakai.rwiki.service.message.api.TriggerService;
import uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.TriggerDao;
import uk.ac.cam.caret.sakai.rwiki.service.message.api.model.Trigger;

/**
 * @author ieb
 */
@Slf4j
public class TriggerServiceImpl implements TriggerService
{
	private TriggerDao triggerDao;

	private Map triggerHandlers;

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.TriggerService#fireSpaceTriggers(java.lang.String)
	 */
	public void fireSpaceTriggers(String space)
	{
		List l = triggerDao.findBySpace(space);
		for (Iterator il = l.iterator(); il.hasNext();)
		{
			Trigger t = (Trigger) il.next();
			String triggerSpec = t.getTriggerspec();
			TriggerHandler ts = (TriggerHandler) triggerHandlers
					.get(triggerSpec);
			if (ts != null)
			{
				ts.fireOnSpace(space);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.TriggerService#firePageTriggers(java.lang.String,
	 *      java.lang.String)
	 */
	public void firePageTriggers(String space, String page)
	{
		List l = triggerDao.findByPage(space, page);
		for (Iterator il = l.iterator(); il.hasNext();)
		{
			Trigger t = (Trigger) il.next();
			String triggerSpec = t.getTriggerspec();
			TriggerHandler ts = (TriggerHandler) triggerHandlers
					.get(triggerSpec);
			if (ts != null)
			{
				ts.fireOnPage(space, page);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.TriggerService#addTrigger(java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public void addTrigger(String user, String space, String page, String spec)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.TriggerService#removeTrigger(uk.ac.cam.caret.sakai.rwiki.service.message.api.model.Trigger)
	 */
	public void removeTrigger(Trigger trigger)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.TriggerService#updateTrigger(uk.ac.cam.caret.sakai.rwiki.service.message.api.model.Trigger)
	 */
	public void updateTrigger(Trigger trigger)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.TriggerService#getUserTriggers(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public List getUserTriggers(String user, String space, String page)
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.TriggerService#getUserTriggers(java.lang.String,
	 *      java.lang.String)
	 */
	public List getUserTriggers(String user, String space)
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.TriggerService#getUserTriggers(java.lang.String)
	 */
	public List getUserTriggers(String user)
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.TriggerService#getPageTriggers(java.lang.String,
	 *      java.lang.String)
	 */
	public List getPageTriggers(String space, String page)
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.TriggerService#getSpaceTriggers(java.lang.String)
	 */
	public List getSpaceTriggers(String space)
	{
		return null;
	}

	/**
	 * @return Returns the triggerDao.
	 */
	public TriggerDao getTriggerDao()
	{
		return triggerDao;
	}

	/**
	 * @param triggerDao
	 *        The triggerDao to set.
	 */
	public void setTriggerDao(TriggerDao triggerDao)
	{
		this.triggerDao = triggerDao;
	}

	/**
	 * @return Returns the triggerHandlers.
	 */
	public Map getTriggerHandlers()
	{
		return triggerHandlers;
	}

	/**
	 * @param triggerHandlers
	 *        The triggerHandlers to set.
	 */
	public void setTriggerHandlers(Map triggerHandlers)
	{
		this.triggerHandlers = triggerHandlers;
	}

}
