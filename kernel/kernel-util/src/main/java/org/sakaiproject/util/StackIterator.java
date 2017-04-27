/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

package org.sakaiproject.util;

import java.util.Iterator;
import java.util.Stack;

/**
 * StackIterator is both a {@link java.util.Iterator} and a stack (though not a {@link java.util.Collection})
 * 
 * @deprecated use commons-collection instead, this will be removed after 2.9 - Dec 2011
 */
@Deprecated 
public class StackIterator<E> implements Iterator<E>
{
	protected Stack<E> stack;

	/**
     * @param stack
     */
    public StackIterator()
    {
	    this.stack = new Stack<E>();
    }

	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext()
	{
		return ! this.stack.empty();
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public E next()
	{
		return this.stack.pop();
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove()
	{
		this.stack.pop();
	}

	/* (non-Javadoc)
     * @see java.util.Stack#empty()
     */
    public boolean empty()
    {
	    // TODO Auto-generated method stub
	    return stack.empty();
    }

	/* (non-Javadoc)
     * @see java.util.Stack#peek()
     */
    public synchronized E peek()
    {
	    // TODO Auto-generated method stub
	    return stack.peek();
    }

	/* (non-Javadoc)
     * @see java.util.Stack#pop()
     */
    public synchronized E pop()
    {
	    // TODO Auto-generated method stub
	    return stack.pop();
    }

	/* (non-Javadoc)
     * @see java.util.Stack#push(java.lang.Object)
     */
    public E push(E arg0)
    {
	    // TODO Auto-generated method stub
	    return stack.push(arg0);
    }

	/* (non-Javadoc)
     * @see java.util.Stack#search(java.lang.Object)
     */
    public synchronized int search(Object arg0)
    {
	    // TODO Auto-generated method stub
	    return stack.search(arg0);
    }

}
