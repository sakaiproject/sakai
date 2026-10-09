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
package org.apache.myfaces.custom.crosstable;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIData;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.ValueBinding;
import jakarta.faces.model.ArrayDataModel;
import jakarta.faces.model.DataModel;
import jakarta.faces.model.ListDataModel;
import jakarta.faces.model.ResultDataModel;
import jakarta.faces.model.ResultSetDataModel;
import jakarta.faces.model.ScalarDataModel;
import jakarta.servlet.jsp.jstl.sql.Result;

/**
 * An object which can be a child of a t:dataTable, and inserts a dynamic
 * set of columns into the parent table. The set of columns inserted are
 * defined by a collection returned from a value-binding.
 * <p>
 * Class org.apache.myfaces.component.html.ext.HtmlDataTable (aka t:dataTable)
 * has special-case code to detect a child of this type, and invoke the
 * necessary methods on this class. This class does not work with other
 * UIData implementations. There is no renderer for this component; the
 * HtmlTableRenderer associated with ext.HtmlDataTable is UIColumns-aware,
 * and implements the necessary logic itself.
 * <p>
 * This class is actually a UIData itself, which is effectively "merged"
 * with the parent HtmlDataTable. It can't be used as a stand-alone table,
 * however, because it uses the DataModel returned by the "value" value-binding
 * to define the columns, and depends on the parent table's "value" 
 * value-binding to define the row DataModel.
 * <p>
 * The "value" attribute of this class must be a value-binding which
 * returns a DataModel of objects (or a List, Array or ResultSet which
 * automatically gets wrapped in the appropriate DataModel). 
 * <p>
 * In a normal table, each UIColumn child has facets and child components
 * which define that column. In this component, the same child components
 * apply to each column (ie are repeated dataModel.size times). However
 * as the columns are rendered, the current DataModel object (ie the object
 * defining the current column) is stored into a variable whose name
 * is defined by attribute "var". This allows the child components of
 * this component to refer to attributes on those objects to set things
 * like the current column's name. When the objects must be rendered as
 * different components (eg h:outputText or h:outputDate), multiple
 * child components can be used with rendered attributes selecting the
 * appropriate one depending upon the current column object's data.
 * 
 * @JSFComponent
 * 
 * @author Mathias Broekelmann (latest modification by $Author: lu4242 $)
 * @version $Revision: 659874 $ $Date: 2008-05-24 15:59:15 -0500 (Sat, 24 May 2008) $
 */
public class UIColumns extends UIData {
    public static final String COMPONENT_TYPE = "org.apache.myfaces.Columns";

    private static final Class OBJECT_ARRAY_CLASS = (new Object[0]).getClass();

    private static final int PROCESS_DECODES = 1;
    private static final int PROCESS_VALIDATORS = 2;
    private static final int PROCESS_UPDATES = 3;

    private boolean _isValidChilds = true;

    // holds the the state for each cell of the child components of this UIColumns
    private Map _cellStates = new HashMap();

    private Object _initialDescendantComponentState = null;

    private int _colIndex = -1;
    private UIData _parentUIData;

    private Map _dataModelMap = new HashMap();

    public UIColumns() {
        super();
    }

    /**
     * Return true if there are any more <i>columns</i> to process.
     */
    public boolean isRowAvailable() {
        return getDataModel().isRowAvailable();
    }

    /**
     * Get the number of dynamic columns represented by this instance.
     */
    public int getRowCount() {
        return getDataModel().getRowCount();
    }

    /** Get the object representing the current column. */
    public Object getRowData() {
        return getDataModel().getRowData();
    }

    /** Get the currently selected column index. */
    public int getRowIndex() {
        return _colIndex;
    }

