package org.sakaiproject.tool.assessment.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.PublishedItemFacade;

public class PublishedItemService extends ItemService {
	private Logger log = LoggerFactory.getLogger(PublishedItemService.class);

	public ItemFacade getItem(Long itemId, String agentId) {
		PublishedItemFacade item = null;
		try {
			item = PersistenceService.getInstance()
					.getPublishedItemFacadeQueries().getItem(itemId, agentId);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}

		return item;
	}
	
	public ItemFacade getItem(String itemId) {
		PublishedItemFacade item = null;
		try {
			item = PersistenceService.getInstance()
					.getPublishedItemFacadeQueries().getItem(itemId);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}

		return item;
	}

	public void deleteItemContent(Long itemId, String agentId) {
		try {
			PersistenceService.getInstance().getPublishedItemFacadeQueries()
					.deleteItemContent(itemId, agentId);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

    /**
     * Save a question item.
     */
    public ItemFacade saveItem(PublishedItemFacade item)
    {
      try
      {
        return PersistenceService.getInstance().getPublishedItemFacadeQueries().saveItem(item);
      }
      catch(Exception e)
      {
        log.error(e.getMessage(), e);

        return item;
      }
    }
}
