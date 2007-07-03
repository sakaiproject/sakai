package org.sakaiproject.importer.impl.importables;

public class DiscussionTopic extends AbstractImportable {
	
	private String title;
	private String description;

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
		return "sakai-discussion-topic";
	}

}
