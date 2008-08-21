package org.sakaiproject.email.api;

import java.io.File;

/**
 * Holds an attachment for an email message.  The attachment will be included with the message.
 * 
 * TODO: Make available for attachments to be stored in CHS.
 */
public interface Attachment
{
	/**
	 * Get the file associated to this attachment
	 * 
	 * @return
	 */
	public File getFile();

	/**
	 * Set the file associated to this attachment
	 * 
	 * @param file
	 */
	public void setFile(File file);
}