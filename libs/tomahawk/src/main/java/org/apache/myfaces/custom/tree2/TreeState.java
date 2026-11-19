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

import java.io.Serializable;

public interface TreeState extends Serializable
{

    /**
     * Indicates whether or not the specified {@link TreeNode} is expanded.
     *
     * @param nodeId The id of the node in question.
     * @return If the node is expanded.
     */
    public boolean isNodeExpanded(String nodeId);

    /**
     * Toggle the expanded state of the specified {@link TreeNode}.
     * @param nodeId The id of the node whose expanded state should be toggled.
     */
    public void toggleExpanded(String nodeId);

    /**
     * Expand the complete path specified.  If any node in the path is already expanded,
     * that node should be left as it is.
     *
     * @param nodePath The path to be expanded.
     */
    public void expandPath(String[] nodePath);

    /**
     * Collapse the complete path specified.  If any node in the path is already collapsed,
     * that node should be left as it is.
     *
     * @param nodePath The path to be collapsed.
     */
    public void collapsePath(String[] nodePath);

    /**
     * Getter for transient property.
     * @return boolean
     */
    public boolean isTransient();

    /**
     * Setter for transient property
     * @param trans boolean
     */
    public void setTransient(boolean trans);

    /**
     * Sets the id of the currently selected node
     * @param nodeId The id of the currently selected node
     */
    public void setSelected(String nodeId);

    /**
     * Indicates whether or not the specified node is selected.
     * @param nodeId String
     * @return boolean
     */
    public boolean isSelected(String nodeId);
}
