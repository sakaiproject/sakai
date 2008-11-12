package org.sakaiproject.sitestats.tool.wicket.providers;

import java.util.Iterator;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;

public class SortableSearchableDataProvider extends SortableDataProvider{
	private String searchKeyword = null;

	public Iterator iterator(int first, int count) {
		// TODO Auto-generated method stub
		return null;
	}

	public IModel model(Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void clearSearchKeyword() {
		this.searchKeyword = null;
	}

	public void setSearchKeyword(String searchKeyword) {
		this.searchKeyword = searchKeyword;
	}

	public String getSearchKeyword() {
		return searchKeyword;
	}

}
