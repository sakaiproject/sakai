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

import java.util.ListIterator;

import uk.ac.cam.caret.sakai.rwiki.service.api.dao.ObjectProxy;

/**
 * Provides a proxy implementation of a ListIterator, proxying objects with a
 * ObjectProxy
 * 
 * @author ieb
 */
// FIXME: Component
public class ListIteratorProxy implements ListIterator
{
	private ListIterator li;

	private ObjectProxy lop;

	public ListIteratorProxy(ListIterator li, ObjectProxy lop)
	{
		this.li = li;
		this.lop = lop;
	}

	public boolean hasNext()
	{
		return li.hasNext();
	}

	public Object next()
	{
		return lop.proxyObject(li.next());
	}

	public boolean hasPrevious()
	{
		return li.hasPrevious();
	}

	public Object previous()
	{
		return lop.proxyObject(li.previous());
	}

	public int nextIndex()
	{
		return li.nextIndex();
	}

	public int previousIndex()
	{
		return li.previousIndex();
	}

	public void remove()
	{
		li.remove();
	}

	public void set(Object arg0)
	{
		li.set(lop.proxyObject(arg0));
	}

	public void add(Object arg0)
	{
		li.add(lop.proxyObject(arg0));
	}

}
