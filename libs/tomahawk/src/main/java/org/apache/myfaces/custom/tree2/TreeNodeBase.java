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
import java.util.ArrayList;

public class TreeNodeBase implements TreeNode, Comparable
{
    private static final long serialVersionUID = 278589014441538822L;
    private List children = new ArrayList();
    private String type;
    private String description;
    private boolean leaf;
    private String identifier;

    public TreeNodeBase()
    {}

    public TreeNodeBase(String type, String description, boolean leaf)
    {
        this(type, description, null, leaf);
    }

    public TreeNodeBase(String type, String description, String identifier, boolean leaf)
    {
        this.type = type;
        this.description = description;
        this.identifier = identifier;
        this.leaf = leaf;
    }

    public boolean isLeaf()
    {
        return leaf || (getChildCount() == 0);
    }

    public void setLeaf(boolean leaf)
    {
        this.leaf = leaf;
    }

    public List getChildren()
    {
        return children;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }

    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public int getChildCount()
    {
        return getChildren().size();
    }

    public int compareTo(Object obj)
    {
        // branches come before leaves, after this criteria nodes are sorted alphabetically
        TreeNode otherNode = (TreeNode)obj;

        if (isLeaf() && !otherNode.isLeaf())
        {
            // leaves come after branches
            return 1;
        }
        else if (!isLeaf() && otherNode.isLeaf())
        {
            // branches come before leaves
            return -1;
        }
        else
        {
            // both nodes are leaves or both node are branches, so compare the descriptions
            return getDescription().compareTo(otherNode.getDescription());
        }
    }
}
