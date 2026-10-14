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
package org.apache.myfaces.custom.crosstable;

import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.PartialStateHolder;
import jakarta.faces.component.StateHolder;
import org.apache.myfaces.component.AttachedDeltaWrapper;
import jakarta.faces.component.UIComponent;


// Generated from class org.apache.myfaces.custom.crosstable.AbstractHtmlColumns.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlColumns extends org.apache.myfaces.custom.crosstable.AbstractHtmlColumns
    implements org.apache.myfaces.custom.column.HtmlColumn
{

    static public final String COMPONENT_FAMILY =
        "jakarta.faces.Data";
    static public final String COMPONENT_TYPE =
        "org.apache.myfaces.HtmlColumns";


    public HtmlColumns()
    {
        setRendererType(null);
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }


    static private final java.util.Collection<String> CLIENT_EVENTS_LIST = 
        java.util.Collections.unmodifiableCollection(
            java.util.Arrays.asList(
             "footerclick"
            , "footerdblclick"
            , "footerkeydown"
            , "footerkeypress"
            , "footerkeyup"
            , "footermousedown"
            , "footermousemove"
            , "footermouseout"
            , "footermouseover"
            , "footermouseup"
            , "headerclick"
            , "headerdblclick"
            , "headerkeydown"
            , "headerkeypress"
            , "headerkeyup"
            , "headermousedown"
            , "headermousemove"
            , "headermouseout"
            , "headermouseover"
            , "headermouseup"
            , "click"
            , "dblclick"
            , "keydown"
            , "keypress"
            , "keyup"
            , "mousedown"
            , "mousemove"
            , "mouseout"
            , "mouseover"
            , "mouseup"
        ));

    public java.util.Collection<String> getEventNames()
    {
        return CLIENT_EVENTS_LIST;
    }

    @Override
    public void addClientBehavior(String eventName, jakarta.faces.component.behavior.ClientBehavior behavior)
    {
        super.addClientBehavior(eventName, behavior);
        org.apache.myfaces.shared_tomahawk.renderkit.html.CommonEventConstants.markEvent(this, eventName);
    }

    
    // Property: groupBy
    public boolean isGroupBy()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.groupBy, false);
    }
    
    public void setGroupBy(boolean groupBy)
    {
        getStateHelper().put(PropertyKeys.groupBy, groupBy ); 
    }    
    // Property: groupByValue
    public Object getGroupByValue()
    {
        return  getStateHelper().eval(PropertyKeys.groupByValue);
    }
    
    public void setGroupByValue(Object groupByValue)
    {
        getStateHelper().put(PropertyKeys.groupByValue, groupByValue ); 
    }    
    // Property: defaultSorted
    public boolean isDefaultSorted()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.defaultSorted, false);
    }
    
    public void setDefaultSorted(boolean defaultSorted)
    {
        getStateHelper().put(PropertyKeys.defaultSorted, defaultSorted ); 
    }    
    // Property: sortable
    public boolean isSortable()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.sortable, false);
    }
    
    public void setSortable(boolean sortable)
    {
        getStateHelper().put(PropertyKeys.sortable, sortable ); 
    }    
    // Property: sortPropertyName
    public String getSortPropertyName()
    {
        return (String) getStateHelper().eval(PropertyKeys.sortPropertyName);
    }
    
    public void setSortPropertyName(String sortPropertyName)
    {
        getStateHelper().put(PropertyKeys.sortPropertyName, sortPropertyName ); 
    }    
    // Property: footerdir
    public String getFooterdir()
    {
        return (String) getStateHelper().eval(PropertyKeys.footerdir);
    }
    
    public void setFooterdir(String footerdir)
    {
        getStateHelper().put(PropertyKeys.footerdir, footerdir ); 
    }    
    // Property: footerlang
    public String getFooterlang()
    {
        return (String) getStateHelper().eval(PropertyKeys.footerlang);
    }
    
    public void setFooterlang(String footerlang)
    {
        getStateHelper().put(PropertyKeys.footerlang, footerlang ); 
    }    
    // Property: footeronclick
    public String getFooteronclick()
    {
        return (String) getStateHelper().eval(PropertyKeys.footeronclick);
    }
    
    public void setFooteronclick(String footeronclick)
    {
        getStateHelper().put(PropertyKeys.footeronclick, footeronclick ); 
    }    
    // Property: footerondblclick
    public String getFooterondblclick()
    {
        return (String) getStateHelper().eval(PropertyKeys.footerondblclick);
    }
    
    public void setFooterondblclick(String footerondblclick)
    {
        getStateHelper().put(PropertyKeys.footerondblclick, footerondblclick ); 
    }    
    // Property: footeronkeydown
    public String getFooteronkeydown()
    {
        return (String) getStateHelper().eval(PropertyKeys.footeronkeydown);
    }
    
    public void setFooteronkeydown(String footeronkeydown)
    {
        getStateHelper().put(PropertyKeys.footeronkeydown, footeronkeydown ); 
    }    
    // Property: footeronkeypress
    public String getFooteronkeypress()
    {
        return (String) getStateHelper().eval(PropertyKeys.footeronkeypress);
    }
    
    public void setFooteronkeypress(String footeronkeypress)
    {
        getStateHelper().put(PropertyKeys.footeronkeypress, footeronkeypress ); 
    }    
    // Property: footeronkeyup
    public String getFooteronkeyup()
    {
        return (String) getStateHelper().eval(PropertyKeys.footeronkeyup);
    }
    
    public void setFooteronkeyup(String footeronkeyup)
    {
        getStateHelper().put(PropertyKeys.footeronkeyup, footeronkeyup ); 
    }    
    // Property: footeronmousedown
    public String getFooteronmousedown()
    {
        return (String) getStateHelper().eval(PropertyKeys.footeronmousedown);
    }
    
    public void setFooteronmousedown(String footeronmousedown)
    {
        getStateHelper().put(PropertyKeys.footeronmousedown, footeronmousedown ); 
    }    
    // Property: footeronmousemove
    public String getFooteronmousemove()
    {
        return (String) getStateHelper().eval(PropertyKeys.footeronmousemove);
    }
    
    public void setFooteronmousemove(String footeronmousemove)
    {
        getStateHelper().put(PropertyKeys.footeronmousemove, footeronmousemove ); 
    }    
    // Property: footeronmouseout
    public String getFooteronmouseout()
    {
        return (String) getStateHelper().eval(PropertyKeys.footeronmouseout);
    }
    
    public void setFooteronmouseout(String footeronmouseout)
    {
        getStateHelper().put(PropertyKeys.footeronmouseout, footeronmouseout ); 
    }    
    // Property: footeronmouseover
    public String getFooteronmouseover()
    {
        return (String) getStateHelper().eval(PropertyKeys.footeronmouseover);
    }
    
    public void setFooteronmouseover(String footeronmouseover)
    {
        getStateHelper().put(PropertyKeys.footeronmouseover, footeronmouseover ); 
    }    
    // Property: footeronmouseup
    public String getFooteronmouseup()
    {
        return (String) getStateHelper().eval(PropertyKeys.footeronmouseup);
    }
    
    public void setFooteronmouseup(String footeronmouseup)
    {
        getStateHelper().put(PropertyKeys.footeronmouseup, footeronmouseup ); 
    }    
    // Property: footerstyle
    public String getFooterstyle()
    {
        return (String) getStateHelper().eval(PropertyKeys.footerstyle);
    }
    
    public void setFooterstyle(String footerstyle)
    {
        getStateHelper().put(PropertyKeys.footerstyle, footerstyle ); 
    }    
    // Property: footerstyleClass
    public String getFooterstyleClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.footerstyleClass);
    }
    
    public void setFooterstyleClass(String footerstyleClass)
    {
        getStateHelper().put(PropertyKeys.footerstyleClass, footerstyleClass ); 
    }    
    // Property: footertitle
    public String getFootertitle()
    {
        return (String) getStateHelper().eval(PropertyKeys.footertitle);
    }
    
    public void setFootertitle(String footertitle)
    {
        getStateHelper().put(PropertyKeys.footertitle, footertitle ); 
    }    
    // Property: headerdir
    public String getHeaderdir()
    {
        return (String) getStateHelper().eval(PropertyKeys.headerdir);
    }
    
    public void setHeaderdir(String headerdir)
    {
        getStateHelper().put(PropertyKeys.headerdir, headerdir ); 
    }    
    // Property: headerlang
    public String getHeaderlang()
    {
        return (String) getStateHelper().eval(PropertyKeys.headerlang);
    }
    
    public void setHeaderlang(String headerlang)
    {
        getStateHelper().put(PropertyKeys.headerlang, headerlang ); 
    }    
    // Property: headeronclick
    public String getHeaderonclick()
    {
        return (String) getStateHelper().eval(PropertyKeys.headeronclick);
    }
    
    public void setHeaderonclick(String headeronclick)
    {
        getStateHelper().put(PropertyKeys.headeronclick, headeronclick ); 
    }    
    // Property: headerondblclick
    public String getHeaderondblclick()
    {
        return (String) getStateHelper().eval(PropertyKeys.headerondblclick);
    }
    
    public void setHeaderondblclick(String headerondblclick)
    {
        getStateHelper().put(PropertyKeys.headerondblclick, headerondblclick ); 
    }    
    // Property: headeronkeydown
    public String getHeaderonkeydown()
    {
        return (String) getStateHelper().eval(PropertyKeys.headeronkeydown);
    }
    
    public void setHeaderonkeydown(String headeronkeydown)
    {
        getStateHelper().put(PropertyKeys.headeronkeydown, headeronkeydown ); 
    }    
    // Property: headeronkeypress
    public String getHeaderonkeypress()
    {
        return (String) getStateHelper().eval(PropertyKeys.headeronkeypress);
    }
    
    public void setHeaderonkeypress(String headeronkeypress)
    {
        getStateHelper().put(PropertyKeys.headeronkeypress, headeronkeypress ); 
    }    
    // Property: headeronkeyup
    public String getHeaderonkeyup()
    {
        return (String) getStateHelper().eval(PropertyKeys.headeronkeyup);
    }
    
    public void setHeaderonkeyup(String headeronkeyup)
    {
        getStateHelper().put(PropertyKeys.headeronkeyup, headeronkeyup ); 
    }    
    // Property: headeronmousedown
    public String getHeaderonmousedown()
    {
        return (String) getStateHelper().eval(PropertyKeys.headeronmousedown);
    }
    
    public void setHeaderonmousedown(String headeronmousedown)
    {
        getStateHelper().put(PropertyKeys.headeronmousedown, headeronmousedown ); 
    }    
    // Property: headeronmousemove
    public String getHeaderonmousemove()
    {
        return (String) getStateHelper().eval(PropertyKeys.headeronmousemove);
    }
    
    public void setHeaderonmousemove(String headeronmousemove)
    {
        getStateHelper().put(PropertyKeys.headeronmousemove, headeronmousemove ); 
    }    
    // Property: headeronmouseout
    public String getHeaderonmouseout()
    {
        return (String) getStateHelper().eval(PropertyKeys.headeronmouseout);
    }
    
    public void setHeaderonmouseout(String headeronmouseout)
    {
        getStateHelper().put(PropertyKeys.headeronmouseout, headeronmouseout ); 
    }    
    // Property: headeronmouseover
    public String getHeaderonmouseover()
    {
        return (String) getStateHelper().eval(PropertyKeys.headeronmouseover);
    }
    
    public void setHeaderonmouseover(String headeronmouseover)
    {
        getStateHelper().put(PropertyKeys.headeronmouseover, headeronmouseover ); 
    }    
    // Property: headeronmouseup
    public String getHeaderonmouseup()
    {
        return (String) getStateHelper().eval(PropertyKeys.headeronmouseup);
    }
    
    public void setHeaderonmouseup(String headeronmouseup)
    {
        getStateHelper().put(PropertyKeys.headeronmouseup, headeronmouseup ); 
    }    
    // Property: headerstyle
    public String getHeaderstyle()
    {
        return (String) getStateHelper().eval(PropertyKeys.headerstyle);
    }
    
    public void setHeaderstyle(String headerstyle)
    {
        getStateHelper().put(PropertyKeys.headerstyle, headerstyle ); 
    }    
    // Property: headerstyleClass
    public String getHeaderstyleClass()
    {
        return (String) getStateHelper().eval(PropertyKeys.headerstyleClass);
    }
    
    public void setHeaderstyleClass(String headerstyleClass)
    {
        getStateHelper().put(PropertyKeys.headerstyleClass, headerstyleClass ); 
    }    
    // Property: headertitle
    public String getHeadertitle()
    {
        return (String) getStateHelper().eval(PropertyKeys.headertitle);
    }
    
    public void setHeadertitle(String headertitle)
    {
        getStateHelper().put(PropertyKeys.headertitle, headertitle ); 
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
    // Property: width
    public String getWidth()
    {
        return (String) getStateHelper().eval(PropertyKeys.width);
    }
    
    public void setWidth(String width)
    {
        getStateHelper().put(PropertyKeys.width, width ); 
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
         groupBy
        , groupByValue
        , defaultSorted
        , sortable
        , sortPropertyName
        , footerdir
        , footerlang
        , footeronclick
        , footerondblclick
        , footeronkeydown
        , footeronkeypress
        , footeronkeyup
        , footeronmousedown
        , footeronmousemove
        , footeronmouseout
        , footeronmouseover
        , footeronmouseup
        , footerstyle
        , footerstyleClass
        , footertitle
        , headerdir
        , headerlang
        , headeronclick
        , headerondblclick
        , headeronkeydown
        , headeronkeypress
        , headeronkeyup
        , headeronmousedown
        , headeronmousemove
        , headeronmouseout
        , headeronmouseover
        , headeronmouseup
        , headerstyle
        , headerstyleClass
        , headertitle
        , dir
        , lang
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
        , style
        , styleClass
        , title
        , width
    }

 }
