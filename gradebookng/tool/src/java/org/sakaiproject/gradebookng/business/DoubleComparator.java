package org.sakaiproject.gradebookng.business;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

/**
 * Comparator to ensure correct ordering of percents
 */
public class DoubleComparator implements Comparator<String>, Serializable {
	private static final long serialVersionUID = 1L;

	Map<String, Double> base;

	public DoubleComparator(Map<String, Double> base) {
		this.base = base;
	}

	public int compare(String a, String b) {
		if (base.get(a) >= base.get(b)) {
			return -1;
		} else {
			return 1;
		}
	}
}
