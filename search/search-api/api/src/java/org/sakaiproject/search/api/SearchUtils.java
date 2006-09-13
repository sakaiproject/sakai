package org.sakaiproject.search.api;

public class SearchUtils
{
	public static String getCleanString(String text ) {
		return text.replaceAll("[\\x00-\\x08\\x0b\\x0c\\x0e-\\x1f\\ud800-\\udfff\\uffff\\ufffe]", "");
	}
}
