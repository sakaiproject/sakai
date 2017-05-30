/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

// package
package org.sakaiproject.util;

import java.util.Iterator;

/**
 * <p>
 * EmptyIterator is an iterator which is empty.
 * </p>
 * @deprecated use {@link org.apache.commons.collections4.iterators.EmptyIterator} instead, this will be removed after 2.9 - Dec 2011
 */
@Deprecated 
public class EmptyIterator<E> implements Iterator<E>
{
	public E next()
	{
		return null;
	}

	public boolean hasNext()
	{
		return false;
	}

	public void remove()
	{
		throw new UnsupportedOperationException();
	}
}
