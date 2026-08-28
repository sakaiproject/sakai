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
package org.apache.myfaces.custom.equalvalidator;

import jakarta.faces.context.FacesContext;
import jakarta.el.ValueExpression;
import jakarta.faces.validator.Validator;
import jakarta.faces.application.Application;
import jakarta.servlet.jsp.JspException;


// Generated from class org.apache.myfaces.custom.equalvalidator.AbstractEqualValidator.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class ValidateEqualTag
    extends org.apache.myfaces.validator.ValidatorBaseTag
{
    private static final long serialVersionUID = -3249115551944863108L; 

    public ValidateEqualTag()
    {    
    }    
 
    private ValueExpression _for;
    
    public void setFor(ValueExpression forParam)
    {
        _for = forParam;
    }
 
    private ValueExpression _forId;
    
    public void setForId(ValueExpression forId)
    {
        _forId = forId;
    }

    protected Validator createValidator() throws JspException {
        String validatorId = "org.apache.myfaces.validator.Equal";
        Application appl = FacesContext.getCurrentInstance().getApplication();
        Validator validator = (Validator)appl.createValidator(validatorId);
        _setProperties(validator);
        return validator;
    }
    
    protected void _setProperties(Validator val) throws JspException {
        super._setProperties(val);    
        FacesContext facesContext = FacesContext.getCurrentInstance();

        org.apache.myfaces.custom.equalvalidator.EqualValidator validator = (org.apache.myfaces.custom.equalvalidator.EqualValidator) val;
        if (_for != null)
        {
            if (!_for.isLiteralText())
            {
                validator.setValueExpression("for", _for);
            }
            else
            {
                Object _forValue = _for.getValue(facesContext.getELContext());
                if (_forValue != null)
                {
                    validator.setFor(
                        (java.lang.String)  _forValue.toString());                
                }
            }
        }
        if (_forId != null)
        {
            if (!_forId.isLiteralText())
            {
                validator.setValueExpression("forId", _forId);
            }
            else
            {
                Object _forIdValue = _forId.getValue(facesContext.getELContext());
                if (_forIdValue != null)
                {
                    validator.setForId(
                        (java.lang.String)  _forIdValue.toString());                
                }
            }
        }
    }

    public void release()
    {
        super.release();
        _for = null;
        _forId = null;
    }
}
