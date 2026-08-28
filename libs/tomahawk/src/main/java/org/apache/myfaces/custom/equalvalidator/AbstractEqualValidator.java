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
package org.apache.myfaces.custom.equalvalidator;

import jakarta.faces.FacesException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.el.ValueBinding;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.RenderKitFactory;
import jakarta.faces.render.Renderer;
import jakarta.faces.validator.ValidatorException;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFValidator;
import org.apache.myfaces.validator.ValidatorBase;


/**
 * A custom validator for validations against foreign component values. 
 * 
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * 
 * @since 1.1.7
 * @deprecated use myfaces commons mcv:validateEqual instead
 * @author mwessendorf (latest modification by $Author: lu4242 $)
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (mié, 03 sep 2008) $
 */
@JSFValidator(
   name = "t:validateEqual",
   clazz = "org.apache.myfaces.custom.equalvalidator.EqualValidator",
   tagClass = "org.apache.myfaces.custom.equalvalidator.ValidateEqualTag",
   serialuidtag = "-3249115551944863108L")
public abstract class AbstractEqualValidator extends ValidatorBase {

    /**
     * <p>The standard converter id for this converter.</p>
     */
    public static final String     VALIDATOR_ID        = "org.apache.myfaces.validator.Equal";

    /**
     * <p>The message identifier of the {@link FacesMessage} to be created if
     * the equal_for check fails.</p>
     */
    public static final String EQUAL_MESSAGE_ID = "org.apache.myfaces.Equal.INVALID";

    public AbstractEqualValidator(){
    }

  // -------------------------------------------------------- ValidatorIF
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

        String forId = getFor();
        forId = (forId != null && forId.length() > 0) ? forId : getForId();
        if (forId == null) {
            throw new FacesException("No id set to compare. Use 'for' in jsp mode and 'forId' in facelets mode");
        }
        UIComponent foreignComp = uiComponent.getParent().findComponent(forId);
        if(foreignComp==null)
            throw new FacesException("Unable to find component '" + forId + "' (calling findComponent on component '" + uiComponent.getId() + "')");
        if(false == foreignComp instanceof EditableValueHolder)
            throw new FacesException("Component '" + foreignComp.getId() + "' does not implement EditableValueHolder");
        EditableValueHolder foreignEditableValueHolder = (EditableValueHolder) foreignComp;

        if (foreignEditableValueHolder.isRequired() && foreignEditableValueHolder.getValue()== null ) {
            return;
        }
        
        Object foreignValue;
        if (foreignEditableValueHolder.isValid())
        {
            foreignValue = foreignEditableValueHolder.getValue();
        }
        else
        {
            try 
            {
                foreignValue = getConvertedValueNonValid(facesContext, foreignComp);
            }
            catch(ConverterException e)
            {
                /*
                 * If the value cannot be converted this should return,
                 * because does not have sense compare one
                 * foreign invalid value with other value.
                 * this force end the validation but do not continue
                 * with the next phases, because the converter
                 * of the foreign component fails and show a validation error.
                 */
                return;
            }
        }

        // Don't perform validation if the foreign value is null
        if (null == foreignValue)
        {
            return;
        }
        

        Object[] args = {value.toString(),(foreignValue==null) ? foreignComp.getId():foreignValue.toString()};

        if(foreignEditableValueHolder.getValue()==null || !foreignValue.toString().equals(value.toString())  )
        {
            throw new ValidatorException(getFacesMessage(EQUAL_MESSAGE_ID, args));
        }

    }
    
    // ---------------- Borrowed to convert foreign submitted values

    protected Renderer getRenderer(FacesContext context, UIComponent foreignComponent)
    {
        if (context == null) throw new NullPointerException("context");
        String rendererType = foreignComponent.getRendererType();
        if (rendererType == null) return null;
        String renderKitId = context.getViewRoot().getRenderKitId();
        RenderKitFactory rkf = (RenderKitFactory)FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit renderKit = rkf.getRenderKit(context, renderKitId);
        Renderer renderer = renderKit.getRenderer(foreignComponent.getFamily(), rendererType);
        if (renderer == null)
        {
            getFacesContext().getExternalContext().log("No Renderer found for component " + foreignComponent + " (component-family=" + foreignComponent.getFamily() + ", renderer-type=" + rendererType + ")");
        }
        return renderer;
    }

    protected Converter findUIOutputConverter(FacesContext facesContext, UIComponent component)
    {
        Converter converter = ((EditableValueHolder)component).getConverter();
        if (converter != null) return converter;

        //Try to find out by value binding
        ValueBinding vb = component.getValueBinding("value");
        if (vb == null) return null;

        Class valueType = vb.getType(facesContext);
        if (valueType == null) return null;

        if (String.class.equals(valueType)) return null;    //No converter needed for String type
        if (Object.class.equals(valueType)) return null;    //There is no converter for Object class

        try
        {
            return facesContext.getApplication().createConverter(valueType);
        }
        catch (FacesException e)
        {
            getFacesContext().getExternalContext().log("No Converter for type " + valueType.getName() + " found", e);
            return null;
        }
    }


    // --------------------- borrowed and modified from UIInput ------------

    protected Object getConvertedValueNonValid(FacesContext facesContext, UIComponent component)
        throws ConverterException
    {
        Object componentValueObject;
        Object submittedValue = ((EditableValueHolder) component).getSubmittedValue();
        if (submittedValue == null)
        {
            componentValueObject = null;
        }
        else
        {
            Renderer renderer = getRenderer(facesContext, component);
            if (renderer != null)
            {
                componentValueObject = renderer.getConvertedValue(facesContext, component, submittedValue);
            }
            else if (submittedValue instanceof String)
            {
                Converter converter = findUIOutputConverter(facesContext, component);
                if (converter != null)
                {
                    componentValueObject = converter.getAsObject(facesContext, component, (String)submittedValue);
                }
                else
                {
                    componentValueObject = submittedValue;
                }
            }else{
                componentValueObject = submittedValue;
            }
        }
        return componentValueObject;
    }
        
    // -------------------------------------------------------- GETTER & SETTER

    /**
     * the id of the foreign component, which is needed for the validation
     * 
     * In JSF 2.0 facelets mode is used to identify the components this 
     * validator should be applied to when using composite components.
     * Please use forId in that case instead.
     * 
     * @JSFProperty
     * @return the foreign component_id, on which a value should be validated
     */
    public abstract String getFor();
    
    /**
     * @param string the foreign component_id, on which a value should be validated
     */
    public abstract void setFor(String string);
    
    /**
     * the id of the foreign component, which is needed for the validation
     * 
     * @return
     */
    @JSFProperty(faceletsOnly=true)
    public abstract String getForId();
    
    /**
     * the id of the foreign component, which is needed for the validation
     * 
     * @param string
     */
    public abstract void setForId(String string);

}
