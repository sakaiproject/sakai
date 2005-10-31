package org.sakaiproject.tool.messageforums.jsf;

/**
 * @author <a href="mailto:cwen.iupui.edu">Chen Wen</a>
 * @version $Id$
 * 
 */
public class BarLinkTag extends com.sun.faces.taglib.html_basic.CommandButtonTag
{
  public String getComponentType()
  {
    return "BarLink";
  }

  public String getRendererType()
  {
    return "org.sakaiproject.tool.messageforums.jsf.BarLinkRenderer";
  }
}



