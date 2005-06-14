package org.sakaiproject.jsf.tag;

import javax.faces.component.UIComponent;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;

public class RichTextEditArea extends UIComponentTag
{
  private String value;
  private String columns;
  private String rows;
  private String justArea;

  public void setValue(String newValue) 
  { 
    value = newValue; 
  }

  public String getValue()
  {
    return value;
  }

  public void setColumns(String newC) 
  { 
    columns = newC;
  }

  public String getColumns()
  {
    return columns;
  }
    
  public void setRows(String newRows) 
  { 
    rows = newRows; 
  }

  public String getRows()
  {
    return rows;
  }

  public void setJustArea(String newJ) 
  { 
    justArea = newJ; 
  }

  public String getJustArea()
  {
    return justArea;
  }

  public String getComponentType()
	{
		return "SakaiRichTextEditArea";
	}

	public String getRendererType()
	{
		return "SakaiRichTextEditArea";
	}

	protected void setProperties(UIComponent component)
	{
		super.setProperties(component);
    setString(component, "value", value);
    setString(component, "columns", columns);
    setString(component, "rows", rows);
    setString(component, "justArea", justArea);
	}

	public void release() 
	{
    super.release();
    value = null;
    columns = null;
    rows = null;
    justArea = null;
  }

  public static void setString(UIComponent component, String attributeName,
          String attributeValue) 
  {
    if(attributeValue == null)
      return;
    if(UIComponentTag.isValueReference(attributeValue))
      setValueBinding(component, attributeName, attributeValue);
    else
      component.getAttributes().put(attributeName, attributeValue);
  }

  public static void setValueBinding(UIComponent component, String attributeName,
          String attributeValue) 
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Application app = context.getApplication();
    ValueBinding vb = app.createValueBinding(attributeValue);
    component.setValueBinding(attributeName, vb);
  }     
}