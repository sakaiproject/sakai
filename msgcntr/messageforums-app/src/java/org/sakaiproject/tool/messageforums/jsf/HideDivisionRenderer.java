package org.sakaiproject.tool.messageforums.jsf;

import java.io.IOException;
import java.util.Iterator;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import java.util.List;

/**
 * @author <a href="mailto:cwen.iupui.edu">Chen Wen</a>
 * @version $Id$
 * 
 */
public class HideDivisionRenderer extends Renderer
{
  private static final String BARSTYLE = "";
  private static final String BARTAG = "h4";
  private static final String RESOURCE_PATH;
  private static final String BARIMG;
  private static final String CURSOR;

  static {
    RESOURCE_PATH = "/" + "sakai-jsf-resource";
    BARIMG = RESOURCE_PATH + "/" +"hideDivision/images/right_arrow.gif";
    CURSOR = "cursor:pointer";
    /*ConfigurationResource cr = new ConfigurationResource();
     RESOURCE_PATH = "/" + cr.get("resources");
     BARIMG = RESOURCE_PATH + "/" +cr.get("hideDivisionRight");
     CURSOR = cr.get("picker_style");*/
  }
  
  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof org.sakaiproject.tool.messageforums.jsf.HideDivisionComponent);
  }
  
  public void decode(FacesContext context, UIComponent component)
  {
  }
  
  public void encodeChildren(FacesContext context, UIComponent component) 
  	throws IOException 
  {
    if (!component.isRendered()) 
    {
      return;
    }
    
    Iterator children = component.getChildren().iterator();
    while (children.hasNext()) {
      UIComponent child = (UIComponent) children.next();
      if(!(child instanceof org.sakaiproject.tool.messageforums.jsf.BarLinkComponent))
      {
        child.encodeBegin(context);
        child.encodeChildren(context);
        child.encodeEnd(context);
      }
    }
  }
  
  public void encodeBegin(FacesContext context, UIComponent component)
  throws IOException {
    
    if (!component.isRendered()) {
      return;
    }
    
    ResponseWriter writer = context.getResponseWriter();
    String jsfId = (String) RendererUtil.getAttribute(context, component, "id");
    String id = jsfId;
    
    if (component.getId() != null &&
        !component.getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX))
    {
      id = component.getClientId(context);
    }
    
    String title = (String) RendererUtil.getAttribute(context, component, "title");
    
    writer.write("<" + BARTAG + " class=\"" + BARSTYLE + "\">");
    writer.write("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
    writer.write("<tr><td nowrap=\"nowrap\" align=\"left\">");
    writer.write("  <img id=\"" + id + "__img_hide_division_" + "\" alt=\"" +
        title + "\"" + " onclick=\"javascript:showHideDiv('" + id +
        "', '" +  RESOURCE_PATH + "');\"");
    writer.write("    src=\""   + BARIMG + "\" style=\"" + CURSOR + "\" />");
    writer.write("  " + title + "");
    writer.write("</td><td width=\"100%\">&nbsp;</td>");
    writer.write("<td nowrap=\"nowrap\" align=\"right\">");
    List childrenList = component.getChildren();
    for(int i=0; i<childrenList.size(); i++)
    {
      UIComponent thisComponent = (UIComponent)childrenList.get(i);
      if(thisComponent instanceof org.sakaiproject.tool.messageforums.jsf.BarLinkComponent)
      {
        thisComponent.encodeBegin(context);
        thisComponent.encodeChildren(context);
        thisComponent.encodeEnd(context);
      }
    }
    writer.write("</td></tr></table>");
    writer.write("</"+ BARTAG + ">");
    writer.write("<div \" style=\"display:none\" " +
        " id=\"" + id + "__hide_division_" + "\">");
    
  }
  
  
  public void encodeEnd(FacesContext context, UIComponent component) throws
  IOException {
    if (!component.isRendered()) {
      return;
    }
    
    ResponseWriter writer = context.getResponseWriter();
    
    String jsfId = (String) RendererUtil.getAttribute(context, component, "id");
    String id = jsfId;
    
    if (component.getId() != null &&
        !component.getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX))
    {
      id = component.getClientId(context);
    }
    
    writer.write("</div>");
    
    writer.write("<script type=\"text/javascript\">");
    writer.write("  showHideDiv('" + id +
        "', '" +  RESOURCE_PATH + "');");
    writer.write("</script>");
  }

}
