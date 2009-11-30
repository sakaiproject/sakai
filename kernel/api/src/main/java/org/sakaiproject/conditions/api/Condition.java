package org.sakaiproject.conditions.api;

import org.apache.commons.collections.Predicate;

public interface Condition extends Predicate {
		
	public String getOperator();
	
	public String getReceiver();
	
	public String getMethod();
	
	public Object getArgument();

}
