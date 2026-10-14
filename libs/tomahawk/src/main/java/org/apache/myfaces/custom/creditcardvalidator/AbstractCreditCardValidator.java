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
package org.apache.myfaces.custom.creditcardvalidator;

import org.apache.myfaces.validator.ValidatorBase;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;

/**
 * A custom validator for creditCards, based upon Jakarta Commons. 
 * 
 * Unless otherwise specified, all attributes accept static values or EL expressions
 * 
 * @JSFValidator
 *   name = "t:validateCreditCard"
 *   class = "org.apache.myfaces.custom.creditcardvalidator.CreditCardValidator"
 *   bodyContent = "empty"
 *   tagClass = "org.apache.myfaces.custom.creditcardvalidator.ValidateCreditCardTag"
 *   serialuidtag = "3810660506302799072L"
 *   
 * @since 1.1.7
 * @deprecated use myfaces commons mcv:validateCreditCard instead
 * @author mwessendorf (latest modification by $Author: lu4242 $)
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (Wed, 03 Sep 2008) $
 */
public abstract class AbstractCreditCardValidator extends ValidatorBase {

    /**
     * <p>The standard converter id for this converter.</p>
     */
    public static final String     VALIDATOR_ID        = "org.apache.myfaces.validator.CreditCard";

    /**
     * <p>The message identifier of the {@link FacesMessage} to be created if
     * the creditcard check fails.</p>
     */
    public static final String CREDITCARD_MESSAGE_ID = "org.apache.myfaces.Creditcard.INVALID";

    public AbstractCreditCardValidator(){
    }

    //Field, to init the desired Validator
    private int _initSum = 0;

    private org.apache.commons.validator.CreditCardValidator creditCardValidator = null;

    /**
     *
     */
    public void validate(
        FacesContext facesContext,
        UIComponent uiComponent,
        Object value)
        throws ValidatorException {

            if (facesContext == null) throw new NullPointerException("facesContext");
            if (uiComponent == null) throw new NullPointerException("uiComponent");

            if (value == null)
            {
                return;
            }
        initValidator();
        if (!this.creditCardValidator.isValid(value.toString())){
            Object[] args = {value.toString()};
            throw new ValidatorException(getFacesMessage(CREDITCARD_MESSAGE_ID, args));
        }
    }


    // -------------------------------------------------------- Private Methods

    /**
     * <p>initializes the desired validator.</p>
     */

    private void initValidator() {
        if(isNone()){
            //no cardtypes are allowed
            creditCardValidator = new org.apache.commons.validator.CreditCardValidator(org.apache.commons.validator.CreditCardValidator.NONE);
        }
        else{
            computeValidators();
            creditCardValidator = new org.apache.commons.validator.CreditCardValidator(_initSum);
        }
    }

    /**
     * private methode, that counts the desired creditCards
     */
    private void computeValidators(){
        if(isAmex()){
            this._initSum= org.apache.commons.validator.CreditCardValidator.AMEX + _initSum;
        }
        if(isVisa()){
            this._initSum= org.apache.commons.validator.CreditCardValidator.VISA+ _initSum;
        }
        if(isMastercard()){
            this._initSum= org.apache.commons.validator.CreditCardValidator.MASTERCARD+ _initSum;
        }
        if(isDiscover()){
            this._initSum= org.apache.commons.validator.CreditCardValidator.DISCOVER+ _initSum;
        }
    }

    //GETTER & SETTER
    
    /**
     * american express cards
     * 
     * @JSFProperty
     *   defaultValue = "true"
     */
    public abstract boolean isAmex();

    /**
     * validation for discover
     * 
     * @JSFProperty
     *   defaultValue = "true"
     */
    public abstract boolean isDiscover();

    /**
     * validation for mastercard
     * 
     * @JSFProperty
     *   defaultValue = "true"
     */
    public abstract boolean isMastercard();

    /**
     * none of the given cardtypes is allowed.
     * 
     * @JSFProperty
     *   defaultValue = "false"
     */
    public abstract boolean isNone();

    /**
     * validation for visa
     * 
     * @JSFProperty
     *   defaultValue = "true"
     */
    public abstract boolean isVisa();

    public abstract void setAmex(boolean b);

    public abstract void setDiscover(boolean b);

    public abstract void setMastercard(boolean b);

    public abstract void setNone(boolean b);

    public abstract void setVisa(boolean b);

}
