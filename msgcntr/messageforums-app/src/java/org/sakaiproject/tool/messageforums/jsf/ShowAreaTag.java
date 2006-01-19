package org.sakaiproject.tool.messageforums.jsf;

import javax.faces.component.UIComponent;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;

/**
 * @author Chen Wen
 * @version $Id$
 * 
 */
public class ShowAreaTag extends UIComponentTag
{
  private String value;
  private String hideBorder;
  private String showInputTextArea;
  
  public void setvalue(String value)
  {
    this.value = value;
  }
  
  public String getvalue()
  {
    return value;
  }
    
  /**
   * @return Returns the hideBorder.
   */
  public String gethideBorder()
  {
    return hideBorder;
  }

  /**
   * @param hideBorder The hideBorder to set.
   */
  public void sethideBorder(String hideBorder)
  {
    this.hideBorder = hideBorder;
  }

  
  /**
   * @return Returns the showInputTextArea.
   */
  public String getShowInputTextArea()
  {
    return showInputTextArea;
  }

  /**
   * @param showInputTextArea The showInputTextArea to set.
   */
  public void setShowInputTextArea(String showInputTextArea)
  {
    this.showInputTextArea = showInputTextArea;
  }

  public String getComponentType()
  {
    return "ShowArea";
  }
  
  public String getRendererType()
  {
    return "ShowAreaRender";
  }
  protected void setProperties(UIComponent component)
  {
    super.setProperties(component);
    setString(component, "value", value);
    setString(component, "hideBorder", hideBorder);
    setString(component, "showInputTextArea", showInputTextArea);
  }
  
  public void release()
  {
    super.release();
    
    value = null;
  }
  
  public static void setString(UIComponent component, String attributeName,
      String attributeValue)
  {
    if (attributeValue == null) return;
    if (UIComponentTag.isValueReference(attributeValue)) setValueBinding(
        component, attributeName, attributeValue);
    else
      component.getAttributes().put(attributeName, attributeValue);
  }

  public static void setValueBinding(UIComponent component,
      String attributeName, String attributeValue)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Application app = context.getApplication();
    ValueBinding vb = app.createValueBinding(attributeValue);
    component.setValueBinding(attributeName, vb);
  }
}



