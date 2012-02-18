package org.sakaiproject.calendar.dao.hbm;

import org.sakaiproject.calendar.api.OpaqueUrl;

public class OpaqueUrlHbm implements OpaqueUrl {

	private String userUUID;
	private String calendarRef;
	private String opaqueUUID;

	public OpaqueUrlHbm() {
	}

	public OpaqueUrlHbm(String userUUID, String calendarRef, String opaqueUUID) {
		this.userUUID = userUUID;
		this.calendarRef = calendarRef;
		this.opaqueUUID = opaqueUUID;
	}

	public String getUserUUID() {
		return userUUID;
	}

	public void setUserUUID(String userUUID) {
		this.userUUID = userUUID;
	}

	public String getCalendarRef() {
		return calendarRef;
	}

	public void setCalendarRef(String calendarRef) {
		this.calendarRef = calendarRef;
	}

	public String getOpaqueUUID() {
		return opaqueUUID;
	}

	public void setOpaqueUUID(String opaqueUUID) {
		this.opaqueUUID = opaqueUUID;
	}

}
