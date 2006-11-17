/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.section.jsf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.renderkit.html.HTML;
import org.apache.myfaces.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.renderkit.html.ext.HtmlTableRenderer;
import org.apache.myfaces.util.ArrayUtils;
import org.apache.myfaces.util.StringUtils;
import org.sakaiproject.tool.section.decorator.CourseSectionDecorator;

public class SectionTableRenderer extends HtmlTableRenderer{
	private static final Log log = LogFactory.getLog(SectionTableRenderer.class);

	public static final String SECTION_STYLE_CLASS = "sectionRow";
	public static final String CATEGORY_HEADER_STYLE_CLASS = "categoryHeader";
	public static final String FIRST_CATEGORY_HEADER_STYLE_CLASS = "firstCategoryHeader";

	protected String generateRowClasses(UIComponent component) {
        UIData uiData = (UIData) component;

        // The data in this table
        List rowData = (List)uiData.getValue();

        // A list of classes for the data in the table
        List<String> rowClassList = new ArrayList<String>();

        // Iterate over the data in the table, adding category header classes to the list when the category changes
        String currentCategory = null;
		for(Iterator iter = rowData.iterator(); iter.hasNext();) {
			CourseSectionDecorator section = (CourseSectionDecorator)iter.next();
			if( ! section.getCategory().equals(currentCategory)) {
				if(rowClassList.isEmpty()) {
					rowClassList.add(FIRST_CATEGORY_HEADER_STYLE_CLASS);
				} else {
					rowClassList.add(CATEGORY_HEADER_STYLE_CLASS);
				}
			}
			// Whether this is a new category or not, add the section row
			rowClassList.add(SECTION_STYLE_CLASS);
			
			// Update the current catgory
			currentCategory = section.getCategory();
		}

		// Build the rowClass string
		StringBuffer sb = new StringBuffer();
		for(Iterator iter = rowClassList.iterator(); iter.hasNext();) {
			sb.append(iter.next());
			if(iter.hasNext()) {
				sb.append(",");
			}
		}
		String rowClass = sb.toString();
		return rowClass;
	}
	
     public void encodeInnerHtml(FacesContext facesContext, UIComponent component)throws IOException{

        UIData uiData = (UIData) component;
        ResponseWriter writer = facesContext.getResponseWriter();

        // Insert the proper row class for the category headers
		String rowClasses = generateRowClasses(component);

        
        String columnClasses = ((HtmlDataTable) component).getColumnClasses();

        Iterator rowStyleIterator = new StyleIterator(rowClasses);
        StyleIterator columnStyleIterator = new StyleIterator(columnClasses);

        int first = uiData.getFirst();
        int rows = uiData.getRows();
        int rowCount = uiData.getRowCount();
        if (rows <= 0)
        {
            rows = rowCount - first;
        }
        int last = first + rows;
        if (last > rowCount)
            last = rowCount;

        for (int i = first; i < last; i++)
        {            
            uiData.setRowIndex(i);
            if (!uiData.isRowAvailable())
            {
                log.error("Row is not available. Rowindex = " + i);
                return;
            }
            columnStyleIterator.reset();
            
            int columns = component.getChildCount();
            renderCategoryRow(i, columns, uiData, writer, rowStyleIterator);

            beforeRow(facesContext, uiData);
            HtmlRendererUtils.writePrettyLineSeparator(facesContext);
            renderRowStart(facesContext, writer, uiData, rowStyleIterator);
            
            List children = component.getChildren();
            for (int j = 0, size = component.getChildCount(); j < size; j++)
            {
                UIComponent child = (UIComponent) children.get(j);
                if(child.isRendered())
                {
                    encodeColumnChild(facesContext, writer, uiData, child, columnStyleIterator);
                }
            }
            renderRowEnd(facesContext, writer, uiData);

            afterRow(facesContext, uiData);
        }
    }
	
     private void renderCategoryRow(int rowNumber, int columns, UIData uiData, ResponseWriter writer, Iterator rowStyleIterator) throws IOException {
         FacesContext facesContext = FacesContext.getCurrentInstance();

         // Cast the uiData into our custom component
			SectionTable sectionTable;
			try {
				sectionTable = (SectionTable)uiData;
			} catch (ClassCastException cce) {
				log.warn("uiData is not a SectionTable.  It is a " + uiData.getClass());
				return;
			}
			
			// Get the current section
			CourseSectionDecorator section;
			List list = (List)uiData.getValue();
			try {
				section = (CourseSectionDecorator)list.get(rowNumber);
			} catch (IndexOutOfBoundsException ioobe) {
				log.error(ioobe);
				return;
			}

			// Is this section different from the previous category?
			if( ! section.getCategory().equals(sectionTable.category)) {
				// Update the SectionTable's current category
				sectionTable.category = section.getCategory();

				// Render a table row for the category header
				beforeRow(facesContext, uiData);
	            HtmlRendererUtils.writePrettyLineSeparator(facesContext);
	            renderRowStart(facesContext, writer, uiData, rowStyleIterator);

	            // Render a single colspanned cell displaying the current category
	            writer.startElement(HTML.TD_ELEM, uiData);
	            writer.writeAttribute(HTML.COLSPAN_ATTR, columns, null);
	            writer.write(JsfUtil.getLocalizedMessage("section_table_category_header", new String[] {section.getCategoryForDisplay()}));
	            writer.endElement(HTML.TD_ELEM);

	            renderRowEnd(facesContext, writer, uiData);
	            afterRow(facesContext, uiData);
			}
     }
     
     private static class StyleIterator implements Iterator
     {
         //~ Instance fields
         // ------------------------------------------------------------------------

         private String[] _style;
         private int _idx = 0;

         //~ Constructors
         // ---------------------------------------------------------------------------
         StyleIterator(String styles)
         {
             _style = (styles == null) ? ArrayUtils.EMPTY_STRING_ARRAY : StringUtils.trim(StringUtils
                     .splitShortString(styles, ','));
         }
         
         /**
          * @see java.util.Iterator#hasNext()
          */
         public boolean hasNext()
         {
             return _style.length > 0;
         }
         
         /**
          * @see java.util.Iterator#next()
          */
         public Object next()
         {
             if(hasNext())
             {
                 return _style[_idx++ % _style.length];
             }
             throw new NoSuchElementException("no style defined");
         }

         /**
          * @see java.util.Iterator#remove()
          */
         public void remove()
         {
             throw new UnsupportedOperationException("remove is not supported");
         }
         
         public void reset()
         {
             _idx = 0;
         }
     }

}
