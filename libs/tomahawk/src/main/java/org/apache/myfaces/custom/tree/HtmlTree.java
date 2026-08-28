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
package org.apache.myfaces.custom.tree;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.html.HtmlPanelGroup;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.ValueBinding;

import org.apache.myfaces.custom.tree.event.TreeSelectionEvent;
import org.apache.myfaces.custom.tree.event.TreeSelectionListener;
import org.apache.myfaces.custom.tree.model.TreeModel;
import org.apache.myfaces.custom.tree.model.TreeModelEvent;
import org.apache.myfaces.custom.tree.model.TreeModelListener;
import org.apache.myfaces.custom.tree.model.TreePath;


/**
 * A tree data component. 
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * <p>
 * Tree implementation based on javax.swing.JTree.
 * </p>
 * <p>
 * The tree model is assigned by using a value binding named <code>model</code>
 * and is not stored in view state.
 * </p>
 * <p>
 * A hierarchy of {@link HtmlTreeNode}objects is used to represent the current
 * expanded state of the tree. The root node is held as a faces named * 
 * <code>rootNode</code>.
 * </p>
 *
 * @JSFComponent
 *   name = "t:tree"
 *   tagClass = "org.apache.myfaces.custom.tree.taglib.TreeTag"
 *   tagSuperclass = "org.apache.myfaces.custom.tree.taglib.AbstractTreeTag"
 *   type = "org.apache.myfaces.HtmlTree"
 *   tagHandler = "org.apache.myfaces.custom.tree.taglib.TreeTagHandler"
 *
 * @JSFJspProperty name = "headerClass" returnType = "java.lang.String"
 * @JSFJspProperty name = "footerClass" returnType = "java.lang.String"
 * @JSFJspProperty name = "expandRoot" returnType = "boolean" literalOnly="true" inheritedTag="true"
 * @JSFJspProperty name = "style" tagExcluded = "true"
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller </a>
 * @version $Revision: 672986 $ $Date: 2008-06-30 23:13:55 -0500 (lun, 30 jun 2008) $
 */
public class HtmlTree extends HtmlPanelGroup implements TreeModelListener
{
    public static final String COMPONENT_TYPE = "org.apache.myfaces.HtmlTree";
    public static final String COMPONENT_FAMILY = "org.apache.myfaces.HtmlTree";
    private static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.HtmlTree";
    
    public static final long DEFAULT_EXPIRE_LISTENERS = 8 * 60 * 60 * 1000; // 8 hours
    private static final String FACET_ROOTNODE = "rootNode";
    private static final String PREVIOUS_VIEW_ROOT = HtmlTree.class.getName() + ".PREVIOUS_VIEW_ROOT";
    private static final int EVENT_CHANGED = 0;
    private static final int EVENT_INSERTED = 1;
    private static final int EVENT_REMOVED = 2;
    private static final int EVENT_STRUCTURE_CHANGED = 3;
    private static int counter = 0;

    private IconProvider iconProvider;
    private boolean itemStatesRestored = false;
    private String var;
    private String nodeClass;
    private String rowClasses;
    private String columnClasses;
    private String selectedNodeClass;
    private String iconClass;
    private String iconLine;
    private String iconNoline;
    private String iconChildFirst;
    private String iconChildMiddle;
    private String iconChildLast;
    private String iconNodeOpen;
    private String iconNodeOpenFirst;
    private String iconNodeOpenMiddle;
    private String iconNodeOpenLast;
    private String iconNodeClose;
    private String iconNodeCloseFirst;
    private String iconNodeCloseMiddle;
    private String iconNodeCloseLast;
    private int uniqueIdCounter = 0;
    private int[] selectedPath;
    private int internalId;
    private Long expireListeners;


    /**
     * <p/>
     * Default constructor.
     * </p>
     */
    public HtmlTree()
    {
        internalId = counter++;
    }


    /**
     * @JSFProperty
     *   jspName = "value"
     *   required = "true"
     *   inheritedTag = "true"
     */
    public TreeModel getModel(FacesContext context)
    {
        ValueBinding binding = getValueBinding("model");

        if (binding != null)
        {
            TreeModel model = (TreeModel) binding.getValue(context);
            if (model != null)
            {
                return model;
            }
        }
        return null;
    }


