package org.sakaiproject.content.migration;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.db.api.SqlReader;

public class MigrationTableSqlReader implements SqlReader
{
	private static Log log = LogFactory.getLog(MigrationTableSqlReader.class);

	/*
	 * Going to return a List<ThingToMigrate>
	 * 
	 * @see org.sakaiproject.db.api.SqlReader#readSqlResultRecord(java.sql.ResultSet)
	 */
	public Object readSqlResultRecord(ResultSet result)
	{
		// List<ThingToMigrate> things = new ArrayList<ThingToMigrate>();

		try
		{
			// while(result.next()) {
			ThingToMigrate thing = new ThingToMigrate();
			thing.contentId = result.getString("CONTENT_ID");
			thing.status = result.getInt("STATUS");
			// TODO TODO TODO The time added
			thing.eventType = result.getString("EVENT_TYPE");
			return thing;
			// things.add(thing);
			// }
		}
		catch (SQLException e)
		{
			log.error("Error getting the next set of things to migrate from CHS to JCR",
					e);
		}
		return null;
		// return things;
	}

}
