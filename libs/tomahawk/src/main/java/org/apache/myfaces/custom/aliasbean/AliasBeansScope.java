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
package org.apache.myfaces.custom.aliasbean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.faces.FacesException;
import jakarta.faces.component.ContextCallback;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.component.visit.VisitCallback;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.FacesEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFJspProperties;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFJspProperty;
import org.apache.myfaces.shared_tomahawk.component.BindingAware;
import org.apache.myfaces.shared_tomahawk.util.RestoreStateUtils;

/**
 * Holds several aliases that are configured by aliasBean tags.
 * <p>
 * The aliasBean tag must enclose all the components that are within the scope
 * of the alias. When multiple aliasas are defined, this makes the page structure
 * very clumsy; for example defining 5 aliases means the content must be nested
 * 5 indentation levels deep. This tag instead allows the content block to be
 * wrapped in just one AliasBeansScope tag, and then have AliasBean tags with
 * empty bodies added as direct children of this component. The scope of the AliasBean
 * tag still starts when the tag begins, but instead of ending when the tag ends
 * the scope of the nested AliasBean tags extends to the end of this component.
 * </p>
 * 
 * @author Sylvain Vieujot (latest modification by $Author: lu4242 $)
 * @version $Revision: 690079 $ $Date: 2008-08-28 21:58:51 -0500 (jue, 28 ago 2008) $
 */
@JSFComponent(
        name = "t:aliasBeansScope",
        tagClass = "org.apache.myfaces.custom.aliasbean.AliasBeansScopeTag",
        tagHandler = "org.apache.myfaces.custom.aliasbean.AliasBeansScopeTagHandler")
@JSFJspProperties(properties={
        @JSFJspProperty(
                name = "rendered",
                returnType = "boolean", 
                tagExcluded = true),
        @JSFJspProperty(
                name = "binding",
                returnType = "java.lang.String",
                tagExcluded = true)
                })
public class AliasBeansScope extends UIComponentBase implements BindingAware
{
    static final Log log = LogFactory.getLog(AliasBeansScope.class);

    public static final String COMPONENT_TYPE = "org.apache.myfaces.AliasBeansScope";
    public static final String COMPONENT_FAMILY = "jakarta.faces.Data";

    private ArrayList<Alias> _aliases = new ArrayList<Alias>();
    transient FacesContext _context = null;

    void addAlias(Alias alias)
    {
        _aliases.add(alias);
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }

    public String getRendererType() {
      return null;
    }

    public Object saveState(FacesContext context)
    {
        log.debug("saveState");
        _context = context;

        return super.saveState(context);
    }

    public void restoreState(FacesContext context, Object state)
    {
        log.debug("restoreState");
        _context = context;

        super.restoreState(context, state);
    }

    public Object processSaveState(FacesContext context)
    {
        if (context == null)
            throw new NullPointerException("context");
        if (isTransient())
            return null;

        makeAliases(context);

        Map<String, Object> facetMap = null;
        for (Iterator<Map.Entry<String, UIComponent>> it = getFacets().entrySet().iterator(); it.hasNext();)
        {
            Map.Entry<String, UIComponent> entry = (Map.Entry<String, UIComponent>) it.next();
            if (facetMap == null)
                facetMap = new HashMap<String, Object>();
            UIComponent component = (UIComponent) entry.getValue();
            if (!component.isTransient())
            {
                facetMap.put(entry.getKey(), component.processSaveState(context));
            }
        }

        List<Object> childrenList = null;
        if (getChildCount() > 0)
        {
            for (Iterator<UIComponent> it = getChildren().iterator(); it.hasNext();)
            {
                UIComponent child = (UIComponent) it.next();
                if (!child.isTransient())
                {
                    if (childrenList == null)
                        childrenList = new ArrayList<Object>(getChildCount());
                    childrenList.add(child.processSaveState(context));
                }
            }
        }

        removeAliases(context);

        return new Object[]{saveState(context), facetMap, childrenList};
    }