    public String createUniqueId(FacesContext context)
    {
        return getClientId(context).replaceAll(":", "_") + "_node_" + uniqueIdCounter++;
    }


    public void addTreeSelectionListener(TreeSelectionListener listener)
    {
        addFacesListener(listener);
    }


    public IconProvider getIconProvider()
    {
        return iconProvider;
    }


    public void setIconProvider(IconProvider iconProvider)
    {
        this.iconProvider = iconProvider;
    }


    /**
     * @JSFProperty
     * @return Returns the var.
     */
    public String getVar()
    {
        return getStringValue(var, "var");
    }


    /**
     * @param var The var to set.
     */
    public void setVar(String var)
    {
        this.var = var;
    }

    protected String getStringValue(String value, String vbName)
    {
        if(value != null)
        {
            return value;
        }
        ValueBinding vb = getValueBinding(vbName);
        if(vb != null)
        {
            Object obj = vb.getValue(getFacesContext());
            if(obj != null)
            {
                return obj.toString();
            }
        }
        return null;
    }

    /**
     * @JSFProperty
     */
    public String getIconLine()
    {
        return getStringValue(iconLine, "iconLine");
    }

    public void setIconLine(String iconLine)
    {
        this.iconLine = iconLine;
    }

    /**
     * @JSFProperty
     */
    public String getIconNoline()
    {
        return getStringValue(iconNoline, "iconNoline");
    }


    public void setIconNoline(String iconNoline)
    {
        this.iconNoline = iconNoline;
    }

    /**
     * @JSFProperty
     */
    public String getIconChildFirst()
    {
        return getStringValue(iconChildFirst, "iconChildFirst");
    }


    public void setIconChildFirst(String iconChildFirst)
    {
        this.iconChildFirst = iconChildFirst;
    }


    /**
     * @JSFProperty
     */
    public String getIconChildMiddle()
    {
        return getStringValue(iconChildMiddle, "iconChildMiddle");
    }


    public void setIconChildMiddle(String iconChildMiddle)
    {
        this.iconChildMiddle = iconChildMiddle;
    }


    /**
     * @JSFProperty
     */
    public String getIconChildLast()
    {
        return getStringValue(iconChildLast, "iconChildLast");
    }


    public void setIconChildLast(String iconChildLast)
    {
        this.iconChildLast = iconChildLast;
    }


    /**
     * @JSFProperty
     */
    public String getIconNodeOpen()
    {
        return getStringValue(iconNodeOpen, "iconNodeOpen");
    }


    public void setIconNodeOpen(String iconNodeOpen)
    {
        this.iconNodeOpen = iconNodeOpen;
    }


    /**
     * @JSFProperty
     */
    public String getIconNodeOpenFirst()
    {
        return getStringValue(iconNodeOpenFirst, "iconNodeOpenFirst");
    }


    public void setIconNodeOpenFirst(String iconNodeOpenFirst)
    {
        this.iconNodeOpenFirst = iconNodeOpenFirst;
    }


    /**
     * @JSFProperty
     */
    public String getIconNodeOpenMiddle()
    {
        return getStringValue(iconNodeOpenMiddle, "iconNodeOpenMiddle");
    }


    public void setIconNodeOpenMiddle(String iconNodeOpenMiddle)
    {
        this.iconNodeOpenMiddle = iconNodeOpenMiddle;
    }


    /**
     * @JSFProperty
     */
    public String getIconNodeOpenLast()
    {
        return getStringValue(iconNodeOpenLast, "iconNodeOpenLast");
    }


    public void setIconNodeOpenLast(String iconNodeOpenLast)
    {
        this.iconNodeOpenLast = iconNodeOpenLast;
    }


    /**
     * @JSFProperty
     */
    public String getIconNodeClose()
    {
        return getStringValue(iconNodeClose, "iconNodeClose");
    }


    public void setIconNodeClose(String iconNodeClose)
    {
        this.iconNodeClose = iconNodeClose;
    }


    /**
     * @JSFProperty
     */
    public String getIconNodeCloseFirst()
    {
        return getStringValue(iconNodeCloseFirst, "iconNodeCloseFirst");
    }


