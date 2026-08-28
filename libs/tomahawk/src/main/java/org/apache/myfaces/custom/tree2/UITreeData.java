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
package org.apache.myfaces.custom.tree2;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.faces.FacesException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.ContextCallback;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.UniqueIdVendor;
import jakarta.faces.component.visit.VisitCallback;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.ValueBinding;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.FacesEvent;
import jakarta.faces.event.FacesListener;
import jakarta.faces.event.PhaseId;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;
import org.apache.myfaces.shared_tomahawk.util.MessageUtils;
import org.apache.myfaces.tomahawk.util.Constants;

/**
 * TreeData is a {@link UIComponent} that supports binding data stored in a tree represented
 * by a {@link TreeNode} instance.  During iterative processing over the tree nodes in the
 * data model, the object for the current node is exposed as a request attribute under the key
 * specified by the <code>var</code> property.  {@link jakarta.faces.render.Renderer}s of this
 * component should use the appropriate facet to assist in rendering.
 *
 * @author Sean Schofield
 * @author Hans Bergsten (Some code taken from an example in his O'Reilly JavaServer Faces book. Copied with permission)
 * @version $Revision: 703742 $ $Date: 2008-10-11 17:10:36 -0500 (sáb, 11 oct 2008) $
 */
@JSFComponent
public class UITreeData extends UIComponentBase implements NamingContainer, Tree, UniqueIdVendor {
    private Log log = LogFactory.getLog(UITreeData.class);

    public static final String COMPONENT_TYPE = "org.apache.myfaces.UITree2";
    public static final String COMPONENT_FAMILY = "org.apache.myfaces.HtmlTree2";
    //private static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.Tree2";
    private static final String MISSING_NODE = "org.apache.myfaces.tree2.MISSING_NODE";
    private static final int PROCESS_DECODES = 1;
    private static final int PROCESS_VALIDATORS = 2;
    private static final int PROCESS_UPDATES = 3;

    private TreeModel _cachedModel;
    private String _nodeId;
    private TreeNode _node;

    private Object _value;
    private String _var;
    private Map _saved = new HashMap();

    private TreeState _restoredState = null;

    private transient FacesContext _facesContext;
    
    private static final String SKIP_ITERATION_HINT = "jakarta.faces.visit.SKIP_ITERATION";

    /**
     * Constructor
     */
    public UITreeData()
    {
        //setRendererType(DEFAULT_RENDERER_TYPE);
    }


