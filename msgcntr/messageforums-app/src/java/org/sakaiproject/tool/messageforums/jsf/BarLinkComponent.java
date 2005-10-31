package org.sakaiproject.tool.messageforums.jsf; 

import javax.faces.component.html.HtmlCommandLink;

/**
 * @author <a href="mailto:cwen.iupui.edu">Chen Wen</a>
 * @version $Id$
 * 
 */
public class BarLinkComponent extends HtmlCommandLink
{
  public BarLinkComponent()
  {
    this.setRendererType("org.sakaiproject.tool.messageforums.jsf.BarLinkRenderer");
  }
}