    public void setIconNodeCloseFirst(String iconNodeCloseFirst)
    {
        this.iconNodeCloseFirst = iconNodeCloseFirst;
    }


    /**
     * @JSFProperty
     */
    public String getIconNodeCloseMiddle()
    {
        return getStringValue(iconNodeCloseMiddle, "iconNodeCloseMiddle");
    }


    public void setIconNodeCloseMiddle(String iconNodeCloseMiddle)
    {
        this.iconNodeCloseMiddle = iconNodeCloseMiddle;
    }


    /**
     * @JSFProperty
     */
    public String getIconNodeCloseLast()
    {
        return getStringValue(iconNodeCloseLast, "iconNodeCloseLast");
    }


    public void setIconNodeCloseLast(String iconNodeCloseLast)
    {
        this.iconNodeCloseLast = iconNodeCloseLast;
    }


    /**
     * @JSFProperty
     */
    public String getNodeClass()
    {
        return getStringValue(nodeClass, "nodeClass");
    }


    public void setNodeClass(String nodeClass)
    {
        this.nodeClass = nodeClass;
    }


    /**
     * @JSFProperty
     * @return Returns the rowClasses.
     */
    public String getRowClasses()
    {
        return getStringValue(rowClasses, "rowClasses");
    }


    /**
     * @param rowClasses The rowClasses to set.
     */
    public void setRowClasses(String rowClasses)
    {
        this.rowClasses = rowClasses;
    }


    /**
     * @JSFProperty
     * @return Returns the columnClasses.
     */
    public String getColumnClasses()
    {
        return getStringValue(columnClasses, "columnClasses");
    }


    /**
     * @param columnClasses The columnClasses to set.
     */
    public void setColumnClasses(String columnClasses)
    {
        this.columnClasses = columnClasses;
    }


    /**
     * @JSFProperty
     * @return Returns the selectedNodeClass.
     */
    public String getSelectedNodeClass()
    {
        return getStringValue(selectedNodeClass, "selectedNodeClass");
    }


    /**
     * @param selectedNodeClass The selectedNodeClass to set.
     */
    public void setSelectedNodeClass(String selectedNodeClass)
    {
        this.selectedNodeClass = selectedNodeClass;
    }


    /**
     * @JSFProperty
     */
    public String getIconClass()
    {
        return getStringValue(iconClass, "iconClass");
    }


    public void setIconClass(String iconClass)
    {
        this.iconClass = iconClass;
    }


    /**
     * Time interval the tree will remain registered as a TreeModelListener 
     * without being accessed
     * 
     * @JSFProperty
     */
    public long getExpireListeners()
    {
        if(expireListeners != null)
        {
            return expireListeners.longValue();
        }
        ValueBinding vb = getValueBinding("expireListeners");
        if(vb != null)
        {
            Object obj = vb.getValue(getFacesContext());
            if(obj instanceof java.lang.Number)
            {
                return ((java.lang.Number)obj).longValue();
            }
        }
        return DEFAULT_EXPIRE_LISTENERS;
    }


    public void setExpireListeners(long expireListeners)
    {
        this.expireListeners = new Long(expireListeners);
    }


    public String getFamily()
    {
        return "org.apache.myfaces.HtmlTree";
    }


    /**
     * Ensures that the node identified by the specified path is expanded and
     * viewable. If the last item in the path is a leaf, this will have no
     * effect.
     *
     * @param path the <code>TreePath</code> identifying a node
     */
    public void expandPath(TreePath path, FacesContext context)
    {
        // Only expand if not leaf!
        TreeModel model = getModel(context);

        if (path != null && model != null && !model.isLeaf(path.getLastPathComponent()))
        {
            int[] translatedPath = HtmlTreeNode.translatePath(path, getModel(context));
            HtmlTreeNode rootNode = getRootNode();
            if (rootNode == null)
            {
                createRootNode(context);
                rootNode = getRootNode();
            }
            if (!rootNode.isExpanded())
            {
                rootNode.setExpanded(true);
            }
            rootNode.expandPath(translatedPath, 0);

        }
    }


