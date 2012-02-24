/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaibrary.osid.repository.xserver;

/**
 * @author Massachusetts Institute of Techbology, Sakai Software Development Team
 * @version
 */
public class RecordStructureIterator
implements org.osid.repository.RecordStructureIterator
{
    private java.util.Vector vector = new java.util.Vector();
    private int i = 0;

    public RecordStructureIterator(java.util.Vector vector)
    throws org.osid.repository.RepositoryException
    {
        this.vector = vector;
    }

    public boolean hasNextRecordStructure()
    throws org.osid.repository.RepositoryException
    {
        return (i < vector.size());
    }

    public org.osid.repository.RecordStructure nextRecordStructure()
    throws org.osid.repository.RepositoryException
    {
        if (i >= vector.size())
        {
            throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.NO_MORE_ITERATOR_ELEMENTS);
        }
        return (org.osid.repository.RecordStructure)vector.elementAt(i++);
    }
}
