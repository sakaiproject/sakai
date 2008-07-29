package org.sakaiproject.email.api;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds an attachment for an email message.  The attachment will be included with the message.
 * 
 * TODO: Make available for attachments to be stored in CHS.
 * 
 * @author <a href="mailto:carl.hall@et.gatech.edu">Carl Hall</a>
 */
public class Attachment
{
	/**
	 * files to associated to this attachment
	 */
	private File file;

	public Attachment(File file)
	{
		this.file = file;
	}

	public Attachment(String filename)
	{
		this.file = new File(filename);
	}

	/**
	 * Get the file associated to this attachment
	 * 
	 * @return
	 */
	public File getFile()
	{
		return file;
	}

	/**
	 * Set the file associated to this attachment
	 * 
	 * @param file
	 */
	public void setFile(File file)
	{
		this.file = file;
	}

	public static List<Attachment> toAttachment(List<File> files)
	{
		ArrayList<Attachment> attachments = null;
		if (files != null)
		{
			attachments = new ArrayList<Attachment>();
			for (File f : files)
			{
				attachments.add(new Attachment(f));
			}
		}
		return attachments;
	}
}