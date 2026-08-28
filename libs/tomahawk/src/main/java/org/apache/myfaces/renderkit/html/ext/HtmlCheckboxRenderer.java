/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.renderkit.html.ext;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UISelectBoolean;
import jakarta.faces.component.UISelectMany;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.model.SelectItem;
import jakarta.faces.model.SelectItemGroup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.component.UserRoleUtils;
import org.apache.myfaces.component.html.ext.HtmlSelectManyCheckbox;
import org.apache.myfaces.custom.checkbox.HtmlCheckbox;
import org.apache.myfaces.shared_tomahawk.component.DisplayValueOnlyCapable;
import org.apache.myfaces.shared_tomahawk.renderkit.JSFAttr;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlCheckboxRendererBase;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;


/**
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC"
 *   family = "org.apache.myfaces.Checkbox"
 *   type = "org.apache.myfaces.Checkbox"
 * 
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC"
 *   family = "jakarta.faces.SelectBoolean"
 *   type = "org.apache.myfaces.Checkbox"
 *   
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC"
 *   family = "jakarta.faces.SelectMany"
 *   type = "org.apache.myfaces.Checkbox"
 * 
 * @author Manfred Geiler (latest modification by $Author: lu4242 $)
 * @version $Revision: 1393888 $ $Date: 2012-10-03 21:51:42 -0500 (Wed, 03 Oct 2012) $
 */
