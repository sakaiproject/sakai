/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package uk.ac.cam.caret.sakai.rwiki.component.dao.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import uk.ac.cam.caret.sakai.rwiki.service.api.dao.ObjectProxy;

/**
 * Provides a List as a Proxy monitoring all objects in and of the list with a
 * ObjectProxy
 * 
 * @author ieb
 */
public class ListProxy implements List
{
	private List outputList;

	private ObjectProxy lop;

	public ListProxy(List outputList, ObjectProxy listObjectProxy)
	{
		this.outputList = outputList;
		this.lop = listObjectProxy;
	}

	public int size()
	{
		return outputList.size();
	}

	public boolean isEmpty()
	{
		return outputList.isEmpty();
	}

	public boolean contains(Object arg0)
	{
		return outputList.contains(arg0);
	}

	public Iterator iterator()
	{
		return new IteratorProxy(outputList.iterator(), lop);
	}

	public Object[] toArray()
	{
		return outputList.toArray();
	}

	public Object[] toArray(Object[] arg0)
	{
		return outputList.toArray(arg0);
	}

	public boolean add(Object arg0)
	{
		return outputList.add(arg0);
	}

	public boolean remove(Object arg0)
	{
		return outputList.remove(arg0);
	}

	public boolean containsAll(Collection arg0)
	{
		return outputList.containsAll(arg0);
	}

	public boolean addAll(Collection arg0)
	{
		return outputList.addAll(arg0);
	}

	public boolean addAll(int arg0, Collection arg1)
	{
		return outputList.addAll(arg0, arg1);
	}

	public boolean removeAll(Collection arg0)
	{
		return outputList.removeAll(arg0);
	}

	public boolean retainAll(Collection arg0)
	{
		return outputList.retainAll(arg0);
	}

	public void clear()
	{
		outputList.clear();
	}

	public Object get(int arg0)
	{
		return lop.proxyObject(outputList.get(arg0));
	}

	public Object set(int arg0, Object arg1)
	{
		return lop.proxyObject(outputList.set(arg0, lop.proxyObject(arg1)));
	}

	public void add(int arg0, Object arg1)
	{
		outputList.add(arg0, lop.proxyObject(arg1));
	}

	public Object remove(int arg0)
	{
		return lop.proxyObject(outputList.remove(arg0));
	}

	public int indexOf(Object arg0)
	{
		return outputList.indexOf(lop.proxyObject(arg0));
	}

	public int lastIndexOf(Object arg0)
	{
		return outputList.lastIndexOf(lop.proxyObject(arg0));
	}

	public ListIterator listIterator()
	{
		return new ListIteratorProxy(outputList.listIterator(), lop);
	}

	public ListIterator listIterator(int arg0)
	{
		return new ListIteratorProxy(outputList.listIterator(arg0), lop);
	}

	public List subList(int arg0, int arg1)
	{
		return new ListProxy(outputList.subList(arg0, arg1), lop);
	}
}