    /**
     * Ensures that the node identified by the specified path is collapsed and
     * viewable.
     *
     * @param path the <code>TreePath</code> identifying a node
     */
    public void collapsePath(TreePath path, FacesContext context)
    {
        HtmlTreeNode node = findNode(path, context);

        if (node != null)
        {
            node.setExpanded(false);
        }
    }


    public boolean isExpanded(TreePath path, FacesContext context)
    {
        if (path == null)
        {
            return false;
        }

        return findNode(path, context) != null;
    }


    private HtmlTreeNode findNode(TreePath path, FacesContext context)
    {
        HtmlTreeNode node = getRootNode();
        int[] translatedPath = HtmlTreeNode.translatePath(path, getModel(context));

        for (int i = 0; i < translatedPath.length; i++)
        {
            if (!node.isExpanded())
            {
                return null;
            }
            int index = translatedPath[i];
            node = (HtmlTreeNode) node.getChildren().get(index);
        }
        return node;
    }


    public TreePath getSelectionPath()
    {
        if (selectedPath == null)
        {
            return null;
        }
        return HtmlTreeNode.translatePath(selectedPath, getModel(FacesContext.getCurrentInstance()));
    }


    public void selectionChanged(HtmlTreeNode node)
    {
        TreePath oldPath = null;

        if (selectedPath != null)
        {
            oldPath = HtmlTreeNode.translatePath(selectedPath, getModel(FacesContext.getCurrentInstance()));
        }
        selectedPath = node.getTranslatedPath();
        if (node.isSelected())
        {
            queueEvent(new TreeSelectionEvent(this, oldPath, node.getPath()));
        } else
        {
            queueEvent(new TreeSelectionEvent(this, oldPath, null));
        }
    }


    private void createRootNode(FacesContext context)
    {
        HtmlTreeNode node;
        TreeModel model = getModel(context);
        Object root = model.getRoot();
        node = (HtmlTreeNode) context.getApplication().createComponent(HtmlTreeNode.COMPONENT_TYPE);
        String id = createUniqueId(context);
        node.setId(id);

        node.setPath(new TreePath(new Object[]{root}));
        node.setUserObject(root);
        node.setLayout(new int[]{HtmlTreeNode.CLOSED_SINGLE});
        getFacets().put(FACET_ROOTNODE, node);
    }


    public HtmlTreeNode getRootNode()
    {
        return (HtmlTreeNode) getFacet(FACET_ROOTNODE);
    }


    public Object saveState(FacesContext context)
    {
        Object values[] = new Object[24];
        values[0] = super.saveState(context);
        values[1] = iconChildFirst;
        values[2] = iconChildMiddle;
        values[3] = iconChildLast;
        values[4] = iconLine;
        values[5] = iconNodeClose;
        values[6] = iconNodeCloseFirst;
        values[7] = iconNodeCloseLast;
        values[8] = iconNodeCloseMiddle;
        values[9] = iconNodeOpen;
        values[10] = iconNodeOpenFirst;
        values[11] = iconNodeOpenLast;
        values[12] = iconNodeOpenMiddle;
        values[13] = iconNoline;
        values[14] = var;
        values[15] = nodeClass;
        values[16] = selectedNodeClass;
        values[17] = new Integer(uniqueIdCounter);
        values[18] = selectedPath;
        values[19] = iconClass;
        values[20] = new Integer(internalId);
        values[21] = expireListeners;
        values[22] = rowClasses;
        values[23] = columnClasses;
        return ((Object) (values));
    }


    public void restoreState(FacesContext context, Object state)
    {
        Object values[] = (Object[]) state;
        super.restoreState(context, values[0]);
        iconChildFirst = (String) values[1];
        iconChildMiddle = (String) values[2];
        iconChildLast = (String) values[3];
        iconLine = (String) values[4];
        iconNodeClose = (String) values[5];
        iconNodeCloseFirst = (String) values[6];
        iconNodeCloseLast = (String) values[7];
        iconNodeCloseMiddle = (String) values[8];
        iconNodeOpen = (String) values[9];
        iconNodeOpenFirst = (String) values[10];
        iconNodeOpenLast = (String) values[11];
        iconNodeOpenMiddle = (String) values[12];
        iconNoline = (String) values[13];
        var = (String) values[14];
        nodeClass = (String) values[15];
        selectedNodeClass = (String) values[16];
        uniqueIdCounter = ((Integer) values[17]).intValue();
        selectedPath = (int[]) values[18];
        iconClass = (String) values[19];
        internalId = ((Integer) values[20]).intValue();
        expireListeners = (Long) values[21];
        rowClasses = (String) values[22];
        columnClasses = (String) values[23];
        addToModelListeners();
    }