public class HtmlCheckboxRenderer
        extends HtmlCheckboxRendererBase
{
    private static final Log log = LogFactory.getLog(HtmlCheckboxRenderer.class);

    private static final String PAGE_DIRECTION = "pageDirection";

    private static final String LINE_DIRECTION = "lineDirection";

    private static final String LAYOUT_SPREAD = "spread";

    public void encodeEnd(FacesContext context, UIComponent component) throws IOException
    {
        if (context == null) throw new NullPointerException("context");
        if (component == null) throw new NullPointerException("component");

        if (component instanceof HtmlCheckbox)
        {
            renderSingleCheckbox(context, (HtmlCheckbox)component);
        }
        else if (component instanceof DisplayValueOnlyCapable && HtmlRendererUtils.isDisplayValueOnly(component))
        {
            HtmlRendererUtils.renderDisplayValueOnlyForSelects(context, component, true);
        }
        else if (component instanceof UISelectMany)
        {
            String layout = getLayout((UISelectMany)component);
            if (layout != null && layout.equals(LAYOUT_SPREAD))
            {
                return; //checkbox inputs are rendered by spread checkbox components
            }
            else
            {
                super.encodeEnd(context, component);
            }
        }
        else if(component instanceof UISelectBoolean)
        {
            super.encodeEnd(context,component);
        }
        else
        {
            throw new IllegalArgumentException("Unsupported component class " + component.getClass().getName());
        }
    }

    @Override
    public void renderCheckboxList(FacesContext facesContext,
            UISelectMany selectMany) throws IOException
    {
        final String layout = getLayout(selectMany);
        if (layout != null)
        {
            Converter converter = getConverter(facesContext, selectMany);
            if (layout.equals(PAGE_DIRECTION))
            {
                renderCheckboxListVertically(facesContext, selectMany,
                        converter);
            }
            else if (layout.equals(LINE_DIRECTION))
            {
                renderCheckboxListHorizontally(facesContext, selectMany,
                        converter);
            }
            else
            {
                log.error("Wrong layout attribute for component "
                        + selectMany.getClientId(facesContext) + ": " + layout);
            }
        }
    }

    protected void renderCheckboxListHorizontally(FacesContext facesContext,
            UISelectMany selectMany, Converter converter) throws IOException
    {
        Set lookupSet = RendererUtils.getSubmittedValuesAsSet(facesContext,
                selectMany, converter, selectMany);
        boolean useSubmittedValues = lookupSet != null;
        if (!useSubmittedValues)
        {
            lookupSet = RendererUtils.getSelectedValuesAsSet(facesContext,
                    selectMany, converter, selectMany);
        }

        ResponseWriter writer = facesContext.getResponseWriter();
        writer.startElement(HTML.TABLE_ELEM, selectMany);
        HtmlRendererUtils.renderHTMLAttributes(writer, selectMany,
                HTML.SELECT_TABLE_PASSTHROUGH_ATTRIBUTES);
        HtmlRendererUtils.writeIdIfNecessary(writer, selectMany, facesContext);

        final int numRows = getLayoutWidth(selectMany);
        for (int i = 0; i < numRows; i++)
        {
            renderRowForHorizontal(facesContext, selectMany, converter,
                    lookupSet, writer, numRows, i);
        }

        writer.endElement(HTML.TABLE_ELEM);
    }

    protected void renderRowForHorizontal(FacesContext facesContext,
            UISelectMany selectMany, Converter converter, Set lookupSet,
            ResponseWriter writer, int totalRows, int rowNum)
            throws IOException
    {

        writer.startElement(HTML.TR_ELEM, selectMany);
        int colNum = 0;
        List items = RendererUtils.getSelectItemList(selectMany);
        for (int count = rowNum; count < items.size(); count++)
        {
            int mod = count % totalRows;
            if (mod == rowNum)
            {
                colNum++;
                SelectItem selectItem = (SelectItem) items.get(count);
                writer.startElement(HTML.TD_ELEM, selectMany);
                renderGroupOrItemCheckbox(facesContext, selectMany, selectItem,
                        lookupSet != null, lookupSet, converter, false);
                writer.endElement(HTML.TD_ELEM);
            }
        }
        int totalItems = items.size();
        int totalCols = (totalItems / totalRows);
        if (totalItems % totalRows != 0)
        {
            totalCols++;
        }
        if (colNum < totalCols)
        {
            writer.startElement(HTML.TD_ELEM, selectMany);
            writer.endElement(HTML.TD_ELEM);
        }
        writer.endElement(HTML.TR_ELEM);
    }

    protected void renderCheckboxListVertically(FacesContext facesContext,
            UISelectMany selectMany, Converter converter) throws IOException
    {

        Set lookupSet = RendererUtils.getSubmittedValuesAsSet(facesContext,
                selectMany, converter, selectMany);
        boolean useSubmittedValues = lookupSet != null;
        if (!useSubmittedValues)
        {
            lookupSet = RendererUtils.getSelectedValuesAsSet(facesContext,
                    selectMany, converter, selectMany);
        }

        ResponseWriter writer = facesContext.getResponseWriter();
        writer.startElement(HTML.TABLE_ELEM, selectMany);
        HtmlRendererUtils.renderHTMLAttributes(writer, selectMany,
                HTML.SELECT_TABLE_PASSTHROUGH_ATTRIBUTES);
        HtmlRendererUtils.writeIdIfNecessary(writer, selectMany, facesContext);

        List items = RendererUtils.getSelectItemList(selectMany);
        int totalItems = items.size();
        for (int count = 0; count < totalItems; count++)
        {
            writer.startElement(HTML.TR_ELEM, selectMany);
            final int numCols = getLayoutWidth(selectMany);
            for (int i = 0; i < numCols; i++)
            {
                writer.startElement(HTML.TD_ELEM, selectMany);
                if (count < totalItems)
                {
                    SelectItem selectItem = (SelectItem) items.get(count);
                    renderGroupOrItemCheckbox(facesContext, selectMany,
                            selectItem, lookupSet != null, lookupSet,
                            converter, true);
                }
                writer.endElement(HTML.TD_ELEM);
                if (i < numCols - 1)
                {
                    count += 1;
                }
            }
            writer.endElement(HTML.TR_ELEM);
        }
        writer.endElement(HTML.TABLE_ELEM);
    }

    protected void renderGroupOrItemCheckbox(FacesContext facesContext,
            UIComponent uiComponent, SelectItem selectItem,
            boolean useSubmittedValues, Set lookupSet,
            Converter converter, boolean pageDirectionLayout) throws IOException {
        ResponseWriter writer = facesContext.getResponseWriter();
        
        boolean isSelectItemGroup = (selectItem instanceof SelectItemGroup);

        if (isSelectItemGroup)
        {
            SelectItemGroup selectItemGroup = (SelectItemGroup) selectItem;
            renderCheckboxGroup(facesContext, uiComponent, selectItemGroup,
                    useSubmittedValues, lookupSet, converter,
                    pageDirectionLayout);
        }
        else
        {
            UISelectMany selectMany = (UISelectMany) uiComponent;
            Object itemValue = selectItem.getValue(); // TODO : Check here for getSubmittedValue. Look at RendererUtils.getValue
            String itemStrValue = getItemStringValue(facesContext, selectMany,
                    converter, itemValue);

            boolean checked = (useSubmittedValues && lookupSet
                    .contains(itemStrValue))
                    || (!useSubmittedValues && lookupSet.contains(itemValue));

            boolean disabled = selectItem.isDisabled();

            writer.startElement(HTML.LABEL_ELEM, selectMany);
            renderLabelClassIfNecessary(facesContext, selectMany, disabled);
            renderCheckbox(facesContext, selectMany, itemStrValue, disabled, checked, false,0);
            writer.write(HTML.NBSP_ENTITY);
            if(selectItem.isEscape())
            {
                writer.writeText(selectItem.getLabel(), null);
            }
            else
            {
                writer.write(selectItem.getLabel());
            }
            writer.endElement(HTML.LABEL_ELEM);
        }
    }

    protected void renderLabelClassIfNecessary(FacesContext facesContext,
            UISelectMany selectMany, boolean disabled) throws IOException
    {
        String labelClass = null;
        boolean componentDisabled = isDisabled(facesContext, selectMany);
        if (componentDisabled || disabled)
        {
            labelClass = (String) selectMany.getAttributes().get(
                    JSFAttr.DISABLED_CLASS_ATTR);
        }
        else
        {
            labelClass = (String) selectMany.getAttributes().get(
                    JSFAttr.ENABLED_CLASS_ATTR);
        }
        if (labelClass != null)
        {
            ResponseWriter writer = facesContext.getResponseWriter();
            writer.writeAttribute("class", labelClass, "labelClass");
        }
    }

    protected void renderCheckboxGroup(FacesContext facesContext,
            UIComponent uiComponent, SelectItemGroup selectItemGroup,
            boolean useSubmittedValues, Set lookupSet,
            Converter converter, boolean pageDirectionLayout) throws IOException {
        ResponseWriter writer = facesContext.getResponseWriter();
        UISelectMany selectMany = (UISelectMany)uiComponent;
        writer.startElement(HTML.TABLE_ELEM, selectMany);
        if (pageDirectionLayout)
            writer.startElement(HTML.TR_ELEM, selectMany);
        writer.startElement(HTML.TD_ELEM, selectMany);
        if (selectItemGroup.isEscape())
        {
            writer.writeText(selectItemGroup.getLabel(), HTML.LABEL_ATTR);
        }
        else
        {
            writer.write(selectItemGroup.getLabel());
        }
        writer.endElement(HTML.TD_ELEM);
        
        if (pageDirectionLayout) {
            writer.endElement(HTML.TR_ELEM);
            writer.startElement(HTML.TR_ELEM, selectMany);
        }
        writer.startElement(HTML.TD_ELEM, selectMany);
        writer.startElement(HTML.TABLE_ELEM, selectMany);
        writer.writeAttribute(HTML.BORDER_ATTR, "0", null);

        SelectItem[] selectItems = selectItemGroup.getSelectItems();
        for (int i=0; i<selectItems.length; i++) {
            renderGroupOrItemCheckbox(facesContext, selectMany, selectItems[i], useSubmittedValues, lookupSet, converter, pageDirectionLayout);
        }
        
        writer.endElement(HTML.TABLE_ELEM);
        writer.endElement(HTML.TD_ELEM);
        if (pageDirectionLayout)
            writer.endElement(HTML.TR_ELEM);
        writer.endElement(HTML.TABLE_ELEM);
    }

    /**
     * Determines the layout setting.  Defaults to
     * <code>lineDirection</code> if not specified.
     * @param selectMany the component
     * @return the layout
     */
    protected String getLayout(UISelectMany selectMany) {
        String layout = super.getLayout(selectMany);
        if (layout == null) {
            layout = LINE_DIRECTION;
        }
        return layout;
    }
    /**
     * Gets the layout width.
     * Returns the default layout width of 1 if the layout width
     * is not set or is less than 1.  
     * @param selectMany the component
     * @return the layout width
     */
    protected int getLayoutWidth(UISelectMany selectMany) {
        String layoutWidthString = null;
        if (selectMany instanceof HtmlSelectManyCheckbox) {
            layoutWidthString = ((HtmlSelectManyCheckbox) selectMany).getLayoutWidth();
        } else {
            layoutWidthString = (String) selectMany.getAttributes().get(JSFAttr.LAYOUT_WIDTH_ATTR);
        }
        final int defaultLayoutWidth = 1;
        int layoutWidth = defaultLayoutWidth;
        try {
            if (layoutWidthString != null && layoutWidthString.trim().length() > 0) {
                layoutWidth = Integer.parseInt(layoutWidthString);
            }
            if (layoutWidth < 1) {
                layoutWidth = defaultLayoutWidth;
            }
        } catch (Exception e) {
            layoutWidth = defaultLayoutWidth;
        }
        return layoutWidth;
    }

    protected void renderSingleCheckbox(FacesContext facesContext, HtmlCheckbox checkbox) throws IOException
    {
        String forAttr = checkbox.getFor();
        if (forAttr == null)
        {
            throw new IllegalStateException("mandatory attribute 'for'");
        }
        int index = checkbox.getIndex();
        if (index < 0)
        {
            throw new IllegalStateException("positive index must be given");
        }

        UIComponent uiComponent = checkbox.findComponent(forAttr);
        if (uiComponent == null)
        {
            throw new IllegalStateException("Could not find component '" + forAttr + "' (calling findComponent on component '" + checkbox.getClientId(facesContext) + "')");
        }
        if (!(uiComponent instanceof UISelectMany))
        {
            throw new IllegalStateException("UISelectMany expected");
        }

        UISelectMany uiSelectMany = (UISelectMany)uiComponent;
        Converter converter = getConverter(facesContext, uiSelectMany);
        List selectItemList = RendererUtils.getSelectItemList(uiSelectMany);
        if (index >= selectItemList.size())
        {
            throw new IndexOutOfBoundsException("index " + index + " >= " + selectItemList.size());
        }

        SelectItem selectItem = (SelectItem)selectItemList.get(index);
        Object itemValue = selectItem.getValue();
        String itemStrValue = getItemStringValue(facesContext, uiSelectMany, converter, itemValue);

        //TODO: we must cache this Set!
        Set lookupSet = RendererUtils.getSubmittedValuesAsSet(facesContext, uiComponent, converter, uiSelectMany);
        
        boolean useSubmittedValues = (lookupSet != null);
        if (!useSubmittedValues)
        {
            lookupSet = RendererUtils.getSelectedValuesAsSet(facesContext, uiComponent, converter, uiSelectMany);
        }

        ResponseWriter writer = facesContext.getResponseWriter();
        
        //renderCheckbox(facesContext,
        //               uiSelectMany,
        //               itemStrValue,
        //               selectItem.getLabel(),
        //               isDisabled(facesContext,uiSelectMany),
        //               lookupSet.contains(itemStrValue), true);
        
        String itemId = renderCheckbox(facesContext,
                uiSelectMany,
                itemStrValue,
                isDisabled(facesContext,uiSelectMany),
                lookupSet.contains(itemStrValue), false, index);

        //Render the
        // label element after the input
        boolean componentDisabled = isDisabled(facesContext, uiSelectMany);
        boolean itemDisabled = selectItem.isDisabled();
        boolean disabled = (componentDisabled || itemDisabled);

        HtmlRendererUtils.renderLabel(writer, uiSelectMany, itemId, selectItem.getLabel(), disabled);
        
    }


    protected boolean isDisabled(FacesContext facesContext, UIComponent uiComponent)
    {
        if (!UserRoleUtils.isEnabledOnUserRole(uiComponent))
        {
            return true;
        }
        else
        {
            return super.isDisabled(facesContext, uiComponent);
        }
    }

    public void decode(FacesContext facesContext, UIComponent uiComponent)
    {
        if (uiComponent instanceof HtmlCheckbox)
        {
            //nothing to decode
        }
        else
        {
            super.decode(facesContext, uiComponent);
        }
    }

    protected String getItemStringValue(FacesContext facesContext, UISelectMany selectMany, 
            Converter converter, Object itemValue) {
        String itemStrValue;
        if (converter == null)
        {
            itemStrValue = itemValue.toString();
        }
        else
        {
            itemStrValue = converter.getAsString(facesContext, selectMany, itemValue);
        }
        return itemStrValue;
    }

    /**
     * Overrides HtmlCheckboxRendererBase to handle valueType attribute on UISelectMany.
     */
    @Override
    public Object getConvertedValue(FacesContext facesContext,
            UIComponent component, Object submittedValue)
            throws ConverterException
    {
        RendererUtils.checkParamValidity(facesContext, component, null);
        
        if (component instanceof UISelectMany) 
        {
            // invoke getConvertedUISelectManyValue() with considerValueType = true
            return RendererUtils.getConvertedUISelectManyValue(facesContext,
                    (UISelectMany) component, submittedValue, true); 
        } 
        else 
        {
            // component is not a UISelectMany --> no change needed
            return super.getConvertedValue(facesContext, component, submittedValue);
        }
    }
    
    /**
     * Overrides HtmlCheckboxRendererBase to handle valueType attribute on UISelectMany.
     */
    @Override
    protected Converter getConverter(FacesContext facesContext,
            UIComponent component)
    {
        if (component instanceof UISelectMany)
        {
            // invoke findUISelectManyConverterFailsafe() with considerValueType = true
            return HtmlRendererUtils.findUISelectManyConverterFailsafe(facesContext, 
                    (UISelectMany) component, true);
        }
        else
        {
            // component is not a UISelectMany --> no change needed
            return super.getConverter(facesContext, component);
        }
    }

}
