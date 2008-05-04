package org.sakaiproject.entity.api;

import java.util.List;
import java.util.Stack;

import org.w3c.dom.Document;

public interface MergeEntityService {
	/**
	 * Archive the resources for the given site.
	 * 
	 * @param siteId
	 *        the id of the site.
	 * @param doc
	 *        The document to contain the xml.
	 * @param stack
	 *        The stack of elements, the top of which will be the containing element of the "service.name" element.
	 * @param archivePath
	 *        The path to the folder where we are writing auxilary files.
	 * @param attachments
	 *        A list of attachments - add to this if any attachments need to be included in the archive.
	 * @return A log of status messages from the archive.
	 */
	String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments);

}
