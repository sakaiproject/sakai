/**********************************************************************************
*
* Header:
*
***********************************************************************************
*
 * Copyright (c) 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/


package org.sakaiproject.tool.gradebook.jsf.spreadsheetUI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.component.UIColumn;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.faces.render.Renderer;
import org.apache.myfaces.custom.sortheader.HtmlCommandSortHeader;

/**
 * <p>This does not render children, but can deal with children by surrounding them in a comment.</p>
 *
 */
public class SpreadsheetUIRenderer extends Renderer
{
  /**
   * This component renders its children
   * @return true
   */
  public boolean getRendersChildren()
  {
	  return true;
  }
  
  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof org.apache.myfaces.component.html.ext.HtmlDataTable);
  }

  public void encodeBegin(FacesContext context, UIComponent component) throws IOException
  {
	if(!component.isRendered()){
		//tool_bar tag is not to be rendered, return now
		return;
	}
	UIData data = (UIData) component;
			
	//Begin Rendering
	ResponseWriter writer = context.getResponseWriter();

	writer.startElement("div", data);
	writer.writeAttribute("id", "mainwrap", null);
	
	writer.startElement("div", data);
	writer.writeAttribute("id", "headers", null);
	

	String colLock = (String) data.getAttributes().get("colLock");
	if(colLock == null){
		colLock = "1";
	}
	int columnLock = Integer.parseInt(colLock);
	
	//Render Header Facets
	UIComponent header = data.getFacet("header");
	int headerFacets = getFacetCount(data, "header");
	if(headerFacets > 0){

		writer.startElement("ul", data);
		writer.writeAttribute("id", "q1", null);
		
		Iterator columns = getColumns(data);
		int count = 0;
		while (columns.hasNext()) {
			UIColumn column = (UIColumn) columns.next();
			if(count == columnLock){
				writer.endElement("ul");
				
				writer.startElement("div", data);
				writer.writeAttribute("id", "q2", null);
				
				writer.startElement("div", data);
				writer.startElement("ul", data);
			}
			writer.startElement("li", data);
			UIComponent facet = column.getFacet("header");
			if (facet != null){
				facet.encodeBegin(context);
				facet.encodeChildren(context);
				facet.encodeEnd(context);
				//writer.writeText(facet.toString(), null);
			}
			writer.endElement("li");
			count++;
		}
		if(count > Integer.parseInt(colLock)){
			writer.endElement("ul");
			writer.endElement("div");
			writer.endElement("div");
			writer.writeText("\n", null);
		} else {
			writer.endElement("ul");
			writer.writeText("\n", null);
		}
	}
	writer.endElement("div"); //end <div id="headers">
	writer.writeText("\n", null);
  }
	
    /**
     * We put all our processing in the encodeChildren method
     * @param context
     * @param component
     * @throws IOException
     */
    public void encodeChildren(FacesContext context, UIComponent component)
      throws IOException
    {
        if ((context == null) || (component == null)) {
            return;
        }

        if (!component.isRendered())
	    {
		  return;
	    }

		UIData data = (UIData) component;
			
		ValueBinding msgsBinding = component.getValueBinding("value");
		List msgBeanList = (List)msgsBinding.getValue(context);
	
		ResponseWriter writer = context.getResponseWriter();
		Iterator kids = null;
		Iterator grandkids = null;
		
		String colLock = (String) data.getAttributes().get("colLock");
		if(colLock == null){
			colLock = "1";
		}
		int columnLock = Integer.parseInt(colLock);
		
		//get total column count
		int totalCount = 0;
		Iterator columns = getColumns(data);
		while(columns.hasNext())
		{
			columns.next();
			totalCount++;
		}
	
		// Iterate over the rows of data that are provided
		int processed = 0;
		int rowIndex = data.getFirst() - 1;
		int rows = data.getRows();
		int rowStyle = 0;
		
		writer.startElement("div", data);
		writer.writeAttribute("id", "contents", null);
		
		writer.startElement("div", data);
		writer.writeAttribute("id", "q3", null);
		
		writer.startElement("div", data);
		if(totalCount <= columnLock){
			writer.writeAttribute("style", "overflow:auto;",null);
		}
		
		writer.startElement("table", data);
		writer.writeAttribute("cellpadding", "0", null);
		writer.writeAttribute("cellspacing", "0", null);
		
		writer.startElement("tbody", data);
		
		while (true) {
			// Select the current row
			data.setRowIndex(++rowIndex);
			if (!data.isRowAvailable()) {
				break; // Scrolled past the last row
			}
			
			writer.startElement("tr", data);
			// Iterate through the childrens
			kids = getColumns(data);
			int count = 0;
			//get only the columns from the start of the data to where the colLock is
			while (kids.hasNext() && count < columnLock){
				//get column
				UIColumn column = (UIColumn) kids.next();
				
				//begin rendering cell
				writer.startElement("td", data);
				
				//render the contents of this cell by iterating over
				//the kids of our kids
				Iterator oKids = getChildren(column);
				for(; oKids.hasNext();){
			          UIComponent oKid = (UIComponent)oKids.next();  
			          encodeRecursive(context,oKid);
			        }
								
				//Render the ending of this cell
				writer.endElement("td");
				writer.writeText("\n", null);
				
				count++;
			}
			//render the ending of this row
			writer.endElement("tr");
			writer.writeText("\n", null);
		}

		writer.endElement("tbody");
		writer.writeText("\n", null);
		
		writer.endElement("table");
		writer.writeText("\n", null);
		
		writer.endElement("div");
		writer.writeText("\n", null);
		
		writer.endElement("div"); //end <div id="q3">
		writer.writeText("\n", null);
		
		//render the body of the data if we have any left over from the lock
		if(totalCount > columnLock)
		{
				
			writer.startElement("div", data);
			writer.writeAttribute("id", "q4", null);
			
			writer.startElement("div", data);
			
			writer.startElement("table",data);
			writer.writeAttribute("cellpadding", "0", null);
			writer.writeAttribute("cellspacing", "0", null);
			
			writer.startElement("tbody", data);
			
			rowIndex = data.getFirst() - 1;
			rows = data.getRows();
			rowStyle = 0;
			while (true) {
				// Select the current row
				data.setRowIndex(++rowIndex);
				if (!data.isRowAvailable()) {
					break; // Scrolled past the last row
				}
				
				writer.startElement("tr", data);
				// Iterate through the childrens
				kids = getColumns(data);
				int count = 0;
				//get only the columns from the start of the data to where the colLock is
				while (kids.hasNext()){
					//get column
					UIColumn column = (UIColumn) kids.next();
					if(count >= columnLock){
					
     					//begin rendering cell
						writer.startElement("td", data);
	
						UIComponent facet = column.getFacet("header");
						if (foundInactiveInChildren(facet)){
							writer.writeAttribute("class", "inactive-column", null);
						}
						//render the contents of this cell by iterating over
						//the kids of our kids
						Iterator oKids = getChildren(column);
						for(; oKids.hasNext();){
					          UIComponent oKid = (UIComponent)oKids.next();  
					          encodeRecursive(context,oKid);
					        }
						//writer.writeText(column.toString(), null);
						
						//Render the ending of this cell
						writer.endElement("td");
						writer.writeText("\n", null);
					}
					count++;
				}
				//render the ending of this row
				writer.endElement("tr");
				writer.writeText("\n", null);
			}
			
			writer.endElement("tbody");
			writer.writeText("\n", null);
			
			writer.endElement("table");
			writer.writeText("\n", null);
			
			writer.endElement("div");
			writer.endElement("div");  //end <div id="q4">
		
		} //end IF totalCount > columnLock
		
		writer.endElement("div");  //end <div id="content">
		
		// Clean up after ourselves
		data.setRowIndex(-1);
		
		
	}
	
	public void encodeEnd(FacesContext context, UIComponent component)
	throws IOException {
		
		if ((context == null) || (component == null)) {
			return;
		}
		if (!component.isRendered()) {
			return;
		}
		UIData data = (UIData) component;
		data.setRowIndex(-1);
		ResponseWriter writer = context.getResponseWriter();
		
		// Render the ending of this table
		writer.endElement("div");
		writer.writeText("\n", null);
		
	}
  
	/**
	 * <p>Return an Iterator over the <code>UIColumn</code> children
	 * of the specified <code>UIData</code> that have a
	 * <code>rendered</code> property of <code>true</code>.</p>
	 *
	 * @param data <code>UIData</code> for which to extract children
	 */
	private Iterator getColumns(UIData data) {
		
		List results = new ArrayList();
		Iterator kids = data.getChildren().iterator();
		while (kids.hasNext()) {
			UIComponent kid = (UIComponent) kids.next();
			if ((kid instanceof UIColumn) && kid.isRendered()) {
				results.add(kid);
			}
		}
		return (results.iterator());
		
	}
	
	/**
	 * <p>Return the number of child <code>UIColumn</code> components
	 * nested in the specified <code>UIData</code> that have a facet with
	 * the specified name.</p>
	 *
	 * @param data <code>UIData</code> component being analyzed
	 * @param name Name of the facet being analyzed
	 */
	private int getFacetCount(UIData data, String name) {
		
		int n = 0;
		Iterator kids = getColumns(data);
		while (kids.hasNext()) {
			UIComponent kid = (UIComponent) kids.next();
			if (kid.getFacet(name) != null) {
				n++;
			}
		}
		return (n);
		
	}
	
	protected Iterator getChildren(UIComponent oComponent){
        List oResults = new ArrayList();
        for(Iterator oKids = oComponent.getChildren().iterator(); oKids.hasNext();){
          UIComponent oKid = (UIComponent)oKids.next();
          if(oKid.isRendered())
             oResults.add(oKid);
        }
        return oResults.iterator();
    }
	
	private void encodeRecursive(FacesContext oContext,UIComponent oComponent) throws IOException{
	       if (!oComponent.isRendered()){
	         return;  
	       }
	       oComponent.encodeBegin(oContext);
	       if (oComponent.getRendersChildren()){
	         oComponent.encodeChildren(oContext); 
	       }
	       else{
	         Iterator oChildren = getChildren(oComponent);
	         for(;oChildren.hasNext();){
	           UIComponent oChildComponent = (UIComponent)oChildren.next();
	           encodeRecursive(oContext,oChildComponent);
	         }
	       }
	       oComponent.encodeEnd(oContext);
	    }
	
	private Boolean foundInactiveInChildren(UIComponent component) {
		for (Iterator iter = component.getChildren().iterator(); iter.hasNext();){
			UIComponent child = (UIComponent) iter.next();
			if (child instanceof HtmlCommandSortHeader){
				HtmlCommandSortHeader hcsh = (HtmlCommandSortHeader)child;
				if(hcsh.getStyleClass().contains("inactive-column")){
					return true;
				}
			}
			return foundInactiveInChildren(child);
		}
		return false;
	}
}