    public void decode(FacesContext context)
    {
        super.decode(context);

        //Save the current view root for later reference...
        context.getExternalContext().getRequestMap().put(PREVIOUS_VIEW_ROOT, context.getViewRoot());
        //...and remember that this instance needs NO special treatment on
        // rendering:
        itemStatesRestored = true;
    }


    public void processDecodes(FacesContext context)
    {
        addToModelListeners();
        super.processDecodes(context);
    }


    public void processValidators(FacesContext context)
    {
        addToModelListeners();
        super.processValidators(context);
    }


    public void processUpdates(FacesContext context)
    {
        addToModelListeners();
        super.processUpdates(context);
    }


    public void encodeBegin(FacesContext context) throws IOException
    {
        addToModelListeners();
        processModelEvents();

        HtmlTreeNode node = getRootNode();

        if (node == null)
        {
            createRootNode(context);
        }

        if (!itemStatesRestored)
        {
            UIViewRoot previousRoot = (UIViewRoot) context.getExternalContext().getRequestMap().get(PREVIOUS_VIEW_ROOT);
            if (previousRoot != null)
            {
                restoreItemStates(context, previousRoot);
            } else
            {
                //no previous root, means no decode was done
                //--> a new request
            }
        }

        super.encodeBegin(context);
    }


    public void encodeEnd(FacesContext context) throws IOException
    {
        super.encodeEnd(context);
    }


    public void restoreItemStates(FacesContext facesContext, UIViewRoot previousRoot)
    {
        HtmlTree previousTree = (HtmlTree) previousRoot.findComponent(getClientId(facesContext));

        if (previousTree != null)
        {
            HtmlTreeNode node = previousTree.getRootNode();

            if (node != null)
            {
                getRootNode().restoreItemState(node);
            }

            selectedPath = previousTree.selectedPath;
        }
    }


    public void treeNodesChanged(TreeModelEvent e)
    {
        TreePath path = e.getTreePath();
        FacesContext context = FacesContext.getCurrentInstance();
        HtmlTreeNode node = findNode(path, context);

        if (node != null)
        {
            node.childrenChanged(e.getChildIndices(), context);
        }
    }


    public void treeNodesInserted(TreeModelEvent e)
    {
        TreePath path = e.getTreePath();
        FacesContext context = FacesContext.getCurrentInstance();
        HtmlTreeNode node = findNode(path, context);

        if (node != null)
        {
            node.childrenAdded(e.getChildIndices(), context);
        }
    }


    public void treeNodesRemoved(TreeModelEvent e)
    {
        TreePath path = e.getTreePath();
        FacesContext context = FacesContext.getCurrentInstance();
        HtmlTreeNode node = findNode(path, context);

        if (node != null)
        {
            node.childrenRemoved(e.getChildIndices());
        }
    }


    public void treeStructureChanged(TreeModelEvent e)
    {
        TreePath path = e.getTreePath();
        FacesContext context = FacesContext.getCurrentInstance();

        if (isExpanded(path, context))
        {
            collapsePath(path, context);
            expandPath(path, context);
        }
    }


    public boolean equals(Object obj)
    {
        if (!(obj instanceof HtmlTree))
        {
            return false;
        }
        HtmlTree other = (HtmlTree) obj;

        return other.getId().equals(getId());
    }


    public int hashCode()
    {
        return getClientId(FacesContext.getCurrentInstance()).hashCode();
    }


    public void addToModelListeners()
    {
        Collection listeners = getModel(FacesContext.getCurrentInstance()).getTreeModelListeners();
        long currentTime = System.currentTimeMillis();
        boolean found = false;

        for (Iterator iterator = listeners.iterator(); iterator.hasNext();)
        {
            ModelListener listener = (ModelListener) iterator.next();

            if (listener.getId() == internalId)
            {
                found = true;
            } else if (currentTime - listener.getLastAccessTime() > getExpireListeners())
            {
                iterator.remove();
            }
        }
        if (!found)
        {
            listeners.add(new ModelListener(internalId));
        }
    }


