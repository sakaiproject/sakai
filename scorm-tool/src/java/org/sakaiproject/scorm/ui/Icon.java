package org.sakaiproject.scorm.ui;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.image.Image;

public class Icon extends Image {
	private static final long serialVersionUID = 1L;
	private ResourceReference reference;
	
	public Icon(String id, ResourceReference reference) {
		super(id);
		this.reference = reference;
	}

	protected ResourceReference getImageResourceReference()
	{
		return reference;
	}
}
