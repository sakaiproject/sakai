package org.sakaiproject.dashboard.db;

public class DashboardDataQueriesDefault implements DashboardDataQueries 
{
	// queries from EventHandler:
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dashboard.db.DashboardDataQueries#getInsertPersonSql()
	 */
	@Override
	public String getInsertPersonSql() {
		return "insert into dash_person (user_id) values (?);";
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dashboard.db.DashboardDataQueries#getInsertContextSql()
	 */
	@Override
	public String getInsertContextSql() {
		return "insert into dash_context (context_id, context_url, context_title) values (?, ?, ?);";
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dashboard.db.DashboardDataQueries#getInsertSourceSql()
	 */
	@Override
	public String getInsertSourceSql() {
		return "insert into dash_source (source_id) values (?);";
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dashboard.db.DashboardDataQueries#getInsertRealmSql()
	 */
	@Override
	public String getInsertRealmSql() {
		return "insert into dash_realm (realm_id) values (?);";
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dashboard.db.DashboardDataQueries#getInsertEventItemSql()
	 */
	@Override
	public String getInsertEventItemSql() {
		return "insert into dash_event_item (event_time, title, access_url, context_id, realm_id) values (?, ?, ?, ?, ?) on duplicate key update event_time=?, title=?;";
	}
		
	/* (non-Javadoc)
	 * @see org.sakaiproject.dashboard.db.DashboardDataQueries#getInsertEventItemJoinSql()
	 */
	@Override
	public String getInsertEventItemJoinSql() {
		return "insert into dash_event_join (person_id, context_id, item_id, realm_id) values (?, ?, ?, ?);";
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dashboard.db.DashboardDataQueries#getInsertNewsItemSql()
	 */
	@Override
	public String getInsertNewsItemSql() {
		return "insert into dash_news_item (news_time, title, entity_id, access_url, context_id, realm_id) values (?, ?, ?, ?, ?, ?) on duplicate key update event_time=?, title=?, entity_id=?;";
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dashboard.db.DashboardDataQueries#getInsertNewsItemJoinSql()
	 */
	@Override
	public String getInsertNewsItemJoinSql() {
		return "insert into dash_news_join (person_id, context_id, item_id, realm_id) values (?, ?, ?, ?);";
	}

	// update news/calendar items and links for users, context and realm
	// delete news/calendar items and links for users, context and realm


	// queries from UI: 
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dashboard.db.DashboardDataQueries#getSelectNewsItemsForUserSql(boolean, boolean)
	 */
	@Override
	public String getSelectNewsItemsForUserSql(boolean inContext, boolean includeHidden) {
		StringBuilder buf = new StringBuilder();
		
		buf.append("select person.id, item.id, item.news_time, item.title, item.access_url, ");
		buf.append("context.context_id, context.context_url, context.context_title ");
		buf.append("from dash_person person join dash_news_join jointable on jointable.person_id=person.id ");
		buf.append("join dash_context context on jointable.context_id=context.id ");
		buf.append("join dash_news_item item  on jointable.item_id=item.id ");
		buf.append("where person.user_id=? ");
		if(inContext) {
			buf.append("and context.context_id=?  ");
		}
		if(! includeHidden) {
			buf.append("and jointable.hidden='0' ");
		}
		buf.append("and jointable.sticky='0' and item.news_time < ? order by item.news_time desc, item.title limit ?;");
		
		return buf.toString();
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dashboard.db.DashboardDataQueries#getSelectEventItemsForUserSql(boolean, boolean)
	 */
	@Override
	public String getSelectEventItemsForUserSql(boolean inContext, boolean includeHidden) {
		StringBuilder buf = new StringBuilder();
		
		buf.append("select person.id,item.id, item.event_time, item.title, item.access_url, ");
		buf.append("context.context_id, context.context_url, context.context_title ");
		buf.append("from dash_person person join dash_event_join jointable on jointable.person_id=person.id ");
		buf.append("join dash_context context on jointable.context_id=context.id ");
		buf.append("join dash_event_item item  on jointable.item_id=item.id ");
		buf.append("where person.user_id=? ");
		if(inContext) {
			buf.append("and context.context_id=?  ");
		}
		if(! includeHidden) {
			buf.append("and jointable.hidden='0' ");
		}
		buf.append("and jointable.sticky='0' and item.event_time > ? order by item.event_time asc, item.title limit ?;");
		
		return buf.toString();
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dashboard.db.DashboardDataQueries#getSelectStickyNewsItemsForUserSql(boolean)
	 */
	@Override
	public String getSelectStickyNewsItemsForUserSql(boolean inContext) {
		StringBuilder buf = new StringBuilder();
		
		buf.append("select person.id, item.id, item.news_time, item.title, item.access_url, ");
		buf.append("context.context_id, context.context_url, context.context_title ");
		buf.append("from dash_person person join dash_news_join jointable on jointable.person_id=person.id ");
		buf.append("join dash_context context on jointable.context_id=context.id ");
		buf.append("join dash_news_item item  on jointable.item_id=item.id ");
		buf.append("where person.user_id=? ");
		if(inContext) {
			buf.append("and context.context_id=?  ");
		}
		buf.append("and jointable.sticky='1' order by item.news_time desc, item.title;");
		
		return buf.toString();
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dashboard.db.DashboardDataQueries#getSelectStickyEventItemsForUserSql(boolean)
	 */
	@Override
	public String getSelectStickyEventItemsForUserSql(boolean inContext) {
		StringBuilder buf = new StringBuilder()
		;
		buf.append("select person.id,item.id, item.event_time, item.title, item.access_url, ");
		buf.append("context.context_id, context.context_url, context.context_title ");
		buf.append("from dash_person person join dash_event_join jointable on jointable.person_id=person.id ");
		buf.append("join dash_context context on jointable.context_id=context.id ");
		buf.append("join dash_event_item item  on jointable.item_id=item.id ");
		buf.append("where person.user_id=? ");
		if(inContext) {
			buf.append("and context.context_id=?  ");
		}
		buf.append("and jointable.sticky='1' order by item.event_time asc, item.title;");
		
		return buf.toString();
		
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dashboard.db.DashboardDataQueries#getUpdateNewsItemSetStickyForUserSql()
	 */
	@Override
	public String getUpdateNewsItemSetStickyForUserSql() {
		return "update dash_event_join set sticky='?' where person_id in (select id from dash_person where user_id=?) and item_id=?;";
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dashboard.db.DashboardDataQueries#getUpdateEventItemSetStickyForUserSql()
	 */
	@Override
	public String getUpdateEventItemSetStickyForUserSql() {
		return "update dash_news_join set sticky='?' where person_id in (select id from dash_person where user_id=?) and item_id=?;";
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dashboard.db.DashboardDataQueries#getUpdateNewsItemSetHiddenForUserSql()
	 */
	@Override
	public String getUpdateNewsItemSetHiddenForUserSql() {
		return "update dash_event_join set hidden='?' where person_id in (select id from dash_person where user_id=?) and item_id=?;";
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dashboard.db.DashboardDataQueries#getUpdateEventItemSetHiddenForUserSql()
	 */
	@Override
	public String getUpdateEventItemSetHiddenForUserSql() {
		return "update dash_news_join set hidden='?' where person_id in (select id from dash_person where user_id=?) and item_id=?;";
	}

	@Override
	public String getSelectIdForContextIdQuerySql() {
		
		return "select id from dash_context where context_id=? limit 1;";
	}

	@Override
	public String getSelectIdForNewsItemSql() {
		return "select id from dash_news_item where news_time=? and entity_id=? and context_id=? limit 1;";
	}

	
}
