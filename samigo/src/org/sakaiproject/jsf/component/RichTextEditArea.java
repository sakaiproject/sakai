package org.sakaiproject.jsf.component;

import javax.faces.component.UIInput;

public class RichTextEditArea extends UIInput
{
	public RichTextEditArea()
	{
		super();
		this.setRendererType("SakaiRichTextEditArea");
	}

	public String getFamily()
	{
		return "SakaiRichTextEditArea";
	}
}