    public void processRestoreState(FacesContext context, Object state)
    {
        if (context == null)
            throw new NullPointerException("context");
        Object myState = ((Object[]) state)[0];

        restoreState(context, myState);

        makeAliases(context);

        Map<String, Object> facetMap = (Map<String, Object>) ((Object[]) state)[1];

        for (Iterator<Map.Entry<String, UIComponent>> it = getFacets().entrySet().iterator(); it.hasNext();)
        {
            Map.Entry<String, UIComponent> entry = (Map.Entry<String, UIComponent>) it.next();
            Object facetState = facetMap.get(entry.getKey());
            if (facetState != null)
            {
                ((UIComponent) entry.getValue()).processRestoreState(context, facetState);
            }
            else
            {
                context.getExternalContext().log("No state found to restore facet " + entry.getKey());
            }
        }

        List<Object> childrenList = (List<Object>) ((Object[]) state)[2];
        if (getChildCount() > 0)
        {
            int idx = 0;
            for (Iterator<UIComponent> it = getChildren().iterator(); it.hasNext();)
            {
                UIComponent child = (UIComponent) it.next();
                Object childState = childrenList.get(idx++);
                if (childState != null)
                {
                    child.processRestoreState(context, childState);
                }
                else
                {
                    context.getExternalContext().log("No state found to restore child of component " + getId());
                }
            }
        }

        removeAliases(context);
    }

    public void processValidators(FacesContext context)
    {
        log.debug("processValidators");
        makeAliases(context);
        super.processValidators(context);
        removeAliases(context);
    }

    public void processDecodes(FacesContext context)
    {
        log.debug("processDecodes");
        makeAliases(context);
        super.processDecodes(context);
        removeAliases(context);
    }

    public void processUpdates(FacesContext context)
    {
        log.debug("processUpdates");
        makeAliases(context);
        super.processUpdates(context);
        removeAliases(context);
    }

    public void encodeBegin(FacesContext context) throws IOException
    {
        log.debug("encodeBegin");
        makeAliases(context);
    }

    public void encodeEnd(FacesContext context)
    {
        log.debug("encodeEnd");
        removeAliases(context);
    }

    public void queueEvent(FacesEvent event)
    {
        super.queueEvent(new FacesEventWrapper(event, this));
    }

    public void broadcast(FacesEvent event) throws AbortProcessingException
    {
        makeAliases();

        if (event instanceof FacesEventWrapper)
        {
            FacesEvent originalEvent = ((FacesEventWrapper) event).getWrappedFacesEvent();
            originalEvent.getComponent().broadcast(originalEvent);
        }
        else
        {
            super.broadcast(event);
        }

        removeAliases();
    }

    void makeAliases(FacesContext context)
    {
        _context = context;
        makeAliases();
    }

    private void makeAliases()
    {
        for (Iterator i = _aliases.iterator(); i.hasNext();)
            ((Alias) i.next()).make(_context);
    }

    void removeAliases(FacesContext context)
    {
        _context = context;
        removeAliases();
    }

    private void removeAliases()
    {
        for (Iterator i = _aliases.iterator(); i.hasNext();)
            ((Alias) i.next()).remove(_context);
    }

    @Deprecated
    public void handleBindings()
    {
        makeAliases(getFacesContext());

        RestoreStateUtils.recursivelyHandleComponentReferencesAndSetValid(getFacesContext(), this, true);

        removeAliases(getFacesContext());
    }

    @Override
    public boolean invokeOnComponent(FacesContext context, String clientId,
            ContextCallback callback) throws FacesException
    {
        makeAliases(getFacesContext());
        try
        {
            return super.invokeOnComponent(context, clientId, callback);
        }
        finally
        {
            removeAliases(getFacesContext());
        }
    }

    @Override
    public boolean visitTree(VisitContext context, VisitCallback callback)
    {
        makeAliases(getFacesContext());
        try
        {
            return super.visitTree(context, callback);
        }
        finally
        {
            removeAliases(getFacesContext());
        }
    }
}
