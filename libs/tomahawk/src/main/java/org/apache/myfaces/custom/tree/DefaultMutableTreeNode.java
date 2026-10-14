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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Default implementation of {@link MutableTreeNode}.
 *
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller </a>
 * @version $Revision: 472638 $ $Date: 2006-11-08 15:54:13 -0500 (Wed, 08 Nov 2006) $
 */
public class DefaultMutableTreeNode implements MutableTreeNode, Serializable
{

    private List children = new ArrayList();

    private Object userObject;

    MutableTreeNode parent;

    private boolean allowsChildren = true;

    /**
     * @param userObject The userObject.
     */
    public DefaultMutableTreeNode(Object userObject)
    {
        this.userObject = userObject;
    }

    /**
     * @param children The children.
     * @param allowsChildren The allowsChildren.
     */
    public DefaultMutableTreeNode(List children, boolean allowsChildren)
    {
        this.children = children;
        this.allowsChildren = allowsChildren;
    }

    /**
     * @param userObject The userobject.
     * @param parent The parent.
     * @param allowsChildren The allowsChildren.
     */
    public DefaultMutableTreeNode(Object userObject, MutableTreeNode parent, boolean allowsChildren)
    {
        this.userObject = userObject;
        this.parent = parent;
        this.allowsChildren = allowsChildren;
    }

    /**
     * @see org.apache.myfaces.custom.tree.MutableTreeNode#insert(org.apache.myfaces.custom.tree.MutableTreeNode)
     */
    public void insert(MutableTreeNode child)
    {
        children.add(child);
        child.setParent(this);
    }

    /**
     * @see org.apache.myfaces.custom.tree.MutableTreeNode#insert(org.apache.myfaces.custom.tree.MutableTreeNode, int)
     */
    public void insert(MutableTreeNode child, int index)
    {
        children.add(index, child);
        child.setParent(this);
    }

    /**
     * @see org.apache.myfaces.custom.tree.MutableTreeNode#remove(int)
     */
    public void remove(int index)
    {
        MutableTreeNode child = (MutableTreeNode) children.remove(index);
        child.setParent(null);
    }

    /**
     * @see org.apache.myfaces.custom.tree.MutableTreeNode#remove(org.apache.myfaces.custom.tree.MutableTreeNode)
     */
    public void remove(MutableTreeNode node)
    {
        if (children.remove(node))
        {
            node.setParent(null);
        }
    }

    /**
     * @see org.apache.myfaces.custom.tree.MutableTreeNode#setUserObject(java.lang.Object)
     */
    public void setUserObject(Object object)
    {
        this.userObject = object;
    }

    /**
     * @see org.apache.myfaces.custom.tree.TreeNode#getUserObject()
     */
    public Object getUserObject()
    {
        return userObject;
    }

    /**
     * @see org.apache.myfaces.custom.tree.MutableTreeNode#removeFromParent()
     */
    public void removeFromParent()
    {
        if (parent == null)
        {
            return;
        }
        parent.remove(this);
    }

    /**
     * @see org.apache.myfaces.custom.tree.MutableTreeNode#setParent(org.apache.myfaces.custom.tree.MutableTreeNode)
     */
    public void setParent(MutableTreeNode parent)
    {
        this.parent = parent;
    }

    /**
     * @see org.apache.myfaces.custom.tree.TreeNode#getChildAt(int)
     */
    public TreeNode getChildAt(int index)
    {
        return (TreeNode) children.get(index);
    }

    /**
     * @see org.apache.myfaces.custom.tree.TreeNode#getChildCount()
     */
    public int getChildCount()
    {
        return children.size();
    }

    /**
     * @see org.apache.myfaces.custom.tree.TreeNode#getParent()
     */
    public TreeNode getParent()
    {
        return parent;
    }

    /**
     * @see org.apache.myfaces.custom.tree.TreeNode#getIndex(org.apache.myfaces.custom.tree.TreeNode)
     */
    public int getIndex(TreeNode node)
    {
        return children.indexOf(node);
    }

    /**
     * @see org.apache.myfaces.custom.tree.TreeNode#getAllowsChildren()
     */
    public boolean getAllowsChildren()
    {
        return allowsChildren;
    }

    /**
     * @see org.apache.myfaces.custom.tree.TreeNode#isLeaf()
     */
    public boolean isLeaf()
    {
        return children.isEmpty();
    }

    /**
     * @see org.apache.myfaces.custom.tree.TreeNode#children()
     */
    public Iterator children()
    {
        return children == null ? 
                null : Collections.unmodifiableCollection(children).iterator();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        if (userObject != null)
        {
            return userObject.toString();
        }
        return super.toString();
    }
}