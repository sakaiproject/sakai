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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIData;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.el.MethodBinding;
import jakarta.faces.el.ValueBinding;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.FacesEvent;
import jakarta.faces.event.FacesListener;
import jakarta.faces.event.ValueChangeListener;
import jakarta.faces.validator.Validator;

import org.apache.myfaces.shared_tomahawk.component.ExecuteOnCallback;

/**
 * @author Martin Marinschek (latest modification by $Author: mmarinschek $)
 * @version $Revision: 371487 $ $Date: 2006-01-23 09:16:20 +0100 (Mo, 23 Jän 2006) $
 */
public class UIComponentPerspective extends UIInput
{
    private boolean enableSetupPerspective = false;

    private UIData uiData;
    private UIComponent delegate;
    private int rowIndex;
    private int oldRowIndex;

    public UIComponentPerspective(UIData uiData, UIComponent delegate, int rowIndex)
    {
        super();

        this.enableSetupPerspective = true;
        this.uiData = uiData;
        this.rowIndex = rowIndex;
        this.delegate = delegate;
    }

    public UIData getUiData()
    {
        return uiData;
    }

    public void setUiData(UIData uiData)
    {
        this.uiData = uiData;
    }

    public int getRowIndex()
    {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex)
    {
        this.rowIndex = rowIndex;
    }

    public Object executeOn(FacesContext context, ExecuteOnCallback callback)
    {
        setupPerspective();

        Object retVal;

        if(delegate instanceof UIComponentPerspective)
        {
            retVal = ((UIComponentPerspective) delegate).executeOn(context, callback);
        }
        else
        {
            retVal = callback.execute(context, delegate);
        }
        teardownPerspective();
        return retVal;
    }

    private UIComponent innerGetDelegate()
    {
        if(delegate==null)
        {
            delegate = new UIInput();
        }

        return delegate;
    }

    private UIInput innerGetDelegateInput()
    {
        return (UIInput) delegate;
    }

    public Map getAttributes()
    {
        setupPerspective();
        Map retVal = innerGetDelegate().getAttributes();
        teardownPerspective();
        return retVal;
    }

    protected void teardownPerspective()
    {
        if(enableSetupPerspective)
        {
            uiData.setRowIndex(oldRowIndex);
        }
    }

    protected void setupPerspective()
    {
        if(enableSetupPerspective)
        {
            oldRowIndex = uiData.getRowIndex();
            uiData.setRowIndex(rowIndex);
        }
    }

    public ValueBinding getValueBinding(String name)
    {
        setupPerspective();
        ValueBinding retVal = innerGetDelegate().getValueBinding(name);
        teardownPerspective();
        return retVal;
    }

    public void setValueBinding(String name, ValueBinding binding)
    {
        setupPerspective();
        innerGetDelegate().setValueBinding(name, binding);
        teardownPerspective();
    }

    public String getClientId(FacesContext context)
    {
        setupPerspective();
        String retVal = innerGetDelegate().getClientId(context);
        teardownPerspective();
        return retVal;
    }

    public String getFamily()
    {
        setupPerspective();
        String retVal = innerGetDelegate().getFamily();
        teardownPerspective();
        return retVal;
    }

    public String getId()
    {
        setupPerspective();
        String retVal = innerGetDelegate().getId();
        teardownPerspective();
        return retVal;
    }

    public void setId(String id)
    {
        throw new UnsupportedOperationException("you are not allowed to set the id through the perspective.");
    }

    public UIComponent getParent()
    {
        setupPerspective();
        UIComponent retVal = innerGetDelegate().getParent();
        teardownPerspective();
        return retVal;
    }

    public void setParent(UIComponent parent)
    {
        throw new UnsupportedOperationException("you are not allowed to set the parent through the perspective.");
    }

    public boolean isRendered()
    {
        setupPerspective();
        boolean retVal = innerGetDelegate().isRendered();
        teardownPerspective();
        return retVal;
    }

    public void setRendered(boolean rendered)
    {
        setupPerspective();
        innerGetDelegate().setRendered(rendered);
        teardownPerspective();
    }

    public String getRendererType()
    {
        setupPerspective();
        String rendererType = innerGetDelegate().getRendererType();
        teardownPerspective();
        return rendererType;
    }

    public void setRendererType(String rendererType)
    {
        setupPerspective();
        innerGetDelegate().setRendererType(rendererType);
        teardownPerspective();
    }

    public boolean getRendersChildren()
    {
        setupPerspective();
        boolean retVal = innerGetDelegate().getRendersChildren();
        teardownPerspective();
        return retVal;
    }

