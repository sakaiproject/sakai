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
package org.apache.myfaces.custom.emailvalidator;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;

import org.apache.commons.validator.GenericValidator;
import org.apache.myfaces.validator.ValidatorBase;


/**
 * A custom validator for email address format, based upons Jakarta Commons.
 * 
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * 
 * @JSFValidator
 *   name = "t:validateEmail"
 *   tagClass = "org.apache.myfaces.custom.emailvalidator.ValidateEmailTag"
 *   serialuidtag = "6041422002721046221L"
 * @since 1.1.7
 * @deprecated use myfaces commons mcv:validateEmail instead
 * @author mwessendorf (latest modification by $Author: lu4242 $)
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (Wed, 03 Sep 2008) $
 */
public class EmailValidator extends ValidatorBase {

    /**
     * <p>The standard converter id for this converter.</p>
     */
    public static final String     VALIDATOR_ID        = "org.apache.myfaces.validator.Email";
    /**
     * <p>The message identifier of the {@link FacesMessage} to be created if
     * the maximum length check fails.</p>
     */
    public static final String EMAIL_MESSAGE_ID = "org.apache.myfaces.Email.INVALID";

    public EmailValidator(){
    }

    /**
     * methode that validates an email-address.
     * it uses the commons-validator
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
            if (!GenericValidator.isEmail(value.toString().trim())) {
                Object[] args = {value.toString()};
                throw new ValidatorException(getFacesMessage(EMAIL_MESSAGE_ID, args));
            }

    }

}
