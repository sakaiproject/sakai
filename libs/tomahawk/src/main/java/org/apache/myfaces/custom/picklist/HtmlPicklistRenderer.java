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
package org.apache.myfaces.custom.picklist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.faces.application.ResourceDependency;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UISelectMany;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.model.SelectItem;

import org.apache.commons.collections.CollectionUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.JSFAttr;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlListboxRendererBase;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;

/**
 * 
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC" 
 *   family = "jakarta.faces.SelectMany"
 *   type = "org.apache.myfaces.PicklistRenderer"
 * @since 1.1.7
 * @author Bruno Aranda (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@ResourceDependency(library="oam.custom.picklist",name="picklist.js")
public class HtmlPicklistRenderer extends HtmlListboxRendererBase
{

    private static final String FUNCTION_ADD_TO_SELECTED = "myfaces_picklist_addToSelected";
    private static final String FUNCTION_ADD_ALL_TO_SELECTED = "myfaces_picklist_addAllToSelected";    
    private static final String FUNCTION_REMOVE_FROM_SELECTED = "myfaces_picklist_removeFromSelected";
    private static final String FUNCTION_REMOVE_ALL_FROM_SELECTED = "myfaces_picklist_removeAllFromSelected";

    private static final String AVAILABLE_SUFFIX = "_AVAILABLE";
    private static final String SELECTED_SUFFIX = "_SELECTED";
    private static final String HIDDEN_SUFFIX = "_HIDDEN";

    public void decode(FacesContext facesContext, UIComponent uiComponent)
    {
        RendererUtils.checkParamValidity(facesContext, uiComponent, null);

        if (!(uiComponent instanceof EditableValueHolder))
        {
            throw new IllegalArgumentException("Component "
                                               + uiComponent.getClientId(facesContext)
                                               + " is not an EditableValueHolder");
        }

        String hiddenClientId = uiComponent.getClientId(facesContext)
                                + HIDDEN_SUFFIX;

        Map paramValuesMap = facesContext.getExternalContext()
                .getRequestParameterValuesMap();

        if (HtmlRendererUtils.isDisabledOrReadOnly(uiComponent))
            return;

        if (paramValuesMap.containsKey(hiddenClientId))
        {
            String[] valuesInline = (String[]) paramValuesMap
                    .get(hiddenClientId);

            if (valuesInline[0].trim().equals(""))
            {
                ((EditableValueHolder) uiComponent)
                .setSubmittedValue(new String[] {});
            }
            else
            {
                String[] reqValues = valuesInline[0].split(",");
                ((EditableValueHolder) uiComponent)
                .setSubmittedValue(reqValues);
            }
        }
        else
        {
            /* request parameter not found, nothing to decode - set submitted value to an empty array
             as we should get here only if the component is on a submitted form, is rendered
             and if the component is not readonly or has not been disabled.

             So in fact, there must be component value at this location, but for listboxes, comboboxes etc.
             the submitted value is not posted if no item is selected. */
            ((EditableValueHolder) uiComponent)
                    .setSubmittedValue(new String[] {});
        }
    }

    public void encodeEnd(FacesContext facesContext, UIComponent uiComponent)
            throws IOException
    {
        RendererUtils.checkParamValidity(facesContext, uiComponent,
                                         HtmlSelectManyPicklist.class);
        
        // check for displayValueOnly
        if(HtmlRendererUtils.isDisplayValueOnly(uiComponent))
        {
            HtmlRendererUtils.renderDisplayValueOnlyForSelects(facesContext, uiComponent, true);
            return;
        }

        HtmlSelectManyPicklist picklist = (HtmlSelectManyPicklist) uiComponent;

        String addButtonText = picklist.getAddButtonText();
        String addAllButtonText = picklist.getAddAllButtonText();        
        String removeButtonText = picklist.getRemoveButtonText();
        String removeAllButtonText = picklist.getRemoveAllButtonText();        
        String addButtonStyle = picklist.getAddButtonStyle();
        String addAllButtonStyle = picklist.getAddAllButtonStyle();  
        String removeButtonStyle = picklist.getRemoveButtonStyle();
        String removeAllButtonStyle = picklist.getRemoveAllButtonStyle();        
        String addButtonStyleClass = picklist.getAddButtonStyleClass();
        String addAllButtonStyleClass = picklist.getAddAllButtonStyleClass();        
        String removeButtonStyleClass = picklist.getRemoveButtonStyleClass();
        String removeAllButtonStyleClass = picklist.getRemoveAllButtonStyleClass();        
        
        //Set the default values for addButtonText and removeButtonText
        if(addButtonText == null || addButtonText.length() == 0)
            addButtonText = ">";
        
        if(addAllButtonText == null || addAllButtonText.length() == 0)
            addAllButtonText = ">>";        
        
        if(removeButtonText == null || removeButtonText.length() == 0)
            removeButtonText = "<";
        
        if(removeAllButtonText == null || removeAllButtonText.length() == 0)
            removeAllButtonText = "<<";        

        //encodeJavascript(facesContext, uiComponent);

        String availableListClientId = uiComponent.getClientId(facesContext)
                                       + AVAILABLE_SUFFIX;
        String selectedListClientId = uiComponent.getClientId(facesContext)
                                      + SELECTED_SUFFIX;
        String hiddenFieldCliendId = uiComponent.getClientId(facesContext)
                                     + HIDDEN_SUFFIX;

        List selectItemList = RendererUtils
                .getSelectItemList((UISelectMany) uiComponent);
        Converter converter = getConverter(facesContext, uiComponent); 

        Set lookupSet = HtmlRendererUtils.getSubmittedOrSelectedValuesAsSet(
                true, uiComponent, facesContext, converter);

        List selectItemsForSelectedValues = selectItemsForSelectedList(
                facesContext, uiComponent, selectItemList, converter, lookupSet);
        List selectItemsForAvailableList = selectItemsForAvailableList(
                facesContext, uiComponent, selectItemList,
                selectItemsForSelectedValues, converter);

        ResponseWriter writer = facesContext.getResponseWriter();

        writer.startElement(HTML.TABLE_ELEM, uiComponent);
        
        Map<String, List<ClientBehavior>> behaviors = null;
        if (uiComponent instanceof ClientBehaviorHolder)
        {
            behaviors = ((ClientBehaviorHolder) uiComponent).getClientBehaviors();
        }
        
        if (behaviors != null && !behaviors.isEmpty())
        {
            writer.writeAttribute(HTML.ID_ATTR, uiComponent.getClientId(facesContext),null);
        }
        else
        {
            HtmlRendererUtils.writeIdIfNecessary(writer, uiComponent, facesContext);
        }
        
        writer.startElement(HTML.TR_ELEM, uiComponent);
        writer.startElement(HTML.TD_ELEM, uiComponent);

        encodeSelect(facesContext, picklist, availableListClientId, isDisabled(
                facesContext, uiComponent), picklist.getSize(),
                                            selectItemsForAvailableList, converter);

        writer.endElement(HTML.TD_ELEM);

        // encode buttons
        writer.startElement(HTML.TD_ELEM, uiComponent);

        String javascriptAddToSelected = FUNCTION_ADD_TO_SELECTED + "('"
                                         + availableListClientId + "','" + selectedListClientId + "','"
                                         + hiddenFieldCliendId + "')";
        
        String javascriptAddAllToSelected = FUNCTION_ADD_ALL_TO_SELECTED + "('"
                                         + availableListClientId + "','" + selectedListClientId + "','"
                                         + hiddenFieldCliendId + "')";        
        
        String javascriptRemoveFromSelected = FUNCTION_REMOVE_FROM_SELECTED
                                              + "('" + availableListClientId + "','" + selectedListClientId
                                              + "','" + hiddenFieldCliendId + "')";
        
        String javascriptRemoveAllFromSelected = FUNCTION_REMOVE_ALL_FROM_SELECTED
                                              + "('" + availableListClientId + "','" + selectedListClientId
                                              + "','" + hiddenFieldCliendId + "')";        

        // encode (add selected) button.
        encodeSwapButton(facesContext, uiComponent, javascriptAddToSelected,
                addButtonText, addButtonStyle, addButtonStyleClass);

        writer.startElement(HTML.BR_ELEM, uiComponent);
        writer.endElement(HTML.BR_ELEM);
        
        // encode (add all) button.
        encodeSwapButton(facesContext, uiComponent, javascriptAddAllToSelected, 
                addAllButtonText, addAllButtonStyle, addAllButtonStyleClass);      
        
        writer.startElement(HTML.BR_ELEM, uiComponent);
        writer.endElement(HTML.BR_ELEM);

        // encode (remove selected) button.
        encodeSwapButton(facesContext, uiComponent, javascriptRemoveFromSelected, 
                removeButtonText, removeButtonStyle, removeButtonStyleClass);  
        
        writer.startElement(HTML.BR_ELEM, uiComponent);
        writer.endElement(HTML.BR_ELEM);        
        
        // encode (remove all) button.        
        encodeSwapButton(facesContext, uiComponent, javascriptRemoveAllFromSelected, 
                removeAllButtonText, removeAllButtonStyle, removeAllButtonStyleClass);        

        writer.endElement(HTML.TD_ELEM);

        // encode selected list
        writer.startElement(HTML.TD_ELEM, uiComponent);

        encodeSelect(facesContext, picklist, selectedListClientId, isDisabled(
                facesContext, uiComponent), picklist.getSize(),
                                            selectItemsForSelectedValues, converter);

        // hidden field with the selected values
        encodeHiddenField(facesContext, uiComponent, hiddenFieldCliendId,
                          lookupSet);

        writer.endElement(HTML.TD_ELEM);
        writer.endElement(HTML.TR_ELEM);
        writer.endElement(HTML.TABLE_ELEM);
    }

    /*private void encodeJavascript(FacesContext facesContext,
                                  UIComponent uiComponent)
    {
        // AddResource takes care to add only one reference to the same script
        AddResource addResource = AddResourceFactory.getInstance(facesContext);
        addResource.addJavaScriptAtPosition(facesContext, AddResource.HEADER_BEGIN,
                                            HtmlPicklistRenderer.class, "picklist.js");
    }*/

    private void encodeSwapButton(FacesContext facesContext,
                                  UIComponent uiComponent, String javaScriptFunction,
                                  String text, String style, String styleClass)
            throws IOException
    {
        ResponseWriter writer = facesContext.getResponseWriter();

        writer.startElement(HTML.INPUT_ELEM, uiComponent);
        writer.writeAttribute(HTML.STYLE_ATTR, style, null);
        writer.writeAttribute(HTML.CLASS_ATTR, styleClass, null);
        writer.writeAttribute(HTML.TYPE_ATTR, HTML.INPUT_TYPE_BUTTON,
                              JSFAttr.TYPE_ATTR);
        writer.writeAttribute(HTML.ONCLICK_ATTR, javaScriptFunction, null);
        writer.writeAttribute(HTML.VALUE_ATTR, text, null);
        writer.endElement(HTML.INPUT_ELEM);
    }
    
    /**
     * Overrides HtmlListboxRendererBase to handle valueType attribute on UISelectMany.
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
     * Overrides HtmlListboxRendererBase to handle valueType attribute on UISelectMany.
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

    private void encodeSelect(FacesContext facesContext,
                              UIComponent uiComponent, String clientId, boolean disabled,
                              int size, List selectItemsToDisplay, Converter converter)
            throws IOException
    {
        ResponseWriter writer = facesContext.getResponseWriter();

        writer.startElement(HTML.SELECT_ELEM, uiComponent);
        writer.writeAttribute(HTML.ID_ATTR, clientId, JSFAttr.ID_ATTR);
        writer.writeAttribute(HTML.NAME_ATTR, clientId, null);

        writer.writeAttribute(HTML.MULTIPLE_ATTR, "true", null);

        if (size == 0)
        {
            //No size given (Listbox) --> size is number of select items
            writer.writeAttribute(HTML.SIZE_ATTR, Integer
                    .toString(selectItemsToDisplay.size()), null);
        }
        else
        {
            writer.writeAttribute(HTML.SIZE_ATTR, Integer.toString(size), null);
        }
        
        Map<String, List<ClientBehavior>> behaviors = null;
        if (uiComponent instanceof ClientBehaviorHolder)
        {
            behaviors = ((ClientBehaviorHolder) uiComponent).getClientBehaviors();
        }
        
        if (behaviors != null && !behaviors.isEmpty())
        {
            HtmlRendererUtils.renderBehaviorizedOnchangeEventHandler(facesContext, writer, uiComponent, behaviors);
            HtmlRendererUtils.renderBehaviorizedEventHandlers(facesContext, writer, uiComponent, behaviors);
            HtmlRendererUtils.renderBehaviorizedFieldEventHandlersWithoutOnchange(facesContext, writer, uiComponent, behaviors);
            HtmlRendererUtils.renderHTMLAttributes(writer, uiComponent, HTML.SELECT_PASSTHROUGH_ATTRIBUTES_WITHOUT_DISABLED_AND_EVENTS);
        }
        else
        {
            HtmlRendererUtils.renderHTMLAttributes(writer, uiComponent,
                                                   HTML.SELECT_PASSTHROUGH_ATTRIBUTES_WITHOUT_DISABLED);
        }
        if (disabled)
        {
            writer.writeAttribute(HTML.DISABLED_ATTR, Boolean.TRUE, null);
        }

        HtmlRendererUtils.renderSelectOptions(facesContext, uiComponent,
                                              converter, Collections.EMPTY_SET, selectItemsToDisplay);

        // bug #970747: force separate end tag
        writer.writeText("", null);
        writer.endElement(HTML.SELECT_ELEM);
    }

    private void encodeHiddenField(FacesContext facesContext,
                                   UIComponent uiComponent, String hiddenFieldCliendId, Set lookupSet)
            throws IOException
    {
        ResponseWriter writer = facesContext.getResponseWriter();

        writer.startElement(HTML.INPUT_ELEM, uiComponent);
        writer.writeAttribute(HTML.TYPE_ATTR, HTML.INPUT_TYPE_HIDDEN,
                              JSFAttr.TYPE_ATTR);
        writer.writeAttribute(HTML.ID_ATTR, hiddenFieldCliendId,
                              JSFAttr.ID_ATTR);
        writer.writeAttribute(HTML.NAME_ATTR, hiddenFieldCliendId, null);

        StringBuffer sb = new StringBuffer();
        int n = 0;
        for (Iterator i = lookupSet.iterator(); i.hasNext();)
        {
            if (n > 0)
            {
                sb.append(",");
            }
            String value = (String) i.next();
            sb.append(value);
            n++;
        }

        writer.writeAttribute(HTML.VALUE_ATTR, sb.toString(), null);
        writer.endElement(HTML.INPUT_ELEM);

    }

    private List selectItemsForSelectedList(FacesContext facesContext,
                                            UIComponent uiComponent, List selectItemList, Converter converter,
                                            Set lookupSet)
    {
        List selectItemForSelectedValues = new ArrayList(lookupSet.size());

        for (Iterator i = selectItemList.iterator(); i.hasNext();)
        {
            SelectItem selectItem = (SelectItem) i.next();
            String itemStrValue = RendererUtils.getConvertedStringValue(facesContext, uiComponent,
                    converter, selectItem);


            for (Iterator i2 = lookupSet.iterator(); i2.hasNext();)
            {
                Object value = i2.next();
                if (value.equals(itemStrValue))
                {
                    selectItemForSelectedValues.add(selectItem);
                }
            }
        }

        return selectItemForSelectedValues;
    }

    private List selectItemsForAvailableList(FacesContext facesContext,
                                             UIComponent uiComponent, List selectItemList,
                                             List selectItemsForSelectedList, Converter converter)
    {

        return new ArrayList(CollectionUtils.subtract(selectItemList,
                                                      selectItemsForSelectedList));
    }
    
}