    /** Set the currently selected column. */
    public void setRowIndex(int colIndex) {
        if (colIndex < -1) {
            throw new IllegalArgumentException("colIndex is less than -1");
        }

        if (_colIndex == colIndex) {
            return;
        }

        FacesContext facesContext = getFacesContext();

        if (_colIndex == -1) {
            if (_initialDescendantComponentState == null) {
                _initialDescendantComponentState = saveDescendantComponentStates(getFacetsAndChildren());
            }
        } else {
            _cellStates.put(getClientId(facesContext), saveDescendantComponentStates(getFacetsAndChildren()));
        }

        _colIndex = colIndex;

        DataModel dataModel = getDataModel();
        dataModel.setRowIndex(colIndex);

        String var = getVar();
        if (colIndex == -1) {
            if (var != null) {
                facesContext.getExternalContext().getRequestMap().remove(var);
            }
        } else {
            if (var != null) {
                if (isRowAvailable()) {
                    Object rowData = dataModel.getRowData();
                    facesContext.getExternalContext().getRequestMap().put(var, rowData);
                } else {
                    facesContext.getExternalContext().getRequestMap().remove(var);
                }
            }
        }

        if (_colIndex == -1) {
            restoreDescendantComponentStates(getFacetsAndChildren(), _initialDescendantComponentState);
        } else {
            Object rowState = _cellStates.get(getClientId(facesContext));
            if (rowState == null) {
                restoreDescendantComponentStates(getFacetsAndChildren(), _initialDescendantComponentState);
            } else {
                restoreDescendantComponentStates(getFacetsAndChildren(), rowState);
            }
        }
    }

    protected void restoreDescendantComponentStates(Iterator childIterator, Object state) {
        Iterator descendantStateIterator = null;
        while (childIterator.hasNext()) {
            if (descendantStateIterator == null && state != null) {
                descendantStateIterator = ((Collection) state).iterator();
            }
            UIComponent component = (UIComponent) childIterator.next();
            // reset the client id (see spec 3.1.6)
            component.setId(component.getId());
            if (!component.isTransient()) {
                Object childState = null;
                Object descendantState = null;
                if (descendantStateIterator != null && descendantStateIterator.hasNext()) {
                    Object[] object = (Object[]) descendantStateIterator.next();
                    childState = object[0];
                    descendantState = object[1];
                }
                if (component instanceof EditableValueHolder) {
                    ((EditableValueHolderState) childState).restoreState((EditableValueHolder) component);
                }
                restoreDescendantComponentStates(component.getFacetsAndChildren(), descendantState);
            }
        }
    }

    protected Object saveDescendantComponentStates(Iterator childIterator) {
        Collection childStates = null;
        while (childIterator.hasNext()) {
            if (childStates == null) {
                childStates = new ArrayList();
            }
            UIComponent child = (UIComponent) childIterator.next();
            if (!child.isTransient()) {
                Object descendantState = saveDescendantComponentStates(child.getFacetsAndChildren());
                Object state = null;
                if (child instanceof EditableValueHolder) {
                    state = new EditableValueHolderState((EditableValueHolder) child);
                }
                childStates.add(new Object[] { state, descendantState });
            }
        }
        return childStates;
    }

    public void setValue(Object value) {
        super.setValue(value);
        _dataModelMap.clear();
        _cellStates.clear();
        _isValidChilds = true;
    }

    public void setValueBinding(String name, ValueBinding binding) {
        if (name == null) {
            throw new NullPointerException("name");
        } else if (name.equals("value")) {
            _dataModelMap.clear();
        } else if (name.equals("var") || name.equals("rowIndex")) {
            throw new IllegalArgumentException("name " + name);
        }
        super.setValueBinding(name, binding);
    }

    /**
     * Get a DataModel whose wrapped data contains a collection of
     * objects representing columns.
     */
    protected DataModel getDataModel() {
        String clientID = "";
        UIComponent parent = getParentUIData().getParent();
        if (parent != null) {
            clientID = parent.getClientId(getFacesContext());
        }
        DataModel dataModel = (DataModel) _dataModelMap.get(clientID);
        if (dataModel == null) {
            dataModel = createDataModel();
            _dataModelMap.put(clientID, dataModel);
        }
        return dataModel;
    }

