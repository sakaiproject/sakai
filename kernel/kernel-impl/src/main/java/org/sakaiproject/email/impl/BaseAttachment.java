package org.sakaiproject.email.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.email.api.Attachment;

/**
 * Holds an attachment for an email message. The attachment will be included with the message.
 * 
 * TODO: Make available for attachments to be stored in CHS.
 */
public class BaseAttachment implements Attachment
{
	/**
	 * files to associated to this attachment
	 */
	private File file;

	public BaseAttachment(File file)
	{
		this.file = file;
	}

	public BaseAttachment(String filename)
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
				attachments.add(new BaseAttachment(f));
			}
		}
		return attachments;
	}
}
