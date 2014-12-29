package org.sakaiproject.content.providers;

public class BaseEventDelayHandlerSqlMySql extends BaseEventDelayHandlerSqlDefault {

	public String getDelayWriteSql() {
		return "insert into SAKAI_EVENT_DELAY (EVENT, EVENT_CODE, PRIORITY, REF, USER_ID) VALUES (?, ?, ?, ?, ?)";
	}	

}