    public List getChildren()
    {
        setupPerspective();
        List retVal = innerGetDelegate().getChildren();
        teardownPerspective();
        return retVal;
    }

    public int getChildCount()
    {
        setupPerspective();
        int retVal = innerGetDelegate().getChildCount();
        teardownPerspective();
        return retVal;
    }

    public UIComponent findComponent(String expr)
    {
        throw new UnsupportedOperationException("you cannot find components via perspectives.");
    }

    public Map getFacets()
    {
        throw new UnsupportedOperationException("you cannot get facets via perspectives.");
    }

    public UIComponent getFacet(String name)
    {
        throw new UnsupportedOperationException("you cannot get a facet via perspectives.");
    }

    public Iterator getFacetsAndChildren()
    {
        throw new UnsupportedOperationException("you cannot find components via perspectives.");
    }

    public void broadcast(FacesEvent event) throws AbortProcessingException
    {
        throw new UnsupportedOperationException("you cannot find broadcast via perspectives.");
    }

    public void decode(FacesContext context)
    {
        setupPerspective();
        innerGetDelegate().decode(context);
        teardownPerspective();
    }

    public void encodeBegin(FacesContext context) throws IOException
    {
        setupPerspective();
        innerGetDelegate().encodeBegin(context);
        teardownPerspective();
    }

    public void encodeChildren(FacesContext context) throws IOException
    {
        setupPerspective();
        innerGetDelegate().encodeChildren(context);
        teardownPerspective();
    }

    public void encodeEnd(FacesContext context) throws IOException
    {
        setupPerspective();
        innerGetDelegate().encodeEnd(context);
        teardownPerspective();
    }

    protected void addFacesListener(FacesListener listener)
    {
        throw new UnsupportedOperationException("you cannot add faces listener via perspectives.");
    }

    protected FacesListener[] getFacesListeners(Class clazz)
    {
        throw new UnsupportedOperationException("you cannot get faces listeners via perspectives.");
    }

    protected void removeFacesListener(FacesListener listener)
    {
        throw new UnsupportedOperationException("you cannot remove faces listener via perspectives.");
    }

    public void queueEvent(FacesEvent event)
    {
        throw new UnsupportedOperationException("you cannot queue events via perspectives.");
    }

    public void processRestoreState(FacesContext context, Object state)
    {
        setupPerspective();
        innerGetDelegate().processRestoreState(context, state);
        teardownPerspective();
    }

    public void processDecodes(FacesContext context)
    {
        setupPerspective();
        innerGetDelegate().processDecodes(context);
        teardownPerspective();
    }

    public void processValidators(FacesContext context)
    {
        setupPerspective();
        innerGetDelegate().processValidators(context);
        teardownPerspective();
    }

    public void processUpdates(FacesContext context)
    {
        setupPerspective();
        innerGetDelegate().processUpdates(context);
        teardownPerspective();
    }

    public Object processSaveState(FacesContext context)
    {
        setupPerspective();
        Object retVal=innerGetDelegate().processSaveState(context);
        teardownPerspective();
        return retVal;
    }


    public Object saveState(FacesContext context)
    {
        setupPerspective();
        Object retVal=innerGetDelegate().saveState(context);
        teardownPerspective();
        return retVal;
    }

    public void restoreState(FacesContext context, Object state)
    {
        setupPerspective();
        innerGetDelegate().restoreState(context,state);
        teardownPerspective();
    }

    public boolean isTransient()
    {
        setupPerspective();
        boolean retVal=innerGetDelegate().isTransient();
        teardownPerspective();
        return retVal;
    }

    public void setTransient(boolean newTransientValue)
    {
        setupPerspective();
        innerGetDelegate().setTransient(newTransientValue);
        teardownPerspective();
    }

    // use javadoc inherited from EditableValueHolder
    public Object getSubmittedValue()
    {
        setupPerspective();
        Object retValue = innerGetDelegateInput().getSubmittedValue();
        teardownPerspective();
        return retValue;
    }

    // use javadoc inherited from EditableValueHolder
    public void setSubmittedValue(Object submittedValue)
    {
        setupPerspective();
        innerGetDelegateInput().setSubmittedValue(submittedValue);
        teardownPerspective();
    }

    public void setValue(Object value)
    {
        setupPerspective();
        innerGetDelegateInput().setValue(value);
        teardownPerspective();
    }

