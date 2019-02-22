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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;

/**
 *
 * @author bjones86
 */
public class ActivityTreeExpansion implements Set<SeqActivityNode>, Serializable
{
    private static final long serialVersionUID = 1L;
    private static MetaDataKey<ActivityTreeExpansion> KEY = new MetaDataKey<ActivityTreeExpansion>()
    {
        private static final long serialVersionUID = 1L;
    };

    @Getter @Setter
    private String              selectedID  = "";
    private Set<String>         ids         = new HashSet<>();
    private boolean             inverse;

    /**
     * Get the expansion for the session.
     *
     * @param provider
     * @return expansion
     */
    public static ActivityTreeExpansion get()
    {
        ActivityTreeExpansion expansion = Session.get().getMetaData( KEY );
        if( expansion == null )
        {
            expansion = new ActivityTreeExpansion();
            Session.get().setMetaData( KEY, expansion );
        }

        return expansion;
    }

    public void selectNode( String seqActivityID )
    {
        selectedID = seqActivityID;
    }

    public void expandAll()
    {
        ids.clear();
        inverse = true;
    }

    public void collapseAll()
    {
        ids.clear();
        inverse = false;
    }

    /********************************
     * Overridden Set methods below *
     ********************************/

    @Override
    public boolean add( SeqActivityNode seqActivityNode )
    {
        if( inverse )
        {
            return ids.remove( seqActivityNode.getID() );
        }
        else
        {
            return ids.add( seqActivityNode.getID() );
        }
    }

    @Override
    public boolean remove( Object o )
    {
        SeqActivityNode activityNode = (SeqActivityNode) o;
        if( inverse )
        {
            return ids.add( activityNode.getID() );
        }
        else
        {
            return ids.remove( activityNode.getID() );
        }
    }

    @Override
    public boolean contains( Object o )
    {
        SeqActivityNode activityNode = (SeqActivityNode) o;
        if( inverse )
        {
            return !ids.contains( activityNode.getID() );
        }
        else
        {
            return ids.contains( activityNode.getID() );
        }
    }

    

    /***********************************
     * Unimplemented Set methods below *
     ***********************************/

    @Override public boolean isEmpty() { throw new UnsupportedOperationException(); }
    @Override public void                       clear()                                             { throw new UnsupportedOperationException(); }
    @Override public int                        size()                                              { throw new UnsupportedOperationException(); }
    @Override public <A> A[]                    toArray( A[] a )                                    { throw new UnsupportedOperationException(); }
    @Override public Iterator<SeqActivityNode>  iterator()                                          { throw new UnsupportedOperationException(); }
    @Override public Object[]                   toArray()                                           { throw new UnsupportedOperationException(); }
    @Override public boolean                    containsAll( Collection<?> c )                      { throw new UnsupportedOperationException(); }
    @Override public boolean                    addAll( Collection<? extends SeqActivityNode> c )   { throw new UnsupportedOperationException(); }
    @Override public boolean                    retainAll( Collection<?> c )                        { throw new UnsupportedOperationException(); }
    @Override public boolean                    removeAll( Collection<?> c )                        { throw new UnsupportedOperationException(); }
}