    protected void setDataModel(DataModel dataModel) {
        if (dataModel == null && getParent() == null) {
            return;
        }
        UIComponent parent = getParentUIData().getParent();
        String clientID = "";
        if (parent != null) {
            clientID = parent.getClientId(getFacesContext());
        }
        _dataModelMap.put(clientID, dataModel);
    }

    /**
     * Creates a new DataModel around the current value.
     */
    protected DataModel createDataModel() {
        Object value = getValue();
        if (value == null) {
            return EMPTY_DATA_MODEL;
        } else if (value instanceof DataModel) {
            return (DataModel) value;
        } else if (value instanceof List) {
            return new ListDataModel((List) value);
        } else if (value instanceof Collection) {
            return new ListDataModel(new ArrayList((Collection) value));
        } else if (OBJECT_ARRAY_CLASS.isAssignableFrom(value.getClass())) {
            return new ArrayDataModel((Object[]) value);
        } else if (value instanceof ResultSet) {
            return new ResultSetDataModel((ResultSet) value);
        } else if (value instanceof Result) {
            return new ResultDataModel((Result) value);
        } else {
            return new ScalarDataModel(value);
        }
    }

    private static final DataModel EMPTY_DATA_MODEL = new DataModel() {
        public boolean isRowAvailable() {
            return false;
        }

        public int getRowCount() {
            return 0;
        }

        public Object getRowData() {
            throw new IllegalArgumentException();
        }

        public int getRowIndex() {
            return -1;
        }

        public void setRowIndex(int i) {
            if (i < -1)
                throw new IndexOutOfBoundsException("Index < 0 : " + i);
        }

        public Object getWrappedData() {
            return null;
        }

        public void setWrappedData(Object obj) {
            if (obj == null)
                return; //Clearing is allowed
            throw new UnsupportedOperationException(this.getClass().getName() + " UnsupportedOperationException");
        }
    };

    /**
     * Update the input component (if any) in each dynamic column.
     * 
     * @see jakarta.faces.component.UIData#processDecodes(jakarta.faces.context.FacesContext)
     */
    public void processDecodes(FacesContext context) {
        if (context == null)
            throw new NullPointerException("context");
        if (!isRendered())
            return;

        setRowIndex(-1);
        processColumnsFacets(context, PROCESS_DECODES);
        processRows(context, PROCESS_DECODES);
        setRowIndex(-1);

        try {
            decode(context);
        } catch (RuntimeException e) {
            context.renderResponse();
            throw e;
        }
    }

    private void processColumnsFacets(FacesContext context, int processAction) {
        int first = getFirst();
        int cols = getRows();
        int last;
        if (cols == 0) {
            last = getRowCount();
        } else {
            last = first + cols;
        }

        for (int colIndex = first; colIndex < last; colIndex++) {
            setRowIndex(colIndex);
            if (isRowAvailable()) {
                for (Iterator facetsIter = getFacets().values().iterator(); facetsIter.hasNext();) {
                    UIComponent facet = (UIComponent) facetsIter.next();
                    process(context, facet, processAction);
                }
            }
        }
        setRowIndex(-1);
    }

    private void processRows(FacesContext context, int processAction) {
        UIData parentUIData = getParentUIData();
        int first = parentUIData.getFirst();
        int rows = parentUIData.getRows();
        int last;
        if (rows == 0) {
            last = parentUIData.getRowCount();
        } else {
            last = first + rows;
        }

        for (int rowIndex = first; rowIndex < last; rowIndex++) {
            parentUIData.setRowIndex(rowIndex);
            if (parentUIData.isRowAvailable()) {
                processColumns(context, processAction);
            }
        }
    }

    /**
     * Return the UIData this component is nested within.
     * <p>
     * Actually, this component will only function correctly when nested within
     * an org.apache.myfaces.component.html.ext.HtmlDataTable.
     */
    private UIData getParentUIData() {
        if (_parentUIData == null) {
            UIComponent parent = getParent();
            if (!(parent instanceof UIData)) {
                throw new IllegalStateException("UIColumns component must be a child of a UIData component");
            }
            _parentUIData = (UIData) parent;
        }
        return _parentUIData;
    }

