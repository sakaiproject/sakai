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
package org.apache.myfaces.custom.regexprvalidator;

import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;


// Generated from class org.apache.myfaces.custom.regexprvalidator.AbstractRegExprValidator.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class RegExprValidator extends org.apache.myfaces.custom.regexprvalidator.AbstractRegExprValidator
{

    static public final String VALIDATOR_ID = 
        "org.apache.myfaces.validator.RegExpr";

    public RegExprValidator()
    {
    }
    

    // Property: pattern
    private String _pattern ;
     
    public String getPattern()
    {
        return _pattern;
    }

    public void setPattern(String pattern)
    {
        this._pattern = pattern;
    }

    public Object saveState(FacesContext facesContext)
    {
        Object[] values = new Object[2];
        values[0] = super.saveState(facesContext);
        values[1] = _pattern;
        return values; 
    }

    public void restoreState(FacesContext facesContext, Object state)
    {
        Object[] values = (Object[])state;
        super.restoreState(facesContext,values[0]);
        _pattern = (java.lang.String) values[1];
    }
}
