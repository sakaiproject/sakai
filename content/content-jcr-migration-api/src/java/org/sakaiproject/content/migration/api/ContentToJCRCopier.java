package org.sakaiproject.content.migration.api;

import javax.jcr.Session;

/**
 * This interface is used to copy a file or folder from the Legacy CHS Implementation
 * to the JCR Implementation.
 * 
 * This interface is only responsible for moving single instances of files and folders
 * over.  I am still not sure of the best algorithm for migrating an entire installation 
 * of data, so that logic is left to other modules, but they can use this to make their 
 * life easy.
 * 
 * Just as an example, you may want to migrate all your data during a down time, or do it
 * in the background and continue to listen for content events to they can be added to
 * the queue for processing.
 * 
 * @author sgithens
 */
public interface ContentToJCRCopier
{

	/**
	 * Copies the ContentCollection folder to JCR for the content collection at
	 * absolute path 'abspath'. This does not copy the contents of the folder,
	 * just creates the empty nt:folder and copies all the Sakai metadata. Needs
	 * to handle updating a collection if it already exists. Saves the node but
	 * does not log out of the session.
	 * 
	 * @param session The JCR Session to use.
	 * @param abspath The absolute path of the collection to copy.
	 * @return
	 */
	public boolean copyCollectionFromCHStoJCR(Session session, String abspath);

	/**
	 * This will copy the resource from the Legacy ContentHosting implementation
	 * to the JCR Repository Needs to handle replacing a resource (say from a
	 * content.write event) Saves the node but does not log out of the session.
	 * 
	 * @param session
	 * @param abspath
	 * @return
	 */
	public boolean copyResourceFromCHStoJCR(Session session, String abspath);

	/**
	 * Attempts to delete an item. If it ends with a slash it is assumed to be a
	 * ContentCollections (generated from a content.remove event)
	 * 
	 * @param session
	 * @param abspath
	 * @return
	 */
	public boolean deleteItem(Session session, String abspath);
}
