package org.sakaiproject.conditions.api;
/**
 * @author Zach A. Thomas
 *
 */
public interface Operator {
	
	public static final int LESS_THAN = 0;
	public static final int GREATER_THAN = 1;
	public static final int EQUAL_TO = 3;
	public static final int GREATER_THAN_EQUAL_TO = 4;
	public static final int NO_OP = 5;
	
	public int getType();

}
