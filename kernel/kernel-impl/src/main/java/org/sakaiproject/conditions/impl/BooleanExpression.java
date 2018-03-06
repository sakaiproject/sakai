/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/conditionalrelease/tags/sakai_2-4-1/impl/src/java/org/sakaiproject/conditions/impl/BooleanExpression.java $
 * $Id: BooleanExpression.java 44301 2007-12-17 02:27:17Z zach.thomas@txstate.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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
package org.sakaiproject.conditions.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.conditions.api.Condition;
import org.sakaiproject.conditions.api.Operator;

/**
 * @author Zach A. Thomas
 *
 */
@Slf4j
public class BooleanExpression implements Condition {
	private String eventDataClassName;
	private String missingTermMethodName;
	private Operator op;
	private Object argument;
	
	public BooleanExpression() {
		
	}
	
	public BooleanExpression(String eventDataClassName, String missingTermMethodName, Operator op, Object argument) {
		this.eventDataClassName = eventDataClassName;
		this.missingTermMethodName = missingTermMethodName;
		this.op = op;
		this.argument = argument;
	}

	public BooleanExpression(String eventDataClassName, String missingTermMethodName, String operatorValue, Object argument) {
		this.eventDataClassName = eventDataClassName;
		this.missingTermMethodName = missingTermMethodName;
		this.argument = argument;
		setOperator(operatorValue);
	}

	public boolean evaluate(Object arg) {
		try {
			Class[] parameterTypes = {};
			Method methodToInvoke = Class.forName(eventDataClassName).getDeclaredMethod(missingTermMethodName, parameterTypes);
			Object[] methodArgs = {};
			Object missingTerm =  methodToInvoke.invoke(arg, methodArgs);
			if (missingTerm == null) {
				return false;
			}
			if ((missingTerm instanceof Boolean) && op.getType() == Operator.NO_OP) {
				return ((Boolean)missingTerm).booleanValue();
			} else return evalExpression(missingTerm, op, argument);
		} catch (SecurityException e) {
			log.error(e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			log.error(e.getMessage(), e);
		} catch (ClassNotFoundException e) {
			log.error(e.getMessage(), e);
		} catch (NoSuchMethodException e) {
			log.error(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			log.error(e.getMessage(), e);
		} catch (InvocationTargetException e) {
			log.error(e.getMessage(), e);
		}
		// if all else fails, return false
		return false;
	}

	private boolean evalExpression(Object leftTerm, Operator op,
			Object rightTerm) {
		if (argumentsAndOperatorAgree(leftTerm, op, rightTerm)) {
			switch(op.getType()) {
			case Operator.LESS_THAN:
				int comparison = ((Double)leftTerm).compareTo((Double)rightTerm);
				return (comparison < 0);
			
			case Operator.GREATER_THAN:
				return ((Comparable)leftTerm).compareTo(rightTerm) > 0;
			
			case Operator.EQUAL_TO:
				return ((Comparable)leftTerm).compareTo(rightTerm) == 0;
				
			case Operator.GREATER_THAN_EQUAL_TO:
				return ((((Comparable)leftTerm).compareTo(rightTerm) > 0) || (((Comparable)leftTerm).compareTo(rightTerm) == 0));
			}
		}
		// fall-through return value
		return false;
	}

	private boolean argumentsAndOperatorAgree(Object term1, Operator op2,
			Object term2) {
		return true;
	}
	
	public String toString() {
		return eventDataClassName + "." + missingTermMethodName + "() less than " + argument;
	}
	
	public void setReceiver(String receiver) {
		this.eventDataClassName = receiver;
	}
	
	public void setMethod(String method) {
		this.missingTermMethodName = method;
	}
	
	public void setOperator(String operator) {
		if ("no_operator".equals(operator)) {
			this.op = new Operator() {
				public int getType() {
					return Operator.NO_OP;
				}
			};
		} else if ("less_than".equals(operator)) {
			this.op = new Operator() {
				public int getType() {
					return Operator.LESS_THAN;
				}
			};
		} else if ("greater_than_equal_to".equals(operator)) {
			this.op = new Operator() {
				public int getType() {
					return Operator.GREATER_THAN_EQUAL_TO;
				}
			};
		}
	}
	
	public String getOperator() {
		if (op.getType() == Operator.NO_OP) {
			return "no_operator";
		} else if (op.getType() == Operator.LESS_THAN) {
			return "less_than";
		} else if (op.getType() == Operator.GREATER_THAN_EQUAL_TO) {
			return "greater_than_equal_to";
		} else return null;
	}
	
	public String getReceiver() {
		return this.eventDataClassName;
	}
	
	public String getMethod() {
		return this.missingTermMethodName;
	}
	
	public Object getArgument() {
		return argument;
	}

	public void setArgument(Object argument) {
		if (argument instanceof String) {
			try {
				this.argument = Double.parseDouble(((String) argument).trim());
			} catch (NumberFormatException e) {
				// this must not be a number
				this.argument = argument;
			}
		} else {
			this.argument = argument;
		}
	}

}
