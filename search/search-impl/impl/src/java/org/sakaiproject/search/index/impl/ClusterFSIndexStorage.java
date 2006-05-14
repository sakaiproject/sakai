/**
 * 
 */
package org.sakaiproject.search.index.impl;

import java.io.IOException;

/**
 * @author ieb
 *
 */
public class ClusterFSIndexStorage extends FSIndexStorage
{
	public void doPreIndexUpdate() throws IOException {
		super.doPreIndexUpdate();
		// prepare for clustered index update, 
		// execute cp -rl index index.DATE
	}
	
	
	public void doPostIndexUpdate() throws IOException {
		super.doPostIndexUpdate();
		// perform an RSYNC on the database to all other nodes.
		
	}

}
