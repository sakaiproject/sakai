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

import org.apache.myfaces.custom.tree.model.TreeModel;
import org.apache.myfaces.custom.tree.model.TreePath;

import jakarta.faces.component.html.HtmlCommandLink;
import jakarta.faces.context.FacesContext;
import java.util.Iterator;
import java.util.List;


/**
 * Represents a single node of a three. A custom html link component representing the expand/collapse icon
 * is held as a facet named <code>expandCollapse</code>.
 *
 * @JSFComponent
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller</a>
 * @version $Revision: 659874 $ $Date: 2008-05-24 15:59:15 -0500 (sáb, 24 may 2008) $
 */
public class HtmlTreeNode
    extends HtmlCommandLink
{

    private static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.HtmlTreeNode";

    public static final String COMPONENT_TYPE = "org.apache.myfaces.HtmlTreeNode";
    public static final String EXPAND_COLLAPSE_FACET = "expandCollapse";

    public static final int OPEN = 0;
    public static final int OPEN_FIRST = 1;
    public static final int OPEN_LAST = 2;
    public static final int OPEN_SINGLE = 3;
    public static final int CLOSED = 10;
    public static final int CLOSED_FIRST = 11;
    public static final int CLOSED_LAST = 12;
    public static final int CLOSED_SINGLE = 13;
    public static final int CHILD = 20;
    public static final int CHILD_FIRST = 21;
    public static final int CHILD_LAST = 22;
    public static final int LINE = 30;
    public static final int EMPTY = 40;
    private static final int OFFSET = 10;
    private static final int MOD_FIRST = 1;
    private static final int MOD_LAST = 2;

    private transient TreePath path;
    private int[] translatedPath;
    private transient Object userObject;
    private boolean expanded = false;
    private boolean selected = false;
    private int[] layout;


    public HtmlTreeNode()
    {
        setRendererType(DEFAULT_RENDERER_TYPE);
    }


    public int getLevel()
    {
        return layout.length - 1;
    }


    public int getMaxChildLevel()
    {
        if (getChildCount() == 0)
        {
            return getLevel();
        }

        int max = getLevel();
        for (Iterator iterator = getChildren().iterator(); iterator.hasNext();)
        {
            HtmlTreeNode child = (HtmlTreeNode) iterator.next();
            max = Math.max(max, child.getMaxChildLevel());
        }
        return max;
    }


    public TreePath getPath()
    {
        if (path == null)
        {
            if (translatedPath == null)
            {
                throw new IllegalStateException("No path and no translated path available");
            }
            path = translatePath(translatedPath, getTreeModel(FacesContext.getCurrentInstance()));
        }
        return path;
    }


    public void setPath(TreePath path)
    {
        this.path = path;
    }


    int[] getTranslatedPath()
    {
        if (translatedPath != null)
        {
            return translatedPath;
        }
        
        return translatePath(getPath(), getTreeModel(FacesContext.getCurrentInstance()));
    }


    public Object getUserObject()
    {
        if (userObject == null)
        {
            userObject = getPath().getLastPathComponent();
        }
        return userObject;
    }


    public void setUserObject(Object userObject)
    {
        this.userObject = userObject;
        setValue(userObject.toString());
    }


    public boolean isExpanded()
    {
        return expanded;
    }


    void childrenAdded(int[] children, FacesContext context)
    {
        if (getChildCount() == 0 && children.length > 0)
        {
            // change to CLOSED_*
            layout[layout.length - 1] -= OFFSET;
        }

        if (!expanded)
        {
            // nothing to do
            return;
        }

        TreeModel model = getTreeModel(context);
        int childCount = model.getChildCount(getUserObject());
        int pathUpdateIndex = getTranslatedPath().length;

        for (int i = 0; i < children.length; i++)
        {
            int index = children[i];
            translateChildrenPath(pathUpdateIndex, index, 1);
            Object userObject = model.getChild(getUserObject(), index);
            addNode(model, userObject, index, childCount, context);
        }
    }


    void childrenChanged(int[] children, FacesContext context)
    {
        if (!expanded)
        {
            // nothing to do
            return;
        }

        TreeModel model = getTreeModel(context);

        for (int i = 0; i < children.length; i++)
        {
            int index = children[i];
            Object userObject = model.getChild(getUserObject(), index);
            HtmlTreeNode node = (HtmlTreeNode) getChildren().get(index);
            node.setUserObject(userObject);
            // todo: modify path????
        }
    }


    private void addNode(TreeModel model, Object userObject, int index, int childCount, FacesContext context)
    {
        HtmlTreeNode node = createNode(model, userObject, childCount, index, context);
        List children = getChildren();

        if (!children.isEmpty())
        {
            if (index == 0)
            {
                HtmlTreeNode first = (HtmlTreeNode) getChildren().get(0);
                int[] layout = first.getLayout();
                if (layout[layout.length - 1] % OFFSET == MOD_FIRST)
                {
                    // change from *_FIRST to *
                    layout[layout.length - 1]--;
                }
            } else if (index == childCount - 1)
            {
                HtmlTreeNode last = (HtmlTreeNode) getChildren().get(childCount - 2);
                int[] layout = last.getLayout();
                if (layout[layout.length - 1] % OFFSET == MOD_LAST)
                {
                    // change from *_LAST to *
                    layout[layout.length - 1] -= 2;
                }
            }
        }

        children.add(index, node);
    }


    void childrenRemoved(int[] children)
    {
        if (!expanded)
        {
            // nothing to do
            return;
        }
        List childNodes = getChildren();
        int pathUpdateIndex = getTranslatedPath().length;

        for (int i = children.length - 1; i >= 0; i--)
        {
            translateChildrenPath(pathUpdateIndex, children[i], -1);
            HtmlTreeNode child = (HtmlTreeNode) childNodes.get(children[i]);

            if (child.isSelected()) {
                child.setSelected(false);
                if (children[i] < childNodes.size() - 1) {
                    ((HtmlTreeNode) childNodes.get(children[i] + 1)).setSelected(true);
                } else {
                    if (children[i] > 0) {
                        ((HtmlTreeNode) childNodes.get(children[i] - 1)).setSelected(true);
                    } else {
                        setSelected(true);
                    }
                }
            }
            childNodes.remove(children[i]);
        }
    }


    /**
     * Update the translatedPath of all child nodes so the path points to the same object in the model
     * after adding/removing a node
     *
     * @param pathUpdateIndex
     * @param startIndex
     * @param offset
     */
    private void translateChildrenPath(int pathUpdateIndex, int startIndex, int offset) {
        List children = getChildren();
        int childrenSize = children.size();

        for (int i = startIndex; i < childrenSize; i++) {
            HtmlTreeNode node = (HtmlTreeNode) children.get(i);
            node.updateTranslatedPath(pathUpdateIndex, offset);
        }
    }


    private void updateTranslatedPath(int pathUpdateIndex, int offset)
    {
        translatedPath[pathUpdateIndex] += offset;
        // reset path and userObject so the values are acquired from the model
        path = null;
        userObject = null;

        if (isExpanded()) {
            // continue with the children of this node
            translateChildrenPath(pathUpdateIndex, 0, offset);
        }
    }


    public void setExpanded(boolean expanded)
    {
        if (this.expanded == expanded)
        {
            // no change
            return;
        }
        this.expanded = expanded;

        if (expanded)
        {
            FacesContext context = FacesContext.getCurrentInstance();
            TreeModel model = getTreeModel(context);
            int childCount = model.getChildCount(getUserObject());

            for (int i = 0; i < childCount; i++)
            {
                Object child = model.getChild(getUserObject(), i);
                HtmlTreeNode node = createNode(model, child, childCount, i, context);
                getChildren().add(node);
            }
            layout[layout.length - 1] -= OFFSET;
        } else
        {
            if (clearSelection())
            {
                setSelected(true);
            }
            getChildren().clear();
            layout[layout.length - 1] += OFFSET;
        }

    }


    private HtmlTreeNode createNode(TreeModel model, Object child, int childCount, int index, FacesContext context)
    {
        HtmlTreeNode node = (HtmlTreeNode) context.getApplication().createComponent(HtmlTreeNode.COMPONENT_TYPE);
        String id = getTree().createUniqueId(context);
        node.setId(id);

        node.setPath(getPath().pathByAddingChild(child));
        node.setUserObject(child);
        int state = CHILD;

        if (model.isLeaf(child))
        {

            if (childCount > 1)
            {
                if (index == 0)
                {
                    state = CHILD;
                } else if (index == childCount - 1)
                {
                    state = CHILD_LAST;
                }
            } else
            {
                state = CHILD_LAST;
            }
        } else
        {
            if (childCount > 1)
            {
                if (index == 0)
                {
                    state = CLOSED;
                } else if (index == childCount - 1)
                {
                    state = CLOSED_LAST;
                } else
                {
                    state = CLOSED;
                }
            } else
            {
                state = CLOSED_LAST;
            }

        }
        node.setLayout(layout, state);

        return node;
    }


    public void toggleExpanded()
    {
        setExpanded(!expanded);
    }


    public boolean isSelected()
    {
        return selected;
    }


    public void setSelected(boolean selected)
    {
        if (selected)
        {
            getTree().getRootNode().clearSelection();
        }
        this.selected = selected;
        getTree().selectionChanged(this);
    }


    public void toggleSelected()
    {
        setSelected(!selected);
    }


    private boolean clearSelection()
    {
        if (isSelected())
        {
            selected = false;
            return true;
        }
        for (Iterator iterator = getChildren().iterator(); iterator.hasNext();)
        {
            HtmlTreeNode node = (HtmlTreeNode) iterator.next();
            if (node.clearSelection())
            {
                return true;
            }
        }
        return false;
    }


    public int[] getLayout()
    {
        return layout;
    }


    public void setLayout(int[] layout)
    {
        this.layout = layout;
    }


    public void setLayout(int[] parentLayout, int layout)
    {
        this.layout = new int[parentLayout.length + 1];
        this.layout[parentLayout.length] = layout;

        for (int i = 0; i < parentLayout.length; i++)
        {
            int state = parentLayout[i];
            if (state == OPEN || state == OPEN_FIRST || state == CLOSED || state == CLOSED_FIRST || state == CHILD || state == CHILD_FIRST || state == LINE)
            {
                this.layout[i] = LINE;
            } else
            {
                this.layout[i] = EMPTY;
            }
        }
    }


    public HtmlTreeImageCommandLink getExpandCollapseCommand(FacesContext context)
    {
        HtmlTreeImageCommandLink command = (HtmlTreeImageCommandLink) getFacet(EXPAND_COLLAPSE_FACET);

        if (command == null)
        {
            command = (HtmlTreeImageCommandLink) context.getApplication().createComponent(HtmlTreeImageCommandLink.COMPONENT_TYPE);
            String id = getTree().createUniqueId(context);
            command.setId(id);
            getFacets().put(EXPAND_COLLAPSE_FACET, command);
        }
        return command;
    }


    public Object saveState(FacesContext context)
    {
        Object values[] = new Object[5];
        values[0] = super.saveState(context);
        values[1] = Boolean.valueOf(expanded);
        values[2] = layout;
        values[3] = translatePath(getPath(), getTreeModel(context));
        values[4] = Boolean.valueOf(selected);
        return values;
    }


    public void restoreState(FacesContext context, Object state)
    {
        Object values[] = (Object[]) state;
        super.restoreState(context, values[0]);
        expanded = ((Boolean) values[1]).booleanValue();
        layout = (int[]) values[2];
        translatedPath = (int[]) values[3];
        selected = ((Boolean) values[4]).booleanValue();
    }


    protected static int[] translatePath(TreePath treePath, TreeModel model)
    {
        Object[] path = treePath.getPath();
        int[] translated = new int[path.length - 1];

        Object parent = path[0];

        for (int i = 1; i < path.length; i++)
        {
            Object element = path[i];
            translated[i - 1] = model.getIndexOfChild(parent, element);
            parent = element;
        }
        return translated;
    }


    protected static TreePath translatePath(int[] path, TreeModel model)
    {
        Object[] translated = new Object[path.length + 1];
        Object parent = model.getRoot();

        translated[0] = parent;

        for (int i = 0; i < path.length; i++)
        {
            int index = path[i];
            translated[i + 1] = model.getChild(parent, index);
            parent = translated[i + 1];
        }
        return new TreePath(translated);
    }


    protected TreeModel getTreeModel(FacesContext context)
    {
        return getTree().getModel(context);
    }


    protected HtmlTree getTree()
    {
        if (getParent() instanceof HtmlTree)
        {
            return (HtmlTree) getParent();
        }
        return ((HtmlTreeNode) getParent()).getTree();
    }


    public boolean isLeaf(FacesContext context)
    {
        return getTreeModel(context).isLeaf(getUserObject());
    }


    public void expandPath(int[] translatedPath, int current)
    {
        if (current >= translatedPath.length)
        {
            return;
        }

        HtmlTreeNode child = (HtmlTreeNode) getChildren().get(translatedPath[current]);

        if (!child.isExpanded())
        {
            child.setExpanded(true);
        }

        child.expandPath(translatedPath, current + 1);
    }


    public void restoreItemState(HtmlTreeNode node)
    {
        setExpanded(node.isExpanded());
        selected = node.isSelected();
        List children = getChildren();
        List otherChildren = node.getChildren();
        for (int i = 0; i < children.size(); i++)
        {
            HtmlTreeNode child = (HtmlTreeNode) children.get(i);
            child.restoreItemState((HtmlTreeNode) otherChildren.get(i));
        }
    }
}
