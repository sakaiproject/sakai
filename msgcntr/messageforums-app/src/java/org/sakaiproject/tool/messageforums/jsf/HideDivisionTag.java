package org.sakaiproject.tool.messageforums.jsf;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;

public class HideDivisionTag extends UIComponentTag
{
  private String title = null;
  private String id = null;
  private String first;
  private String rows;
  private String value;
  private String var;
  private String separator;
  private String onclick;
  private String ondblclick;
  private String onkeydown;
  private String onkeypress;
  private String onkeyup;
  private String onmousedown;
  private String onmousemove;
  private String onmouseout;
  private String onmouseover;
  private String onmouseup;
  private String hideByDefault;

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getTitle()
  {
    return title;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getId()
  {
    return id;
  }

  public String getComponentType()
  {
    return ("HideDivision");
  }

  public String getRendererType()
  {
    return "org.sakaiproject.HideDivision";
  }

  /**
   * @param first
   */
  public void setFirst(String first)
  {
    this.first = first;
  }

  /**
   * The rows property refers to rows in the mini-model, NOT table rows.
   * 
   * @param rows
   */
  public void setRows(String rows)
  {
    this.rows = rows;
  }

  /**
   * @param value
   */
  public void setValue(String value)
  {
    this.value = value;
  }

  /**
   * @param var
   */
  public void setVar(String var)
  {
    this.var = var;
  }

  /**
   * @param separator
   */
  public void setSeparator(String separator)
  {
    this.separator = separator;
  }

  public void setOnclick(String onclick)
  {
    this.onclick = onclick;
  }

  public void setOndblclick(String ondblclick)
  {
    this.ondblclick = ondblclick;
  }

  public void setOnkeydown(String onkeydown)
  {
    this.onkeydown = onkeydown;
  }

  public void setOnkeypress(String onkeypress)
  {
    this.onkeypress = onkeypress;
  }

  public void setOnkeyup(String onkeyup)
  {
    this.onkeyup = onkeyup;
  }

  public void setOnmousedown(String onmousedown)
  {
    this.onmousedown = onmousedown;
  }

  public void setOnmousemove(String onmousemove)
  {
    this.onmousemove = onmousemove;
  }

  public void setOnmouseout(String onmouseout)
  {
    this.onmouseout = onmouseout;
  }

  public void setOnmouseover(String onmouseover)
  {
    this.onmouseover = onmouseover;
  }

  public void setOnmouseup(String onmouseup)
  {
    this.onmouseup = onmouseup;
  }

  public String getHideByDefault()
  {
    return hideByDefault;
  }

  public void setHideByDefault(String hideByDefault)
  {
    this.hideByDefault = hideByDefault;
  }

  protected void setProperties(UIComponent component)
  {
    super.setProperties(component);
    TagUtil.setString(component, "title", title);
    TagUtil.setString(component, "ondblclick", ondblclick);
    TagUtil.setString(component, "onclick", onclick);
    TagUtil.setString(component, "onkeydown", onkeydown);
    TagUtil.setString(component, "onkeypress", onkeypress);
    TagUtil.setString(component, "onkeyup", onkeyup);
    TagUtil.setString(component, "onmousedown", onmousedown);
    TagUtil.setString(component, "onmousemove", onmousemove);
    TagUtil.setString(component, "onmouseout", onmouseout);
    TagUtil.setString(component, "onmouseover", onmouseover);
    TagUtil.setString(component, "onmouseup", onmouseup);
    TagUtil.setString(component, "hideByDefault", hideByDefault);
  }

}
