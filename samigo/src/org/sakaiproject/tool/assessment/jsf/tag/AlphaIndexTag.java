package org.sakaiproject.tool.assessment.jsf.tag;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;

/**
 * <p> </p>
 * <p>Description:<br />
 * This class is the tag handler that evaluates the <code>alphaIndex</code>
 * custom tag.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id: AlphaIndexTag.java,v 1.1 2004/09/21 01:38:58 esmiley.stanford.edu Exp $
 */


public class AlphaIndexTag
    extends UIComponentTag
{

  private String initials = null;

  public String getInitials()
  {
    return initials;
  }

  public void setInitials(String initials)
  {
    this.initials = initials;
  }


  public String getComponentType()
  {
    return ("javax.faces.Output");
  }

  public String getRendererType()
  {
    return "AlphaIndex";
  }

  /**
   * set the component properties
   * @param component
   */
  protected void setProperties(UIComponent component)
  {

    super.setProperties(component);

    if (initials != null)
    {
      if (initials.startsWith("#"))
      {
        component.setValueBinding("initials",
          getFacesContext().getApplication().createValueBinding(getInitials()));
      }
      else
      {
        component.getAttributes().put("initials", initials);
      }
    }
  }

}
