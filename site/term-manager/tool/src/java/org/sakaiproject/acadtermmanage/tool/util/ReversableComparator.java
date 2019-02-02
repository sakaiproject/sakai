package org.sakaiproject.acadtermmanage.tool.util;

import java.util.Comparator;

/**
 * Reverses the result of a Comparator.
 * It's a helper class which provides an easy way to reverse the sort order of a list. 
 * 
 */
public class ReversableComparator<T> implements Comparator<T> {

	private boolean reverse = false;
	private Comparator<T> realComparator;
	
	public ReversableComparator(Comparator<T> realComparator, boolean reverse) {
		this.reverse=reverse;
		this.realComparator = realComparator;
	}
	
	@Override
	public int compare(T o1, T o2) {
		if (reverse) {
			return realComparator.compare(o2, o1);
		}
		else {
			return realComparator.compare(o1,o2);
		}
	}

}
