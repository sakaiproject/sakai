package org.sakaiproject.importer.impl.importables;

public class TextDocument extends AbstractImportable {
	private String title;
	private String content;

	public String getTypeName() {
		return "sakai-text-document";
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
