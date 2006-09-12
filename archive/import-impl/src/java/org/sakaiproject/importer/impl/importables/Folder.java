package org.sakaiproject.importer.impl.importables;

public class Folder extends AbstractImportable {
	private String title;
	private String description;
	private String path;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTypeName() {
		return "sakai-folder";
	}

}
