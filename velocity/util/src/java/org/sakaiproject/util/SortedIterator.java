/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

// package
package org.sakaiproject.util;

// imports
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

/**
* <p>SortedIterator is a wrapper iterator that iterates over the wrapped iterator in a sorted order,
* the order controlled by a Comparator function provided at construction.</p>
* 
* @author University of Michigan, CHEF Software Development Team
* @version $Revision$
*/
public class SortedIterator 
	implements Iterator
{
	/** The sorted iterator. */
	protected Iterator m_iterator = null;

	/**
	* Creates new SortedIterator based on the base iterator and the comparator function
	* @param aIterator The original Iterator
	* @param aComparator The comparator object
	*/	
    public SortedIterator(Iterator iterator, Comparator comparator) 
	{
		// construct a collection (Vector) from the base iterator so we can sort
		Vector collection = new Vector();
		while (iterator.hasNext())
		{
			collection.add(iterator.next());
		}
		
		// sort the collection based on comparator
		Collections.sort(collection, comparator);
		
		// remember the final sorted iterator
		m_iterator = collection.iterator();

    }	// SortedIterator

	/**
	* Returns true if the iteration has more elements.
	* @return True if the iteration has more elements; False otherwise.
	*/
	public boolean hasNext()
	{
		return m_iterator.hasNext();
		
	}	// hasNext
	
	/**
	* Returns the next element in the iteration.
	* @return The next element in the iteration
	*/
	public Object next()
	{
		return m_iterator.next();
		
	}	// next
	
	/**
	* Removes from the underlying collection the last element returned by the iterator
	* (optional operation) (not supported).
	*/
	public void remove()
	{
		throw new UnsupportedOperationException();

	}	// remove

}	// SortedIterator



