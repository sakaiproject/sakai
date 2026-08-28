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
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.el.ValueExpression;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.ValueBinding;
import jakarta.faces.model.ArrayDataModel;
import jakarta.faces.model.DataModel;
import jakarta.faces.model.ListDataModel;
import jakarta.faces.model.ResultDataModel;
import jakarta.faces.model.ResultSetDataModel;
import jakarta.faces.model.ScalarDataModel;
import jakarta.servlet.jsp.jstl.sql.Result;

import org.apache.commons.lang.StringUtils;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;
import org.apache.myfaces.component.html.util.HtmlComponentUtils;
import org.apache.myfaces.custom.ExtendedComponentBase;

/**
 * Reimplement all UIData functionality to be able to have (protected) access
 * the internal DataModel.
 *
 * @JSFComponent
 *  configExcluded = "true"
 *
 * @author Manfred Geiler (latest modification by $Author: lu4242 $)
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (mié, 03 sep 2008) $
 */
public abstract class HtmlDataTableHack extends
                jakarta.faces.component.html.HtmlDataTable implements
                ExtendedComponentBase
                
{
    @SuppressWarnings("unchecked")
    private Map<String, DataModel> _dataModelMap = new HashMap<String, DataModel>();

    // will be set to false if the data should not be refreshed at the beginning of the encode phase
    private boolean _isValidChilds = true;

    // holds for each row the states of the child components of this UIData
    private Map<String, Object> _rowStates = new HashMap<String, Object>();
    
    // holds delta state information
    private Map<String, Map<String, Object> > _rowDeltaStates = new HashMap<String, Map<String, Object> >();
    private Map<String, Map<String, Object> > _rowTransientStates = new HashMap<String, Map<String, Object> >();    

    // contains the initial row state which is used to initialize each row
    private Object _initialDescendantComponentState = null;

    private Object _initialDescendantFullComponentState = null;
    
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // Every field and method from here is identical to UIData !!!!!!!!!
    // EXCEPTION: we can create a DataModel from a Collection

    @SuppressWarnings("unchecked")
    private static final Class OBJECT_ARRAY_CLASS = (new Object[0]).getClass();

    private static final boolean DEFAULT_PRESERVEROWSTATES = false;
    
    private static final String UNIQUE_ROW_ID_PREFIX = "r_id_";

    private int _rowIndex = -1;
    
    // BEGIN Reset mode saveState
    /**
     * Normal saveState behavior
     */
    static final int RESET_MODE_OFF = 0;
    
    /**
     * Clear all transient values like _dataModelMap,_rowTransientStates and 
     * other variables used in the lifecycle (isValidChildren or cachedFacesContext 
     * for example)
     */
    static final int RESET_MODE_SOFT = 1;
    /**
     * Clear all transient values and state set after markInitialState() call,
     * like _rowDeltaStates
     */
    static final int RESET_MODE_HARD = 2;
    
    static final String RESET_SAVE_STATE_MODE_KEY = 
            "oam.view.resetSaveStateMode";    
    // END Reset mode saveState

    public boolean isRowAvailable()
    {
        return getDataModel().isRowAvailable();
    }

    public int getRowCount()
    {
        return getDataModel().getRowCount();
    }

    public Object getRowData()
    {
        return getDataModel().getRowData();
    }

    public int getRowIndex()
    {
        return _rowIndex;
    }
    
    /**
     * Hack since RI does not call getRowIndex()
     */
    public String getClientId(FacesContext context)
    {
        // check for forceId
        String clientId = HtmlComponentUtils.getClientId(this, getRenderer(context), context);
        if (clientId == null)
        {
            clientId = super.getClientId(context);
            // remove index if necesssary 
            int rowIndex = getRowIndex();
            if (rowIndex != -1)
            {
                char separator = UINamingContainer.getSeparatorChar(context);
                int index = clientId.lastIndexOf(separator);
                if(index != -1)
                {
                    String rowIndexString = clientId.substring(index + 1);
                    if (rowIndexString.length() > 0 && 
                        StringUtils.isNumeric(rowIndexString) &&
                        rowIndex == Integer.valueOf(rowIndexString).intValue())
                    {
                        clientId = clientId.substring(0,index - 1);
                    }
                }
            }
        }
        return clientId;
    }
    
    @Override
    public String getContainerClientId(FacesContext context)
    {
        // check for forceId
        String clientId = HtmlComponentUtils.getClientId(this, getRenderer(context), context);
        if (clientId == null)
        {
            clientId = super.getClientId(context);
        }
        int rowIndex = getRowIndex();
        if (rowIndex == -1)
        {
            return clientId;
        }
        // the following code tries to avoid rowindex to be twice in the client id
        char separator = UINamingContainer.getSeparatorChar(context);
        int index = clientId.lastIndexOf(separator);
        if(index != -1)
        {
            String rowIndexString = clientId.substring(index + 1);
            
            if(rowIndexString.length() > 0 && 
               StringUtils.isNumeric(rowIndexString) &&
               Integer.valueOf(rowIndexString) == rowIndex)
            {
                //return clientId;
                return clientId.substring(0,index+1)+getDerivedSubClientId();
            }
        }
        //return clientId + separator + rowIndex;
        return clientId + separator + getDerivedSubClientId();
    }

    /**
     * @see jakarta.faces.component.UIData#processUpdates(jakarta.faces.context.FacesContext)
     */
    public void processUpdates(FacesContext context)
    {
        super.processUpdates(context);
        // check if a update model error forces the render response for our data
        if (context.getRenderResponse())
        {
            _isValidChilds = false;
        }
    }
    
    /**
     * This method is used when a custom processUpdates and processValidators
     * is defined, to check if a update model error forces the render 
     * response for our data, because _isValidChilds is a private field
     * and is not available on child components that inherits this 
     * component class like t:dataList. (TOMAHAWK-1225)
     */
    protected void checkUpdateModelError(FacesContext context)
    {
        if (context.getRenderResponse())
        {
            _isValidChilds = false;
        }        
    }

    /**
     * @see jakarta.faces.component.UIData#processValidators(jakarta.faces.context.FacesContext)
     */
    public void processValidators(FacesContext context)
    {
        super.processValidators(context);
        // check if a validation error forces the render response for our data
        if (context.getRenderResponse())
        {
            _isValidChilds = false;
        }
    }

    /**
     * @see jakarta.faces.component.UIData#encodeBegin(jakarta.faces.context.FacesContext)
     */
    public void encodeBegin(FacesContext context) throws IOException
    {
        _initialDescendantComponentState = null;
        if (_isValidChilds && !hasErrorMessages(context))
        {
            //Refresh DataModel for rendering:
            _dataModelMap.clear();
            if (!isPreserveRowStates())
            {
                _rowStates.clear();
            }
        }
        super.encodeBegin(context);
    }

    public void setPreserveRowStates(boolean preserveRowStates)
    {
        getStateHelper().put(PropertyKeys.preserveRowStates, preserveRowStates);
    }

    /**
     * Indicates whether the state for each row should not be 
     * discarded before the datatable is rendered again. 
     * 
     * Setting this to true might be hepful if an input 
     * component inside the datatable has no valuebinding and 
     * the value entered in there should be displayed again.
     *  
     * This will only work reliable if the datamodel of the 
     * datatable did not change either by sorting, removing or 
     * adding rows. Default: false
     * 
     * @JSFProperty
     *   defaultValue="false"
     */
    public boolean isPreserveRowStates()
    {
        Object value = getStateHelper().eval(PropertyKeys.preserveRowStates,DEFAULT_PRESERVEROWSTATES);
        if (value != null)
        {
            return (Boolean) value;
        }
        return DEFAULT_PRESERVEROWSTATES;
    }
    
    /**
     * Indicates whether the state for a component in each row should not be 
     * discarded before the datatable is rendered again.
     * 
     * In tomahawk, this property is the same as t:dataTable preserveRowComponentState
     * 
     * This will only work reliable if the datamodel of the 
     * datatable did not change either by sorting, removing or 
     * adding rows. Default: false
     * 
     * @return
     */
    @JSFProperty(literalOnly=true, faceletsOnly=true, defaultValue="false")
    public boolean isRowStatePreserved()
    {
        Boolean b = (Boolean) getStateHelper().get(PropertyKeys.rowStatePreserved);
        return b == null ? false : b.booleanValue(); 
    }
    
    public void setRowStatePreserved(boolean preserveComponentState)
    {
        getStateHelper().put(PropertyKeys.rowStatePreserved, preserveComponentState);
    }

    protected boolean hasErrorMessages(FacesContext context)
    {
        for(Iterator<FacesMessage> iter = context.getMessages(); iter.hasNext();)
        {
            FacesMessage message = (FacesMessage) iter.next();
            if(FacesMessage.SEVERITY_ERROR.compareTo(message.getSeverity()) <= 0)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @see jakarta.faces.component.UIComponentBase#encodeEnd(jakarta.faces.context.FacesContext)
     */
    public void encodeEnd(FacesContext context) throws IOException
    {
        setRowIndex(-1);
        super.encodeEnd(context);
    }

    @SuppressWarnings("unchecked")
    public void setRowIndex(int rowIndex)
    {
        if ( (isPreserveRowComponentState() || isRowStatePreserved()) && _initialDescendantFullComponentState != null)
        {
            setRowIndexPreserveComponentState(rowIndex);
        }
        else
        {
            setRowIndexWithoutPreserveComponentState(rowIndex);
        }
    }

    private void setRowIndexWithoutPreserveComponentState(int rowIndex)
    {
        if (rowIndex < -1)
        {
            throw new IllegalArgumentException("rowIndex is less than -1");
        }

        if (_rowIndex == rowIndex)
        {
            return;
        }

        FacesContext facesContext = getFacesContext();

        if (_rowIndex == -1)
        {
            if (_initialDescendantComponentState == null)
            {
                _initialDescendantComponentState = saveDescendantComponentStates();
            }
        }
        else
        {
            _rowStates.put(getContainerClientId(facesContext),
                            saveDescendantComponentStates());
        }

        _rowIndex = rowIndex;

        DataModel dataModel = getDataModel();
        dataModel.setRowIndex(rowIndex);

        String var = getVar();
        if (rowIndex == -1)
        {
            if (var != null)
            {
                facesContext.getExternalContext().getRequestMap().remove(var);
            }
        }
        else
        {
            if (var != null)
            {
                if (isRowAvailable())
                {
                    Object rowData = dataModel.getRowData();
                    facesContext.getExternalContext().getRequestMap().put(var,
                                    rowData);
                }
                else
                {
                    facesContext.getExternalContext().getRequestMap().remove(
                                    var);
                }
            }
        }

        if (_rowIndex == -1)
        {
            restoreDescendantComponentStates(_initialDescendantComponentState);
        }
        else
        {
            Object rowState = _rowStates.get(getContainerClientId(facesContext));
            if (rowState == null)
            {
                restoreDescendantComponentStates(_initialDescendantComponentState);
            }
            else
            {
                restoreDescendantComponentStates(rowState);
            }
        }
    }
    
    private void setRowIndexPreserveComponentState(int rowIndex)
    {
        if (rowIndex < -1)
        {
            throw new IllegalArgumentException("rowIndex is less than -1");
        }

        if (_rowIndex == rowIndex)
        {
            return;
        }

        FacesContext facesContext = getFacesContext();

        if (_initialDescendantFullComponentState != null)
        {
            //Just save the row
            Map<String, Object> sm = saveFullDescendantComponentStates(facesContext);
            if (sm != null && !sm.isEmpty())
            {
                _rowDeltaStates.put(getContainerClientId(facesContext), sm);
            }
            if (_rowIndex != -1)
            {
                _rowTransientStates.put(getContainerClientId(facesContext),
                        saveTransientDescendantComponentStates(facesContext));
            }
        }

        _rowIndex = rowIndex;

        DataModel dataModel = getDataModel();
        dataModel.setRowIndex(rowIndex);

        String var = getVar();
        if (rowIndex == -1)
        {
            if (var != null)
            {
                facesContext.getExternalContext().getRequestMap().remove(var);
            }
        }
        else
        {
            if (var != null)
            {
                if (isRowAvailable())
                {
                    Object rowData = dataModel.getRowData();
                    facesContext.getExternalContext().getRequestMap().put(var,
                                    rowData);
                }
                else
                {
                    facesContext.getExternalContext().getRequestMap().remove(
                                    var);
                }
            }
        }

        if (_initialDescendantFullComponentState != null)
        {
            Map<String, Object> rowState = _rowDeltaStates.get(getContainerClientId(facesContext));
            if (rowState == null)
            {
                //Restore as original
                restoreFullDescendantComponentStates(facesContext, _initialDescendantFullComponentState);
            }
            else
            {
                //Restore first original and then delta
                restoreFullDescendantComponentDeltaStates(facesContext, rowState, _initialDescendantFullComponentState);
            }
            if (_rowIndex == -1)
            {
                restoreTransientDescendantComponentStates(facesContext, null);
            }
            else
            {
                rowState = _rowTransientStates.get(getContainerClientId(facesContext));
                if (rowState == null)
                {
                    restoreTransientDescendantComponentStates(facesContext, null);
                }
                else
                {
                    restoreTransientDescendantComponentStates(facesContext, rowState);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void restoreDescendantComponentStates(Object state)
    {
        restoreDescendantComponentStates(getChildren().iterator(), state, false);
    }
    
    @SuppressWarnings("unchecked")
    protected void restoreDescendantComponentStates(Iterator<UIComponent> childIterator,
            Object state, boolean restoreChildFacets)
    {
        Iterator descendantStateIterator = null;
        while (childIterator.hasNext())
        {
            if (descendantStateIterator == null && state != null)
            {
                descendantStateIterator = ((Collection) state).iterator();
            }
            UIComponent component = (UIComponent) childIterator.next();
            // reset the client id (see spec 3.1.6)
            component.setId(component.getId());
            if(!component.isTransient())
            {
                Object childState = null;
                Object descendantState = null;
                if (descendantStateIterator != null
                        && descendantStateIterator.hasNext())
                {
                    Object[] object = (Object[]) descendantStateIterator.next();
                    childState = object[0];
                    descendantState = object[1];
                }
                if (childState != null && component instanceof EditableValueHolder)
                {
                    ((EditableValueHolderState) childState)
                            .restoreState((EditableValueHolder) component);
                }
                Iterator<UIComponent> childsIterator;
                if (restoreChildFacets)
                {
                    childsIterator = component.getFacetsAndChildren();
                }
                else
                {
                    childsIterator = component.getChildren().iterator();
                }
                restoreDescendantComponentStates(childsIterator, descendantState,
                        true);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected Object saveDescendantComponentStates()
    {
        return saveDescendantComponentStates(getChildren().iterator(), false);
    }
    
    @SuppressWarnings("unchecked")
    protected Object saveDescendantComponentStates(Iterator<UIComponent> childIterator,
            boolean saveChildFacets)
    {
        Collection childStates = null;
        while (childIterator.hasNext())
        {
            if (childStates == null)
            {
                childStates = new ArrayList();
            }
            UIComponent child = (UIComponent) childIterator.next();
            if(!child.isTransient())
            {
                Iterator<UIComponent> childsIterator;
                if (saveChildFacets)
                {
                    childsIterator = child.getFacetsAndChildren();
                }
                else
                {
                    childsIterator = child.getChildren().iterator();
                }
                Object descendantState = saveDescendantComponentStates(
                        childsIterator, true);
                Object state = null;
                if (child instanceof EditableValueHolder)
                {
                    state = new EditableValueHolderState(
                            (EditableValueHolder) child);
                }
                childStates.add(new Object[] { state, descendantState });
            }
        }
        return childStates;
    }
    
    
    
    
    @Override
    public void markInitialState()
    {
        if (isPreserveRowComponentState() || isRowStatePreserved())
        {
            if (getFacesContext().getAttributes().containsKey("jakarta.faces.view.ViewDeclarationLanguage.IS_BUILDING_INITIAL_STATE"))
            {
                _initialDescendantFullComponentState = saveDescendantInitialComponentStates(getFacesContext());
            }
        }
        super.markInitialState();
    }

    protected void restoreFullDescendantComponentStates(FacesContext facesContext, Object initialState)
    {
        restoreFullDescendantComponentStates(facesContext, getChildren().iterator(), initialState, false);
    }

    protected void restoreFullDescendantComponentStates(FacesContext facesContext,
            Iterator<UIComponent> childIterator, Object initialState,
            boolean restoreChildFacets)
    {
        Iterator<? extends Object[]> descendantStateIterator = null;
        while (childIterator.hasNext())
        {
            if (descendantStateIterator == null && initialState != null)
            {
                descendantStateIterator = ((Collection<? extends Object[]>) initialState)
                        .iterator();
            }
            UIComponent component = childIterator.next();

            // reset the client id (see spec 3.1.6)
            component.setId(component.getId());
            if (!component.isTransient())
            {
                Object childState = null;
                Object descendantState = null;
                String childId = null;
                if (descendantStateIterator != null
                        && descendantStateIterator.hasNext())
                {
                    do
                    {
                        Object[] object = descendantStateIterator.next();
                        childState = object[0];
                        descendantState = object[1];
                        childId = (String) object[2];
                    }
                    while(descendantStateIterator.hasNext() && !component.getId().equals(childId));
                    
                    if (!component.getId().equals(childId))
                    {
                        // cannot apply initial state to components correctly.
                        throw new IllegalStateException("Cannot restore row correctly.");
                    }
                }
                
                component.clearInitialState();
                component.restoreState(facesContext, childState);
                component.markInitialState();
                
                Iterator<UIComponent> childsIterator;
                if (restoreChildFacets)
                {
                    childsIterator = component.getFacetsAndChildren();
                }
                else
                {
                    childsIterator = component.getChildren().iterator();
                }
                restoreFullDescendantComponentStates(facesContext, childsIterator,
                        descendantState, true);
            }
        }
    }

    protected Collection<Object[]> saveDescendantInitialComponentStates(FacesContext facesContext)
    {
        return saveDescendantInitialComponentStates(facesContext, getChildren().iterator(), false);
    }
    
    protected Collection<Object[]> saveDescendantInitialComponentStates(FacesContext facesContext,
            Iterator<UIComponent> childIterator, boolean saveChildFacets)
    {
        Collection<Object[]> childStates = null;
        while (childIterator.hasNext())
        {
            if (childStates == null)
            {
                childStates = new ArrayList<Object[]>();
            }

            UIComponent child = childIterator.next();
            if (!child.isTransient())
            {
                // Add an entry to the collection, being an array of two
                // elements. The first element is the state of the children
                // of this component; the second is the state of the current
                // child itself.

                Iterator<UIComponent> childsIterator;
                if (saveChildFacets)
                {
                    childsIterator = child.getFacetsAndChildren();
                }
                else
                {
                    childsIterator = child.getChildren().iterator();
                }
                Object descendantState = saveDescendantInitialComponentStates(
                        facesContext, childsIterator, true);
                Object state = null;
                if (child.initialStateMarked())
                {
                    child.clearInitialState();
                    state = child.saveState(facesContext); 
                    child.markInitialState();
                }
                else
                {
                    state = child.saveState(facesContext);
                }
                
                childStates.add(new Object[] { state, descendantState, child.getId()});
            }
        }
        return childStates;
    }

    protected Map<String,Object> saveFullDescendantComponentStates(FacesContext facesContext)
    {
        return saveFullDescendantComponentStates(facesContext, null, getChildren().iterator(), false, getContainerClientId(facesContext));
    }

    protected Map<String,Object> saveFullDescendantComponentStates(FacesContext facesContext, Map<String,Object> stateMap,
            Iterator<UIComponent> childIterator, boolean saveChildFacets, String containerClientId)
    {
        while (childIterator.hasNext())
        {
            UIComponent child = childIterator.next();
            if (!child.isTransient())
            {
                // Add an entry to the collection, being an array of two
                // elements. The first element is the state of the children
                // of this component; the second is the state of the current
                // child itself.

                Iterator<UIComponent> childsIterator;
                if (saveChildFacets)
                {
                    childsIterator = child.getFacetsAndChildren();
                }
                else
                {
                    childsIterator = child.getChildren().iterator();
                }
                stateMap = saveFullDescendantComponentStates(facesContext, stateMap,
                        childsIterator, true, containerClientId);
                Object state = child.saveState(facesContext);
                if (state != null)
                {
                    if (stateMap == null)
                    {
                        stateMap = new HashMap<String,Object>();
                    }
                    stateMap.put(child.getClientId(facesContext).substring(containerClientId.length()+1), state);
                }
            }
        }
        return stateMap;
    }
    
    protected void restoreFullDescendantComponentDeltaStates(FacesContext facesContext,
            Map<String, Object> rowState, Object initialState)
    {
        restoreFullDescendantComponentDeltaStates(facesContext, getChildren().iterator(), rowState, initialState, false, getContainerClientId(facesContext));
    }
    
    protected void restoreFullDescendantComponentDeltaStates(FacesContext facesContext,
            Iterator<UIComponent> childIterator, Map<String, Object> state, Object initialState,
            boolean restoreChildFacets, String containerClientId)
    {
        Iterator<? extends Object[]> descendantFullStateIterator = null;
        while (childIterator.hasNext())
        {
            if (descendantFullStateIterator == null && initialState != null)
            {
                descendantFullStateIterator = ((Collection<? extends Object[]>) initialState).iterator();
            }
            UIComponent component = childIterator.next();

            // reset the client id (see spec 3.1.6)
            component.setId(component.getId());
            if (!component.isTransient())
            {
                Object childInitialState = null;
                Object descendantInitialState = null;
                Object childState = null;
                String childId = null;
                childState = (state == null) ? null : state.get(component.getClientId(facesContext).substring(containerClientId.length()+1));
                if (descendantFullStateIterator != null
                        && descendantFullStateIterator.hasNext())
                {
                    do
                    {
                        Object[] object = descendantFullStateIterator.next();
                        childInitialState = object[0];
                        descendantInitialState = object[1];
                        childId = (String) object[2];
                    }while(descendantFullStateIterator.hasNext() && !component.getId().equals(childId));
                    
                    if (!component.getId().equals(childId))
                    {
                        // cannot apply initial state to components correctly. State is corrupt
                        throw new IllegalStateException("Cannot restore row correctly.");
                    }
                }
                
                component.clearInitialState();
                if (childInitialState != null)
                {
                    component.restoreState(facesContext, childInitialState);
                    component.markInitialState();
                    component.restoreState(facesContext, childState);
                }
                else
                {
                    component.restoreState(facesContext, childState);
                    component.markInitialState();
                }
                
                Iterator<UIComponent> childsIterator;
                if (restoreChildFacets)
                {
                    childsIterator = component.getFacetsAndChildren();
                }
                else
                {
                    childsIterator = component.getChildren().iterator();
                }
                restoreFullDescendantComponentDeltaStates(facesContext, childsIterator,
                        state, descendantInitialState , true, containerClientId);
            }
        }
    }
    
    protected Map<String,Object> saveTransientDescendantComponentStates(FacesContext facesContext)
    {
        return saveTransientDescendantComponentStates(facesContext, null, getChildren().iterator(), false, getContainerClientId(facesContext));
    }

    protected Map<String,Object> saveTransientDescendantComponentStates(FacesContext facesContext, Map<String,Object> stateMap,
            Iterator<UIComponent> childIterator, boolean saveChildFacets, String containerClientId)
    {
        while (childIterator.hasNext())
        {
            UIComponent child = childIterator.next();
            if (!child.isTransient())
            {
                // Add an entry to the collection, being an array of two
                // elements. The first element is the state of the children
                // of this component; the second is the state of the current
                // child itself.

                Iterator<UIComponent> childsIterator;
                if (saveChildFacets)
                {
                    childsIterator = child.getFacetsAndChildren();
                }
                else
                {
                    childsIterator = child.getChildren().iterator();
                }
                stateMap = saveTransientDescendantComponentStates(facesContext, stateMap,
                        childsIterator, true, containerClientId);
                Object state = child.saveTransientState(facesContext);
                if (state != null)
                {
                    if (stateMap == null)
                    {
                        stateMap = new HashMap<String,Object>();
                    }
                    stateMap.put(child.getClientId(facesContext).substring(containerClientId.length()+1), state);
                }
            }
        }
        return stateMap;
    }
    
    protected void restoreTransientDescendantComponentStates(FacesContext facesContext,
            Map<String, Object> rowState)
    {
        restoreTransientDescendantComponentStates(facesContext, getChildren().iterator(), rowState, false, getContainerClientId(facesContext));
    }
    
    protected void restoreTransientDescendantComponentStates(FacesContext facesContext,
            Iterator<UIComponent> childIterator, Map<String, Object> state,
            boolean restoreChildFacets, String containerClientId)
    {
        while (childIterator.hasNext())
        {
            UIComponent component = childIterator.next();

            // reset the client id (see spec 3.1.6)
            component.setId(component.getId());
            if (!component.isTransient())
            {
                component.restoreTransientState(facesContext,
                        (state == null)
                        ? null
                        : state.get(component.getClientId(facesContext).substring(containerClientId.length()+1)));
                Iterator<UIComponent> childsIterator;
                if (restoreChildFacets)
                {
                    childsIterator = component.getFacetsAndChildren();
                }
                else
                {
                    childsIterator = component.getChildren().iterator();
                }
                restoreTransientDescendantComponentStates(facesContext, childsIterator,
                        state, true, containerClientId);
            }
        }
    }
    
    @Override
    public void restoreState(FacesContext context, Object state)
    {
        if (state == null)
        {
            return;
        }
        
        Object values[] = (Object[]) state;
        super.restoreState(context, values[0]);
        Object restoredRowStates = UIComponentBase.restoreAttachedState(context, values[1]);
        if (restoredRowStates == null)
        {
            if (!_rowDeltaStates.isEmpty())
            {
                _rowDeltaStates.clear();
            }
        }
        else
        {
            _rowDeltaStates = (Map<String, Map<String, Object> >) restoredRowStates;
        } 
    }

    @Override
    public Object saveState(FacesContext context)
    {
        // BEGIN Reset mode saveState
        if (context.getViewRoot() != null)
        {
            Integer resetMode = (Integer) context.getViewRoot().getAttributes().get(
                    RESET_SAVE_STATE_MODE_KEY);
            if (resetMode != null)
            {
                if (resetMode == RESET_MODE_SOFT)
                {
                    _dataModelMap.clear();
                    _isValidChilds = true;
                    _rowTransientStates.clear();
                }
                if (resetMode == RESET_MODE_HARD)
                {
                    _dataModelMap.clear();
                    _isValidChilds = true;
                    _rowStates.clear();
                    _rowDeltaStates.clear();
                    _rowTransientStates.clear();
                }
            }
        }
        // END Reset mode saveState
        
        if (initialStateMarked())
        {
            Object parentSaved = super.saveState(context);
            if (parentSaved == null &&_rowDeltaStates.isEmpty())
            {
                return null;
            }
            else
            {
                Object values[] = new Object[2];
                values[0] = parentSaved;
                values[1] = UIComponentBase.saveAttachedState(context, _rowDeltaStates);
                return values; 
            }
        }
        else
        {
            Object values[] = new Object[2];
            values[0] = super.saveState(context);
            values[1] = UIComponentBase.saveAttachedState(context, _rowDeltaStates);
            return values;
        }
    }

    public void setValueBinding(String name, ValueBinding binding)
    {
        if (name == null)
        {
            throw new NullPointerException("name");
        }
        else if (name.equals("value"))
        {
            _dataModelMap.clear();
        }
        else if (name.equals("var") || name.equals("rowIndex"))
        {
            throw new IllegalArgumentException(
                    "You can never set the 'rowIndex' or the 'var' attribute as a value-binding. Set the property directly instead. Name " + name);
        }
        super.setValueBinding(name, binding);
    }
    
    public void setValueExpression(String name, ValueExpression binding)
    {
        if (name == null)
        {
            throw new NullPointerException("name");
        }
        else if (name.equals("value"))
        {
            _dataModelMap.clear();
        }
        else if (name.equals("var") || name.equals("rowIndex"))
        {
            throw new IllegalArgumentException(
                    "You can never set the 'rowIndex' or the 'var' attribute as a value-binding. Set the property directly instead. Name " + name);
        }
        super.setValueExpression(name, binding);
    }

    /**
     * @see jakarta.faces.component.UIData#setValue(java.lang.Object)
     */
    public void setValue(Object value)
    {
        super.setValue(value);
        _dataModelMap.clear();
        _rowStates.clear();
        _rowDeltaStates.clear();
        _isValidChilds = true;
    }

    @SuppressWarnings("unchecked")
    protected DataModel getDataModel()
    {
        DataModel dataModel = null;
        String clientID = "";
        
        UIComponent parent = getParent();
        if (parent != null) 
        {
            clientID = parent.getContainerClientId(getFacesContext());
        }
        dataModel = (DataModel) _dataModelMap.get(clientID);
        if (dataModel == null)
        {
            dataModel = createDataModel();
            _dataModelMap.put(clientID, dataModel);
        }               
        
        return dataModel;
    }

    @SuppressWarnings("unchecked")
    protected void setDataModel(DataModel datamodel)
    {
        UIComponent parent = getParent();
        String clientID = "";
        if(parent != null)
        {
            clientID = parent.getContainerClientId(getFacesContext());
        }
        _dataModelMap.put(clientID, datamodel);
    }

    /**
     * Creates a new DataModel around the current value.
     */
    @SuppressWarnings("unchecked")
    protected DataModel createDataModel()
    {
        Object value = getValue();
        if (value == null)
        {
            return EMPTY_DATA_MODEL;
        }
        else if (value instanceof DataModel)
        {
            return (DataModel) value;
        }
        else if (value instanceof List)
        {
            return new ListDataModel((List) value);
        }
        // accept a Collection is not supported in the Spec
        else if (value instanceof Collection)
        {
            return new ListDataModel(new ArrayList((Collection) value));
        }
        else if (OBJECT_ARRAY_CLASS.isAssignableFrom(value.getClass()))
        {
            return new ArrayDataModel((Object[]) value);
        }
        else if (value instanceof ResultSet)
        {
            return new ResultSetDataModel((ResultSet) value);
        }
        else if (value instanceof Result)
        {
            return new ResultDataModel((Result) value);
        }
        else
        {
            return new ScalarDataModel(value);
        }
    }
    
    @SuppressWarnings("unchecked")
    private static final DataModel EMPTY_DATA_MODEL = new _SerializableDataModel()
    {
        public boolean isRowAvailable()
        {
            return false;
        }

        public int getRowCount()
        {
            return 0;
        }

        public Object getRowData()
        {
            throw new IllegalArgumentException();
        }

        public int getRowIndex()
        {
            return -1;
        }

        public void setRowIndex(int i)
        {
            if (i < -1)
                throw new IndexOutOfBoundsException("Index < 0 : " + i);
        }

        public Object getWrappedData()
        {
            return null;
        }

        public void setWrappedData(Object obj)
        {
            if (obj == null)
                return; //Clearing is allowed
            throw new UnsupportedOperationException(this.getClass().getName()
                            + " UnsupportedOperationException");
        }
    };

    private class EditableValueHolderState
    {
        private final Object _value;
        private final boolean _localValueSet;
        private final boolean _valid;
        private final Object _submittedValue;

        public EditableValueHolderState(EditableValueHolder evh)
        {
            _value = evh.getLocalValue();
            _localValueSet = evh.isLocalValueSet();
            _valid = evh.isValid();
            _submittedValue = evh.getSubmittedValue();
        }

        public void restoreState(EditableValueHolder evh)
        {
            evh.setValue(_value);
            evh.setLocalValueSet(_localValueSet);
            evh.setValid(_valid);
            evh.setSubmittedValue(_submittedValue);
        }
    }
    
    // Property: forceId
    
    /**
     * If true, this component will force the use of the specified id when rendering.
     * 
     * @JSFProperty
     *   literalOnly = "true"
     *   defaultValue = "false"
     *   
     * @return
     */
    public boolean isForceId()
    {
        Object value = getStateHelper().get(PropertyKeys.forceId);
        if (value != null)
        {
            return (Boolean) value;        
        }
        return false;  
    }

    public void setForceId(boolean forceId)
    {
        getStateHelper().put(PropertyKeys.forceId, forceId );
    }
    // Property: forceIdIndex
    
    /**
     * If false, this component will not append a '[n]' suffix 
     * (where 'n' is the row index) to components that are 
     * contained within a "list." This value will be true by 
     * default and the value will be ignored if the value of 
     * forceId is false (or not specified.)
     * 
     * @JSFProperty
     *   literalOnly = "true"
     *   defaultValue = "true"
     *   
     * @return
     */
    public boolean isForceIdIndex()
    {
        Object value = getStateHelper().get(PropertyKeys.forceIdIndex);
        if (value != null)
        {
            return (Boolean) value;        
        }
        return true;
    }

    public void setForceIdIndex(boolean forceIdIndex)
    {
        getStateHelper().put(PropertyKeys.forceIdIndex, forceIdIndex );
    }
    
    /**
     * Remove all preserved row state for the dataTable
     */
    public void clearRowStates()
    {
        if (isPreserveRowComponentState() || isRowStatePreserved())
        {
            _rowDeltaStates.clear();
        }
        else
        {
            _rowStates.clear();
        }
    }
    
    /**
     * Remove preserved row state for deleted row and adjust row state to reflect deleted row.
     * @param deletedIndex index of row to delete
     */
    public void deleteRowStateForRow(int deletedIndex)
    {
        // save row index
        int savedRowIndex = getRowIndex();
        
        FacesContext facesContext = getFacesContext();
         setRowIndex(deletedIndex);
        String currentRowStateKey = getContainerClientId(facesContext);

        Object rowKey = getRowKey(); 
        if (rowKey != null)
        {
            setRowIndex(deletedIndex);
            if (isPreserveRowComponentState() || isRowStatePreserved())
            {
                _rowDeltaStates.remove(currentRowStateKey);
                _rowTransientStates.remove(currentRowStateKey);
            }
            else
            {
                _rowStates.remove(currentRowStateKey);
            }
            setRowIndex(savedRowIndex);
        }
        else
        {
            // copy next rowstate to current row for each row from deleted row onward.
            int rowCount = getRowCount();
            if (isPreserveRowComponentState() || isRowStatePreserved())
            {
                for (int index = deletedIndex + 1; index < rowCount; ++index)
                {
                    setRowIndex(index);
                    String nextRowStateKey = getContainerClientId(facesContext);
        
                    Map<String, Object> nextRowState = _rowDeltaStates.get(nextRowStateKey);
                    if (nextRowState == null)
                    {
                        _rowDeltaStates.remove(currentRowStateKey);
                    }
                    else
                    {
                        _rowDeltaStates.put(currentRowStateKey, nextRowState);
                    }
                    nextRowState = _rowTransientStates.get(nextRowStateKey);
                    if (nextRowState == null)
                    {
                        _rowTransientStates.remove(currentRowStateKey);
                    }
                    else
                    {
                        _rowTransientStates.put(currentRowStateKey, nextRowState);
                    }                    
                    currentRowStateKey = nextRowStateKey;
                }
    
                // restore saved row index
                setRowIndex(savedRowIndex);
        
                // Remove last row
                _rowDeltaStates.remove(currentRowStateKey);
                _rowTransientStates.remove(currentRowStateKey);
            }
            else
            {
                for (int index = deletedIndex + 1; index < rowCount; ++index)
                {
                    setRowIndex(index);
                    String nextRowStateKey = getContainerClientId(facesContext);
        
                    Object nextRowState = _rowStates.get(nextRowStateKey);
                    if (nextRowState == null)
                    {
                        _rowStates.remove(currentRowStateKey);
                    }
                    else
                    {
                        _rowStates.put(currentRowStateKey, nextRowState);
                    }
                    currentRowStateKey = nextRowStateKey;
                }
    
                // restore saved row index
                setRowIndex(savedRowIndex);
        
                // Remove last row
                _rowStates.remove(currentRowStateKey);
            }
        }
    }
    
    
    /**
     * Indicates whether the state for a component in each row should not be 
     * discarded before the datatable is rendered again.
     * 
     * This property is similar to tomahawk t:dataTable preserveRowStates
     * 
     * This will only work reliable if the datamodel of the 
     * datatable did not change either by sorting, removing or 
     * adding rows. Default: false
     * 
     * @return
     */
    @JSFProperty(faceletsOnly=true, literalOnly=true)
    public boolean isPreserveRowComponentState()
    {
        Boolean b = (Boolean) getStateHelper().get(PropertyKeys.preserveRowComponentState);
        return b == null ? false : b.booleanValue(); 
    }
    
    public void setPreserveRowComponentState(boolean preserveComponentState)
    {
        getStateHelper().put(PropertyKeys.preserveRowComponentState, preserveComponentState);
    }
    
    /**
     * Used to assign a value expression that identify in a unique way a row. This value
     * will be used later instead of rowIndex as a key to be appended to the container 
     * client id using getDerivedSubClientId() method.  
     * 
     * @return
     */
    @JSFProperty
    public Object getRowKey()
    {
        return getStateHelper().eval(PropertyKeys.rowKey);
    }
    
    public void setRowKey(Object rowKey)
    {
        getStateHelper().put(PropertyKeys.rowKey, rowKey);
    }
    
    /**
     * This attribute is used to append an unique prefix when rowKey is not used, to prevent
     * a key match a existing component id (note two different components can't have the
     * same unique id).
     * 
     * @return
     */
    @JSFProperty(defaultValue="r_id_")
    public String getDerivedRowKeyPrefix()
    {
        return (String) getStateHelper().eval(PropertyKeys.derivedRowKeyPrefix, UNIQUE_ROW_ID_PREFIX);
    }
    
    public void setDerivedRowKeyPrefix(String derivedRowKeyPrefix)
    {
        getStateHelper().put(PropertyKeys.derivedRowKeyPrefix, derivedRowKeyPrefix);
    }

    /**
     * Return the fragment to be used on the container client id to
     * identify a row. As a side effect, it will be used to indicate 
     * a row component state and a datamodel in nested datatable case.
     * 
     * <p>
     * The returned value must comply with the following rules:
     * </p>
     * <ul>
     * <li> Can be followed by: letters (A-Za-z), digits (0-9), hyphens ("-"), 
     *   underscores ("_"), colons (":"), and periods (".") </li>
     * <li> Values are case-sensitive </li>
     * </ul>
     * 
     * @return
     */
    protected String getDerivedSubClientId()
    {
        Object key = getRowKey();
        if (key == null)
        {
            return _SubIdConverter.encode(Integer.toString(getRowIndex()));
        }
        else
        {
            return getDerivedRowKeyPrefix() + _SubIdConverter.encode(key.toString());
        }
    }

    protected enum PropertyKeys
    {
        preserveRowStates
        , forceId
        , forceIdIndex
        , preserveRowComponentState
        , rowKey
        , derivedRowKeyPrefix
        , rowStatePreserved
    }
}
