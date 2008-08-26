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
