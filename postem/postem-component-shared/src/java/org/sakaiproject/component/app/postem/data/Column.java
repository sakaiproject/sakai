/*
 * Created on Dec 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.sakaiproject.component.app.postem.data;

/**
 * @author rduhon
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

import java.util.*;

import org.sakaiproject.api.app.postem.data.Gradebook;

public class Column {
	
	public Gradebook gradebook;
	public int column;
	
	public Column(Gradebook gradebook, int column) {
		this.gradebook = gradebook;
		this.column = column;
	}
	
	public List getSummary() {
		try {
			return gradebook.getAggregateData(column);
		} catch (Exception exception) {
			return null;
		}
	}
	
	public List getRaw() {
		return gradebook.getRawData(column);
	}
	
	public boolean getHasName() {
		return gradebook.getHeadings().size() > 0;
	}
	
	public String getName() {
		try {
			return (String) gradebook.getHeadings().get(column + 1);
		} catch(Exception exception) {
			return "" + (column + 1);
		}
	}
}