    // use javadoc inherited from EditableValueHolder
    public boolean isLocalValueSet()
    {
        setupPerspective();
        boolean retVal = innerGetDelegateInput().isLocalValueSet();
        teardownPerspective();
        return retVal;
    }

    // use javadoc inherited from EditableValueHolder
    public void setLocalValueSet(boolean localValueSet)
    {
        setupPerspective();
        innerGetDelegateInput().setLocalValueSet(localValueSet);
        teardownPerspective();
    }

    // use javadoc inherited from EditableValueHolder
    public boolean isValid()
    {
        setupPerspective();
        boolean retValue=innerGetDelegateInput().isValid();
        teardownPerspective();
        return retValue;
    }

    // use javadoc inherited from EditableValueHolder
    public void setValid(boolean valid)
    {
        setupPerspective();
        innerGetDelegateInput().setValid(valid);
        teardownPerspective();
    }

    // use javadoc inherited from EditableValueHolder
    public MethodBinding getValidator()
    {
        setupPerspective();
        MethodBinding validator = innerGetDelegateInput().getValidator();
        teardownPerspective();
        return validator;
    }

    // use javadoc inherited from EditableValueHolder
    public void setValidator(MethodBinding validator)
    {
        setupPerspective();
        innerGetDelegateInput().setValidator(validator);
        teardownPerspective();
    }

    // use javadoc inherited from EditableValueHolder
    public MethodBinding getValueChangeListener()
    {
        setupPerspective();
        MethodBinding retValue = innerGetDelegateInput().getValueChangeListener();
        teardownPerspective();
        return retValue;
    }

    // use javadoc inherited from EditableValueHolder
    public void setValueChangeListener(MethodBinding valueChangeListener)
    {
        setupPerspective();
        innerGetDelegateInput().setValueChangeListener(valueChangeListener);
        teardownPerspective();
    }

    public void updateModel(FacesContext context)
    {
        setupPerspective();
        innerGetDelegateInput().updateModel(context);
        teardownPerspective();
    }

    public void validate(FacesContext context)
    {
        setupPerspective();
        innerGetDelegateInput().validate(context);
        teardownPerspective();
    }


    public void addValidator(Validator validator)
    {
        setupPerspective();
        innerGetDelegateInput().addValidator(validator);
        teardownPerspective();
    }

    public Validator[] getValidators()
    {
        setupPerspective();
        Validator[] retValue = innerGetDelegateInput().getValidators();
        teardownPerspective();
        return retValue;
    }

    public void removeValidator(Validator validator)
    {
        setupPerspective();
        innerGetDelegateInput().removeValidator(validator);
        teardownPerspective();
    }

    public void addValueChangeListener(ValueChangeListener listener)
    {
        setupPerspective();
        innerGetDelegateInput().addValueChangeListener(listener);
        teardownPerspective();
    }

    public ValueChangeListener[] getValueChangeListeners()
    {
        setupPerspective();
        ValueChangeListener[] listeners = innerGetDelegateInput().getValueChangeListeners();
        teardownPerspective();
        return listeners;
    }

    public void removeValueChangeListener(ValueChangeListener listener)
    {
        setupPerspective();
        innerGetDelegateInput().removeValueChangeListener(listener);
        teardownPerspective();
    }

    public void setImmediate(boolean immediate)
    {
        setupPerspective();
        innerGetDelegateInput().setImmediate(immediate);
        teardownPerspective();
    }

    public boolean isImmediate()
    {
        setupPerspective();
        boolean retValue = innerGetDelegateInput().isImmediate();
        teardownPerspective();
        return retValue;
    }

    public void setRequired(boolean required)
    {
        setupPerspective();
        innerGetDelegateInput().setRequired(required);
        teardownPerspective();
    }

    public boolean isRequired()
    {
        setupPerspective();
        boolean retValue = innerGetDelegateInput().isRequired();
        teardownPerspective();
        return retValue;
    }

    public Object getLocalValue()
    {
        setupPerspective();
        Object retValue = innerGetDelegateInput().getLocalValue();
        teardownPerspective();
        return retValue;
    }

    public void setConverter(Converter converter)
    {
        setupPerspective();
        innerGetDelegateInput().setConverter(converter);
        teardownPerspective();
    }

    public Converter getConverter()
    {
        setupPerspective();
        Converter retValue = innerGetDelegateInput().getConverter();
        teardownPerspective();
        return retValue;
    }

    public Object getValue()
    {
        setupPerspective();
        Object retValue = innerGetDelegateInput().getValue();
        teardownPerspective();
        return retValue;
    }
}
