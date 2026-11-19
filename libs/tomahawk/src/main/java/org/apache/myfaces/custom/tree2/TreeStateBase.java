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

import java.util.HashSet;

public class TreeStateBase implements TreeState
{

    private static final long serialVersionUID = -6767283932185878071L;
    private HashSet _expandedNodes = new HashSet();
    private boolean _transient = false;
    private String _selected;

    // see interface
    public boolean isNodeExpanded(String nodeId)
    {
        return (_expandedNodes.contains(nodeId) /*&& !getNode().isLeaf()*/);
    }

    // see interface
    public void toggleExpanded(String nodeId)
    {
        if (_expandedNodes.contains(nodeId))
        {
            _expandedNodes.remove(nodeId);
        }
        else
        {
            _expandedNodes.add(nodeId);
        }
    }

    // see interface
    public boolean isTransient()
    {
        return _transient;
    }

    // see interface
    public void setTransient(boolean trans)
    {
        _transient = trans;
    }

    // see interface
    public void expandPath(String[] nodePath)
    {
        for (int i=0; i < nodePath.length; i++)
        {
            String nodeId = nodePath[i];
            _expandedNodes.add(nodeId);
        }
    }

    // see interface
    public void collapsePath(String[] nodePath)
    {
        for (int i=0; i < nodePath.length; i++)
        {
            String nodeId = nodePath[i];
            _expandedNodes.remove(nodeId);
        }
    }

    public void setSelected(String nodeId)
    {
        _selected = nodeId;
    }

    public boolean isSelected(String nodeId)
    {
        return nodeId.equals(_selected);
    }
}
