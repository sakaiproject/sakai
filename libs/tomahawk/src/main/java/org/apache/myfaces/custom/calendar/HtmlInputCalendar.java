/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.myfaces.custom.calendar;

import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.PartialStateHolder;
import jakarta.faces.component.StateHolder;
import org.apache.myfaces.component.AttachedDeltaWrapper;
import jakarta.faces.component.UIComponent;
import jakarta.faces.convert.Converter;
import org.apache.myfaces.custom.calendar.DateBusinessConverter;
import jakarta.faces.el.MethodBinding;


// Generated from class org.apache.myfaces.custom.calendar.AbstractHtmlInputCalendar.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlInputCalendar extends org.apache.myfaces.custom.calendar.AbstractHtmlInputCalendar
{

    static public final String COMPONENT_FAMILY =
        "jakarta.faces.Input";
    static public final String COMPONENT_TYPE =
        "org.apache.myfaces.HtmlInputCalendar";
    static public final String DEFAULT_RENDERER_TYPE = 
        "org.apache.myfaces.Calendar";


    public HtmlInputCalendar()
    {
        setRendererType("org.apache.myfaces.Calendar");
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }



    @Override
    public void addClientBehavior(String eventName, jakarta.faces.component.behavior.ClientBehavior behavior)
    {
        super.addClientBehavior(eventName, behavior);
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonEventConstants.markEvent(this, eventName);
    }

    
    // Property: dateBusinessConverter
    public DateBusinessConverter getDateBusinessConverter()
    {
        return (DateBusinessConverter) getStateHelper().eval(PropertyKeys.dateBusinessConverter);
    }
    
    public void setDateBusinessConverter(DateBusinessConverter dateBusinessConverter)
    {
        getStateHelper().put(PropertyKeys.dateBusinessConverter, dateBusinessConverter ); 
    }    
    // Property: monthYearRowClass
    public String getMonthYearRowClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.monthYearRowClass);
    }
    
    public void setMonthYearRowClass(String monthYearRowClass)
    {
        getStateHelper().put(PropertyKeys.monthYearRowClass, monthYearRowClass ); 
    }    
    // Property: weekRowClass
    public String getWeekRowClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.weekRowClass);
    }
    
    public void setWeekRowClass(String weekRowClass)
    {
        getStateHelper().put(PropertyKeys.weekRowClass, weekRowClass ); 
    }    
    // Property: dayCellClass
    public String getDayCellClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.dayCellClass);
    }
    
    public void setDayCellClass(String dayCellClass)
    {
        getStateHelper().put(PropertyKeys.dayCellClass, dayCellClass ); 
    }    
    // Property: currentDayCellClass
    public String getCurrentDayCellClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.currentDayCellClass);
    }
    
    public void setCurrentDayCellClass(String currentDayCellClass)
    {
        getStateHelper().put(PropertyKeys.currentDayCellClass, currentDayCellClass ); 
    }    
    // Property: popupLeft
    public boolean isPopupLeft()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.popupLeft, false);
    }
    
    public void setPopupLeft(boolean popupLeft)
    {
        getStateHelper().put(PropertyKeys.popupLeft, popupLeft ); 
    }    
    // Property: renderAsPopup
    public boolean isRenderAsPopup()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.renderAsPopup, false);
    }
    
    public void setRenderAsPopup(boolean renderAsPopup)
    {
        getStateHelper().put(PropertyKeys.renderAsPopup, renderAsPopup ); 
    }    
    // Property: addResources
    public boolean isAddResources()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.addResources, true);
    }
    
    public void setAddResources(boolean addResources)
    {
        getStateHelper().put(PropertyKeys.addResources, addResources ); 
    }    
    // Property: popupButtonString
    public String getPopupButtonString()
    {
        return (String) getStateHelper().eval(PropertyKeys.popupButtonString);
    }
    
    public void setPopupButtonString(String popupButtonString)
    {
        getStateHelper().put(PropertyKeys.popupButtonString, popupButtonString ); 
    }    
    // Property: popupButtonStyle
    public String getPopupButtonStyle()
    {
        return (String) getStateHelper().eval(PropertyKeys.popupButtonStyle);
    }
    
    public void setPopupButtonStyle(String popupButtonStyle)
    {
        getStateHelper().put(PropertyKeys.popupButtonStyle, popupButtonStyle ); 
    }    
    // Property: popupButtonStyleClass
    public String getPopupButtonStyleClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.popupButtonStyleClass);
    }
    
    public void setPopupButtonStyleClass(String popupButtonStyleClass)
    {
        getStateHelper().put(PropertyKeys.popupButtonStyleClass, popupButtonStyleClass ); 
    }    
    // Property: renderPopupButtonAsImage
    public boolean isRenderPopupButtonAsImage()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.renderPopupButtonAsImage, false);
    }
    
    public void setRenderPopupButtonAsImage(boolean renderPopupButtonAsImage)
    {
        getStateHelper().put(PropertyKeys.renderPopupButtonAsImage, renderPopupButtonAsImage ); 
    }    
    // Property: popupDateFormat
    public String getPopupDateFormat()
    {
        return (String) getStateHelper().eval(PropertyKeys.popupDateFormat);
    }
    
    public void setPopupDateFormat(String popupDateFormat)
    {
        getStateHelper().put(PropertyKeys.popupDateFormat, popupDateFormat ); 
    }    
    // Property: popupGotoString
    public String getPopupGotoString()
    {
        return (String) getStateHelper().eval(PropertyKeys.popupGotoString);
    }
    
    public void setPopupGotoString(String popupGotoString)
    {
        getStateHelper().put(PropertyKeys.popupGotoString, popupGotoString ); 
    }    
    // Property: popupTodayString
    public String getPopupTodayString()
    {
        return (String) getStateHelper().eval(PropertyKeys.popupTodayString);
    }
    
    public void setPopupTodayString(String popupTodayString)
    {
        getStateHelper().put(PropertyKeys.popupTodayString, popupTodayString ); 
    }    
    // Property: popupTodayDateFormat
    public String getPopupTodayDateFormat()
    {
        return (String) getStateHelper().eval(PropertyKeys.popupTodayDateFormat);
    }
    
    public void setPopupTodayDateFormat(String popupTodayDateFormat)
    {
        getStateHelper().put(PropertyKeys.popupTodayDateFormat, popupTodayDateFormat ); 
    }    
    // Property: popupWeekString
    public String getPopupWeekString()
    {
        return (String) getStateHelper().eval(PropertyKeys.popupWeekString);
    }
    
    public void setPopupWeekString(String popupWeekString)
    {
        getStateHelper().put(PropertyKeys.popupWeekString, popupWeekString ); 
    }    
    // Property: popupScrollLeftMessage
    public String getPopupScrollLeftMessage()
    {
        return (String) getStateHelper().eval(PropertyKeys.popupScrollLeftMessage);
    }
    
    public void setPopupScrollLeftMessage(String popupScrollLeftMessage)
    {
        getStateHelper().put(PropertyKeys.popupScrollLeftMessage, popupScrollLeftMessage ); 
    }    
    // Property: popupScrollRightMessage
    public String getPopupScrollRightMessage()
    {
        return (String) getStateHelper().eval(PropertyKeys.popupScrollRightMessage);
    }
    
    public void setPopupScrollRightMessage(String popupScrollRightMessage)
    {
        getStateHelper().put(PropertyKeys.popupScrollRightMessage, popupScrollRightMessage ); 
    }    
    // Property: popupSelectMonthMessage
    public String getPopupSelectMonthMessage()
    {
        return (String) getStateHelper().eval(PropertyKeys.popupSelectMonthMessage);
    }
    
    public void setPopupSelectMonthMessage(String popupSelectMonthMessage)
    {
        getStateHelper().put(PropertyKeys.popupSelectMonthMessage, popupSelectMonthMessage ); 
    }    
    // Property: popupSelectYearMessage
    public String getPopupSelectYearMessage()
    {
        return (String) getStateHelper().eval(PropertyKeys.popupSelectYearMessage);
    }
    
    public void setPopupSelectYearMessage(String popupSelectYearMessage)
    {
        getStateHelper().put(PropertyKeys.popupSelectYearMessage, popupSelectYearMessage ); 
    }    
    // Property: popupSelectDateMessage
    public String getPopupSelectDateMessage()
    {
        return (String) getStateHelper().eval(PropertyKeys.popupSelectDateMessage);
    }
    
    public void setPopupSelectDateMessage(String popupSelectDateMessage)
    {
        getStateHelper().put(PropertyKeys.popupSelectDateMessage, popupSelectDateMessage ); 
    }    
    // Property: popupTheme
    public String getPopupTheme()
    {
        return (String) getStateHelper().eval(PropertyKeys.popupTheme);
    }
    
    public void setPopupTheme(String popupTheme)
    {
        getStateHelper().put(PropertyKeys.popupTheme, popupTheme ); 
    }    
    // Property: popupButtonImageUrl
    public String getPopupButtonImageUrl()
    {
        return (String) getStateHelper().eval(PropertyKeys.popupButtonImageUrl);
    }
    
    public void setPopupButtonImageUrl(String popupButtonImageUrl)
    {
        getStateHelper().put(PropertyKeys.popupButtonImageUrl, popupButtonImageUrl ); 
    }    
    // Property: helpText
    public String getHelpText()
    {
        return (String) getStateHelper().eval(PropertyKeys.helpText);
    }
    
    public void setHelpText(String helpText)
    {
        getStateHelper().put(PropertyKeys.helpText, helpText ); 
    }    
    // Property: popupSelectMode
    public String getPopupSelectMode()
    {
        return (String) getStateHelper().eval(PropertyKeys.popupSelectMode, "day");
    }
    
    public void setPopupSelectMode(String popupSelectMode)
    {
        getStateHelper().put(PropertyKeys.popupSelectMode, popupSelectMode ); 
    }    
    // Property: enabledOnUserRole
    public String getEnabledOnUserRole()
    {
        return (String) getStateHelper().eval(PropertyKeys.enabledOnUserRole);
    }
    
    public void setEnabledOnUserRole(String enabledOnUserRole)
    {
        getStateHelper().put(PropertyKeys.enabledOnUserRole, enabledOnUserRole ); 
    }    
    // Property: visibleOnUserRole
    public String getVisibleOnUserRole()
    {
        return (String) getStateHelper().eval(PropertyKeys.visibleOnUserRole);
    }
    
    public void setVisibleOnUserRole(String visibleOnUserRole)
    {
        getStateHelper().put(PropertyKeys.visibleOnUserRole, visibleOnUserRole ); 
    }    
    // Property: javascriptLocation
    public String getJavascriptLocation()
    {
        return (String) getStateHelper().eval(PropertyKeys.javascriptLocation);
    }
    
    public void setJavascriptLocation(String javascriptLocation)
    {
        getStateHelper().put(PropertyKeys.javascriptLocation, javascriptLocation ); 
    }    
    // Property: imageLocation
    public String getImageLocation()
    {
        return (String) getStateHelper().eval(PropertyKeys.imageLocation);
    }
    
    public void setImageLocation(String imageLocation)
    {
        getStateHelper().put(PropertyKeys.imageLocation, imageLocation ); 
    }    
    // Property: styleLocation
    public String getStyleLocation()
    {
        return (String) getStateHelper().eval(PropertyKeys.styleLocation);
    }
    
    public void setStyleLocation(String styleLocation)
    {
        getStateHelper().put(PropertyKeys.styleLocation, styleLocation ); 
    }    
    // Property: javascriptLibrary
    public String getJavascriptLibrary()
    {
        return (String) getStateHelper().eval(PropertyKeys.javascriptLibrary);
    }
    
    public void setJavascriptLibrary(String javascriptLibrary)
    {
        getStateHelper().put(PropertyKeys.javascriptLibrary, javascriptLibrary ); 
    }    
    // Property: imageLibrary
    public String getImageLibrary()
    {
        return (String) getStateHelper().eval(PropertyKeys.imageLibrary);
    }
    
    public void setImageLibrary(String imageLibrary)
    {
        getStateHelper().put(PropertyKeys.imageLibrary, imageLibrary ); 
    }    
    // Property: styleLibrary
    public String getStyleLibrary()
    {
        return (String) getStateHelper().eval(PropertyKeys.styleLibrary);
    }
    
    public void setStyleLibrary(String styleLibrary)
    {
        getStateHelper().put(PropertyKeys.styleLibrary, styleLibrary ); 
    }    
    // Property: align
    public String getAlign()
    {
        return (String) getStateHelper().eval(PropertyKeys.align);
    }
    
    public void setAlign(String align)
    {
        getStateHelper().put(PropertyKeys.align, align ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ALIGN_PROP);
    }    
    // Property: maxlength
    public int getMaxlength()
    {
        return (Integer) getStateHelper().eval(PropertyKeys.maxlength, Integer.MIN_VALUE);
    }
    
    public void setMaxlength(int maxlength)
    {
        getStateHelper().put(PropertyKeys.maxlength, maxlength ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.MAXLENGTH_PROP);
    }    
    // Property: size
    public int getSize()
    {
        return (Integer) getStateHelper().eval(PropertyKeys.size, Integer.MIN_VALUE);
    }
    
    public void setSize(int size)
    {
        getStateHelper().put(PropertyKeys.size, size ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.SIZE_PROP);
    }    
    // Property: label
    public String getLabel()
    {
        return (String) getStateHelper().eval(PropertyKeys.label);
    }
    
    public void setLabel(String label)
    {
        getStateHelper().put(PropertyKeys.label, label ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.LABEL_PROP);
    }    
    // Property: style
    public String getStyle()
    {
        return (String) getStateHelper().eval(PropertyKeys.style);
    }
    
    public void setStyle(String style)
    {
        getStateHelper().put(PropertyKeys.style, style ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.STYLE_PROP);
    }    
    // Property: styleClass
    public String getStyleClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.styleClass);
    }
    
    public void setStyleClass(String styleClass)
    {
        getStateHelper().put(PropertyKeys.styleClass, styleClass ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.STYLECLASS_PROP);
    }    
    // Property: alt
    public String getAlt()
    {
        return (String) getStateHelper().eval(PropertyKeys.alt);
    }
    
    public void setAlt(String alt)
    {
        getStateHelper().put(PropertyKeys.alt, alt ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ALT_PROP);
    }    
    // Property: tabindex
    public String getTabindex()
    {
        return (String) getStateHelper().eval(PropertyKeys.tabindex);
    }
    
    public void setTabindex(String tabindex)
    {
        getStateHelper().put(PropertyKeys.tabindex, tabindex ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.TABINDEX_PROP);
    }    
    // Property: onblur
    public String getOnblur()
    {
        return (String) getStateHelper().eval(PropertyKeys.onblur);
    }
    
    public void setOnblur(String onblur)
    {
        getStateHelper().put(PropertyKeys.onblur, onblur ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONBLUR_PROP);
    }    
    // Property: onfocus
    public String getOnfocus()
    {
        return (String) getStateHelper().eval(PropertyKeys.onfocus);
    }
    
    public void setOnfocus(String onfocus)
    {
        getStateHelper().put(PropertyKeys.onfocus, onfocus ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONFOCUS_PROP);
    }    
    // Property: accesskey
    public String getAccesskey()
    {
        return (String) getStateHelper().eval(PropertyKeys.accesskey);
    }
    
    public void setAccesskey(String accesskey)
    {
        getStateHelper().put(PropertyKeys.accesskey, accesskey ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ACCESSKEY_PROP);
    }    
    // Property: onclick
    public String getOnclick()
    {
        return (String) getStateHelper().eval(PropertyKeys.onclick);
    }
    
    public void setOnclick(String onclick)
    {
        getStateHelper().put(PropertyKeys.onclick, onclick ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONCLICK_PROP);
    }    
    // Property: ondblclick
    public String getOndblclick()
    {
        return (String) getStateHelper().eval(PropertyKeys.ondblclick);
    }
    
    public void setOndblclick(String ondblclick)
    {
        getStateHelper().put(PropertyKeys.ondblclick, ondblclick ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONDBLCLICK_PROP);
    }    
    // Property: onkeydown
    public String getOnkeydown()
    {
        return (String) getStateHelper().eval(PropertyKeys.onkeydown);
    }
    
    public void setOnkeydown(String onkeydown)
    {
        getStateHelper().put(PropertyKeys.onkeydown, onkeydown ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONKEYDOWN_PROP);
    }    
    // Property: onkeypress
    public String getOnkeypress()
    {
        return (String) getStateHelper().eval(PropertyKeys.onkeypress);
    }
    
    public void setOnkeypress(String onkeypress)
    {
        getStateHelper().put(PropertyKeys.onkeypress, onkeypress ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONKEYPRESS_PROP);
    }    
    // Property: onkeyup
    public String getOnkeyup()
    {
        return (String) getStateHelper().eval(PropertyKeys.onkeyup);
    }
    
    public void setOnkeyup(String onkeyup)
    {
        getStateHelper().put(PropertyKeys.onkeyup, onkeyup ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONKEYUP_PROP);
    }    
    // Property: onmousedown
    public String getOnmousedown()
    {
        return (String) getStateHelper().eval(PropertyKeys.onmousedown);
    }
    
    public void setOnmousedown(String onmousedown)
    {
        getStateHelper().put(PropertyKeys.onmousedown, onmousedown ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONMOUSEDOWN_PROP);
    }    
    // Property: onmousemove
    public String getOnmousemove()
    {
        return (String) getStateHelper().eval(PropertyKeys.onmousemove);
    }
    
    public void setOnmousemove(String onmousemove)
    {
        getStateHelper().put(PropertyKeys.onmousemove, onmousemove ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONMOUSEMOVE_PROP);
    }    
    // Property: onmouseout
    public String getOnmouseout()
    {
        return (String) getStateHelper().eval(PropertyKeys.onmouseout);
    }
    
    public void setOnmouseout(String onmouseout)
    {
        getStateHelper().put(PropertyKeys.onmouseout, onmouseout ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONMOUSEOUT_PROP);
    }    
    // Property: onmouseover
    public String getOnmouseover()
    {
        return (String) getStateHelper().eval(PropertyKeys.onmouseover);
    }
    
    public void setOnmouseover(String onmouseover)
    {
        getStateHelper().put(PropertyKeys.onmouseover, onmouseover ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONMOUSEOVER_PROP);
    }    
    // Property: onmouseup
    public String getOnmouseup()
    {
        return (String) getStateHelper().eval(PropertyKeys.onmouseup);
    }
    
    public void setOnmouseup(String onmouseup)
    {
        getStateHelper().put(PropertyKeys.onmouseup, onmouseup ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONMOUSEUP_PROP);
    }    
    // Property: onchange
    public String getOnchange()
    {
        return (String) getStateHelper().eval(PropertyKeys.onchange);
    }
    
    public void setOnchange(String onchange)
    {
        getStateHelper().put(PropertyKeys.onchange, onchange ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONCHANGE_PROP);
    }    
    // Property: onselect
    public String getOnselect()
    {
        return (String) getStateHelper().eval(PropertyKeys.onselect);
    }
    
    public void setOnselect(String onselect)
    {
        getStateHelper().put(PropertyKeys.onselect, onselect ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.ONSELECT_PROP);
    }    
    // Property: dir
    public String getDir()
    {
        return (String) getStateHelper().eval(PropertyKeys.dir);
    }
    
    public void setDir(String dir)
    {
        getStateHelper().put(PropertyKeys.dir, dir ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.DIR_PROP);
    }    
    // Property: lang
    public String getLang()
    {
        return (String) getStateHelper().eval(PropertyKeys.lang);
    }
    
    public void setLang(String lang)
    {
        getStateHelper().put(PropertyKeys.lang, lang ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.LANG_PROP);
    }    
    // Property: title
    public String getTitle()
    {
        return (String) getStateHelper().eval(PropertyKeys.title);
    }
    
    public void setTitle(String title)
    {
        getStateHelper().put(PropertyKeys.title, title ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.TITLE_PROP);
    }    
    // Property: disabled
    public boolean isDisabled()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.disabled, false);
    }
    
    public void setDisabled(boolean disabled)
    {
        getStateHelper().put(PropertyKeys.disabled, disabled ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.DISABLED_PROP);
    }    
    // Property: readonly
    public boolean isReadonly()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.readonly, false);
    }
    
    public void setReadonly(boolean readonly)
    {
        getStateHelper().put(PropertyKeys.readonly, readonly ); 
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this,
            org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.READONLY_PROP);
    }    

    public void setValueBinding(String name, jakarta.faces.el.ValueBinding binding)
    {
        super.setValueBinding(name, binding);
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this, name);
    }

    public void setValueExpression(String name, ValueExpression expression)
    {
        super.setValueExpression(name, expression);
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonPropertyConstants.markProperty(this, name);
    }

    protected enum PropertyKeys
    {
         dateBusinessConverter
        , monthYearRowClass
        , weekRowClass
        , dayCellClass
        , currentDayCellClass
        , popupLeft
        , renderAsPopup
        , addResources
        , popupButtonString
        , popupButtonStyle
        , popupButtonStyleClass
        , renderPopupButtonAsImage
        , popupDateFormat
        , popupGotoString
        , popupTodayString
        , popupTodayDateFormat
        , popupWeekString
        , popupScrollLeftMessage
        , popupScrollRightMessage
        , popupSelectMonthMessage
        , popupSelectYearMessage
        , popupSelectDateMessage
        , popupTheme
        , popupButtonImageUrl
        , helpText
        , popupSelectMode
        , enabledOnUserRole
        , visibleOnUserRole
        , javascriptLocation
        , imageLocation
        , styleLocation
        , javascriptLibrary
        , imageLibrary
        , styleLibrary
        , align
        , maxlength
        , size
        , label
        , style
        , styleClass
        , alt
        , tabindex
        , onblur
        , onfocus
        , accesskey
        , onclick
        , ondblclick
        , onkeydown
        , onkeypress
        , onkeyup
        , onmousedown
        , onmousemove
        , onmouseout
        , onmouseover
        , onmouseup
        , onchange
        , onselect
        , dir
        , lang
        , title
        , disabled
        , readonly
    }

 }
