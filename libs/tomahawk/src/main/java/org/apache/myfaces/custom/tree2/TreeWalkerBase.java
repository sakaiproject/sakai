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

import org.apache.commons.lang.StringUtils;
import java.util.Stack;

/**
 * A base implementation of the {@link TreeWalker} interface.  Uses a simple node naming
 * scheme of "0" for the root node, "0:0" for the first child, "0:1" for the second child, etc.
 */
public class TreeWalkerBase implements TreeWalker
{
    private String ROOT_NODE_ID = "0";
    private String TREE_NODE_SEPARATOR = ":";

    private Tree tree;
    private Stack nodeStack = new Stack();
    private Stack idStack = new Stack();
    private boolean checkState = true;
    private boolean startedWalking = false;

    // see interface
    public void setTree(Tree tree)
    {
        this.tree = tree;
    }

    // see interface
    public boolean isCheckState()
    {
        return checkState;
    }

    // see interface
    public void setCheckState(boolean checkState)
    {
        this.checkState = checkState;
    }

    // see interface
    public boolean next()
    {
        if (!startedWalking)
        {
            // the first next() call just needs to set the root node and push it onto the stack
            idStack.push(ROOT_NODE_ID);
            tree.setNodeId(ROOT_NODE_ID);
            nodeStack.push(tree.getNode());

            startedWalking = true;
            return true;
        }

        if (nodeStack.isEmpty())
        {
            return false;
        }

        TreeNode prevNode = (TreeNode)nodeStack.peek();
        String prevNodeId = (String)idStack.peek();

        if (prevNode.isLeaf())
        {
            nodeStack.pop();
            idStack.pop();

            return next();
        }
        else
        {
            TreeNode nextNode = null;
            String nextNodeId = null;

            if (prevNodeId.equals(tree.getNodeId()))
            {
                /**
                 * We know there is at least one child b/c otherwise we would have popped the node after
                 * checking isLeaf.  Basically we need to keep drilling down until we reach the deepest
                 * node that is available for "walking."  Then we'll return to the parent and render its
                 * siblings and walk back up the tree.
                 */
                nextNodeId = prevNodeId + TREE_NODE_SEPARATOR + "0";

                // don't render any children if the node is not expanded
                if (checkState)
                {
                    if (!tree.getDataModel().getTreeState().isNodeExpanded(prevNodeId))
                    {
                        nodeStack.pop();
                        idStack.pop();

                        return next();
                    }
                }
            }
            else
            {
                // get the parent node
                String currentNodeId = tree.getNodeId();
                String parentNodeId = StringUtils.substringBeforeLast(currentNodeId, TREE_NODE_SEPARATOR);
                tree.setNodeId(parentNodeId);
                TreeNode parentNode = tree.getNode();

                int siblingCount = Integer.parseInt(currentNodeId.substring(parentNodeId.length()+1));
                siblingCount++;

                if (siblingCount == parentNode.getChildCount())
                {
                    // no more siblings
                    nodeStack.pop();
                    idStack.pop();

                    return next();
                }

                nextNodeId = parentNodeId + TREE_NODE_SEPARATOR + siblingCount;
            }

            tree.setNodeId(nextNodeId);
            nextNode = tree.getNode();

            nodeStack.push(nextNode);
            idStack.push(nextNodeId);

            return true;
        }
    }

    // see interface
    public String getRootNodeId()
    {
        return ROOT_NODE_ID;
    }

    // see interface
    public void reset()
    {
        nodeStack.empty();
        idStack.empty();
        startedWalking = false;
    }
}
