package org.sakaiproject.content.migration;

public class MigrationSqlQueries
{
	public static final String count_total_content_items_in_queue = "SELECT COUNT(*) FROM"
			+ " MIGRATE_CHS_CONTENT_TO_JCR";

	public static final String count_finished_content_items_in_queue = "SELECT COUNT(*) FROM"
			+ " MIGRATE_CHS_CONTENT_TO_JCR" + " WHERE STATUS = 1";

	public static final String finish_content_item = "UPDATE MIGRATE_CHS_CONTENT_TO_JCR"
			+ " SET STATUS = 1" + " WHERE CONTENT_ID = ?";

	public static final String select_unfinished_items = "SELECT *"
			+ " FROM MIGRATE_CHS_CONTENT_TO_JCR M" + " WHERE STATUS = 0" + " LIMIT 0, ?";

	/*
	 * Two queries for initial creation of the migration table.
	 */
	// First copy over the folder/collection table.
	public static final String add_original_collections_to_migrate = "INSERT INTO MIGRATE_CHS_CONTENT_TO_JCR"
			+ " (CONTENT_ID)"
			+ " SELECT CONTENT_COLLECTION.COLLECTION_ID"
			+ " FROM CONTENT_COLLECTION";

	// Then copy over the file/resource table.
	public static final String add_original_resources_to_migrate = "INSERT INTO MIGRATE_CHS_CONTENT_TO_JCR"
			+ " (CONTENT_ID)"
			+ " SELECT CONTENT_RESOURCE.RESOURCE_ID"
			+ " FROM CONTENT_RESOURCE";

}
