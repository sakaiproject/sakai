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
package org.apache.myfaces.custom.calendar;

import java.util.Date;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.ValueBinding;


/**
 * 
 * @since 1.1.10
 * @author Leonardo Uribe (latest modification by $Author: lu4242 $)
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (mié, 03 sep 2008) $
 */
public class DefaultDateBusinessConverter implements DateBusinessConverter
{

    public Object getBusinessValue(FacesContext context, UIComponent component,
            Date value)
    {
        ValueBinding vb = component.getValueBinding("value");
        if (vb != null)
        {
            Class type = vb.getType(context); 
            if (type != null)
            {
                if (java.sql.Date.class.isAssignableFrom(type))
                {
                    return new java.sql.Date(value.getTime());
                }
            }
        }
        return value;
    }

    public Date getDateValue(FacesContext context, UIComponent component,
            Object value)
    {
        if (value instanceof java.sql.Date)
        {
            //Convert to strict java.util.Date
            return new Date(((java.sql.Date)value).getTime());
        }
        return (Date) value;
    }
}
