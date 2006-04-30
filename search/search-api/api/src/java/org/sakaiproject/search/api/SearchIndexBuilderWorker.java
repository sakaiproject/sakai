package org.sakaiproject.search.api;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.sakaiproject.search.model.SearchWriterLock;

public interface SearchIndexBuilderWorker
{

	/**
	 * update the node lock for the current Worker Object on the current thread.
	 * This MUST only be used by a worker thread
	 * @param connection if null a new connection will be used
	 * @throws SQLException
	 */
	void updateNodeLock(Connection connection) throws SQLException;

	/**
	 * Check running, and ping the thread if in a wait state
	 */
	void checkRunning();
	
	/**
	 * Should the thread be running
	 */
	boolean isRunning();
	/**
	 * get the current running Lock
	 * @return
	 */
	SearchWriterLock getCurrentLock();

	/**
	 * get a list of node status records
	 * @return
	 */
	List getNodeStatus();

	void destroy();

}