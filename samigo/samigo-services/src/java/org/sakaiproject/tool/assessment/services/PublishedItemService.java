package org.sakaiproject.tool.assessment.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.PublishedItemFacade;

public class PublishedItemService extends ItemService {
	private static Log log = LogFactory.getLog(PublishedItemService.class);

	public ItemFacade getItem(Long itemId, String agentId) {
		PublishedItemFacade item = null;
		try {
			item = PersistenceService.getInstance()
					.getPublishedItemFacadeQueries().getItem(itemId, agentId);
		} catch (Exception e) {
			log.error(e);
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
			log.error(e);
			throw new RuntimeException(e);
		}

		return item;
	}

	public void deleteItemContent(Long itemId, String agentId) {
		try {
			PersistenceService.getInstance().getPublishedItemFacadeQueries()
					.deleteItemContent(itemId, agentId);
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}
}
