package org.sakaiproject.content.tool;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class manages the expanded collections in the session.
 * If this didn't need to be sorted then we could use standard classes. At the moment I'm not sure why the original
 * code used a sorted set.
 *
 * @see java.util.concurrent.CopyOnWriteArraySet
 * @author Matthew Buckett
 *
 */
public class ExpandedCollections {

	private static final SortedSet<String> EMPTY_SET = Collections.unmodifiableSortedSet(new TreeSet<String>());

	private SortedSet<String> set = EMPTY_SET;

	public void add(String id) {
		// Only do the expensive copy if we need to.
		if (!set.contains(id)) {
			SortedSet<String> newSet = new TreeSet<String>(set);
			newSet.add(id);
			set = Collections.unmodifiableSortedSet(newSet);
		}
	}

	public void clear() {
		// Just reset to empty set.
		set = EMPTY_SET;
	}

	public SortedSet<String> getSet() {
		// It's unmodifiable so we don't need to wrap it.
		return set;
	}

	public void addAll(Collection<String> other) {
		SortedSet<String> modifiedSet = null;
		for(String id: other) {
			if (!set.contains(id)) {
				if(modifiedSet == null) {
					modifiedSet = new TreeSet<String>(set);
				}
				modifiedSet.add(id);
			}
		}
		if (modifiedSet != null) {
			set = modifiedSet;
		}

	}

}
