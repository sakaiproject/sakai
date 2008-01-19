package org.sakaiproject.tool.assessment.facade;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class PublishedItemFacadeQueries extends HibernateDaoSupport implements
		PublishedItemFacadeQueriesAPI {
	private static Log log = LogFactory
			.getLog(PublishedItemFacadeQueries.class);

	public IdImpl getItemId(String id) {
		return new IdImpl(id);
	}

	public IdImpl getItemId(Long id) {
		return new IdImpl(id);
	}

	public IdImpl getItemId(long id) {
		return new IdImpl(id);
	}

	public PublishedItemFacade getItem(Long itemId, String agent) {
		PublishedItemData item = (PublishedItemData) getHibernateTemplate()
				.load(PublishedItemData.class, itemId);
		return new PublishedItemFacade(item);
	}
	
	public PublishedItemFacade getItem(String itemId) {
		PublishedItemData item = (PublishedItemData) getHibernateTemplate()
				.load(PublishedItemData.class, Long.valueOf(itemId));
		return new PublishedItemFacade(item);
	}

	public void deleteItemContent(Long itemId, String agent) {
		PublishedItemData item = (PublishedItemData) getHibernateTemplate().load(PublishedItemData.class,
				itemId);

		int retryCount = PersistenceService.getInstance().getRetryCount()
				.intValue();
		while (retryCount > 0) {
			try {
				if (item != null) { // need to dissociate with item before deleting in Hibernate 3
					Set set = item.getItemTextSet();
					item.setItemTextSet(new HashSet());
					getHibernateTemplate().deleteAll(set);
					retryCount = 0;
				} else
					retryCount = 0;
			} catch (Exception e) {
				log.warn("problem deleteItemTextSet: " + e.getMessage());
				retryCount = PersistenceService.getInstance().retryDeadlock(e,
						retryCount);
			}
		}

		retryCount = PersistenceService.getInstance().getRetryCount()
				.intValue();
		while (retryCount > 0) {
			try {
				if (item != null) { // need to dissociate with item before deleting in Hibernate 3
					Set set = item.getItemMetaDataSet();
					item.setItemMetaDataSet(new HashSet());
					getHibernateTemplate().deleteAll(set);
					retryCount = 0;
				} else
					retryCount = 0;
			} catch (Exception e) {
				log.warn("problem deleteItemMetaDataSet: " + e.getMessage());
				retryCount = PersistenceService.getInstance().retryDeadlock(e,
						retryCount);
			}
		}

		retryCount = PersistenceService.getInstance().getRetryCount()
				.intValue();
		while (retryCount > 0) {
			try {
				if (item != null) { // need to dissociate with item before deleting in Hibernate 3
					Set set = item.getItemFeedbackSet();
					item.setItemFeedbackSet(new HashSet());
					getHibernateTemplate().deleteAll(set);
					retryCount = 0;
				} else
					retryCount = 0;
			} catch (Exception e) {
				log.warn("problem deleting ItemFeedbackSet: " + e.getMessage());
				retryCount = PersistenceService.getInstance().retryDeadlock(e,
						retryCount);
			}
		}
	}
	
/*
	 public PublishedItemFacade saveItem(PublishedItemFacade item) throws DataFacadeException {
	    try{
	    	ItemDataIfc publishedItemdata = (ItemDataIfc) item.getData();
	      publishedItemdata.setLastModifiedDate(new Date());
	      publishedItemdata.setLastModifiedBy(AgentFacade.getAgentString());
	    int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
	    while (retryCount > 0){
	      try {
	        getHibernateTemplate().saveOrUpdate(publishedItemdata);
	        item.setItemId(publishedItemdata.getItemId());
	        retryCount = 0;
	      }
	      catch (Exception e) {
	        log.warn("problem save or update itemdata: "+e.getMessage());
	        retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
	      }
	    }
	    if ((item.getData()!= null) && (item.getData().getSection()!= null)) {
	    AssessmentIfc assessment = item.getData().getSection().getAssessment();
	    assessment.setLastModifiedBy(AgentFacade.getAgentString());
	    assessment.setLastModifiedDate(new Date());
	    retryCount = PersistenceService.getInstance().getRetryCount().intValue();
	    while (retryCount > 0){
	    	try {
	    		getHibernateTemplate().update(assessment);
	    		retryCount = 0;
	    	}
	    	catch (Exception e) {
	    		log.warn("problem updating asssessment: "+e.getMessage());
	    		retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
	    	}
	    }
	    }
	    return item;
	    }
	    catch(Exception e){
		e.printStackTrace();
		return null;
	    }
	 }
	 */

}
