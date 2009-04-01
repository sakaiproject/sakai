package org.sakaiproject.content.providers;

public class BaseEventDelayHandlerSqlOracle extends BaseEventDelayHandlerSqlDefault {

	
	public String getDelayWriteSql() {
		return "insert into SAKAI_EVENT_DELAY (EVENT_DELAY_ID, EVENT, EVENT_CODE, PRIORITY, REF, USER_ID) VALUES " +
		" (SAKAI_EVENT_DELAY_SEQ.NEXTVAL, ?, ?, ?, ?, ?)";
	}	

}
