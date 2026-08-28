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
package org.apache.myfaces.custom.regexprvalidator;

import jakarta.faces.context.FacesContext;
import jakarta.el.ValueExpression;
import jakarta.faces.validator.Validator;
import jakarta.faces.application.Application;
import jakarta.servlet.jsp.JspException;


// Generated from class org.apache.myfaces.custom.regexprvalidator.AbstractRegExprValidator.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class ValidateRegExprTag
    extends org.apache.myfaces.validator.ValidatorBaseTag
{
    private static final long serialVersionUID = -449945949876262076L; 

    public ValidateRegExprTag()
    {    
    }    
 
    private java.lang.String _pattern;
    
    public void setPattern(java.lang.String pattern)
    {
        _pattern = pattern;
    }

    protected Validator createValidator() throws JspException {
        String validatorId = "org.apache.myfaces.validator.RegExpr";
        Application appl = FacesContext.getCurrentInstance().getApplication();
        Validator validator = (Validator)appl.createValidator(validatorId);
        _setProperties(validator);
        return validator;
    }
    
    protected void _setProperties(Validator val) throws JspException {
        super._setProperties(val);    
        FacesContext facesContext = FacesContext.getCurrentInstance();

        org.apache.myfaces.custom.regexprvalidator.RegExprValidator validator = (org.apache.myfaces.custom.regexprvalidator.RegExprValidator) val;
        if (_pattern != null)
        {
                validator.setPattern(_pattern);
        }
    }

    public void release()
    {
        super.release();
        _pattern = null;
    }
}
