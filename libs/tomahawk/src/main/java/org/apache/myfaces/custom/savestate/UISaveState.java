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
package org.apache.myfaces.custom.savestate;

import jakarta.el.ValueExpression;
import jakarta.faces.component.StateHolder;
import jakarta.faces.component.UIParameter;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;

/**
 * (DEPRECATED: use JSF 2 View Scope or Conversational Beans to store this state)
 * Provides the ability to store a model value inside the view's component tree.
 * <p>
 * JSF provides three scopes for managed beans and therefore all the model
 * objects that the managed beans reference:  request, session, application.
 * However a common requirement is a way for a model object to have a scope
 * that is tied to the duration of the current view; that is longer than the
 * request scope but shorter than session scope.
 * </p> 
 * <p>
 * This component simply holds a reference to an arbitrary object (specified
 * by the value property). Because this object is an ordinary component whose
 * scope is the current view, the reference to the model automatically has that
 * same scope.
 * </p> 
 * <p>
 * When the value is an EL expression, then after the view is restored the
 * recreated target object is stored at the specified location.
 * </p>
 * <p>
 * The object being saved must either:
 * </p>
 * <ul>
 * <li>implement java.io.Serializable, or</li>
 * <li>implement jakarta.faces.component.StateHolder and have a default
 *   constructor.</li>
 * </ul>
 * <p>
 * Note that the saved object can be "chained" from view to view
 * in order to extend its lifetime from a single view to a sequence
 * of views if desired. A UISaveState component with an EL expression
 * such as "#{someBean}" will save the object state after render, and
 * restore it on postback. If navigation occurs to some other view
 * and that view has a UISaveState component with the same EL expression
 * then the object will simply be saved into the new view, thus extending
 * its lifetime.
 * </p>
 * 
 * @deprecated use JSF 2.0 View Scope or Conversational Beans to store this state.
 * @JSFJspProperty name = "name" returnType = "java.lang.String" tagExcluded = "true"
 * @author Manfred Geiler (latest modification by $Author: skitching $)
 * @version $Revision: 705343 $ $Date: 2008-10-16 15:05:11 -0500 (jue, 16 oct 2008) $
 */
@JSFComponent(
    name = "t:saveState",
    tagClass = "org.apache.myfaces.custom.savestate.SaveStateTag")
@Deprecated
public class UISaveState extends UIParameter
{

  static public final String COMPONENT_FAMILY =
    "jakarta.faces.Parameter";
  static public final String COMPONENT_TYPE =
    "org.apache.myfaces.SaveState";

  /**
   * Construct an instance of the UISaveState.
   */
  public UISaveState()
  {
    setRendererType(null);
  }
      
    public Object saveState(FacesContext context)
    {
        Object values[] = new Object[3];
        values[0] = super.saveState(context);
        Object objectToSave = getValue();
        if (objectToSave instanceof StateHolder)
        {
            values[1] = Boolean.TRUE;
            values[2] = saveAttachedState(context, objectToSave);
        }
        else
        {
            values[1] = Boolean.FALSE;
            values[2] = objectToSave;
        }
        return values;
    }

    public void restoreState(FacesContext context, Object state)
    {
        Object values[] = (Object[])state;
        super.restoreState(context, values[0]);
        
        Object savedObject;
        Boolean storedObjectIsAStateHolder = (Boolean) values[1];
        if ( Boolean.TRUE.equals( storedObjectIsAStateHolder ) )
        {
            savedObject = restoreAttachedState(context,values[2]);
        }
        else
        {
            savedObject = values[2];
        }
        ValueExpression vb = getValueExpression("value");
        if (vb != null)
        {
            vb.setValue(context.getELContext(), savedObject);
        }
    }

  @Override
  public String getFamily()
  {
    return COMPONENT_FAMILY;
  }
}
