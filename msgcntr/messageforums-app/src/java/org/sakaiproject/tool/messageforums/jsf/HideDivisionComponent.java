
package org.sakaiproject.tool.messageforums.jsf;

import javax.faces.component.UIComponentBase;

/**
 * @author <a href="mailto:cwen.iupui.edu">Chen Wen</a>
 * @version $Id$
 * 
 */
public class HideDivisionComponent extends UIComponentBase
{
  public HideDivisionComponent()
	{
		super();
		this.setRendererType("org.sakaiproject.HideDivision");
	}

	public String getFamily()
	{
		return "HideDivision";
	}
	
	public boolean getRendersChildren()
	{
	  return true;
	}	
}
