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
package org.apache.myfaces.custom.datalist;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.component.ContextCallback;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.visit.VisitCallback;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.component.UserRoleAware;

/**
 * Similar to dataTable, but does not render a table. 
 * 
 * Instead the layout attribute controls how each dataRow is rendered. 
 * 
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * 
 * @JSFComponent
 *   name = "t:dataList"
 *   class = "org.apache.myfaces.custom.datalist.HtmlDataList"
 *   tagClass = "org.apache.myfaces.custom.datalist.HtmlDataListTag"
 * @since 1.1.7
 * @author Manfred Geiler (latest modification by $Author: lu4242 $)
 * @version $Revision: 940152 $ $Date: 2010-05-01 21:03:39 -0500 (Sáb, 01 May 2010) $
 */
public abstract class AbstractHtmlDataList
        extends org.apache.myfaces.component.html.ext.HtmlDataTableHack
        implements UserRoleAware
{
    public static final String COMPONENT_TYPE = "org.apache.myfaces.HtmlDataList";
    private static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.List";
        
    private static Log log = LogFactory.getLog(AbstractHtmlDataList.class);
    private static final int PROCESS_DECODES = 1;
    private static final int PROCESS_VALIDATORS = 2; // not currently in use
    private static final int PROCESS_UPDATES = 3; // not currently in use
    private static final String SKIP_ITERATION_HINT = "jakarta.faces.visit.SKIP_ITERATION";
    
    private transient FacesContext _facesContext;

    /**
     * Throws NullPointerException if context is null.  Sets row index to -1, 
     * calls processChildren, sets row index to -1.
     */

    public void processDecodes(FacesContext context)
    {

        if (context == null)
            throw new NullPointerException("context");
        if (!isRendered())
            return;

        setRowIndex(-1);
        processChildren(context, PROCESS_DECODES);
        setRowIndex(-1);
        try
        {
            decode(context);
        }
        catch (RuntimeException e)
        {
            context.renderResponse();
            throw e;
        }        
    }

    public void processUpdates(FacesContext context)
    {
        if (context == null)
            throw new NullPointerException("context");
        if (!isRendered())
            return;

        setRowIndex(-1);
        processChildren(context, PROCESS_UPDATES);
        setRowIndex(-1);
        checkUpdateModelError(context);
    }

    public void processValidators(FacesContext context)
    {
        if (context == null)
            throw new NullPointerException("context");
        if (!isRendered())
            return;

        setRowIndex(-1);
        processChildren(context, PROCESS_VALIDATORS);
        setRowIndex(-1);
        checkUpdateModelError(context);        
    }

    /**
     * Iterates over all children, processes each according to the specified 
     * process action if the child is rendered.
     */

    public void processChildren(FacesContext context, int processAction)
    {
        // use this method for processing other than decode ?
        int first = getFirst();
        int rows = getRows();
        int last = rows == 0 ? getRowCount() : first + rows;

        if (log.isTraceEnabled())
        {
            log.trace("processing " + getChildCount()
                            + " children: starting at " + first
                            + ", ending at " + last);
        }

        for (int rowIndex = first; last == -1 || rowIndex < last; rowIndex++)
        {

            setRowIndex(rowIndex);

            if (!isRowAvailable())
            {
                if (log.isTraceEnabled())
                {
                    log.trace("scrolled past the last row, aborting");
                }
                break;
            }

            for (Iterator it = getChildren().iterator(); it.hasNext();)
            {
                UIComponent child = (UIComponent) it.next();
                if (child.isRendered())
                    process(context, child, processAction);
            }
        }
    }

    /**
     * Copy and pasted from UIData in order to maintain binary compatibility.
     */

    private void process(FacesContext context, UIComponent component,
            int processAction)
    {
        switch (processAction)
        {
        case PROCESS_DECODES:
            component.processDecodes(context);
            break;
        case PROCESS_VALIDATORS:
            component.processValidators(context);
            break;
        case PROCESS_UPDATES:
            component.processUpdates(context);
            break;
        }
    }
    
    public void setRowIndex(int rowIndex)
    {
        super.setRowIndex(rowIndex);
        String rowIndexVar = getRowIndexVar();
        String rowCountVar = getRowCountVar();
        if (rowIndexVar != null || rowCountVar != null)
        {
            Map requestMap = FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
            if (rowIndex >= 0)
            {
                //regular row index, update request scope variables
                if (rowIndexVar != null)
                {
                    requestMap.put(getRowIndexVar(), new Integer(rowIndex));
                }
                if (rowCountVar != null)
                {
                    requestMap.put(getRowCountVar(), new Integer(getRowCount()));
                }
            }
            else
            {
                //rowIndex == -1 means end of loop --> remove request scope variables
                if (rowIndexVar != null)
                {
                    requestMap.remove(getRowIndexVar());
                }
                if (rowCountVar != null)
                {
                    requestMap.remove(getRowCountVar());
                }
            }
        }
    }
    
    @Override
    protected void restoreDescendantComponentStates(Object state)
    {
        restoreDescendantComponentStates(getChildren().iterator(), state, true);
    }

    @Override
    protected Object saveDescendantComponentStates()
    {
        return saveDescendantComponentStates(getChildren().iterator(), true);
    }
    
    @Override
    protected Map<String,Object> saveFullDescendantComponentStates(FacesContext facesContext)
    {
        return saveFullDescendantComponentStates(facesContext, null, getChildren().iterator(), true, getContainerClientId(facesContext));
    }
    
    @Override
    protected void restoreFullDescendantComponentStates(FacesContext facesContext, Object initialState)
    {
        restoreFullDescendantComponentStates(facesContext, getChildren().iterator(), initialState, true);
    }

    @Override
    protected void restoreFullDescendantComponentDeltaStates(FacesContext facesContext,
            Map<String, Object> rowState, Object initialState)
    {
        restoreFullDescendantComponentDeltaStates(facesContext, getChildren().iterator(), rowState, initialState, true, getContainerClientId(facesContext));
    }
    
    @Override
    protected Collection<Object[]> saveDescendantInitialComponentStates(FacesContext facesContext)
    {
        return saveDescendantInitialComponentStates(facesContext, getChildren().iterator(), true);
    }

    @Override
    public boolean invokeOnComponent(FacesContext context, String clientId, ContextCallback callback)
        throws FacesException
    {
        if (context == null || clientId == null || callback == null)
        {
            throw new NullPointerException();
        }
        
        final String baseClientId = getClientId(context);

        // searching for this component?
        boolean returnValue = baseClientId.equals(clientId);

        boolean isCachedFacesContext = isTemporalFacesContext();
        if (!isCachedFacesContext)
        {
            setTemporalFacesContext(context);
        }
        
        pushComponentToEL(context, this);
        try
        {
            if (returnValue)
            {
                try
                {
                    callback.invokeContextCallback(context, this);
                    return true;
                }
                catch (Exception e)
                {
                    throw new FacesException(e);
                }
            }
    
            // Now Look throught facets on this UIComponent
            for (Iterator<UIComponent> it = this.getFacets().values().iterator(); !returnValue && it.hasNext();)
            {
                returnValue = it.next().invokeOnComponent(context, clientId, callback);
            }
    
            if (returnValue)
            {
                return returnValue;
            }
            
            // is the component an inner component?
            if (clientId.startsWith(baseClientId))
            {
                // Check if the clientId for the component, which we 
                // are looking for, has a rowIndex attached
                char separator = UINamingContainer.getSeparatorChar(context);
                ValueExpression rowKeyVE = getValueExpression("rowKey");
                boolean rowKeyFound = false;
                
                if (rowKeyVE != null)
                {
                    int oldRow = this.getRowIndex();
                    try
                    {
                        // iterate over the rows
                        int rowsToProcess = getRows();
                        // if getRows() returns 0, all rows have to be processed
                        if (rowsToProcess == 0)
                        {
                            rowsToProcess = getRowCount();
                        }
                        int rowIndex = getFirst();
                        for (int rowsProcessed = 0; rowsProcessed < rowsToProcess; rowsProcessed++, rowIndex++)
                        {
                            setRowIndex(rowIndex);
                            if (!isRowAvailable())
                            {
                                break;
                            }
                            
                            if (clientId.startsWith(getContainerClientId(context)))
                            {
                                rowKeyFound = true;
                                break;
                            }
                        }
                        
                        if (rowKeyFound)
                        {
                            for (Iterator<UIComponent> it1 = getChildren().iterator(); 
                                    !returnValue && it1.hasNext();)
                            {
                                //recursive call to find the component
                                returnValue = it1.next().invokeOnComponent(context, clientId, callback);
                            }
                        }
                    }
                    finally
                    {
                        this.setRowIndex(oldRow);
                    }
                }
                //If the char next to baseClientId is the separator one and
                //the subId matches the regular expression
                if (rowKeyVE == null && clientId.matches(baseClientId + separator+"[0-9]+"+separator+".*"))
                {
                    String subId = clientId.substring(baseClientId.length() + 1);
                    String clientRow = subId.substring(0, subId.indexOf(separator));
        
                    //Now we save the current position
                    int oldRow = this.getRowIndex();
                    
                    // try-finally --> make sure, that the old row index is restored
                    try
                    {
                        //The conversion is safe, because its already checked on the
                        //regular expresion
                        this.setRowIndex(Integer.parseInt(clientRow));
                        
                        // check, if the row is available
                        if (!isRowAvailable())
                        {
                            return false;
                        }
            
                        for (Iterator<UIComponent> it1 = getChildren().iterator(); 
                                !returnValue && it1.hasNext();)
                        {
                            //recursive call to find the component
                            returnValue = it1.next().invokeOnComponent(context, clientId, callback);
                        }
                    }
                    finally
                    {
                        //Restore the old position. Doing this prevent
                        //side effects.
                        this.setRowIndex(oldRow);
                    }
                }
            }
        }
        finally
        {
            //all components must call popComponentFromEl after visiting is finished
            popComponentFromEL(context);
            if (!isCachedFacesContext)
            {
                setTemporalFacesContext(null);
            }
        }

        return returnValue;
    }
    
    @Override
    public boolean visitTree(VisitContext context, VisitCallback callback)
    {
        if (!isVisitable(context))
        {
            return false;
        }

        boolean isTemporalFacesContext = isTemporalFacesContext();
        if (!isTemporalFacesContext)
        {
            setTemporalFacesContext(context.getFacesContext());
        }
        // save the current row index
        int oldRowIndex = getRowIndex();
        // set row index to -1 to process the facets and to get the rowless clientId
        setRowIndex(-1);
        // push the Component to EL
        pushComponentToEL(context.getFacesContext(), this);
        try
        {
            VisitResult visitResult = context.invokeVisitCallback(this,
                    callback);
            switch (visitResult)
            {
            //we are done nothing has to be processed anymore
            case COMPLETE:
                return true;

            case REJECT:
                return false;

                //accept
            default:
                // determine if we need to visit our children 
                Collection<String> subtreeIdsToVisit = context
                        .getSubtreeIdsToVisit(this);
                boolean doVisitChildren = subtreeIdsToVisit != null
                        && !subtreeIdsToVisit.isEmpty();
                if (doVisitChildren)
                {
                    // visit the facets of the component
                    for (UIComponent facet : getFacets().values())
                    {
                        if (facet.visitTree(context, callback))
                        {
                            return true;
                        }
                    }
                    Boolean skipIterationHint = (Boolean) context.getFacesContext().getAttributes().get(SKIP_ITERATION_HINT);
                    if (skipIterationHint != null && skipIterationHint.booleanValue())
                    {
                        // If SKIP_ITERATION is enabled, do not take into account rows.
                        if (getChildCount() > 0) {
                            for (UIComponent child : getChildren()) {
                                if (child.visitTree(context, callback)) {
                                    return true;
                                }
                            }
                        }
                    }
                    else
                    {
                        // iterate over the rows
                        int rowsToProcess = getRows();
                        // if getRows() returns 0, all rows have to be processed
                        if (rowsToProcess == 0)
                        {
                            rowsToProcess = getRowCount();
                        }
                        int rowIndex = getFirst();
                        for (int rowsProcessed = 0; rowsProcessed < rowsToProcess; rowsProcessed++, rowIndex++)
                        {
                            setRowIndex(rowIndex);
                            if (!isRowAvailable())
                            {
                                return false;
                            }
                            // visit the children of every child of the UIData that is an instance of UIColumn
                            for (UIComponent child : getChildren())
                            {
                                if (child.visitTree(context, callback))
                                {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        finally
        {
            // pop the component from EL and restore the old row index
            popComponentFromEL(context.getFacesContext());
            setRowIndex(oldRowIndex);
            if (!isTemporalFacesContext)
            {
                setTemporalFacesContext(null);
            }
        }

        // Return false to allow the visiting to continue
        return false;
    }    
    
    @Override
    protected FacesContext getFacesContext()
    {
        if (_facesContext == null)
        {
            return super.getFacesContext();
        }
        else
        {
            return _facesContext;
        }
    }
    
    private boolean isTemporalFacesContext()
    {
        return _facesContext != null;
    }
    
    private void setTemporalFacesContext(FacesContext facesContext)
    {
        _facesContext = facesContext;
    }

    /**
     * A parameter name, under which the rowCount is set in request 
     * scope similar to the var parameter.
     * 
     * @JSFProperty
     */
    public abstract String getRowCountVar();
    
    /**
     *  A parameter name, under which the current rowIndex is set in 
     *  request scope similar to the var parameter.
     * 
     * @JSFProperty
     */
    public abstract String getRowIndexVar();

    /**
     * simple|unorderedList|orderedList
     * <ul>
     *   <li>simple = for each dataRow all children are simply rendered</li>
     *   <li>unorderedList = the list is rendered as HTML unordered list (= bullet list)</li>
     *   <li>orderedList = the list is rendered as HTML ordered list</li>
     * </ul>
     * Default: simple
     * 
     * @JSFProperty
     */
    public abstract String getLayout();
    
    /**
     * CSS class to be applied to individual items in the list
     * 
     * @JSFProperty
     */
    public abstract String getItemStyleClass();
    
    /**
     * OnClick handler to be applied to individual items in the list
     * 
     * @JSFProperty
     */
    public abstract String getItemOnClick();
}