    private void processModelEvents()
    {
        Collection listeners = getModel(FacesContext.getCurrentInstance()).getTreeModelListeners();

        for (Iterator iterator = listeners.iterator(); iterator.hasNext();)
        {
            ModelListener listener = (ModelListener) iterator.next();

            if (listener.getId() == internalId)
            {
                for (Iterator events = listener.getEvents().iterator(); events.hasNext();)
                {
                    Event event = (Event) events.next();
                    event.process(this);
                    events.remove();
                }
                break;
            }
        }
    }


    public void collapseAll()
    {
        HtmlTreeNode root = getRootNode();
        FacesContext context = FacesContext.getCurrentInstance();
        collapsePath(root.getPath(), context);
        for (int i = 0; i < root.getChildren().size(); i++)
        {
            HtmlTreeNode child = (HtmlTreeNode) (root.getChildren().get(i));
            collapsePath(child.getPath(), context);
            if (!child.isLeaf(context))
            {
                collapseChildren(context, child);
            }
        }
    }


    private void collapseChildren(FacesContext context, HtmlTreeNode parent)
    {
        for (int i = 0; i < parent.getChildren().size(); i++)
        {
            HtmlTreeNode child = (HtmlTreeNode) (parent.getChildren().get(i));
            collapsePath(child.getPath(), context);
            if (!child.isLeaf(context))
            {
                collapseChildren(context, child);
            }
        }

    }


    public void expandAll()
    {
        HtmlTreeNode root = getRootNode();
        FacesContext context = FacesContext.getCurrentInstance();
        expandPath(root.getPath(), context);
        for (int i = 0; i < root.getChildren().size(); i++)
        {
            HtmlTreeNode child = (HtmlTreeNode) (root.getChildren().get(i));
            expandPath(child.getPath(), context);
            if (!child.isLeaf(context))
            {
                expandChildren(context, child);
            }
        }
    }


    private void expandChildren(FacesContext context, HtmlTreeNode parent)
    {
        for (int i = 0; i < parent.getChildren().size(); i++)
        {
            HtmlTreeNode child = (HtmlTreeNode) (parent.getChildren().get(i));
            expandPath(child.getPath(), context);
            if (!child.isLeaf(context))
            {
                expandChildren(context, child);
            }
        }
    }

    private static class ModelListener implements TreeModelListener, Serializable
    {

        private long lastAccessTime = System.currentTimeMillis();

        private LinkedList events = new LinkedList();

        int id;


        public ModelListener(int id)
        {
            this.id = id;
        }


        public List getEvents()
        {
            lastAccessTime = System.currentTimeMillis();
            return events;
        }


        public int getId()
        {
            return id;
        }


        public long getLastAccessTime()
        {
            return lastAccessTime;
        }


        public void treeNodesChanged(TreeModelEvent e)
        {
            events.addLast(new Event(EVENT_CHANGED, e));
        }


        public void treeNodesInserted(TreeModelEvent e)
        {
            events.addLast(new Event(EVENT_INSERTED, e));
        }


        public void treeNodesRemoved(TreeModelEvent e)
        {
            events.addLast(new Event(EVENT_REMOVED, e));
        }


        public void treeStructureChanged(TreeModelEvent e)
        {
            events.addLast(new Event(EVENT_STRUCTURE_CHANGED, e));
        }
    }


    private static class Event
    {

        private int kind;

        private TreeModelEvent event;


        public Event(int kind, TreeModelEvent event)
        {
            this.kind = kind;
            this.event = event;
        }


        public void process(HtmlTree tree)
        {
            switch (kind)
            {
                case EVENT_CHANGED:
                    tree.treeNodesChanged(event);
                    break;
                case EVENT_INSERTED:
                    tree.treeNodesInserted(event);
                    break;
                case EVENT_REMOVED:
                    tree.treeNodesRemoved(event);
                    break;
                case EVENT_STRUCTURE_CHANGED:
                    tree.treeStructureChanged(event);
                    break;
            }
        }
    }
}
