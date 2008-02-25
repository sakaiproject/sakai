/**********************************************************************************
 * $URL: $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006,2007 The Sakai Foundation.
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

package org.sakaiproject.poll.dao.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.model.Vote;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.sakaiproject.exception.PermissionException;

import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.site.api.SiteService;

import org.sakaiproject.id.api.IdManager;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.tool.api.ToolManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PollListManagerDaoImpl extends HibernateDaoSupport implements PollListManager {

	//	 use commons logger
	private static Log log = LogFactory.getLog(PollListManagerDaoImpl.class);
	public static final String REFERENCE_ROOT = Entity.SEPARATOR + "poll";

	private SecurityService securityService;
	public void setSecurityService(SecurityService ss) {
		securityService =ss;
	}


	private EventTrackingService eventTrackingService;
	public void setEventTrackingService(EventTrackingService ets) {
		eventTrackingService = ets;
	}

	private ToolManager toolManager;
	public void setToolManager(ToolManager tm) {
		toolManager = tm;
	}

	private EntityManager entityManager;
	public void setEntityManager(EntityManager em) {
		entityManager = em;
	}


	private FunctionManager functionManager;
	public void setFunctionManager(FunctionManager fm) {
		functionManager = fm;
	}

	public void init() {
		try{
			entityManager.registerEntityProducer(this, REFERENCE_ROOT);
		}
		catch (Throwable t)
		{
			log.warn("init(): ", t);
		}

		functionManager.registerFunction(PERMISSION_VOTE);
		functionManager.registerFunction(PERMISSION_ADD);
		functionManager.registerFunction(PERMISSION_DELETE_OWN);
		functionManager.registerFunction(PERMISSION_DELETE_ANY);
		functionManager.registerFunction(PERMISSION_EDIT_ANY);
		functionManager.registerFunction(PERMISSION_EDIT_OWN);
		log.info(this + " init()");

	}

	public void destroy(){

	}

	private IdManager idManager;

	public void setIdManager(IdManager idm) {
		idManager = idm;
	}

	public boolean savePoll(Poll t) {
		boolean newPoll = false;
		if (t.getId() == null) {
			newPoll = true;
			t.setId(idManager.createUuid());
		}

		//we may need to truncate the description field
		if (t.getDetails().length() > 254)
			t.setDetails(t.getDetails().substring(0, 254));
		
		try {
			getHibernateTemplate().saveOrUpdate(t);


		} catch (DataAccessException e) {
			log.error("Hibernate could not save: " + e.toString());
			e.printStackTrace();
			return false;
		}
		log.debug(" Poll  " + t.toString() + "successfuly saved");
		if (newPoll)

			eventTrackingService.post(eventTrackingService.newEvent("poll.add", "poll/site/" + t.getSiteId() +"/poll/" +  t.getId(), true));
		else 
			eventTrackingService.post(eventTrackingService.newEvent("poll.update", "poll/site/" + t.getSiteId() +" /poll/" + t.getId(), true));


		return true;
	}

	public boolean saveOption(Option t) {

		boolean newOption = false;
		if (t.getId() == null || t.getId().trim().length() == 0) {
			newOption = true;
			t.setId(idManager.createUuid());
		}


		try {
			getHibernateTemplate().saveOrUpdate(t);

		} catch (DataAccessException e) {
			log.error("Hibernate could not save: " + e.toString());
			e.printStackTrace();
			return false;
		}
		log.info("Option  " + t.toString() + "successfuly saved");
		return true;
	}	


	public boolean deletePoll(Poll t)  throws PermissionException {
		if (!pollCanDelete(t))
			throw new PermissionException(UserDirectoryService.getCurrentUser().getId(),"poll.delete","poll." + t.getId().toString());

		try {
			getHibernateTemplate().delete(t);
		} catch (DataAccessException e) {
			log.error("Hibernate could not delete: " + e.toString());
			e.printStackTrace();
			return false;
		}
		log.info("Poll id " + t.getId() + " deleted");
		eventTrackingService.post(eventTrackingService.newEvent("poll.delete", "poll/site/" + t.getSiteId() +"/poll/" +  t.getId(), true));
		return true;
	}

	public List findAllPolls(String siteId) {

		DetachedCriteria d = DetachedCriteria.forClass(Poll.class)
		.add( Restrictions.eq("siteId", siteId) )
		.addOrder( Order.desc("creationDate") );
		Collection pollCollection = getHibernateTemplate().findByCriteria(d);
		List pollList = PollUtil.pollCollectionToList(pollCollection);

		return pollList;
	}


	public Poll getPollById(Long pollId) {
		DetachedCriteria d = DetachedCriteria.forClass(Poll.class)
		.add( Restrictions.eq("pollId", pollId));
		Poll poll =  (Poll)PollUtil.pollCollectionToList(getHibernateTemplate().findByCriteria(d)).get(0);

		//we need to get the options here
		List optionList = getOptionsForPoll(poll);

		poll.setOptions(optionList);

		return poll;
	}


	public List getOptionsForPoll(Poll poll) {

		//we need to get the options here
		DetachedCriteria d = DetachedCriteria.forClass(Option.class)
		.add( Restrictions.eq("pollId", poll.getPollId()));
		List optionList = PollUtil.optionCollectionToList(getHibernateTemplate().findByCriteria(d));
		return optionList;

	}


	public Poll getPollWithVotes(Long pollId) {
		DetachedCriteria d = DetachedCriteria.forClass(Poll.class)
		.add( Restrictions.eq("pollId", pollId));
		return (Poll)PollUtil.pollCollectionToList(getHibernateTemplate().findByCriteria(d)).get(0);

	}

	public Option getOptionById(Long optionId){
		DetachedCriteria d = DetachedCriteria.forClass(Option.class)
		.add( Restrictions.eq("optionId", optionId));
		Option option =  (Option)getHibernateTemplate().findByCriteria(d).get(0);
		//if the id is null set it
		if (option.getId() == null) {
			option.setId(idManager.createUuid());
			saveOption(option);
		}
		return option;
	}

	public void deleteOption(Option option) {


		try {
			getHibernateTemplate().delete(option);
		} catch (DataAccessException e) {
			log.error("Hibernate could not delete: " + e.toString());
			e.printStackTrace();
			return;
		}
		log.info("Option id " + option.getId() + " deleted");

	}

	private boolean pollCanDelete(Poll poll) {
		if (securityService.isSuperUser() || this.isSiteOwner())
			return true;
		if (securityService.unlock(PERMISSION_DELETE_ANY,"/site/" + toolManager.getCurrentPlacement().getContext()))
			return true;

		if (securityService.unlock(PERMISSION_DELETE_OWN,"/site/" + toolManager.getCurrentPlacement().getContext()) && poll.getOwner().equals(UserDirectoryService.getCurrentUser().getId())) 
			return true;

		return false;
	}
	private boolean isSiteOwner(){
		if (securityService.isSuperUser())
			return true;
		else if (securityService.unlock("site.upd", "/site/" + toolManager.getCurrentPlacement().getContext()))
			return true;
		else
			return false;
	}


	/*
	 * EntityProducer Methods
	 */
	public String getLabel() {
		return "poll";
	}

	public boolean willArchiveMerge() {
		return true;
	}

	public String archive(String siteId, Document doc, Stack stack, String archivePath,
			List attachments) {
		log.debug("archive: poll " + siteId);
		// prepare the buffer for the results log
		StringBuilder results = new StringBuilder();

		// String assignRef = assignmentReference(siteId, SiteService.MAIN_CONTAINER);
		results.append("archiving " + getLabel() + " context " + Entity.SEPARATOR + siteId + Entity.SEPARATOR
				+ SiteService.MAIN_CONTAINER + ".\n");

		// start with an element with our very own (service) name
		Element element = doc.createElement(PollListManager.class.getName());
		((Element) stack.peek()).appendChild(element);
		stack.push(element);

		List pollsList = findAllPolls(siteId);
		log.debug("got list of " + pollsList.size() + " polls");	
		for (int i = 0; pollsList.size()> i; i ++)
		{
			try {
				Poll poll = (Poll)pollsList.get(i);
				log.info("got poll " + poll.getId());

				// archive this assignment
				Element el = poll.toXml(doc, stack);

				//get theoptions
				List options = getOptionsForPoll(poll);

				for (int q = 0 ; options.size()>q;q++) {
					Option opt = (Option)options.get(q);
					Element el2 = opt.toXml(doc, stack);
					el.appendChild(el2);
				}

				element.appendChild(el);
			}
			catch (Exception e) {
				e.printStackTrace();
			}

		} // while

		stack.pop();

		return results.toString();
	}

	public String merge(String arg0, Element arg1, String arg2, String arg3,
			Map arg4, Map arg5, Set arg6) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean parseEntityReference(String reference, Reference ref) {
		if (reference.startsWith(REFERENCE_ROOT))
		{
			// /syllabus/siteid/syllabusid
			String[] parts = split(reference, Entity.SEPARATOR);

			String subType = null;
			String context = null;
			String id = null;
			String container = null;

			if (parts.length > 2)
			{
				// the site/context
				context = parts[2];

				// the id
				if (parts.length > 3)
				{
					id = parts[3];
				}
			}

			ref.set(PollListManager.class.getName(), subType, id, container, context);

			return true;
		}

		return false;
	}

	public String getEntityDescription(Reference arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public ResourceProperties getEntityResourceProperties(Reference arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Entity getEntity(Reference ref) {
		// TODO Auto-generated method stub
		Entity rv = null;


		if (REF_POLL_TYPE.equals(ref.getSubType()))
		{
			rv = getPoll(ref.getReference());
		}

		return rv;
	}

	public String getEntityUrl(Reference arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection getEntityAuthzGroups(Reference arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public HttpAccess getHttpAccess() {
		// TODO Auto-generated method stub
		return null;
	}

	protected String[] split(String source, String splitter)
	{
		// hold the results as we find them
		Vector rv = new Vector();
		int last = 0;
		int next = 0;
		do
		{
			// find next splitter in source
			next = source.indexOf(splitter, last);
			if (next != -1)
			{
				// isolate from last thru before next
				rv.add(source.substring(last, next));
				last = next + splitter.length();
			}
		}
		while (next != -1);
		if (last < source.length())
		{
			rv.add(source.substring(last, source.length()));
		}

		// convert to array
		return (String[]) rv.toArray(new String[rv.size()]);

	} // split

	public Poll getPoll(String ref) {
		//we need to get the options here
		DetachedCriteria d = DetachedCriteria.forClass(Poll.class)
		.add( Restrictions.eq("id", ref));
		List optionList = PollUtil.optionCollectionToList(getHibernateTemplate().findByCriteria(d));
		Poll poll = (Poll)optionList.get(0);
		//if the id is null set it
		if (poll.getId() == null) {
			poll.setId(idManager.createUuid());
			savePoll(poll);
		}
		return poll;
	}



}

