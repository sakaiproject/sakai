package org.sakaiproject.tool.section.jsf;


import org.apache.myfaces.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.renderkit.html.HTML;
import org.apache.myfaces.renderkit.html.ext.HtmlTableRenderer;
import org.apache.myfaces.util.ArrayUtils;
import org.apache.myfaces.util.StringUtils;

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.io.IOException;

/**
 * Author:Louis Majanja <louis@media.berkeley.edu>
 * Date: Jan 18, 2007
 * Time: 1:07:32 PM
 */
public class RowGroupDataTableRenderer extends HtmlTableRenderer {

    public static final String SECTION_STYLE_CLASS = "groupRow";
       public static final String CATEGORY_HEADER_STYLE_CLASS = "categoryHeader";
       public static final String FIRST_CATEGORY_HEADER_STYLE_CLASS = "firstCategoryHeader";

       protected String generateRowClasses(UIComponent component) {
           UIData uiData = (UIData) component;

           // The data in this table
           List rowData = (List)uiData.getValue();

           // A list of classes for the data in the table
           List<String> rowClassList = new ArrayList<String>();

           // Iterate over the data in the table, adding RowGroup header classes to the list when theRowGroup changes
           String currentCategory = null;
           for(Iterator iter = rowData.iterator(); iter.hasNext();) {
               RowGroupable rowGroupable = (RowGroupable)iter.next();
               if( ! rowGroupable.getRowGroupId().equals(currentCategory)) {
                   if(rowClassList.isEmpty()) {
                       rowClassList.add(FIRST_CATEGORY_HEADER_STYLE_CLASS);
                   } else {
                       rowClassList.add(CATEGORY_HEADER_STYLE_CLASS);
                   }
               }
               // Whether this is a new RowGroup or not, add the section row
               rowClassList.add(SECTION_STYLE_CLASS);

               // Update the current catgory
               currentCategory = rowGroupable.getRowGroupId();
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

       public void encodeInnerHtml(FacesContext facesContext, UIComponent component)throws IOException {

           UIData uiData = (UIData) component;
           ResponseWriter writer = facesContext.getResponseWriter();

           // Insert the proper row class for the RowGroup headers
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
           RowGroupDataTable rowGroupDataTable;
           try {
               rowGroupDataTable = (RowGroupDataTable)uiData;
           } catch (ClassCastException cce) {
               return;
           }

           // Get the current section
           RowGroupable rowGroupable;
           List list = (List)uiData.getValue();
           try {
               rowGroupable = (RowGroupable)list.get(rowNumber);
           } catch (IndexOutOfBoundsException ioobe) {
               return;
           }

           // Is this section different from the previous RowGroup?
           if( ! rowGroupable.getRowGroupId().equals(rowGroupDataTable.category)) {
               // Update the SectionTable's current RowGroup
               rowGroupDataTable.category = rowGroupable.getRowGroupId();

               // Render a table row for the RowGroup header
               beforeRow(facesContext, uiData);
               HtmlRendererUtils.writePrettyLineSeparator(facesContext);
               renderRowStart(facesContext, writer, uiData, rowStyleIterator);

               // Render a single colspanned cell displaying the current RowGroup
               writer.startElement(HTML.TD_ELEM, uiData);
               writer.writeAttribute(HTML.COLSPAN_ATTR, columns, null);
               writer.write(JsfUtil.getLocalizedMessage("section_table_category_header", new String[] {rowGroupable.getRowGroupTitle()}));
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
