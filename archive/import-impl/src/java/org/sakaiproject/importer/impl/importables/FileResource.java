package org.sakaiproject.importer.impl.importables;

public class FileResource extends AbstractImportable {
	private String destinationResourcePath;
	private String contentType;
	private byte[] fileBytes;
	private String title;
	private String description;
	private String fileName;

	public String getTypeName() {
		return "sakai-file-resource";
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDestinationResourcePath() {
		return destinationResourcePath;
	}

	public void setDestinationResourcePath(String destinationResourcePath) {
		this.destinationResourcePath = destinationResourcePath;
	}

	public byte[] getFileBytes() {
		return fileBytes;
	}

	public void setFileBytes(byte[] fileBytes) {
		this.fileBytes = fileBytes;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
