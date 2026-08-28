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

import jakarta.el.ValueExpression;
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
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;
import org.apache.myfaces.shared_tomahawk.component.BindingAware;
import org.apache.myfaces.shared_tomahawk.util.RestoreStateUtils;

/**
 * The aliasBean tag allows you to create a temporary name for a real bean.
 * The temporary name exists (is visible) only to the children of the aliasBean.
 * <p>
 * One use of this feature is to pass "parameters" from an including page to an
 * included one. The included page can use any name it desires for beans it needs to
 * reference, and the including page can then use aliasBean to make those names
 * refer to the beans it wishes to "pass" as parameters.
 * </p>
 * <p>
 * Suppose you have a block of components you use often but with different beans. You
 * can create a separate JSP page (or equivalent) containing these beans, where the
 * value-bindings refer to some fictive bean name. Document these names as the required
 * "parameters" for this JSP page. Wherever you wish to use this block you then declare
 * an alias component mapping each of these "parameters" to whatever beans (or literal
 * values) you really want to apply the block to, then use jsp:include (or equivalent)
 * to include the reusable block of components.
 * </p>
 * <p>
 * Note, however, that AliasBean does not work for component bindings; JSF1.1
 * just has no mechanism available to set up the alias during the "restore view"
 * phase while the bindings of its children are being re-established, and then
 * remove the alias after the child bindings are done.
 * </p>
 * <p>
 * As a special case, if this component's direct parent is an AliasBeansScope
 * then the alias (temporary name) is active until the end of the parent
 * component, rather than the end of this component.
 * </p>
 *
 * @author Sylvain Vieujot (latest modification by $Author: lu4242 $)
 * @version $Revision: 690079 $ $Date: 2008-08-28 21:58:51 -0500 (jue, 28 ago 2008) $
 */
@JSFComponent(
        name="t:aliasBean",
        tagClass = "org.apache.myfaces.custom.aliasbean.AliasBeanTag",
        tagHandler = "org.apache.myfaces.custom.aliasbean.AliasBeanTagHandler")
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
public class AliasBean extends UIComponentBase implements BindingAware
{
    private static final Log log = LogFactory.getLog(AliasBean.class);

    public static final String COMPONENT_TYPE = "org.apache.myfaces.AliasBean";
    public static final String COMPONENT_FAMILY = "jakarta.faces.Data";

    private Alias alias;
    
    // Indicates whether withinScope has been initialised or not.
    private boolean scopeSearched = false;
    
    // True if this is a direct child of an AliasBeansScope component.
    private boolean withinScope;

    private transient FacesContext _context = null;

    public AliasBean()
    {
        alias = new Alias(this);
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }

    public String getRendererType() {
        return null;
    }

    /**
     * Define the "fictive" name which will be visible to the children
     * of this component as an alias to the "real" object specified
     * by the value attribute of this component.
     *
     * @param aliasBeanExpression
     */
    @JSFProperty
    public void setAlias(String aliasBeanExpression)
    {
        alias.setAliasBeanExpression(aliasBeanExpression);
    }

    /**
     * The existing value that the alias can be set to. This can be 
     * a literal string (like "toto") or a reference to an existing 
     * bean (like "#{myBean.member1}").
     * 
     */
    @JSFProperty(deferredValueType="java.lang.Object")
    public String getValue()
    {
        String valueExpression = alias.getValueExpression();
        if (valueExpression != null)
            return valueExpression;

        // Normally, this component will have no value, because the setValue method always
        // passes that data on to the alias instead. However it is possible for someone
        // to use f:attribute (or other mechanism?) to set the value instead. So when the
        // alias has no value, look for it there.
        ValueExpression vb = getValueExpression("value");
        return vb != null ?  (String) vb.getValue(getFacesContext().getELContext()) : null;
    }

    public void setValue(String valueExpression)
    {
        alias.setValueExpression(valueExpression);
    }

    public Object saveState(FacesContext context)
    {
        log.debug("saveState");

        _context = context;

        return new Object[]{super.saveState(context), alias.saveState()};
    }

    public void restoreState(FacesContext context, Object state)
    {
        log.debug("restoreState");

        _context = context;

        Object values[] = (Object[]) state;
        super.restoreState(context, values[0]);
        alias.restoreState(values[1]);
    }

