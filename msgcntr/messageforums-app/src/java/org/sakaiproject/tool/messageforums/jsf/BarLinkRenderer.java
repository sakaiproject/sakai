package org.sakaiproject.tool.messageforums.jsf;

import javax.faces.component.UIComponent;
import com.sun.faces.renderkit.html_basic.CommandLinkRenderer;

/**
 * @author <a href="mailto:cwen.iupui.edu">Chen Wen</a>
 * @version $Id$
 * 
 */
public class BarLinkRenderer extends CommandLinkRenderer
{
  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof org.sakaiproject.tool.messageforums.jsf.BarLinkComponent);
  }
}


