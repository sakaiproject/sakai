package org.sakaiproject.calendar.api;

public interface OpaqueUrlDao {

	OpaqueUrl newOpaqueUrl(String userUUID, String calendarRef);
	
	OpaqueUrl getOpaqueUrl(String userUUID, String calendarRef);
	
	OpaqueUrl getOpaqueUrl(String opaqueUUID);
	
	void deleteOpaqueUrl(String userUUID, String calendarRef);
	
	// Note: A 'proper' DAO will probably want the methods below too...
	// MockOpaqueUrl getOpaqueUrl(String opaqueUUID);
	// void deleteOpaqueUrl(String opaqueUUID);
	
}
