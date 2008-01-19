package org.sakaiproject.tool.assessment.facade;

import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;

public interface PublishedItemFacadeQueriesAPI {

	public IdImpl getItemId(String id);

	public IdImpl getItemId(Long id);

	public IdImpl getItemId(long id);

	public PublishedItemFacade getItem(Long itemId, String agent);
	
	public PublishedItemFacade getItem(String itemId);
	
	public void deleteItemContent(Long itemId, String agent);
}