    // see superclass for documentation
    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }

    //  see superclass for documentation
    public Object saveState(FacesContext context)
    {
        Object values[] = new Object[3];
        values[0] = super.saveState(context);
        values[1] = _var;
        values[2] = _restoredState;
        return ((Object) (values));
    }


    // see superclass for documentation
    public void restoreState(FacesContext context, Object state)
    {
        Object values[] = (Object[]) state;
        super.restoreState(context, values[0]);

        _var = (String)values[1];
        _restoredState = (TreeState) values[2];
    }

    public void encodeEnd(FacesContext context) throws IOException {
        super.encodeEnd(context);

        // prepare to save the tree state -- fix for MYFACES-618
        // should be done in saveState() but Sun RI does not call saveState() and restoreState()
        // with jakarta.faces.STATE_SAVING_METHOD = server
        TreeState state = getDataModel().getTreeState();
        if ( state == null)
        {
            // the model supplier has forgotten to return a valid state manager, but we need one
            state = new TreeStateBase();
        }
        // save the state with the component, unless it should explicitly not saved eg. session-scoped model and state
        _restoredState = (state.isTransient()) ? null : state;

    }

    public void queueEvent(FacesEvent event)
    {
        super.queueEvent(new FacesEventWrapper(event, getNodeId(), this));
    }


    public void broadcast(FacesEvent event) throws AbortProcessingException
    {
        if (event instanceof FacesEventWrapper)
        {
            FacesEventWrapper childEvent = (FacesEventWrapper) event;
            String currNodeId = getNodeId();
            setNodeId(childEvent.getNodeId());
            FacesEvent nodeEvent = childEvent.getFacesEvent();
            nodeEvent.getComponent().broadcast(nodeEvent);
            setNodeId(currNodeId);
            return;
        }
        else if(event instanceof ToggleExpandedEvent)
        {
            ToggleExpandedEvent toggleEvent = (ToggleExpandedEvent) event;
            String currentNodeId = getNodeId();
            setNodeId(toggleEvent.getNodeId());
            toggleExpanded();
            setNodeId(currentNodeId);
        }
        else
        {
            super.broadcast(event);
            return;
        }
    }


    // see superclass for documentation
    public void processDecodes(FacesContext context)
    {
        if (context == null) throw new NullPointerException("context");
        if (!isRendered()) return;

        _cachedModel = null;
        _saved = new HashMap();

        setNodeId(null);
        decode(context);

        processNodes(context, PROCESS_DECODES, getDataModel().getTreeWalker());
        // After processNodes is executed, the node active is the last one
        // we have to set it to null again to avoid inconsistency on outsider
        // code (just like UIData components does)
        setNodeId(null);

    }

    // see superclass for documentation
    public void processValidators(FacesContext context)
    {
        if (context == null) throw new NullPointerException("context");
        if (!isRendered()) return;

        processNodes(context, PROCESS_VALIDATORS, getDataModel().getTreeWalker());

        setNodeId(null);
    }


    // see superclass for documentation
    public void processUpdates(FacesContext context)
    {
        if (context == null) throw new NullPointerException("context");
        if (!isRendered()) return;

        processNodes(context, PROCESS_UPDATES, getDataModel().getTreeWalker());

        setNodeId(null);
    }

    // see superclass for documentation
    /*
    public String getClientId(FacesContext context)
    {
        String ownClientId = super.getClientId(context);
        if (_nodeId != null)
        {
            return ownClientId + NamingContainer.SEPARATOR_CHAR + _nodeId;
        } else
        {
            return ownClientId;
        }
    }*/
    
    @Override
    public String getContainerClientId(FacesContext context)
    {
        String ownClientId = super.getContainerClientId(context);
        if (_nodeId != null)
        {
            return ownClientId + UINamingContainer.getSeparatorChar(context) + _nodeId;
        } 
        else
        {
            return ownClientId;
        }
    }

    // see superclass for documentation
    public void setValueBinding(String name, ValueBinding binding)
    {
        if ("value".equals(name))
        {
            _cachedModel = null;
        } else if ("nodeVar".equals(name) || "nodeId".equals(name) || "treeVar".equals(name))
        {
            throw new IllegalArgumentException("name " + name);
        }
        super.setValueBinding(name, binding);
    }

    // see superclass for documentation
    public void encodeBegin(FacesContext context) throws IOException
    {
        /**
         * The renderer will handle most of the encoding, but if there are any
         * error messages queued for the components (validation errors), we
         * do want to keep the saved state so that we can render the node with
         * the invalid value.
         */

        if (!keepSaved(context))
        {
            _saved = new HashMap();
        }

        // FIX for MYFACES-404
        // do not use the cached model the render phase
        _cachedModel = null;

        super.encodeBegin(context);
    }

    /**
     * Sets the value of the TreeData.
     *
     * @param value The new value
     *
     * @deprecated
     */
    public void setValue(Object value)
    {
        _cachedModel = null;
        _value = value;
    }


    /**
     * Gets the model of the TreeData -
     *  due to backwards-compatibility, this can also be retrieved by getValue.
     *
     * @return The value
     */
    public Object getModel()
    {
        return getValue();
    }

    /**
     * Sets the model of the TreeData -
     *  due to backwards-compatibility, this can also be set by calling setValue.
     *
     * @param model The new model
     */
    public void setModel(Object model)
    {
        setValue(model);
    }


    /**
     * Gets the value of the TreeData.
     *
     * @JSFProperty
     *   required="true"
     * @return The value
     *
     * @deprecated
     */
    public Object getValue()
    {
        if (_value != null) return _value;
        ValueBinding vb = getValueBinding("value");
        return vb != null ? vb.getValue(getFacesContext()) : null;
    }

    /**
     * Set the request-scope attribute under which the data object for the current node wil be exposed
     * when iterating.
     *
     * @param var The new request-scope attribute name
     */
    public void setVar(String var)
    {
        _var = var;
    }


    /**
     * Return the request-scope attribute under which the data object for the current node will be exposed
     * when iterating. This property is not enabled for value binding expressions.
     * 
     * @JSFProperty
     * @return The iterator attribute
     */
    public String getVar()
    {
        return _var;
    }

    /**
     * Calls through to the {@link TreeModel} and returns the current {@link TreeNode} or <code>null</code>.
     *
     * @return The current node
     */
    public TreeNode getNode()
    {
        return _node;
    }


    public String getNodeId()
    {
        return _nodeId;
    }


    public void setNodeId(String nodeId)
    {
        saveDescendantState();

        _nodeId = nodeId;

        TreeModel model = getDataModel();
        if (model == null)
        {
            return;
        }

        try
        {
            _node = model.getNodeById(nodeId);
        }
        //TODO: change to an own exception
        catch (IndexOutOfBoundsException aob)
        {
            /**
             * This might happen if we are trying to process a commandLink for a node that node that no longer
             * exists.  Instead of allowing a RuntimeException to crash the application, we will add a warning
             * message so the user can optionally display the warning.  Also, we will allow the user to provide
             * their own value binding method to be called so they can handle it how they see fit.
             */
            FacesMessage message = MessageUtils.getMessageFromBundle(Constants.TOMAHAWK_DEFAULT_BUNDLE, MISSING_NODE, new String[] {nodeId});
            message.setSeverity(FacesMessage.SEVERITY_WARN);
            FacesContext.getCurrentInstance().addMessage(getId(), message);

            /** @todo call hook */
            /** @todo figure out whether or not to abort this method gracefully */
        }

        restoreDescendantState();

        if (_var != null)
        {
            Map requestMap = getFacesContext().getExternalContext().getRequestMap();

            if (nodeId == null)
            {
                requestMap.remove(_var);
            } else
            {
                requestMap.put(_var, getNode());
            }
        }
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

        boolean isTemporalFacesContext = isTemporalFacesContext();
        if (!isTemporalFacesContext)
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
                TreeModel model = getDataModel();
                
                //We only use the tree walker to get the RootNodeId()
                TreeWalker walker = model.getTreeWalker();
                UIComponent facet = null;
                walker.reset();
                walker.setTree(this);
                
                String oldNodeId = getNodeId();
                
                try
                {
                    while(!returnValue && walker.next())
                    {
                        TreeNode node = getNode();
                        facet = getFacet(node.getType());
    
                        if (facet == null)
                        {
                            log.warn("Unable to locate facet with the name: " + node.getType());
                            continue;
                        }
                        
                        returnValue = facet.invokeOnComponent(context, baseClientId, callback);
                    }
                }
                finally
                {
                    setNodeId(oldNodeId);
                }
            }
        }
        finally
        {
            //all components must call popComponentFromEl after visiting is finished
            popComponentFromEL(context);
            if (!isTemporalFacesContext)
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
        String oldNodeId = getNodeId();
        // set row index to -1 to process the facets and to get the rowless clientId
        setNodeId(null);
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
                        TreeWalker walker = getDataModel().getTreeWalker();
                        UIComponent facet = null;
                        walker.reset();
                        walker.setTree(this);
    
                        while(walker.next())
                        {
                            TreeNode node = getNode();
                            facet = getFacet(node.getType());
    
                            if (facet == null)
                            {
                                log.warn("Unable to locate facet with the name: " + node.getType());
                                continue;
                                //throw new IllegalArgumentException("Unable to locate facet with the name: " + node.getType());
                            }
    
                            if (facet.visitTree(context, callback))
                            {
                                return true;
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
            setNodeId(oldNodeId);
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
     * Gets an array of String containing the ID's of all of the {@link TreeNode}s in the path to
     * the specified node.  The path information will be an array of <code>String</code> objects
     * representing node ID's. The array will starting with the ID of the root node and end with
     * the ID of the specified node.
     *
     * @param nodeId The id of the node for whom the path information is needed.
     * @return String[]
     */
    public String[] getPathInformation(String nodeId)
    {
        return getDataModel().getPathInformation(nodeId);
    }

    /**
     * Indicates whether or not the specified {@link TreeNode} is the last child in the <code>List</code>
     * of children.  If the node id provided corresponds to the root node, this returns <code>true</code>.
     *
     * @param nodeId The ID of the node to check
     * @return boolean
     */
    public boolean isLastChild(String nodeId)
    {
        return getDataModel().isLastChild(nodeId);
    }

    /**
     * Returns a previously cached {@link TreeModel}, if any, or sets the cache variable to either the
     * current value (if its a {@link TreeModel}) or to a new instance of {@link TreeModel} (if it's a
     * {@link TreeNode}) with the provided value object as the root node.
     *
     * @return TreeModel
     */
    public TreeModel getDataModel()
    {
        if (_cachedModel != null)
        {
            return _cachedModel;
        }

        Object value = getValue();
        if (value != null)
        {
            if (value instanceof TreeModel)
            {
                _cachedModel = (TreeModel) value;
            }
            else if (value instanceof TreeNode)
            {
                _cachedModel = new TreeModelBase((TreeNode) value);
            } else
            {
                throw new IllegalArgumentException("Value must be a TreeModel or TreeNode");
            }
        }

        if (_restoredState != null)
            _cachedModel.setTreeState(_restoredState); // set the restored state (if there is one) on the model

        return _cachedModel;
    }

    /**
     * Epands all nodes by default.
     */
    public void expandAll()
    {
        toggleAll(true);
    }

    /**
     * Collapse all nodes by default.
     */
    public void collapseAll()
    {
        toggleAll(false);
    }

    /**
     * Toggles all of the nodes to either expanded or collapsed depending on the
     * parameter supplied.
     *
     * @param expanded Expand all of the nodes (a value of false indicates collapse
     * all nodes)
     */
    private void toggleAll(boolean expanded)
    {
        TreeWalker walker = getDataModel().getTreeWalker();
        walker.reset();

        TreeState state =  getDataModel().getTreeState();
        walker.setCheckState(false);
        walker.setTree(this);

        while(walker.next())
        {
            String id = getNodeId();
            if ((expanded && !state.isNodeExpanded(id)) || (!expanded && state.isNodeExpanded(id)))
            {
                state.toggleExpanded(id);
            }
        }
    }

    /**
     * Expands all of the nodes in the specfied path.
     * @param nodePath The path to expand.
     */
    public void expandPath(String[] nodePath)
    {
        getDataModel().getTreeState().expandPath(nodePath);
    }

    /**
     * Expands all of the nodes in the specfied path.
     * @param nodePath The path to expand.
     */
    public void collapsePath(String[] nodePath)
    {
        getDataModel().getTreeState().collapsePath(nodePath);
    }


    protected void processNodes(FacesContext context, int processAction, TreeWalker walker)
    {
        UIComponent facet = null;
        walker.reset();
        walker.setTree(this);

        while(walker.next())
        {
            TreeNode node = getNode();
            facet = getFacet(node.getType());

            if (facet == null)
            {
                log.warn("Unable to locate facet with the name: " + node.getType());
                continue;
                //throw new IllegalArgumentException("Unable to locate facet with the name: " + node.getType());
            }

            switch (processAction)
            {
                case PROCESS_DECODES:

                    facet.processDecodes(context);
                    break;

                case PROCESS_VALIDATORS:

                    facet.processValidators(context);
                    break;

                case PROCESS_UPDATES:

                    facet.processUpdates(context);
                    break;
            }
        }

    }

    /**
     * To support using input components for the nodes (e.g., input fields, checkboxes, and selection
     * lists) while still only using one set of components for all nodes, the state held by the components
     * for the current node must be saved for a new node is selected.
     */
    private void saveDescendantState()
    {
        FacesContext context = getFacesContext();
        Iterator i = getFacets().values().iterator();
        while (i.hasNext())
        {
            UIComponent facet = (UIComponent) i.next();
            saveDescendantState(facet, context);
        }
    }

    /**
     * Overloaded helper method for the no argument version of this method.
     *
     * @param component The component whose state needs to be saved
     * @param context   FacesContext
     */
    private void saveDescendantState(UIComponent component, FacesContext context)
    {
        if (component instanceof EditableValueHolder)
        {
            EditableValueHolder input = (EditableValueHolder) component;
            String clientId = component.getClientId(context);
            SavedState state = (SavedState) _saved.get(clientId);
            if (state == null)
            {
                state = new SavedState();
                _saved.put(clientId, state);
            }
            state.setValue(input.getLocalValue());
            state.setValid(input.isValid());
            state.setSubmittedValue(input.getSubmittedValue());
            state.setLocalValueSet(input.isLocalValueSet());
        }

        List kids = component.getChildren();
        for (int i = 0; i < kids.size(); i++)
        {
            saveDescendantState((UIComponent) kids.get(i), context);
        }
    }


    /**
     * Used to configure a new node with the state stored previously.
     */
    private void restoreDescendantState()
    {
        FacesContext context = getFacesContext();
        Iterator i = getFacets().values().iterator();
        while (i.hasNext())
        {
            UIComponent facet = (UIComponent) i.next();
            restoreDescendantState(facet, context);
        }
    }

    /**
     * Overloaded helper method for the no argument version of this method.
     *
     * @param component The component whose state needs to be restored
     * @param context   FacesContext
     */
    private void restoreDescendantState(UIComponent component, FacesContext context)
    {
        String id = component.getId();
        component.setId(id); // forces the cilent id to be reset

        if (component instanceof EditableValueHolder)
        {
            EditableValueHolder input = (EditableValueHolder) component;
            String clientId = component.getClientId(context);
            SavedState state = (SavedState) _saved.get(clientId);
            if (state == null)
            {
                state = new SavedState();
            }
            input.setValue(state.getValue());
            input.setValid(state.isValid());
            input.setSubmittedValue(state.getSubmittedValue());
            input.setLocalValueSet(state.isLocalValueSet());
        }

        List kids = component.getChildren();
        for (int i = 0; i < kids.size(); i++)
        {
            restoreDescendantState((UIComponent)kids.get(i), context);
        }
        Map facets = component.getFacets();
        for(Iterator i = facets.values().iterator(); i.hasNext();)
        {
            restoreDescendantState((UIComponent)i.next(), context);
        }
    }

    /**
     * A regular bean with accessor methods for all state variables.
     *
     * @author Sean Schofield
     * @author Hans Bergsten (Some code taken from an example in his O'Reilly JavaServer Faces book. Copied with permission)
     * @version $Revision: 703742 $ $Date: 2008-10-11 17:10:36 -0500 (sáb, 11 oct 2008) $
     */
    private static class SavedState implements Serializable
    {
        private static final long serialVersionUID = 273343276957070557L;
        private Object submittedValue;
        private boolean valid = true;
        private Object value;
        private boolean localValueSet;

        Object getSubmittedValue()
        {
            return submittedValue;
        }

        void setSubmittedValue(Object submittedValue)
        {
            this.submittedValue = submittedValue;
        }

        boolean isValid()
        {
            return valid;
        }

        void setValid(boolean valid)
        {
            this.valid = valid;
        }

        Object getValue()
        {
            return value;
        }

        void setValue(Object value)
        {
            this.value = value;
        }

        boolean isLocalValueSet()
        {
            return localValueSet;
        }

        void setLocalValueSet(boolean localValueSet)
        {
            this.localValueSet = localValueSet;
        }
    }

    /**
     * Inner class used to wrap the original events produced by child components in the tree.
     * This will allow the tree to find the appropriate component later when its time to
     * broadcast the events to registered listeners.  Code is based on a similar private
     * class for UIData.
     */
    private static class FacesEventWrapper extends FacesEvent
    {
        private static final long serialVersionUID = -3056153249469828447L;
        private FacesEvent _wrappedFacesEvent;
        private String _nodeId;


        public FacesEventWrapper(FacesEvent facesEvent, String nodeId, UIComponent component)
        {
            super(component);
            _wrappedFacesEvent = facesEvent;
            _nodeId = nodeId;
        }


        public PhaseId getPhaseId()
        {
            return _wrappedFacesEvent.getPhaseId();
        }


        public void setPhaseId(PhaseId phaseId)
        {
            _wrappedFacesEvent.setPhaseId(phaseId);
        }


        public void queue()
        {
            _wrappedFacesEvent.queue();
        }


        public String toString()
        {
            return _wrappedFacesEvent.toString();
        }


        public boolean isAppropriateListener(FacesListener faceslistener)
        {
            // this event type is only intended for wrapping a real event
            return false;
        }


        public void processListener(FacesListener faceslistener)
        {
            throw new UnsupportedOperationException("This event type is only intended for wrapping a real event");
        }


        public FacesEvent getFacesEvent()
        {
            return _wrappedFacesEvent;
        }


        public String getNodeId()
        {
            return _nodeId;
        }
    }

    /**
     * Returns true if there is an error message queued for at least one of the nodes.
     *
     * @param context FacesContext
     * @return whether an error message is present
     */
    private boolean keepSaved(FacesContext context)
    {
        Iterator clientIds = _saved.keySet().iterator();
        while (clientIds.hasNext())
        {
            String clientId = (String) clientIds.next();
            Iterator messages = context.getMessages(clientId);
            while (messages.hasNext())
            {
                FacesMessage message = (FacesMessage) messages.next();
                if (message.getSeverity().compareTo(FacesMessage.SEVERITY_ERROR) >= 0)
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Toggle the expanded state of the current node.
     */
    public void toggleExpanded()
    {
        getDataModel().getTreeState().toggleExpanded(getNodeId());
    }

    /**
     * Indicates whether or not the current {@link TreeNode} is expanded.
     * @return boolean
     */
    public boolean isNodeExpanded()
    {
        return getDataModel().getTreeState().isNodeExpanded(getNodeId());
    }

    /**
     * Implements the {@link jakarta.faces.event.ActionListener} interface.  Basically, this
     * method is used to listen for node selection events (when a user has clicked on a
     * leaf node.)
     *
     * @param event ActionEvent
     */
    public void setNodeSelected(ActionEvent event)
    {
        getDataModel().getTreeState().setSelected(getNodeId());
    }

    /**
     * Indicates whether or not the current {@link TreeNode} is selected.
     * @return boolean
     */
    public boolean isNodeSelected()
    {
        return (getNodeId() != null) ? getDataModel().getTreeState().isSelected(getNodeId()) : false;
    }
    
    /**
     * 
     * {@inheritDoc}
     * 
     * @since 2.0
     */
    public String createUniqueId(FacesContext context, String seed)
    {
        StringBuilder bld = new StringBuilder();

        // Generate an identifier for a component. The identifier will be prefixed with UNIQUE_ID_PREFIX, and will be unique within this UIViewRoot. 
        if(seed==null)
        {
            Long uniqueIdCounter = (Long) getStateHelper().get(PropertyKeys.uniqueIdCounter);
            uniqueIdCounter = (uniqueIdCounter == null) ? 0 : uniqueIdCounter;
            getStateHelper().put(PropertyKeys.uniqueIdCounter, (uniqueIdCounter+1L));
            return bld.append(UIViewRoot.UNIQUE_ID_PREFIX).append(uniqueIdCounter).toString();
        }
        // Optionally, a unique seed value can be supplied by component creators which should be included in the generated unique id.
        else
        {
            return bld.append(UIViewRoot.UNIQUE_ID_PREFIX).append(seed).toString();
        }
    }
    
    enum PropertyKeys
    {
        uniqueIdCounter
    }
}
