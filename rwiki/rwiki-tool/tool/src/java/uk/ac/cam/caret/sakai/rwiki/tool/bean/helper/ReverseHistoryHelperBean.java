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

package uk.ac.cam.caret.sakai.rwiki.tool.bean.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;

/**
 * Helper bean to return the history of a RWikiObject reversed
 * 
 * @author andrew
 */
public class ReverseHistoryHelperBean
{

	/**
	 * RWikiObject whose history is to be reversed
	 */
	private RWikiObject rwikiObject;

	private RWikiObjectService rwikiObjectService;

	/**
	 * Get the rwikiObject to which the reversed history is associated
	 * 
	 * @return RWikiObject
	 */
	public RWikiObject getRwikiObject()
	{
		return rwikiObject;
	}

	/**
	 * Set the rwikiObject whose history is to be reversed
	 * 
	 * @param rwikiObject
	 */
	public void setRwikiObject(RWikiObject rwikiObject)
	{
		this.rwikiObject = rwikiObject;
	}

	/**
	 * Get the history of the rwikiObject as a reversed List
	 * 
	 * @return list with history in reverse order
	 */
	public List getReverseHistory()
	{
		List history = rwikiObjectService.findRWikiHistoryObjects(rwikiObject);
		int size = history != null ? history.size() : 0;
		Object[] returnable = new Object[size];

		for (int i = 0; i < size; i++)
		{
			returnable[size - i - 1] = history.get(i);
		}
		reverseList = new WrappedList(Arrays.asList(returnable));

		return reverseList;
	}

	public class WrappedList extends ArrayList
	{
		private static final long serialVersionUID = 1L;

		private int pos = 0;

		public WrappedList(List l)
		{
			super(l);
		}

		/**
		 * @return Returns the pos.
		 */
		public int getPos()
		{
			return pos;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.AbstractList#iterator()
		 */
		public Iterator iterator()
		{
			pos = 0;
			final Iterator iter = super.iterator();
			return new Iterator()
			{

				public boolean hasNext()
				{
					return iter.hasNext();
				}

				public Object next()
				{
					pos++;
					return iter.next();
				}

				public void remove()
				{
					iter.remove();
				}
			};
		}

	}

	private WrappedList reverseList;

	public boolean getTheSame()
	{
		try
		{
			RWikiObject last = (RWikiObject) reverseList.get(reverseList.pos);
			RWikiObject previous = (RWikiObject) reverseList
					.get(reverseList.pos - 1);
			return last.getSha1().equals(previous.getSha1());
		}
		catch (Exception ex)
		{

		}
		return false;
	}

	public RWikiObjectService getRwikiObjectService()
	{
		return rwikiObjectService;
	}

	public void setRwikiObjectService(RWikiObjectService rwikiObjectService)
	{
		this.rwikiObjectService = rwikiObjectService;
	}
}
