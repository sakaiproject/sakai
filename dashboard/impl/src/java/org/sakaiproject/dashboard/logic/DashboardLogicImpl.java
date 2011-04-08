/******************************************************************************
 * DashboardLogicImpl.java - created by Sakai App Builder -AZ
 * 
 * Copyright (c) 2008 Sakai Project/Sakai Foundation
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 *****************************************************************************/

package org.sakaiproject.dashboard.logic;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.genericdao.api.search.Search;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.dashboard.logic.ExternalLogic;
import org.sakaiproject.dashboard.dao.DashboardDao;
import org.sakaiproject.dashboard.db.DashboardPersistence;
import org.sakaiproject.dashboard.logic.DashboardLogic;
import org.sakaiproject.dashboard.model.DashboardItem;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;

/**
 * This is the implementation of the business logic interface
 * @author Sakai App Builder -AZ
 */
public class DashboardLogicImpl implements DashboardLogic {

   private static Log log = LogFactory.getLog(DashboardLogicImpl.class);

   private DashboardDao dao;
   public void setDao(DashboardDao dao) {
      this.dao = dao;
   }

   private ExternalLogic externalLogic;
   public void setExternalLogic(ExternalLogic externalLogic) {
      this.externalLogic = externalLogic;
   }

	protected SiteService siteService;
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}
	
	protected DashboardPersistence dashboardPersister;
	public void setDashboardPersister(DashboardPersistence dashboardPersister) {
		this.dashboardPersister = dashboardPersister;
	}
	
	protected EntityManager entityManager;
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	protected AuthzGroupService authzGroupService;
	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
		this.authzGroupService = authzGroupService;
	}

   /**
    * Place any code that should run when this class is initialized by spring here
    */
   public void init() {
      log.debug("init");
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.dashboard.logic.DashboardLogic#getItemById(java.lang.Long)
    */
   public DashboardItem getItemById(Long id) {
      log.debug("Getting item by id: " + id);
      return dao.findById(DashboardItem.class, id);
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.dashboard.logic.DashboardLogic#canWriteItem(org.sakaiproject.dashboard.model.DashboardItem, java.lang.String, java.lang.String)
    */
   public boolean canWriteItem(DashboardItem item, String locationId, String userId) {
      log.debug("checking if can write for: " + userId + ", " + locationId + ": and item=" + item.getTitle() );
      if (item.getCreatorId().equals( userId ) ) {
         // owner can always modify an item
         return true;
      } else if ( externalLogic.isUserAdmin(userId) ) {
         // the system super user can modify any item
         return true;
      } else if ( locationId.equals(item.getLocationId()) &&
            externalLogic.isUserAllowedInLocation(userId, ExternalLogic.ITEM_WRITE_ANY, locationId) ) {
         // users with permission in the specified site can modify items from that site
         return true;
      }
      return false;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.dashboard.logic.DashboardLogic#getAllVisibleItems(java.lang.String, java.lang.String)
    */
   public List<DashboardItem> getAllVisibleItems(String locationId, String userId) {
      log.debug("Fetching visible items for " + userId + " in site: " + locationId);
      List<DashboardItem> l = null;
      if (locationId == null) {
         // get all items
         l = dao.findAll(DashboardItem.class);
      } else {
         l = dao.findBySearch(DashboardItem.class, 
               new Search("locationId", locationId) );
      }
      // check if the current user can see all items (or is super user)
      if ( externalLogic.isUserAdmin(userId) || 
            externalLogic.isUserAllowedInLocation(userId, ExternalLogic.ITEM_READ_HIDDEN, locationId) ) {
         log.debug("Security override: " + userId + " able to view all items");
      } else {
         // go backwards through the loop to avoid hitting the "end" early
//         for (int i=l.size()-1; i >= 0; i--) {
//            DashboardItem item = (DashboardItem) l.get(i);
//            if ( item.getHidden().booleanValue() &&
//                  !item.getCreatorId().equals(userId) ) {
//               l.remove(item);
//            }
//         }
      }
      return l;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.dashboard.logic.DashboardLogic#removeItem(org.sakaiproject.dashboard.model.DashboardItem)
    */
   public void removeItem(DashboardItem item) {
      log.debug("In removeItem with item:" + item.getId() + ":" + item.getTitle());
      // check if current user can remove this item
      if ( canWriteItem(item, externalLogic.getCurrentLocationId(), externalLogic.getCurrentUserId() ) ) {
         dao.delete(item);
         log.info("Removing item: " + item.getId() + ":" + item.getTitle());
      } else {
         throw new SecurityException("Current user cannot remove item " + 
               item.getId() + " because they do not have permission");
      }
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.dashboard.logic.DashboardLogic#saveItem(org.sakaiproject.dashboard.model.DashboardItem)
    */
   public void saveItem(DashboardItem item) {
      log.debug("In saveItem with item:" + item.getTitle());
      // set the owner and site to current if they are not set
      if (item.getCreatorId() == null) {
         item.setCreatorId( externalLogic.getCurrentUserId() );
      }
      if (item.getLocationId() == null) {
         item.setLocationId( externalLogic.getCurrentLocationId() );
      }
      if (item.getCreatedDate() == null) {
         item.setCreatedDate(new Date());
      }
      // save item if new OR check if the current user can update the existing item
      if ( (item.getId() == null) || 
            canWriteItem(item, externalLogic.getCurrentLocationId(), externalLogic.getCurrentUserId()) ) {
         dao.save(item);
         log.info("Saving item: " + item.getId() + ":" + item.getTitle());
      } else {
         throw new SecurityException("Current user cannot update item " + 
               item.getId() + " because they do not have permission");
      }
   }

	@Override
	public void postDashboardItems(String eventType, Date eventTime,
			String contextId, String entityRef) {
		// TODO Auto-generated method stub
		if(eventType == null) {
			// log an error
			log.warn("Error trying to post a Dashboard item");
		} else {
			Reference reference = entityManager.newReference(entityRef);
			
			if(contextId == null) {
				contextId = reference.getContext();
			}
			// check whether site exists
			if (contextId == null) {
				
			} else if(! this.dashboardPersister.siteExists(contextId)) {
				// create if not
				String contextUrl = null;
				String contextTitle = null;
				try {
					Site site = siteService.getSite(contextId);
					contextUrl = site.getUrl();
					contextTitle = site.getTitle();
				} catch (IdUnusedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				this.dashboardPersister.saveContext(contextId, contextTitle, contextUrl);
			} 
			Long context_id = this.dashboardPersister.getIdForContextId(contextId);
			if(context_id == null) {
				// wooops
				log.warn("Error saving context info: " + contextId);
				// return??
			}
			
			String entityId = reference.getId();
			String entityType = reference.getType();
			String accessUrl = reference.getUrl();
			ResourceProperties props = reference.getProperties();
			
			String entityTitle = props.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
			if(entityTitle == null) {
				entityTitle = reference.getDescription();
			}
			
			
			this.dashboardPersister.saveNewsItem(eventTime, entityId, entityType, entityTitle, accessUrl, context_id);
			
			Long newsItemId = this.dashboardPersister.getIdForNewsItem(eventTime, entityId, entityType, context_id);
			
			Collection<String> authzGroups = reference.getAuthzGroups();
			
			
			List<String> functions = this.getFunctionsForEntity(entityType);
			for(String function : functions) {
				Set<String> userIds = this.authzGroupService.getUsersIsAllowed(function, authzGroups);
				Set<Long> personIds = this.getPersonIdsForUserIds(userIds);
				
				for(Long personId : personIds) {
					this.dashboardPersister.saveNewsItemJoin(newsItemId, personId, context_id, null);
				}
			}
		}
	}

	protected List<String> getFunctionsForEntity(String entityType) {
		// TODO Auto-generated method stub
		return null;
	}

	protected Set<Long> getPersonIdsForUserIds(Set<String> userIds) {
		// TODO Auto-generated method stub
		return null;
	}

}
