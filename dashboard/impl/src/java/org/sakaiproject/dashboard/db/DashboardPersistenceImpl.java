/**
 * 
 */
package org.sakaiproject.dashboard.db;

import java.util.Date;
import java.util.List;

import org.sakaiproject.db.api.SqlService;

/**
 * 
 *
 */
public class DashboardPersistenceImpl implements DashboardPersistence 
{
	protected DashboardDataQueries dashboardDataQueries;
	public void setDashboardDataQueries(DashboardDataQueries dashboardDataQueries) {
		this.dashboardDataQueries = dashboardDataQueries;
	}

	protected SqlService sqlService;
	public void setSqlService(SqlService sqlService) {
		this.sqlService = sqlService;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dashboard.db.DashboardPersistence#getIdForContextId(java.lang.String)
	 */
	@Override
	public Long getIdForContextId(String contextId) {
		String sql1 = this.dashboardDataQueries.getSelectIdForContextIdQuerySql();
		// select id from dash_context where context_id=? limit 1;
		Long id = null;

		
		Object[] fields1 = new Object[1];
		fields1[0] = contextId;
		List results = this.sqlService.dbRead(sql1, fields1, null);
		if(results != null && ! results.isEmpty()) {
			id = (Long) results.get(0);
		}
		
		return id;
		

	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.dashboard.db.DashboardPersistence#getIdForNewsItem(java.util.Date, java.lang.String, java.lang.String, java.lang.Long)
	 */
	@Override
	public Long getIdForNewsItem(Date eventTime, String entityId,
			String entityType, Long context_id) {
		Long id = null;
		String sql = this.dashboardDataQueries.getSelectIdForNewsItemSql();
		// select id from dash_news_item where news_time=? and entity_id=? and context_id=? limit 1;
		
		Object[] fields = new Object[4];
		fields[0] = eventTime;
		fields[1] = entityType;
		fields[2] = entityId;
		fields[3] = context_id;
		List results = this.sqlService.dbRead(sql, fields, null);
		if(results != null && !results.isEmpty()) {
			id = (Long) results.get(0);
		}
		return id;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dashboard.db.DashboardPersistence#saveContext(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void saveContext(String contextId, String contextTitle,
			String contextUrl) {
		String sql0 = this.dashboardDataQueries.getInsertContextSql();
		// insert into dash_context (context_id, context_url, context_title) values (?, ?, ?);
		
		Object[] fields = new Object[3];
		fields[0] = contextId;
		fields[1] = contextUrl;
		fields[2] = contextTitle;
		
		this.sqlService.dbWrite(sql0, fields );
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dashboard.db.DashboardPersistence#saveEventItem(java.lang.String, java.lang.String, java.lang.String, java.lang.Long, java.lang.Long)
	 */
	@Override
	public void saveEventItem(String eventTime, String eventTitle, String accessUrl, Long contextId, Long realmId) {
		String sql = this.dashboardDataQueries.getInsertEventItemSql();
		// insert into dash_event_item (event_time, title, access_url, context_id, realm_id) values (?, ?, ?, ?, ?) on duplicate update set event_time=?, set title=?;
		
		Object[] fields = new Object[7];
		fields[0] = eventTime;
		fields[1] = eventTitle;
		fields[2] = accessUrl;
		fields[3] = contextId;
		fields[4] = realmId;
		fields[5] = eventTime;
		fields[6] = eventTitle;
		
		this.sqlService.dbWrite(sql, fields);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dashboard.db.DashboardPersistence#saveNewsItem(java.util.Date, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Long)
	 */
	@Override
	public void saveNewsItem(Date eventTime, String entityId,
			String entityType, String entityTitle, String accessUrl, Long context_id) {
		String sql = this.dashboardDataQueries.getInsertNewsItemSql();
		// insert into dash_news_item (news_time, title, entity_id, access_url, context_id, entity_type) values (?, ?, ?, ?, ?, ?) on duplicate update set event_time=?, set title=?, set entity_id=?, ;
		
		Object[] fields = new Object[9];
		fields[0] = eventTime;
		fields[1] = entityTitle;
		fields[2] = entityId;
		fields[3] = accessUrl;
		fields[4] = context_id;
		fields[5] = entityType;
		fields[6] = eventTime;
		fields[7] = entityTitle;
		fields[8] = entityId;
		this.sqlService.dbWrite(sql, fields);
		
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dashboard.db.DashboardPersistence#saveNewsItemJoin(java.lang.Long, java.lang.Long, java.lang.Long, java.lang.Long)
	 */
	@Override
	public void saveNewsItemJoin(Long personId, Long context_id, Long newsItemId, Long realmId) {
		String sql = this.dashboardDataQueries.getInsertEventItemJoinSql();
		// insert into dash_news_join (person_id, context_id, item_id, realm_id) values (?, ?, ?, ?);
		
		Object[] fields = new Object[4];
		fields[0] = personId;
		fields[1] = context_id;
		fields[2] = newsItemId;
		fields[3] = realmId;
		
		this.sqlService.dbWrite(sql, fields);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dashboard.db.DashboardPersistence#siteExists(java.lang.String)
	 */
	@Override
	public boolean siteExists(String siteId) {
		String sql = this.dashboardDataQueries.getSelectIdForContextIdQuerySql();
		// select context_id from dash_context where context_id=? limit 1;
		
		Object[] fields = new Object[1];
		fields[0] = siteId;
		
		List results = this.sqlService.dbRead(sql, fields, null);
		
		return results != null && ! results.isEmpty();
	}

}
