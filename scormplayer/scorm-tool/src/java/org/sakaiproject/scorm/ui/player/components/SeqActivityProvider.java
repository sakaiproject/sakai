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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import lombok.extern.slf4j.Slf4j;

import org.adl.sequencer.SeqActivity;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.service.api.ScormSequencingService;

/**
 *
 * @author bjones86
 */
@Slf4j
public class SeqActivityProvider implements ITreeProvider<SeqActivityNode>
{
    private static final long serialVersionUID = 1L;

    private SessionBean sessionBean;
    private ScormSequencingService sequencingService;

    /**
     * Construct.
     */
    public SeqActivityProvider( SessionBean sessionBean, ScormSequencingService sequencingService )
    {
        this.sessionBean = sessionBean;
        this.sequencingService = sequencingService;
    }

    /**
     * Nothing to do.
     */
    @Override
    public void detach() {}

    @Override
    public Iterator<SeqActivityNode> getRoots()
    {
        TreeModel treeModel = sequencingService.getTreeModel( sessionBean );
        if( treeModel == null )
        {
            log.warn( "Received null tree model from sequencing service" );
            return new ArrayList<SeqActivityNode>().iterator();
        }

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        List<SeqActivityNode> list = new ArrayList();
        list.add( new SeqActivityNode( root ) );
        return list.iterator();
    }

    @Override
    public boolean hasChildren( SeqActivityNode seqActivityNode )
    {
        return seqActivityNode.getParent() == null || !seqActivityNode.getChildren().isEmpty();
    }

    @Override
    public Iterator<SeqActivityNode> getChildren( final SeqActivityNode seqActivityNode )
    {
        return seqActivityNode.getChildren().iterator();
    }

    /**
     * Creates a {@link IModel<SeqActivityNode>}.
     */
    @Override
    public IModel<SeqActivityNode> model( SeqActivityNode seqActivityNode )
    {
        return new SeqAcivityNodeModel( seqActivityNode );
    }

    /**
     * A {@link SeqAcivityNodeModel} which uses an id to load its {@link SeqActivityNode}.
     *
     * If {@link SeqActivityNode}s were {@link Serializable} you could just use a standard {@link SeqAcivityNodeModel}.
     *
     * @see #equals(Object)
     * @see #hashCode()
     */
    public class SeqAcivityNodeModel extends LoadableDetachableModel<SeqActivityNode>
    {
        private static final long serialVersionUID = 1L;
        private final String id;

        public SeqAcivityNodeModel( SeqActivityNode seqActivityNode )
        {
            super( seqActivityNode );
            id = seqActivityNode.getID();
        }

        @Override
        protected SeqActivityNode load()
        {
            TreeModel treeModel = sequencingService.getTreeModel( sessionBean );
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();

            for( Enumeration<DefaultMutableTreeNode> e = root.breadthFirstEnumeration(); e.hasMoreElements(); )
            {
                DefaultMutableTreeNode node = e.nextElement();
                SeqActivity activity = (SeqActivity) node.getUserObject();

                String actID = activity == null ? null : activity.getID();
                if( StringUtils.isNotBlank( id ) && id.equals( actID ) )
                {
                    return new SeqActivityNode( node );
                }
            }

            return null;
        }

        @Override
        public int hashCode()
        {
            int hash = 7;
            return hash;
        }

        @Override
        public boolean equals( Object obj )
        {
            if( this == obj )
            {
                return true;
            }
            if( obj == null )
            {
                return false;
            }
            if( getClass() != obj.getClass() )
            {
                return false;
            }
            final SeqAcivityNodeModel other = (SeqAcivityNodeModel) obj;
            if( !Objects.equals( this.id, other.id ) )
            {
                return false;
            }
            return true;
        }
    }
}
