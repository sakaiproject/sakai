package org.sakaiproject.sitestats.api.event.detailed.content;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/**
 * Data about files in the Content tool (Resources/Drop Box/attachments)
 * @author plukasew
 */
public class FileData implements ContentData
{
	public static final HiddenFileData HIDDEN = new HiddenFileData();
	public static final AttachmentFileData ATTACHMENT = new AttachmentFileData();

	public final String name;
	public final Optional<String> displayName;
	public final FolderData folder;

	/**
	 * Constructor
	 * @param name the name of the file
	 * @param displayName the display name for the file (different from the file name)
	 * @param folder the folder containing the file
	 */
	public FileData(String name, String displayName, FolderData folder)
	{
		this.name = name;
		this.displayName = Optional.ofNullable(StringUtils.trimToNull(displayName));
		this.folder = folder;
	}

	/**
	 * Constructor
	 * @param name the name of the file
	 * @param folder the folder containing the file
	 */
	public FileData(String name, FolderData folder)
	{
		this(name, "", folder);
	}

	// hidden files and attachments are treated the same way, details are not revealed
	public static class HiddenFileData implements ContentData { }
	public static class AttachmentFileData implements ContentData { }
}
