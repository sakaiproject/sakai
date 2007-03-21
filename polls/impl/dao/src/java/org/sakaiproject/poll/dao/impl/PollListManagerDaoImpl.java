package org.sakaiproject.poll.dao.impl;

import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.model.Vote;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.sakaiproject.authz.cover.FunctionManager;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PollListManagerDaoImpl extends HibernateDaoSupport implements PollListManager {

	//	 use commons logger
	private static Log log = LogFactory.getLog(PollListManagerDaoImpl.class);
	public static final String REFERENCE_ROOT = Entity.SEPARATOR + "poll";
	
	
	
	
	
	
	public void init() {
		try{
			EntityManager.registerEntityProducer(this, REFERENCE_ROOT);
		}
		catch (Throwable t)
		{
			log.warn("init(): ", t);
		}
		
		FunctionManager.registerFunction(PERMISSION_VOTE);
		FunctionManager.registerFunction(PERMISSION_ADD);
		FunctionManager.registerFunction(PERMISSION_DELETE_OWN);
		FunctionManager.registerFunction(PERMISSION_DELETE_ANY);
		FunctionManager.registerFunction(PERMISSION_EDIT_ANY);
		FunctionManager.registerFunction(PERMISSION_EDIT_OWN);
		log.info(this + " init()");

	}
	
	public void destroy(){
		
	}
	

	
	public boolean savePoll(Poll t) {
		boolean newPoll = false;
		if (t.getId() == null)
			newPoll = true;
		
		try {
			getHibernateTemplate().saveOrUpdate(t);
			
			
		} catch (DataAccessException e) {
			log.error("Hibernate could not save: " + e.toString());
			e.printStackTrace();
			return false;
		}
		log.info(" Poll  " + t.toString() + "successfuly saved");
		 if (newPoll)
			 
	    	  EventTrackingService.post(EventTrackingService.newEvent("poll.add", "poll/site/" + t.getSiteId() +"/poll/" +  t.getId(), true));
	      else 
	    	  EventTrackingService.post(EventTrackingService.newEvent("poll.update", "poll/site/" + t.getSiteId() +" /poll/" + t.getId(), true));
		
		
		return true;
	}

	public boolean saveOption(Option t) {
		
		
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
		EventTrackingService.post(EventTrackingService.newEvent("poll.delete", "poll/site/" + t.getSiteId() +"/poll/" +  t.getId(), true));
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
		.add( Restrictions.eq("id", pollId));
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
		.add( Restrictions.eq("id", optionId));
		Option option =  (Option)getHibernateTemplate().findByCriteria(d).get(0);
				
		return option;
	}
	
	public void deleteOption(Option option) {
		try {
			getHibernateTemplate().delete(option);
		} catch (DataAccessException e) {
			log.error("Hibernate could not delete: " + e.toString());
			e.printStackTrace();
		}
		log.info("Option id " + option.getId() + " deleted");
		
	}
	
	  private boolean pollCanDelete(Poll poll) {
		  if (SecurityService.isSuperUser() || this.isSiteOwner())
			  return true;
		  if (SecurityService.unlock(PERMISSION_DELETE_ANY,"/site/" + ToolManager.getCurrentPlacement().getContext()))
		  	return true;
		  	
		  	if (SecurityService.unlock(PERMISSION_DELETE_OWN,"/site/" + ToolManager.getCurrentPlacement().getContext()) && poll.getOwner().equals(UserDirectoryService.getCurrentUser().getId())) 
		  		return true;
		  	
		  return false;
	  }
	  private boolean isSiteOwner(){
		  if (SecurityService.isSuperUser())
			  return true;
		  else if (SecurityService.unlock("site.upd", "/site/" + ToolManager.getCurrentPlacement().getContext()))
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
			return false;
		}

		public String archive(String arg0, Document arg1, Stack arg2, String arg3,
				List arg4) {
			// TODO Auto-generated method stub
			return null;
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

		public Entity getEntity(Reference arg0) {
			// TODO Auto-generated method stub
			return null;
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
	  
}

