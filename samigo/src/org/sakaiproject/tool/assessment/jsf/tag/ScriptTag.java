package org.sakaiproject.tool.assessment.jsf.tag;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;

/**
 * <p> </p>
 * <p>Description:<br />
 * This class is the tag handler that evaluates the <code>script</code>
 * custom tag.</p>
 * <p>Based on example code by Sun Microsystems. </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class ScriptTag  extends UIComponentTag
{

  private String path = null;
  private String type = null;

  public void setPath(String path)
  {
    this.path = path;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getComponentType()
  {
    return ("javax.faces.Output");
  }

  public String getRendererType()
  {
    return "Script";
  }

  protected void setProperties(UIComponent component)
  {

    super.setProperties(component);

    if (path != null)
    {
      component.getAttributes().put("path", path);
    }

    if (type != null)
    {
      component.getAttributes().put("type", type);
    }

  }

}
