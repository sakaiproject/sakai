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
package org.apache.myfaces.custom.creditcardvalidator;

import jakarta.faces.context.FacesContext;
import jakarta.el.ValueExpression;
import jakarta.faces.validator.Validator;
import jakarta.faces.application.Application;
import jakarta.servlet.jsp.JspException;


// Generated from class org.apache.myfaces.custom.creditcardvalidator.AbstractCreditCardValidator.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class ValidateCreditCardTag
    extends org.apache.myfaces.validator.ValidatorBaseTag
{
    private static final long serialVersionUID = 3810660506302799072L; 

    public ValidateCreditCardTag()
    {    
    }    
 
    private ValueExpression _amex;
    
    public void setAmex(ValueExpression amex)
    {
        _amex = amex;
    }
 
    private ValueExpression _discover;
    
    public void setDiscover(ValueExpression discover)
    {
        _discover = discover;
    }
 
    private ValueExpression _mastercard;
    
    public void setMastercard(ValueExpression mastercard)
    {
        _mastercard = mastercard;
    }
 
    private ValueExpression _none;
    
    public void setNone(ValueExpression none)
    {
        _none = none;
    }
 
    private ValueExpression _visa;
    
    public void setVisa(ValueExpression visa)
    {
        _visa = visa;
    }

    protected Validator createValidator() throws JspException {
        String validatorId = "org.apache.myfaces.validator.CreditCard";
        Application appl = FacesContext.getCurrentInstance().getApplication();
        Validator validator = (Validator)appl.createValidator(validatorId);
        _setProperties(validator);
        return validator;
    }
    
    protected void _setProperties(Validator val) throws JspException {
        super._setProperties(val);    
        FacesContext facesContext = FacesContext.getCurrentInstance();

        org.apache.myfaces.custom.creditcardvalidator.CreditCardValidator validator = (org.apache.myfaces.custom.creditcardvalidator.CreditCardValidator) val;
        if (_amex != null)
        {
            if (!_amex.isLiteralText())
            {
                validator.setValueExpression("amex", _amex);
            }
            else
            {
                Object _amexValue = _amex.getValue(facesContext.getELContext());
                if (_amexValue != null){
                    if (_amexValue instanceof Boolean){
                        validator.setAmex(
                            ((Boolean)_amexValue).booleanValue());                        
                    }
                    else
                    {
                        validator.setAmex(
                            Boolean.valueOf(_amexValue.toString()).booleanValue());
                    }                    
                }
            }
        }
        if (_discover != null)
        {
            if (!_discover.isLiteralText())
            {
                validator.setValueExpression("discover", _discover);
            }
            else
            {
                Object _discoverValue = _discover.getValue(facesContext.getELContext());
                if (_discoverValue != null){
                    if (_discoverValue instanceof Boolean){
                        validator.setDiscover(
                            ((Boolean)_discoverValue).booleanValue());                        
                    }
                    else
                    {
                        validator.setDiscover(
                            Boolean.valueOf(_discoverValue.toString()).booleanValue());
                    }                    
                }
            }
        }
        if (_mastercard != null)
        {
            if (!_mastercard.isLiteralText())
            {
                validator.setValueExpression("mastercard", _mastercard);
            }
            else
            {
                Object _mastercardValue = _mastercard.getValue(facesContext.getELContext());
                if (_mastercardValue != null){
                    if (_mastercardValue instanceof Boolean){
                        validator.setMastercard(
                            ((Boolean)_mastercardValue).booleanValue());                        
                    }
                    else
                    {
                        validator.setMastercard(
                            Boolean.valueOf(_mastercardValue.toString()).booleanValue());
                    }                    
                }
            }
        }
        if (_none != null)
        {
            if (!_none.isLiteralText())
            {
                validator.setValueExpression("none", _none);
            }
            else
            {
                Object _noneValue = _none.getValue(facesContext.getELContext());
                if (_noneValue != null){
                    if (_noneValue instanceof Boolean){
                        validator.setNone(
                            ((Boolean)_noneValue).booleanValue());                        
                    }
                    else
                    {
                        validator.setNone(
                            Boolean.valueOf(_noneValue.toString()).booleanValue());
                    }                    
                }
            }
        }
        if (_visa != null)
        {
            if (!_visa.isLiteralText())
            {
                validator.setValueExpression("visa", _visa);
            }
            else
            {
                Object _visaValue = _visa.getValue(facesContext.getELContext());
                if (_visaValue != null){
                    if (_visaValue instanceof Boolean){
                        validator.setVisa(
                            ((Boolean)_visaValue).booleanValue());                        
                    }
                    else
                    {
                        validator.setVisa(
                            Boolean.valueOf(_visaValue.toString()).booleanValue());
                    }                    
                }
            }
        }
    }

    public void release()
    {
        super.release();
        _amex = null;
        _discover = null;
        _mastercard = null;
        _none = null;
        _visa = null;
    }
}
