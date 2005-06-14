package org.sakaiproject.tool.assessment.jsf.tag;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;

/**
 * <p> </p>
 * <p>Description:<br />
 * This class is the tag handler that evaluates the <code>stylesheet</code>
 * custom tag.</p>
 * <p>Based on example code by Sun Microsystems. </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id: StylesheetTag.java,v 1.1 2004/08/13 01:56:45 esmiley.stanford.edu Exp $
 */

public class StylesheetTag extends UIComponentTag
{

  private String path = null;

  public void setPath(String path)
  {
    this.path = path;
  }

  public String getComponentType()
  {
    return ("javax.faces.Output");
  }

  public String getRendererType()
  {
    return "Stylesheet";
  }

  protected void setProperties(UIComponent component)
  {

    super.setProperties(component);

    if (path != null)
    {
      component.getAttributes().put("path", path);
    }

  }

}
