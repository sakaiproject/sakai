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

import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;


// Generated from class org.apache.myfaces.custom.creditcardvalidator.AbstractCreditCardValidator.
//
// WARNING: This file was automatically generated. Do not edit it directly,
//          or you will lose your changes.
public class CreditCardValidator extends org.apache.myfaces.custom.creditcardvalidator.AbstractCreditCardValidator
{

    static public final String VALIDATOR_ID = 
        "org.apache.myfaces.validator.CreditCard";

    public CreditCardValidator()
    {
    }
    

    // Property: amex
    private boolean _amex;
    
    private boolean _amexSet;
    
    public boolean isAmex()
    {
        if (_amexSet)
        {
            return _amex;
        }
        ValueExpression vb = getValueExpression("amex");
        if (vb != null)
        {
            return ((Boolean) vb.getValue(getFacesContext().getELContext())).booleanValue();
        }
        return true; 
    }

    public void setAmex(boolean amex)
    {
        this._amex = amex;
        this._amexSet = true;        
    }
    // Property: discover
    private boolean _discover;
    
    private boolean _discoverSet;
    
    public boolean isDiscover()
    {
        if (_discoverSet)
        {
            return _discover;
        }
        ValueExpression vb = getValueExpression("discover");
        if (vb != null)
        {
            return ((Boolean) vb.getValue(getFacesContext().getELContext())).booleanValue();
        }
        return true; 
    }

    public void setDiscover(boolean discover)
    {
        this._discover = discover;
        this._discoverSet = true;        
    }
    // Property: mastercard
    private boolean _mastercard;
    
    private boolean _mastercardSet;
    
    public boolean isMastercard()
    {
        if (_mastercardSet)
        {
            return _mastercard;
        }
        ValueExpression vb = getValueExpression("mastercard");
        if (vb != null)
        {
            return ((Boolean) vb.getValue(getFacesContext().getELContext())).booleanValue();
        }
        return true; 
    }

    public void setMastercard(boolean mastercard)
    {
        this._mastercard = mastercard;
        this._mastercardSet = true;        
    }
    // Property: none
    private boolean _none;
    
    private boolean _noneSet;
    
    public boolean isNone()
    {
        if (_noneSet)
        {
            return _none;
        }
        ValueExpression vb = getValueExpression("none");
        if (vb != null)
        {
            return ((Boolean) vb.getValue(getFacesContext().getELContext())).booleanValue();
        }
        return false; 
    }

    public void setNone(boolean none)
    {
        this._none = none;
        this._noneSet = true;        
    }
    // Property: visa
    private boolean _visa;
    
    private boolean _visaSet;
    
    public boolean isVisa()
    {
        if (_visaSet)
        {
            return _visa;
        }
        ValueExpression vb = getValueExpression("visa");
        if (vb != null)
        {
            return ((Boolean) vb.getValue(getFacesContext().getELContext())).booleanValue();
        }
        return true; 
    }

    public void setVisa(boolean visa)
    {
        this._visa = visa;
        this._visaSet = true;        
    }

    public Object saveState(FacesContext facesContext)
    {
        Object[] values = new Object[11];
        values[0] = super.saveState(facesContext);
        values[1] = Boolean.valueOf(_amex);
        values[2] = Boolean.valueOf(_amexSet);
        values[3] = Boolean.valueOf(_discover);
        values[4] = Boolean.valueOf(_discoverSet);
        values[5] = Boolean.valueOf(_mastercard);
        values[6] = Boolean.valueOf(_mastercardSet);
        values[7] = Boolean.valueOf(_none);
        values[8] = Boolean.valueOf(_noneSet);
        values[9] = Boolean.valueOf(_visa);
        values[10] = Boolean.valueOf(_visaSet);
        return values; 
    }

    public void restoreState(FacesContext facesContext, Object state)
    {
        Object[] values = (Object[])state;
        super.restoreState(facesContext,values[0]);
        _amex = ((Boolean) values[1]).booleanValue();
        _amexSet = ((Boolean) values[2]).booleanValue();
        _discover = ((Boolean) values[3]).booleanValue();
        _discoverSet = ((Boolean) values[4]).booleanValue();
        _mastercard = ((Boolean) values[5]).booleanValue();
        _mastercardSet = ((Boolean) values[6]).booleanValue();
        _none = ((Boolean) values[7]).booleanValue();
        _noneSet = ((Boolean) values[8]).booleanValue();
        _visa = ((Boolean) values[9]).booleanValue();
        _visaSet = ((Boolean) values[10]).booleanValue();
    }
}
