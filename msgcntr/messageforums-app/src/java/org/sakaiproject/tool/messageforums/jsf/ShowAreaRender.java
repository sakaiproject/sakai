package org.sakaiproject.tool.messageforums.jsf;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

/**
 * @author Chen Wen
 * @version $Id$
 * 
 */
public class ShowAreaRender extends Renderer
{
  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof ShowAreaComponent);
  }
  
  public void encodeBegin(FacesContext context, UIComponent component)
  throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    
    String value = (String) component.getAttributes().get("value");
    
    if((value!=null) && (!value.equals("")))
    {
      int pos;
//      writer.write("<div>");
      value = value.replaceAll("<strong>", "<b>");
      value = value.replaceAll("</strong>", "</b>");
      writer.write("<table border=\"1\" id=\"message_table\" cellpadding=\"7\" style=\"border-collapse: collapse;" +
            " \"table-layout:fixed\" width=\"90%\"><tr width=\"95%\"><td width=\"100%\" STYLE=\"word-wrap: break-word\">");
/*      int blocks = value.length() % 200 ;
      for (int i=0; i<blocks; i++)
      {
        writer.write(value.substring(i*200, ((i+1)*200-1)));
        writer.flush();
      }
      writer.write(value.substring(blocks*200, (value.length()-1)));
      writer.flush();
*/
      value = value.replaceAll("<a href=", "<a title=\"Open a new window\" target=\"_new\" href=");
      writer.write(value);
      
      writer.write("</td></tr></table>");
//      writer.write("</div>");
    }
  }
  
  public void encodeEnd(FacesContext context, UIComponent component)
  throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    
    String value = (String) component.getAttributes().get("value");

    if((value!=null) && (!value.equals("")))
    {
    }  
  }
}



