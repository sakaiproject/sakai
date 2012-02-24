/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.message.tool;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class CollectionUtils {
	public interface Filter<E> {
		public boolean accept(E element);
	}

	public static <E> void filter(Collection<E> collection,
			Set<Filter<E>> filters) {
		final Iterator<E> iterator = collection.iterator();
		while (iterator.hasNext()) {
			final E element = iterator.next();
			for (Filter<E> filter : filters) {
				if (!filter.accept(element)) {
					iterator.remove();
					break;
				}
			}
		}
	}

	public static void removeTail(final List<?> list, final int newSize) {
		final int listSize = list.size();
		if (newSize < 0 || newSize >= listSize) {
			return;
		}
		list.subList(newSize, listSize).clear();
	}
}
