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

import java.util.List;
import java.io.Serializable;

/**
 * Defines the requirements for an object that can be used as a tree node for
 * use in a {@link UITreeData} component. (inspired by javax.swing.tree.TreeNode).
 *
 * @author Sean Schofield
 * @version $Revision: 472638 $ $Date: 2006-11-08 15:54:13 -0500 (Wed, 08 Nov 2006) $
 */

public interface TreeNode extends Serializable
{
    public boolean isLeaf();

    public void setLeaf(boolean leaf);

    public List getChildren();

    /**
     * Gets the type of {@link TreeNode}.
     * @return The node type
     */
    public String getType();


    /**
     * Sets the type of {@link TreeNode}.
     * @param type The node type
     */
    public void setType(String type);


    public String getDescription();


    public void setDescription(String description);


    /**
     * Sets the identifier associated with the {@link TreeNode}.
     * @param identifier The identifier
     */
    public void setIdentifier(String identifier);


    /**
     * Gets the identifier asociated with the {@link TreeNode}.
     * @return the identifier
     */
    public String getIdentifier();


    /**
     * Gets the number of children this node has.
     * @return the number of children
     */
    public int getChildCount();
    
    /*
    public TreeNode getParentNode();
    
    public void setParentNode(TreeNode parent);
    */

}
