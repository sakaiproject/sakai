package org.sakaiproject.dashboard.db;

import java.util.Date;

public interface DashboardPersistence {

	public abstract Long getIdForContextId(String contextId);

	public abstract Long getIdForNewsItem(Date eventTime, String entityId,
			String entityType, Long context_id);

	public abstract void saveContext(String contextId, String contextTitle,
			String contextUrl);

	public abstract void saveEventItem(String eventTime, String eventTitle,
			String accessUrl, Long contextId, Long realmId);

	public abstract void saveNewsItem(Date eventTime, String entityId,
			String entityType, String entityTitle, String accessUrl,
			Long context_id);

	public abstract void saveNewsItemJoin(Long personId, Long context_id,
			Long newsItemId, Long realmId);

	public abstract boolean siteExists(String siteId);

}