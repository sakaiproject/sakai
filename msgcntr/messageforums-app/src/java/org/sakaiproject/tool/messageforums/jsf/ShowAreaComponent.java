package org.sakaiproject.tool.messageforums.jsf;

import javax.faces.component.UIComponentBase;

/**
 * @author Chen Wen
 * @version $Id$
 * 
 */
public class ShowAreaComponent extends UIComponentBase
{
	public ShowAreaComponent()
	{
		super();
		this.setRendererType("ShowAreaRender");
	}

	public String getFamily()
	{
		return "ShowArea";
	}
}



