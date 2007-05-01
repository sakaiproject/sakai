package org.sakaiproject.tool.gradebook.jsf.gradebookItemTable;

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;

import org.apache.myfaces.component.html.ext.HtmlDataTable;
import org.apache.myfaces.taglib.html.HtmlDataTableTag;
import org.sakaiproject.jsf.util.TagUtil;

public class GradebookItemTableTag extends HtmlDataTableTag {
	private String sortColumn;
	private String sortAscending;
	private String expanded;
	private java.lang.String value;
	private java.lang.String _var;
	private String cellpadding;
	private String cellspacing;
	private String columnClasses;
	private String headerClasses;
	private String rowClasses;
	private String styleClass;
	private String rowIndexVar;
	
	
	public String getRendererType() { return "GradebookItemTableRenderer"; }
	public String getComponentType() { return HtmlDataTable.COMPONENT_TYPE; }
  
	protected void setProperties(UIComponent component)
	{

		super.setProperties(component);

		UIData data = null;
		data = (UIData)component;
				
		FacesContext context = getFacesContext();
		TagUtil.setString(component, "expanded", expanded);
		TagUtil.setString(component, "sortColumn", sortColumn);
		TagUtil.setString(component, "sortAscending", sortAscending);
		TagUtil.setString(component, "cellpadding", cellpadding);
		TagUtil.setString(component, "cellspacing", cellspacing);
		TagUtil.setString(component, "columnClasses", columnClasses);
		TagUtil.setString(component, "headerClasses", headerClasses);
		TagUtil.setString(component, "rowClasses", rowClasses);
		TagUtil.setString(component, "styleClass", styleClass);
		TagUtil.setString(component, "rowIndexVar", rowIndexVar);
		
		TagUtil.setString(component, "value", value);
		if (_var != null) {
			data.setVar(_var);
		}
		//Application application =  FacesContext.getCurrentInstance().getApplication();
		//data.setValueBinding("value", application.createValueBinding(value));
	}

	
	public String getSortColumn() {
	    return sortColumn;
	}
	
	public void setSortColumn(String sortColumn) {
	     this.sortColumn = sortColumn;
	}
	
	public String getSortAscending() {
	    return sortAscending;
	}
	
	public void setSortAscending(String sortAscending) {
	     this.sortAscending = sortAscending;
	}
	
	public void setCellpadding(String cellpadding) {
		this.cellpadding = cellpadding;
	}
	
	public String getCellpadding() {
		return cellpadding;
	}
	
	public void setCellspacing(String cellspacing) {
		this.cellspacing = cellspacing;
	}
	
	public String getCellspacing() {
		return cellspacing;
	}
	
	public void setColumnClasses(String columnClasses) {
		this.columnClasses = columnClasses;
	}
	
	public String getColumnClasses() {
		return columnClasses;
	}
	
	public void setRowClasses(String rowClasses) {
		this.rowClasses = rowClasses;
	}
	
	public String getRowClasses() {
		return rowClasses;
	}
	
	public void setHeaderClasses(String headerClasses) {
		this.headerClasses = headerClasses;
	}
	
	public String getHeaderClasses() {
		return headerClasses;
	}
	
	
	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}
	
	public String getStyleClass() {
		return styleClass;
	}
	
	public void setExpanded(String expanded) {
		this.expanded = expanded;
	}
	
	public String getExpanded() {
		return expanded;
	}
	
	public void setRowIndexVar(String rowIndexVar) {
		this.rowIndexVar = rowIndexVar;
	}
	
	public String getRowIndexVar() {
		return rowIndexVar;
	}
}
