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

public interface TreeWalker
{
    /**
     * Getter for the check state property.  Indicates whether or not the TreeWalker
     * should navigate over nodes that are not currently expanded.
     *
     * @return boolean
     */
    public boolean isCheckState();

    /**
     * Setter for the check state property.  Indicates whether or not the TreeWalker
     * should navigate over nodes that are not currently expanded.
     *
     * @param checkState boolean
     */
    public void setCheckState(boolean checkState);

    /**
     * Walk the tree and set the current node to the next node.
     * @return boolean whether or not there was another node to walk
     */
    public boolean next();

    /**
     * Returns the id of the root node.
     * @return String
     */
    public String getRootNodeId();

    /**
     * This method allows the renderer to pass a reference to the tree object.  With this
     * reference the TreeWalker can set the current node as its walking the tree.
     *
     * @param tree Tree
     */
    public void setTree(Tree tree);

    /**
     * Reset the walker so the tree can be walked again starting from the root.
     */
    public void reset();
}