    public Object processSaveState(FacesContext context)
    {
        if (context == null)
            throw new NullPointerException("context");
        if (isTransient())
            return null;

        makeAlias(context);

        Map facetMap = null;
        for (Iterator it = getFacets().entrySet().iterator(); it.hasNext();)
        {
            Map.Entry entry = (Map.Entry) it.next();
            if (facetMap == null)
                facetMap = new HashMap();
            UIComponent component = (UIComponent) entry.getValue();
            if (!component.isTransient())
            {
                facetMap.put(entry.getKey(), component.processSaveState(context));
            }
        }
        List childrenList = null;
        if (getChildCount() > 0)
        {
            for (Iterator it = getChildren().iterator(); it.hasNext();)
            {
                UIComponent child = (UIComponent) it.next();
                if (!child.isTransient())
                {
                    if (childrenList == null)
                        childrenList = new ArrayList(getChildCount());
                    childrenList.add(child.processSaveState(context));
                }
            }
        }

        removeAlias(context);

        return new Object[]{saveState(context), facetMap, childrenList};
    }

    public void processRestoreState(FacesContext context, Object state)
    {
        if (context == null)
            throw new NullPointerException("context");
        Object myState = ((Object[]) state)[0];

        restoreState(context, myState);
        makeAlias(context);

        Map facetMap = (Map) ((Object[]) state)[1];
        List childrenList = (List) ((Object[]) state)[2];
        for (Iterator it = getFacets().entrySet().iterator(); it.hasNext();)
        {
            Map.Entry entry = (Map.Entry) it.next();
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
        if (getChildCount() > 0)
        {
            int idx = 0;
            for (Iterator it = getChildren().iterator(); it.hasNext();)
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

        removeAlias(context);
    }

    public void processValidators(FacesContext context)
    {
        if (withinScope)
            return;

        log.debug("processValidators");
        makeAlias(context);
        super.processValidators(context);
        removeAlias(context);
    }

    public void processDecodes(FacesContext context)
    {
        log.debug("processDecodes");
        if (withinScope)
        {
            if (! alias.isActive())
                makeAlias(context);

            super.processDecodes(context);
            return;
        }

        makeAlias(context);
        super.processDecodes(context);
        removeAlias(context);
    }

    public void processUpdates(FacesContext context)
    {
        if (withinScope)
            return;

        log.debug("processUpdates");
        makeAlias(context);
        super.processUpdates(context);
        removeAlias(context);
    }


  public void encodeBegin(FacesContext context) throws IOException {
    makeAlias(context);
  }


  public void encodeEnd(FacesContext context) throws IOException {
    removeAlias();
  }

  public void queueEvent(FacesEvent event)
    {
        super.queueEvent(new FacesEventWrapper(event, this));
    }

    public void broadcast(FacesEvent event) throws AbortProcessingException
    {
        makeAlias();

        if (event instanceof FacesEventWrapper)
        {
            FacesEvent originalEvent = ((FacesEventWrapper) event).getWrappedFacesEvent();
            originalEvent.getComponent().broadcast(originalEvent);
        }
        else
        {
            super.broadcast(event);
        }

        removeAlias();
    }

    void makeAlias(FacesContext context)
    {
        _context = context;
        makeAlias();
    }

    private void makeAlias()
    {
        if (! scopeSearched)
        {
            withinScope = getParent() instanceof AliasBeansScope;
            if (withinScope)
            {
                AliasBeansScope aliasScope = (AliasBeansScope) getParent();
                aliasScope.addAlias(alias);
            }
            scopeSearched = true;
        }
        alias.make(_context);
    }

    void removeAlias(FacesContext context)
    {
        _context = context;
        removeAlias();
    }

    private void removeAlias()
    {
        if (! withinScope)
            alias.remove(_context);
    }

    @Deprecated
    public void handleBindings()
    {
        makeAlias(getFacesContext());

        RestoreStateUtils.recursivelyHandleComponentReferencesAndSetValid(getFacesContext(),this,true);

        removeAlias(getFacesContext());
    }

    @Override
    public boolean invokeOnComponent(FacesContext context, String clientId,
            ContextCallback callback) throws FacesException
    {
        makeAlias(getFacesContext());
        try
        {
            return super.invokeOnComponent(context, clientId, callback);
        }
        finally
        {
            removeAlias(getFacesContext());
        }
    }

    @Override
    public boolean visitTree(VisitContext context, VisitCallback callback)
    {
        makeAlias(getFacesContext());
        try
        {
            return super.visitTree(context, callback);
        }
        finally
        {
            removeAlias(getFacesContext());
        }
    }
}
