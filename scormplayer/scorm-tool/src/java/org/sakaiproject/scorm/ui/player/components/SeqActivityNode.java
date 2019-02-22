/**
 * Copyright (c) 2007-2019 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.scorm.ui.player.components;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import lombok.Getter;

import org.adl.sequencer.SeqActivity;

/**
 *
 * @author bjones86
 */
public class SeqActivityNode
{
    private DefaultMutableTreeNode node;

    @Getter
    private SeqActivityNode parent;

    public SeqActivityNode( DefaultMutableTreeNode node )
    {
        this.node = node;
        TreeNode parent = node.getParent();
        if( parent != null )
        {
            this.parent = new SeqActivityNode( (DefaultMutableTreeNode) parent );
        }
    }

    public List<SeqActivityNode> getChildren()
    {
        List<SeqActivityNode> children = new ArrayList<>( node.getChildCount() );
        for( int i = 0; i < node.getChildCount(); i++ )
        {
            children.add( new SeqActivityNode( (DefaultMutableTreeNode) node.getChildAt( i ) ) );
        }

        return children;
    }

    public String getID()
    {
        return ((SeqActivity) node.getUserObject()).getID();
    }

    public SeqActivity getSeqActivity()
    {
        return ((SeqActivity) node.getUserObject());
    }
}
