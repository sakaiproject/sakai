package org.sakaiproject.conditions.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sakaiproject.conditions.api.EvaluationAction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.conditions.api.Condition;
import org.sakaiproject.conditions.api.ConditionService;
import org.sakaiproject.conditions.api.Rule;
import org.sakaiproject.event.api.Event;

public class TestConditionService {
	
	private ConditionService conditionService;
	
	@Before
	public void setUp() {
		conditionService = new ToyConditionsService();
		conditionService.registerConditionTemplates(new MyConditionTemplateSet());
	}
	
	@Test
	public void testRegisterTemplates() {
		Assert.assertEquals("Gradebook", conditionService.getConditionTemplateSetForService("sakai.service.gradebook").getDisplayName());
	}
	
	@Test
	public void testSaveRuleAndNotify() {
		EvaluationAction command = new ToyCommand();
		String resourceId = "zach-makes-the-best-conditions";
		List<Condition> conditions = new ArrayList<Condition>();
		conditions.add(numberLessThan100());
		
		conditionService.addRule("gradebook.newgrade",new BaseRule(resourceId, conditions, command, Rule.Conjunction.OR));
		
		((ToyConditionsService)conditionService).dispatchAnEvent(newUserEvent());
		Assert.assertEquals(0, ToyMessagePad.messages.size());
		
		((ToyConditionsService)conditionService).dispatchAnEvent(newGrade69Event());
		Assert.assertEquals("I've been hit!",ToyMessagePad.messages.get(0));
	}

	private Event newGrade69Event() {
		return new Event() {

			public String getContext() {
				// TODO Auto-generated method stub
				return null;
			}

			public String getEvent() {
				return "gradebook.newgrade";
			}

			public boolean getModify() {
				// TODO Auto-generated method stub
				return false;
			}

			public int getPriority() {
				// TODO Auto-generated method stub
				return 0;
			}

			public String getResource() {
				return "69";
			}

			public String getSessionId() {
				// TODO Auto-generated method stub
				return null;
			}

			public String getUserId() {
				// TODO Auto-generated method stub
				return null;
			}

			public Date getEventTime() {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
	}

	private Event newUserEvent() {
		return new Event() {

			public String getContext() {
				// TODO Auto-generated method stub
				return null;
			}

			public String getEvent() {
				return "users.new-user";
			}

			public boolean getModify() {
				// TODO Auto-generated method stub
				return false;
			}

			public int getPriority() {
				// TODO Auto-generated method stub
				return 0;
			}

			public String getResource() {
				return "zt10";
			}

			public String getSessionId() {
				// TODO Auto-generated method stub
				return null;
			}

			public String getUserId() {
				// TODO Auto-generated method stub
				return null;
			}

			public Date getEventTime() {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
	}

	private Condition numberLessThan100() {
		return new Condition() {

			public Object getArgument() {
				// TODO Auto-generated method stub
				return null;
			}

			public String getMethod() {
				// TODO Auto-generated method stub
				return null;
			}

			public String getOperator() {
				// TODO Auto-generated method stub
				return null;
			}

			public String getReceiver() {
				// TODO Auto-generated method stub
				return null;
			}

			public boolean evaluate(Object arg0) {
				return Integer.parseInt(((Event)arg0).getResource()) < 100;
			}

			public Class<?> classFromEvent() {
				// TODO Auto-generated method stub
				return null;
			}

			public String eventType() {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
	}
}
