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
package org.apache.myfaces.component.html.ext;

import java.util.Iterator;

import jakarta.faces.FacesException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.EvaluationException;
import jakarta.faces.el.MethodBinding;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.component.AltProperty;
import org.apache.myfaces.component.DataProperties;
import org.apache.myfaces.component.DisplayValueOnlyAware;
import org.apache.myfaces.component.EscapeAware;
import org.apache.myfaces.component.ForceIdAware;
import org.apache.myfaces.component.UserRoleAware;
import org.apache.myfaces.component.UserRoleUtils;
import org.apache.myfaces.component.html.util.HtmlComponentUtils;
import org.apache.myfaces.shared_tomahawk.component.DisplayValueOnlyCapable;
import org.apache.myfaces.shared_tomahawk.component.EscapeCapable;
import org.apache.myfaces.shared_tomahawk.renderkit.JSFAttr;
import org.apache.myfaces.shared_tomahawk.util.MessageUtils;

/**
 * Implements the standard html selectOneRadio tag, with additional features. 
 * 
 * Supports user roles. Supports the "spread" layout value, which 
 * gives developer control over radio button positioning. 
 * See the "layout" attribute and the "radio" tag for further 
 * information. 
 * 
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * 
 * @JSFComponent
 *   name = "t:selectOneRadio"
 *   class = "org.apache.myfaces.component.html.ext.HtmlSelectOneRadio"
 *   tagClass = "org.apache.myfaces.generated.taglib.html.ext.HtmlSelectOneRadioTag"
 * @since 1.1.7
 * @author Manfred Geiler (latest modification by $Author: lu4242 $)
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (Wed, 03 Sep 2008) $
 */
public abstract class AbstractHtmlSelectOneRadio
        extends jakarta.faces.component.html.HtmlSelectOneRadio
        implements UserRoleAware, DisplayValueOnlyCapable,
        EscapeCapable, EscapeAware, DisplayValueOnlyAware, 
        ForceIdAware, DataProperties, AltProperty
{
    public static final String COMPONENT_TYPE = "org.apache.myfaces.HtmlSelectOneRadio";
    public static final String DEFAULT_RENDERER_TYPE ="org.apache.myfaces.Radio";

    private static Log log = LogFactory.getLog(AbstractHtmlSelectOneRadio.class);

    public String getClientId(FacesContext context)
    {
        String clientId = HtmlComponentUtils.getClientId(this, getRenderer(context), context);
        if (clientId == null)
        {
            clientId = super.getClientId(context);
        }

        return clientId;
    }

    /**
     * Overridden method, as with extended seletOne, value doesn't necessarily
     * have to be contained within select list, for example, when forceId="true" and
     * forceIdIndex="false" then component may be used in datatable.
     */
    protected void validateValue(FacesContext context, Object value)
    {
        //Is this radio button used within a datatable (forceId=true and forceIdIndex=false)
        boolean forceId = getBooleanValue(JSFAttr.FORCE_ID_ATTR,
                this.getAttributes().get(JSFAttr.FORCE_ID_ATTR), false);

//      see if the originally supplied id should be used
        boolean forceIdIndex = getBooleanValue(JSFAttr.FORCE_ID_INDEX_ATTR,
                this.getAttributes().get(JSFAttr.FORCE_ID_INDEX_ATTR), true);

        boolean dataTable = forceId && !forceIdIndex;

        if (!dataTable)
        {
            super.validateValue(context, value);
        }
        else
        {
            //Specific behavior for data tables, or other scenarios where forceId is
            //true and forceIdIndex is false

            //Check if empty
            boolean empty = value == null
                    || (value instanceof String && ((String) value).length() == 0);

            //Check required and empty
            if (isRequired() && empty)
            {
              //Only add this message once, not for every radio button in set
                String clientId = this.getClientId(context);
                Iterator messages = context.getMessages(clientId);
                boolean messageExists = messages.hasNext();

                if(!messageExists)
                {
                    //Add message
                    FacesMessage message = MessageUtils.getMessage(REQUIRED_MESSAGE_ID, new Object[]{clientId});
                    message.setSeverity(FacesMessage.SEVERITY_WARN);
                    context.addMessage(clientId, message);

                    setValid(false);
                }
                return;
            }

            //Call validators
            if (!empty)
            {
                callValidators(context, this, value);
            }
        }
    }


    private static boolean getBooleanValue(String attribute, Object value, boolean defaultValue)
    {
        if(value instanceof Boolean)
        {
            return ((Boolean) value).booleanValue();
        }
        else if(value instanceof String)
        {
            return Boolean.valueOf((String) value).booleanValue();
        }
        else if(value != null)
        {
            log.error("value for attribute "+attribute+
                    " must be instanceof 'Boolean' or 'String', is of type : "+value.getClass());

            return defaultValue;
        }

        return defaultValue;
    }

    private static void callValidators(FacesContext context, UIComponent input, Object convertedValue)
    {
        if(!(input instanceof EditableValueHolder))
            throw new FacesException("param input not of type EditableValueHolder, but of : "+input.getClass().getName());

        EditableValueHolder holder = (EditableValueHolder) input;

        Validator[] validators = holder.getValidators();
        for (int i = 0; i < validators.length; i++)
        {
            Validator validator = validators[i];
            try
            {
                validator.validate(context, input, convertedValue);
            }
            catch (ValidatorException e)
            {
                holder.setValid(false);
                FacesMessage facesMessage = e.getFacesMessage();
                if (facesMessage != null)
                {
                    facesMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
                    context.addMessage(input.getClientId(context), facesMessage);
                }
            }
        }

        MethodBinding validatorBinding = holder.getValidator();
        if (validatorBinding != null)
        {
            try
            {
                validatorBinding.invoke(context, new Object[] {context, input, convertedValue});
            }
            catch (EvaluationException e)
            {
                holder.setValid(false);
                Throwable cause = e.getCause();
                if (cause instanceof ValidatorException)
                {
                    FacesMessage facesMessage = ((ValidatorException) cause).getFacesMessage();
                    if (facesMessage != null)
                    {
                        facesMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
                        context.addMessage(input.getClientId(context), facesMessage);
                    }
                }
                else
                {
                    throw e;
                }
            }
        }
    }

    public boolean isSetDisplayValueOnly(){
        return getDisplayValueOnly() != null ? true : false;  
    }
    
    public boolean isDisplayValueOnly(){
        return getDisplayValueOnly() != null ? getDisplayValueOnly().booleanValue() : false;
    }
    
    public void setDisplayValueOnly(boolean displayValueOnly){
        this.setDisplayValueOnly((Boolean) Boolean.valueOf(displayValueOnly));
    }

    public boolean isRendered()
    {
        if (!UserRoleUtils.isVisibleOnUserRole(this)) return false;
        return super.isRendered();
    }
    
}
