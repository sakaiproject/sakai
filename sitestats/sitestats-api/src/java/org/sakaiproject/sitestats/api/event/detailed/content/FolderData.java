package org.sakaiproject.sitestats.api.event.detailed.content;

/**
 * Data about folders in the Content tool (Resources/Drop Box/attachments)
 * @author plukasew
 */
public class FolderData implements ContentData
{
	public static final HiddenFolderData HIDDEN = new HiddenFolderData();
	public static final AttachmentFolderData ATTACHMENT = new AttachmentFolderData();

	public final String name;
	public final String url;

	/**
	 * Constructor
	 * @param name the name of the folder
	 * @param url url to the folder
	 */
	public FolderData(String name, String url)
	{
		this.name = name;
		this.url = url;
	}

	// hidden folders and attachment folders are treated the same way, details are not revealed
	public static class HiddenFolderData implements ContentData { }
	public static class AttachmentFolderData implements ContentData { }
}
