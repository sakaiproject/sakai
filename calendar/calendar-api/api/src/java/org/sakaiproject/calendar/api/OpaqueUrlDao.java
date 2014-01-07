package org.sakaiproject.calendar.api;

/**
 * This allows creation and retrieval of OpaqueUrls
 * @see org.sakaiproject.calendar.api.OpaqueUrl
 */
public interface OpaqueUrlDao {

	OpaqueUrl newOpaqueUrl(String userUUID, String calendarRef);

	OpaqueUrl getOpaqueUrl(String userUUID, String calendarRef);

	OpaqueUrl getOpaqueUrl(String opaqueUUID);

	void deleteOpaqueUrl(String userUUID, String calendarRef);

}
