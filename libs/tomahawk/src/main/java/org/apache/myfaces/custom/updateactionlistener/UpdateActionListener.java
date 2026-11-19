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
package org.apache.myfaces.custom.updateactionlistener;

import jakarta.faces.FacesException;
import jakarta.faces.component.StateHolder;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.component.ValueHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.el.ValueBinding;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ActionListener;

/**
 * Set an arbitrary property on a managed bean when an "action" component is
 * selected by the user.
 * <p>
 * An instance of this listener type can be attached to any UIComponent which
 * is an ActionSource (eg a link or button). When the associated component
 * fires its action event, this listener will read the value specified by
 * attribute "value" and assign it to the property specified by attribute
 * "property". The value attribute may be a literal value or may be a
 * value-binding; the property is always expected to be a value-binding.
 * <p>
 * An optional Converter may be associated with this listener, and if present
 * will be invoked to convert the value to the datatype expected by the
 * target property. When no converter is available, a default one will be
 * retrieved from the Application object.
 * <p>
 * A common use for this listener is to attach it to an HtmlCommandLink
 * component, storing some constant value into a managed bean property.
 * After the navigation associated with that link is done, components in
 * the new view can look at that property to determine which link was
 * clicked.
 * <p>
 * Both the fetching of "value" and the updating of "property" occur in
 * the invoke-application phase unless "immediate" is set on the ActionSource
 * component in which case they both occur in the apply-request-values phase.
 * The update is guaranteed to occur before the invocation of the method
 * specified by attribute "action" on the ActionSource (because all
 * actionListeners are executed before the action attribute).
 * <p>
 * JSF 1.2 introduces a "setPropertyActionListener" with the same functionality like this.
 * <p>
 * @author Manfred Geiler (latest modification by $Author: grantsmith $)
 * @version $Revision: 472638 $ $Date: 2006-11-08 15:54:13 -0500 (mié, 08 nov 2006) $
 */
public class UpdateActionListener
        implements ActionListener, ValueHolder, StateHolder
{
    //private static final Log log = LogFactory.getLog(UpdateActionListener.class);

    private ValueBinding _propertyBinding;
    private Object _value;
    private ValueBinding _valueBinding;
    private Converter _converter;

    public void setPropertyBinding(ValueBinding propertyBinding)
    {
        _propertyBinding = propertyBinding;
    }

    public ValueBinding getPropertyBinding()
    {
        return _propertyBinding;
    }

    public void setValue(Object value)
    {
        _value = value;
    }

    public Object getValue()
    {
        if (_value != null) return _value;
        ValueBinding vb = getValueBinding();
        if (vb != null)
        {
            FacesContext context = FacesContext.getCurrentInstance();
            return vb.getValue(context);
        }
        return null;
    }

    public Object getLocalValue()
    {
        return _value;
    }

    public ValueBinding getValueBinding()
    {
        return _valueBinding;
    }

    public void setValueBinding(ValueBinding valueBinding)
    {
        _valueBinding = valueBinding;
    }

    public Converter getConverter()
    {
        return _converter;
    }

    public void setConverter(Converter converter)
    {
        _converter = converter;
    }

    public void processAction(ActionEvent actionEvent) throws AbortProcessingException
    {
        FacesContext context = FacesContext.getCurrentInstance();
        ValueBinding updateBinding = getPropertyBinding();
        Object v = getValue();
        if (v != null &&
            v instanceof String)
        {
            Class type = updateBinding.getType(context);
            type = (type != null) ? type : v.getClass();
            if (!type.equals(String.class) && ! type.equals(Object.class))
            {
                String converterErrorMessage = "No Converter registered with UpdateActionListener and no appropriate standard converter found. Needed to convert String to " + type.getName();
                Converter converter = getConverter();
                if (converter == null)
                {
                    try
                    {
                        converter = context.getApplication().createConverter(type);
                    }
                    catch (Exception e)
                    {
                        throw new FacesException(converterErrorMessage, e);
                    }
                }
                if (null == converter)
                {
                    throw new FacesException(converterErrorMessage);
                }
                v = converter.getAsObject(context, context.getViewRoot(), (String)v);
            }
        }
        updateBinding.setValue(context, v);
    }



    // StateHolder methods

    public Object saveState(FacesContext context)
    {
        Object values[] = new Object[4];
        values[0] = UIComponentBase.saveAttachedState(context, _propertyBinding);
        values[1] = _value;
        values[2] = UIComponentBase.saveAttachedState(context, _valueBinding);
        values[3] = UIComponentBase.saveAttachedState(context, _converter);
        return ((Object) (values));
    }

    public void restoreState(FacesContext context, Object state)
    {
        Object values[] = (Object[])state;
        _propertyBinding = (ValueBinding)UIComponentBase.restoreAttachedState(context, values[0]);
        _value = values[1];
        _valueBinding = (ValueBinding)UIComponentBase.restoreAttachedState(context, values[2]);;
        _converter = (Converter)UIComponentBase.restoreAttachedState(context, values[3]);;
    }

    public boolean isTransient()
    {
        return false;
    }

    public void setTransient(boolean newTransientValue)
    {
        if (newTransientValue == true) throw new IllegalArgumentException();
    }

}