    private void processColumns(FacesContext context, int processAction) {
        int first = getFirst();
        int cols = getRows();
        int last;
        if (cols == 0) {
            last = getRowCount();
        } else {
            last = first + cols;
        }

        for (int colIndex = first; colIndex < last; colIndex++) {
            setRowIndex(colIndex);
            if (isRowAvailable()) {
                for (Iterator columnChildIter = getChildren().iterator(); columnChildIter.hasNext();) {
                    UIComponent columnChild = (UIComponent) columnChildIter.next();
                    process(context, columnChild, processAction);
                }
            }
        }
        setRowIndex(-1);
    }

    /**
     * @see jakarta.faces.component.UIData#processValidators(jakarta.faces.context.FacesContext)
     */
    public void processValidators(FacesContext context) {
        if (context == null)
            throw new NullPointerException("context");
        if (!isRendered())
            return;
        setRowIndex(-1);
        processColumnsFacets(context, PROCESS_VALIDATORS);
        processRows(context, PROCESS_VALIDATORS);
        setRowIndex(-1);

        // check if an validation error forces the render response for our data
        if (context.getRenderResponse()) {
            _isValidChilds = false;
        }
    }

    /**
     * @see jakarta.faces.component.UIData#processUpdates(jakarta.faces.context.FacesContext)
     */
    public void processUpdates(FacesContext context) {
        if (context == null)
            throw new NullPointerException("context");
        if (!isRendered())
            return;
        setRowIndex(-1);
        processColumnsFacets(context, PROCESS_UPDATES);
        processRows(context, PROCESS_UPDATES);
        setRowIndex(-1);

        // check if an validation error forces the render response for our data
        if (context.getRenderResponse()) {
            _isValidChilds = false;
        }
    }

    private void process(FacesContext context, UIComponent component, int processAction) {
        switch (processAction) {
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

    /**
     * Called from HtmlDataTable.encodeBegin, ie called once when rendering
     * for the entire table starts.
     */
    public void encodeTableBegin(FacesContext context) {
        setRowIndex(-1);
        _initialDescendantComponentState = null;
        if (_isValidChilds && !hasErrorMessages(context)) {
            //Refresh DataModel for rendering:
            _dataModelMap.clear();
            _cellStates.clear();
        }
    }

    protected boolean hasErrorMessages(FacesContext context) {
        for (Iterator iter = context.getMessages(); iter.hasNext();) {
            FacesMessage message = (FacesMessage) iter.next();
            if (FacesMessage.SEVERITY_ERROR.compareTo(message.getSeverity()) <= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Called from HtmlDataTable.encodeEnd, ie called once after rendering
     * for the entire table has completed.
     */
    public void encodeTableEnd(FacesContext context) {
        setRowIndex(-1);
    }

    private class EditableValueHolderState {
        private final Object _value;
        private final boolean _localValueSet;
        private final boolean _valid;
        private final Object _submittedValue;

        public EditableValueHolderState(EditableValueHolder evh) {
            _value = evh.getLocalValue();
            _localValueSet = evh.isLocalValueSet();
            _valid = evh.isValid();
            _submittedValue = evh.getSubmittedValue();
        }

        public void restoreState(EditableValueHolder evh) {
            evh.setValue(_value);
            evh.setLocalValueSet(_localValueSet);
            evh.setValid(_valid);
            evh.setSubmittedValue(_submittedValue);
        }
    }

    /// ------ EUR/SI/BS/JC MODIFICATIONS START HERE ---------

    public String getClientId(FacesContext context) {
        String clientId2 = super.getClientId(context);
        int rowIndex = getRowIndex();
        if (rowIndex == -1) {
            return clientId2;
        }
        else {
            return clientId2 + "_" + rowIndex;
        }
    }

    /// ------ EUR/SI/BS/JC MODIFICATIONS END HERE ---------
}
