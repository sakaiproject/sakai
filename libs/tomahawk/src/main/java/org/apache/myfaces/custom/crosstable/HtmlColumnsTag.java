// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
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

import jakarta.faces.component.UIComponent;
import jakarta.el.ValueExpression;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIComponent;


// Generated from class org.apache.myfaces.custom.crosstable.AbstractHtmlColumns.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class HtmlColumnsTag
    extends jakarta.faces.webapp.UIComponentELTag
{
    public HtmlColumnsTag()
    {    
    }
    
    public String getComponentType()
    {
        return "org.apache.myfaces.HtmlColumns";
    }

    public String getRendererType()
    {
        return null;
    }

    private ValueExpression _groupBy;
    
    public void setGroupBy(ValueExpression groupBy)
    {
        _groupBy = groupBy;
    }
    private ValueExpression _groupByValue;
    
    public void setGroupByValue(ValueExpression groupByValue)
    {
        _groupByValue = groupByValue;
    }
    private ValueExpression _defaultSorted;
    
    public void setDefaultSorted(ValueExpression defaultSorted)
    {
        _defaultSorted = defaultSorted;
    }
    private ValueExpression _sortable;
    
    public void setSortable(ValueExpression sortable)
    {
        _sortable = sortable;
    }
    private ValueExpression _sortPropertyName;
    
    public void setSortPropertyName(ValueExpression sortPropertyName)
    {
        _sortPropertyName = sortPropertyName;
    }
    private ValueExpression _footerdir;
    
    public void setFooterdir(ValueExpression footerdir)
    {
        _footerdir = footerdir;
    }
    private ValueExpression _footerlang;
    
    public void setFooterlang(ValueExpression footerlang)
    {
        _footerlang = footerlang;
    }
    private ValueExpression _footeronclick;
    
    public void setFooteronclick(ValueExpression footeronclick)
    {
        _footeronclick = footeronclick;
    }
    private ValueExpression _footerondblclick;
    
    public void setFooterondblclick(ValueExpression footerondblclick)
    {
        _footerondblclick = footerondblclick;
    }
    private ValueExpression _footeronkeydown;
    
    public void setFooteronkeydown(ValueExpression footeronkeydown)
    {
        _footeronkeydown = footeronkeydown;
    }
    private ValueExpression _footeronkeypress;
    
    public void setFooteronkeypress(ValueExpression footeronkeypress)
    {
        _footeronkeypress = footeronkeypress;
    }
    private ValueExpression _footeronkeyup;
    
    public void setFooteronkeyup(ValueExpression footeronkeyup)
    {
        _footeronkeyup = footeronkeyup;
    }
    private ValueExpression _footeronmousedown;
    
    public void setFooteronmousedown(ValueExpression footeronmousedown)
    {
        _footeronmousedown = footeronmousedown;
    }
    private ValueExpression _footeronmousemove;
    
    public void setFooteronmousemove(ValueExpression footeronmousemove)
    {
        _footeronmousemove = footeronmousemove;
    }
    private ValueExpression _footeronmouseout;
    
    public void setFooteronmouseout(ValueExpression footeronmouseout)
    {
        _footeronmouseout = footeronmouseout;
    }
    private ValueExpression _footeronmouseover;
    
    public void setFooteronmouseover(ValueExpression footeronmouseover)
    {
        _footeronmouseover = footeronmouseover;
    }
    private ValueExpression _footeronmouseup;
    
    public void setFooteronmouseup(ValueExpression footeronmouseup)
    {
        _footeronmouseup = footeronmouseup;
    }
    private ValueExpression _footerstyle;
    
    public void setFooterstyle(ValueExpression footerstyle)
    {
        _footerstyle = footerstyle;
    }
    private ValueExpression _footerstyleClass;
    
    public void setFooterstyleClass(ValueExpression footerstyleClass)
    {
        _footerstyleClass = footerstyleClass;
    }
    private ValueExpression _footertitle;
    
    public void setFootertitle(ValueExpression footertitle)
    {
        _footertitle = footertitle;
    }
    private ValueExpression _headerdir;
    
    public void setHeaderdir(ValueExpression headerdir)
    {
        _headerdir = headerdir;
    }
    private ValueExpression _headerlang;
    
    public void setHeaderlang(ValueExpression headerlang)
    {
        _headerlang = headerlang;
    }
    private ValueExpression _headeronclick;
    
    public void setHeaderonclick(ValueExpression headeronclick)
    {
        _headeronclick = headeronclick;
    }
    private ValueExpression _headerondblclick;
    
    public void setHeaderondblclick(ValueExpression headerondblclick)
    {
        _headerondblclick = headerondblclick;
    }
    private ValueExpression _headeronkeydown;
    
    public void setHeaderonkeydown(ValueExpression headeronkeydown)
    {
        _headeronkeydown = headeronkeydown;
    }
    private ValueExpression _headeronkeypress;
    
    public void setHeaderonkeypress(ValueExpression headeronkeypress)
    {
        _headeronkeypress = headeronkeypress;
    }
    private ValueExpression _headeronkeyup;
    
    public void setHeaderonkeyup(ValueExpression headeronkeyup)
    {
        _headeronkeyup = headeronkeyup;
    }
    private ValueExpression _headeronmousedown;
    
    public void setHeaderonmousedown(ValueExpression headeronmousedown)
    {
        _headeronmousedown = headeronmousedown;
    }
    private ValueExpression _headeronmousemove;
    
    public void setHeaderonmousemove(ValueExpression headeronmousemove)
    {
        _headeronmousemove = headeronmousemove;
    }
    private ValueExpression _headeronmouseout;
    
    public void setHeaderonmouseout(ValueExpression headeronmouseout)
    {
        _headeronmouseout = headeronmouseout;
    }
    private ValueExpression _headeronmouseover;
    
    public void setHeaderonmouseover(ValueExpression headeronmouseover)
    {
        _headeronmouseover = headeronmouseover;
    }
    private ValueExpression _headeronmouseup;
    
    public void setHeaderonmouseup(ValueExpression headeronmouseup)
    {
        _headeronmouseup = headeronmouseup;
    }
    private ValueExpression _headerstyle;
    
    public void setHeaderstyle(ValueExpression headerstyle)
    {
        _headerstyle = headerstyle;
    }
    private ValueExpression _headerstyleClass;
    
    public void setHeaderstyleClass(ValueExpression headerstyleClass)
    {
        _headerstyleClass = headerstyleClass;
    }
    private ValueExpression _headertitle;
    
    public void setHeadertitle(ValueExpression headertitle)
    {
        _headertitle = headertitle;
    }
    private ValueExpression _dir;
    
    public void setDir(ValueExpression dir)
    {
        _dir = dir;
    }
    private ValueExpression _lang;
    
    public void setLang(ValueExpression lang)
    {
        _lang = lang;
    }
    private ValueExpression _onclick;
    
    public void setOnclick(ValueExpression onclick)
    {
        _onclick = onclick;
    }
    private ValueExpression _ondblclick;
    
    public void setOndblclick(ValueExpression ondblclick)
    {
        _ondblclick = ondblclick;
    }
    private ValueExpression _onkeydown;
    
    public void setOnkeydown(ValueExpression onkeydown)
    {
        _onkeydown = onkeydown;
    }
    private ValueExpression _onkeypress;
    
    public void setOnkeypress(ValueExpression onkeypress)
    {
        _onkeypress = onkeypress;
    }
    private ValueExpression _onkeyup;
    
    public void setOnkeyup(ValueExpression onkeyup)
    {
        _onkeyup = onkeyup;
    }
    private ValueExpression _onmousedown;
    
    public void setOnmousedown(ValueExpression onmousedown)
    {
        _onmousedown = onmousedown;
    }
    private ValueExpression _onmousemove;
    
    public void setOnmousemove(ValueExpression onmousemove)
    {
        _onmousemove = onmousemove;
    }
    private ValueExpression _onmouseout;
    
    public void setOnmouseout(ValueExpression onmouseout)
    {
        _onmouseout = onmouseout;
    }
    private ValueExpression _onmouseover;
    
    public void setOnmouseover(ValueExpression onmouseover)
    {
        _onmouseover = onmouseover;
    }
    private ValueExpression _onmouseup;
    
    public void setOnmouseup(ValueExpression onmouseup)
    {
        _onmouseup = onmouseup;
    }
    private ValueExpression _style;
    
    public void setStyle(ValueExpression style)
    {
        _style = style;
    }
    private ValueExpression _styleClass;
    
    public void setStyleClass(ValueExpression styleClass)
    {
        _styleClass = styleClass;
    }
    private ValueExpression _title;
    
    public void setTitle(ValueExpression title)
    {
        _title = title;
    }
    private ValueExpression _width;
    
    public void setWidth(ValueExpression width)
    {
        _width = width;
    }
    private ValueExpression _colspan;
    
    public void setColspan(ValueExpression colspan)
    {
        _colspan = colspan;
    }
    private ValueExpression _headercolspan;
    
    public void setHeadercolspan(ValueExpression headercolspan)
    {
        _headercolspan = headercolspan;
    }
    private ValueExpression _footercolspan;
    
    public void setFootercolspan(ValueExpression footercolspan)
    {
        _footercolspan = footercolspan;
    }
    private ValueExpression _columnId;
    
    public void setColumnId(ValueExpression columnId)
    {
        _columnId = columnId;
    }
    private ValueExpression _value;
    
    public void setValue(ValueExpression value)
    {
        _value = value;
    }
    private ValueExpression _first;
    
    public void setFirst(ValueExpression first)
    {
        _first = first;
    }
    private ValueExpression _rows;
    
    public void setRows(ValueExpression rows)
    {
        _rows = rows;
    }
    private java.lang.String _var;
    
    public void setVar(java.lang.String var)
    {
        _var = var;
    }
    private String _rowStatePreserved;
    
    public void setRowStatePreserved(String rowStatePreserved)
    {
        _rowStatePreserved = rowStatePreserved;
    }

    protected void setProperties(UIComponent component)
    {
        if (!(component instanceof org.apache.myfaces.custom.crosstable.HtmlColumns))
        {
            throw new IllegalArgumentException("Component "+
                component.getClass().getName() +" is no org.apache.myfaces.custom.crosstable.HtmlColumns");
        }
        
        org.apache.myfaces.custom.crosstable.HtmlColumns comp = (org.apache.myfaces.custom.crosstable.HtmlColumns) component;
        
        super.setProperties(component);
        
        FacesContext context = getFacesContext();

        if (_groupBy != null)
        {
            comp.setValueExpression("groupBy", _groupBy);
        } 
        if (_groupByValue != null)
        {
            comp.setValueExpression("groupByValue", _groupByValue);
        } 
        if (_defaultSorted != null)
        {
            comp.setValueExpression("defaultSorted", _defaultSorted);
        } 
        if (_sortable != null)
        {
            comp.setValueExpression("sortable", _sortable);
        } 
        if (_sortPropertyName != null)
        {
            comp.setValueExpression("sortPropertyName", _sortPropertyName);
        } 
        if (_footerdir != null)
        {
            comp.setValueExpression("footerdir", _footerdir);
        } 
        if (_footerlang != null)
        {
            comp.setValueExpression("footerlang", _footerlang);
        } 
        if (_footeronclick != null)
        {
            comp.setValueExpression("footeronclick", _footeronclick);
        } 
        if (_footerondblclick != null)
        {
            comp.setValueExpression("footerondblclick", _footerondblclick);
        } 
        if (_footeronkeydown != null)
        {
            comp.setValueExpression("footeronkeydown", _footeronkeydown);
        } 
        if (_footeronkeypress != null)
        {
            comp.setValueExpression("footeronkeypress", _footeronkeypress);
        } 
        if (_footeronkeyup != null)
        {
            comp.setValueExpression("footeronkeyup", _footeronkeyup);
        } 
        if (_footeronmousedown != null)
        {
            comp.setValueExpression("footeronmousedown", _footeronmousedown);
        } 
        if (_footeronmousemove != null)
        {
            comp.setValueExpression("footeronmousemove", _footeronmousemove);
        } 
        if (_footeronmouseout != null)
        {
            comp.setValueExpression("footeronmouseout", _footeronmouseout);
        } 
        if (_footeronmouseover != null)
        {
            comp.setValueExpression("footeronmouseover", _footeronmouseover);
        } 
        if (_footeronmouseup != null)
        {
            comp.setValueExpression("footeronmouseup", _footeronmouseup);
        } 
        if (_footerstyle != null)
        {
            comp.setValueExpression("footerstyle", _footerstyle);
        } 
        if (_footerstyleClass != null)
        {
            comp.setValueExpression("footerstyleClass", _footerstyleClass);
        } 
        if (_footertitle != null)
        {
            comp.setValueExpression("footertitle", _footertitle);
        } 
        if (_headerdir != null)
        {
            comp.setValueExpression("headerdir", _headerdir);
        } 
        if (_headerlang != null)
        {
            comp.setValueExpression("headerlang", _headerlang);
        } 
        if (_headeronclick != null)
        {
            comp.setValueExpression("headeronclick", _headeronclick);
        } 
        if (_headerondblclick != null)
        {
            comp.setValueExpression("headerondblclick", _headerondblclick);
        } 
        if (_headeronkeydown != null)
        {
            comp.setValueExpression("headeronkeydown", _headeronkeydown);
        } 
        if (_headeronkeypress != null)
        {
            comp.setValueExpression("headeronkeypress", _headeronkeypress);
        } 
        if (_headeronkeyup != null)
        {
            comp.setValueExpression("headeronkeyup", _headeronkeyup);
        } 
        if (_headeronmousedown != null)
        {
            comp.setValueExpression("headeronmousedown", _headeronmousedown);
        } 
        if (_headeronmousemove != null)
        {
            comp.setValueExpression("headeronmousemove", _headeronmousemove);
        } 
        if (_headeronmouseout != null)
        {
            comp.setValueExpression("headeronmouseout", _headeronmouseout);
        } 
        if (_headeronmouseover != null)
        {
            comp.setValueExpression("headeronmouseover", _headeronmouseover);
        } 
        if (_headeronmouseup != null)
        {
            comp.setValueExpression("headeronmouseup", _headeronmouseup);
        } 
        if (_headerstyle != null)
        {
            comp.setValueExpression("headerstyle", _headerstyle);
        } 
        if (_headerstyleClass != null)
        {
            comp.setValueExpression("headerstyleClass", _headerstyleClass);
        } 
        if (_headertitle != null)
        {
            comp.setValueExpression("headertitle", _headertitle);
        } 
        if (_dir != null)
        {
            comp.setValueExpression("dir", _dir);
        } 
        if (_lang != null)
        {
            comp.setValueExpression("lang", _lang);
        } 
        if (_onclick != null)
        {
            comp.setValueExpression("onclick", _onclick);
        } 
        if (_ondblclick != null)
        {
            comp.setValueExpression("ondblclick", _ondblclick);
        } 
        if (_onkeydown != null)
        {
            comp.setValueExpression("onkeydown", _onkeydown);
        } 
        if (_onkeypress != null)
        {
            comp.setValueExpression("onkeypress", _onkeypress);
        } 
        if (_onkeyup != null)
        {
            comp.setValueExpression("onkeyup", _onkeyup);
        } 
        if (_onmousedown != null)
        {
            comp.setValueExpression("onmousedown", _onmousedown);
        } 
        if (_onmousemove != null)
        {
            comp.setValueExpression("onmousemove", _onmousemove);
        } 
        if (_onmouseout != null)
        {
            comp.setValueExpression("onmouseout", _onmouseout);
        } 
        if (_onmouseover != null)
        {
            comp.setValueExpression("onmouseover", _onmouseover);
        } 
        if (_onmouseup != null)
        {
            comp.setValueExpression("onmouseup", _onmouseup);
        } 
        if (_style != null)
        {
            comp.setValueExpression("style", _style);
        } 
        if (_styleClass != null)
        {
            comp.setValueExpression("styleClass", _styleClass);
        } 
        if (_title != null)
        {
            comp.setValueExpression("title", _title);
        } 
        if (_width != null)
        {
            comp.setValueExpression("width", _width);
        } 
        if (_colspan != null)
        {
            comp.setValueExpression("colspan", _colspan);
        } 
        if (_headercolspan != null)
        {
            comp.setValueExpression("headercolspan", _headercolspan);
        } 
        if (_footercolspan != null)
        {
            comp.setValueExpression("footercolspan", _footercolspan);
        } 
        if (_columnId != null)
        {
            comp.setValueExpression("columnId", _columnId);
        } 
        if (_value != null)
        {
            comp.setValueExpression("value", _value);
        } 
        if (_first != null)
        {
            comp.setValueExpression("first", _first);
        } 
        if (_rows != null)
        {
            comp.setValueExpression("rows", _rows);
        } 
        if (_var != null)
        {
            comp.getAttributes().put("var", _var);
        } 
        if (_rowStatePreserved != null)
        {
            comp.getAttributes().put("rowStatePreserved", Boolean.valueOf(_rowStatePreserved));
        } 
    }

    public void release()
    {
        super.release();
        _groupBy = null;
        _groupByValue = null;
        _defaultSorted = null;
        _sortable = null;
        _sortPropertyName = null;
        _footerdir = null;
        _footerlang = null;
        _footeronclick = null;
        _footerondblclick = null;
        _footeronkeydown = null;
        _footeronkeypress = null;
        _footeronkeyup = null;
        _footeronmousedown = null;
        _footeronmousemove = null;
        _footeronmouseout = null;
        _footeronmouseover = null;
        _footeronmouseup = null;
        _footerstyle = null;
        _footerstyleClass = null;
        _footertitle = null;
        _headerdir = null;
        _headerlang = null;
        _headeronclick = null;
        _headerondblclick = null;
        _headeronkeydown = null;
        _headeronkeypress = null;
        _headeronkeyup = null;
        _headeronmousedown = null;
        _headeronmousemove = null;
        _headeronmouseout = null;
        _headeronmouseover = null;
        _headeronmouseup = null;
        _headerstyle = null;
        _headerstyleClass = null;
        _headertitle = null;
        _dir = null;
        _lang = null;
        _onclick = null;
        _ondblclick = null;
        _onkeydown = null;
        _onkeypress = null;
        _onkeyup = null;
        _onmousedown = null;
        _onmousemove = null;
        _onmouseout = null;
        _onmouseover = null;
        _onmouseup = null;
        _style = null;
        _styleClass = null;
        _title = null;
        _width = null;
        _colspan = null;
        _headercolspan = null;
        _footercolspan = null;
        _columnId = null;
        _value = null;
        _first = null;
        _rows = null;
        _var = null;
        _rowStatePreserved = null;
    }
}
