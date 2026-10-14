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
package org.apache.myfaces.custom.date;

import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.custom.calendar.DateBusinessConverter;
import org.apache.myfaces.shared_tomahawk.util.ClassUtils;

/**
 * <p>
 * HtmlTree tag.
 * </p>
 * @since 1.1.7
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller </a>
 * @version $Revision: 888604 $ $Date: 2004/10/13 11:50:58
 */
public abstract class AbstractHtmlInputDateTag extends 
    org.apache.myfaces.shared_tomahawk.taglib.html.HtmlInputTextTag
{
    
    private ValueExpression _dateBusinessConverter;
    
    public void setDateBusinessConverter( ValueExpression dateBusinessConverter)
    {
        _dateBusinessConverter = dateBusinessConverter;
    }

    public void release() {
        super.release();
        _dateBusinessConverter = null;
    }

    /**
     * Applies attributes to the tree component
     */
    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        FacesContext context = FacesContext.getCurrentInstance();

        org.apache.myfaces.custom.date.AbstractHtmlInputDate comp =
            (org.apache.myfaces.custom.date.AbstractHtmlInputDate) component;
        
        if (_dateBusinessConverter != null)
        {
            if (!_dateBusinessConverter.isLiteralText())
            {
                comp.setValueExpression("dateBusinessConverter", _dateBusinessConverter);
            }
            else
            {
                String s = _dateBusinessConverter.getExpressionString();
                if (s != null)
                {            
                    try
                    {
                        Class clazz = ClassUtils.classForName(s);
                        comp.setDateBusinessConverter( (DateBusinessConverter) ClassUtils.newInstance(clazz));
                    }
                    catch(ClassNotFoundException e)
                    {
                        throw new IllegalArgumentException("Class referenced in calendarConverter not found: "+_dateBusinessConverter);
                    }
                    catch(ClassCastException e)
                    {
                        throw new IllegalArgumentException("Class referenced in calendarConverter is not instance of org.apache.myfaces.custom.calendar.CalendarConverter: "+_dateBusinessConverter);
                    }
                }
            }
        }
    }
}
