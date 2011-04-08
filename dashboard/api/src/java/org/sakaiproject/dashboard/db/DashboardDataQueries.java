package org.sakaiproject.dashboard.db;

public interface DashboardDataQueries {

	/**
	 * 
	 * @return
	 */
	public abstract String getInsertPersonSql();

	/**
	 * 
	 * @return
	 */
	public abstract String getInsertContextSql();

	/**
	 * 
	 * @return
	 */
	public abstract String getInsertSourceSql();

	/**
	 * 
	 * @return
	 */
	public abstract String getInsertRealmSql();

	/**
	 * 
	 * @return
	 */
	public abstract String getInsertEventItemSql();

	/**
	 * 
	 * @return
	 */
	public abstract String getInsertEventItemJoinSql();

	/**
	 * 
	 * @return
	 */
	public abstract String getInsertNewsItemSql();

	/**
	 * 
	 * @return
	 */
	public abstract String getInsertNewsItemJoinSql();

	/**
	 * Get SQL to retrieve a limited number of news items for a user by date. 
	 * The SQL will take three or four parameters, depending on the value of "inContext".
	 * The parameters will be (in order) the user-id of a person, the context-id of a site 
	 * (if and only if "inContext" is true), the date before which to retrieve items, and 
	 * the maximum number of items to retrieve.  The results of the query will be in 
	 * order by time with newest first.
	 * @param inContext Whether to include the context-id as a parameter in the query.
	 * @param includeHidden Whether the query should return hidden items in addition to visible items.  
	 */
	public abstract String getSelectNewsItemsForUserSql(boolean inContext,
			boolean includeHidden);

	/**
	 * Get SQL to retrieve a limited number of event items for a user by date. 
	 * The SQL will take three or four parameters, depending on the value of "inContext".
	 * The parameters will be (in order) the user-id of a person, the context-id of a site 
	 * (if and only if "inContext" is true), the date after which to retrieve items, and 
	 * the maximum number of items to retrieve.  The results of the query will be in 
	 * order by time of the event with soonest first.
	 * @param inContext Whether to include the context-id as a parameter in the query.
	 * @param includeHidden Whether the query should return hidden items in addition to visible items.  
	 */
	public abstract String getSelectEventItemsForUserSql(boolean inContext,
			boolean includeHidden);

	/**
	 * 
	 * @param inContext
	 * @return
	 */
	public abstract String getSelectStickyNewsItemsForUserSql(boolean inContext);

	/**
	 * 
	 * @param inContext
	 * @return
	 */
	public abstract String getSelectStickyEventItemsForUserSql(boolean inContext);

	/**
	 * 
	 * @return
	 */
	public abstract String getUpdateNewsItemSetStickyForUserSql();

	/**
	 * 
	 * @return
	 */
	public abstract String getUpdateEventItemSetStickyForUserSql();

	/**
	 * 
	 * @return
	 */
	public abstract String getUpdateNewsItemSetHiddenForUserSql();

	/**
	 * 
	 * @return
	 */
	public abstract String getUpdateEventItemSetHiddenForUserSql();

	public abstract String getSelectIdForContextIdQuerySql();

	public abstract String getSelectIdForNewsItemSql();

}