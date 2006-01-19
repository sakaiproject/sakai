package org.sakaiproject.tool.messageforums.jsf;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

/**
 * @author Chen Wen
 * @version $Id$
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
    String hideBorder = (String) component.getAttributes().get("hideBorder");
    String showInputTextArea = (String) component.getAttributes().get(
        "showInputTextArea");
    if ((value != null) && (!value.equals("")))
    {
      int pos;
      // writer.write("<div>");
      value = value.replaceAll("<strong>", "<b>");
      value = value.replaceAll("</strong>", "</b>");
      // writer.write("<table STYLE=\"table-layout:fixed\" width=300><tr width=\"100%\"><td
      // width=\"100%\" STYLE=\"word-wrap: break-all; white-space: -moz-pre-wrap;
      // text-overflow:ellipsis; overflow: auto;\">");
      value = value.replaceAll("<a title=", "<a target=\"_new\" title=");
      value = value.replaceAll("<a href=",
          "<a title=\"Open a new window\" target=\"_new\" href=");
      if (hideBorder != null && hideBorder.equals("true"))
      {
        writer
            .write("<table border=\"0\" id=\"message_table\" cellpadding=\"0\"  width=\"90%\"><tr width=\"95%\"><td width=\"100%\" STYLE=\"word-wrap: break-word\">");
       
        writer.write(value);
        writer.write("</td></tr></table>");
      }
      else
        if (showInputTextArea != null && showInputTextArea.equals("true"))
        {
          writer.write("<textarea id=\"msgForum:forums:1:forum_extended_description\" name=\"msgForum:forums:1:forum_extended_description\" cols=\"100\" rows=\"5\" disabled=\"disabled\">");
          writer.write(value);           
          writer.write("</textarea>");
        }
        else
        {
          writer
              .write("<table border=\"1\" id=\"message_table\" cellpadding=\"7\" style=\"border-collapse: collapse; table-layout:fixed\" width=\"90%\"><tr width=\"95%\"><td width=\"100%\" STYLE=\"word-wrap: break-word\">");
              writer.write(value);
          writer.write("</td></tr></table>");
        }

    }
  }

  public void encodeEnd(FacesContext context, UIComponent component)
      throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();

    String value = (String) component.getAttributes().get("value");

    if ((value != null) && (!value.equals("")))
    {
    }
  }
}
