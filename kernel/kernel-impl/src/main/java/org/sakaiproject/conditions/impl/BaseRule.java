/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/api/src/main/java/org/sakaiproject/antivirus/api/VirusFoundException.java $
 * $Id: VirusFoundException.java 68335 2009-10-29 08:18:43Z david.horwitz@uct.ac.za $
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

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;

import org.w3c.dom.Element;

import org.sakaiproject.conditions.api.EvaluationAction;
import org.sakaiproject.conditions.api.Rule;
import org.sakaiproject.conditions.api.Condition;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.event.api.NotificationAction;

@Slf4j
public class BaseRule implements Rule, NotificationAction {
	
	private String resourceId;
	private List<Condition> predicates;
	private Conjunction conj;
	private EvaluationAction command;
	
	public BaseRule(String resourceId, List<Condition> predicates, EvaluationAction command, Conjunction conj) {
		this.resourceId = resourceId;
		this.predicates = predicates;
		this.command = command;
		this.conj = conj;
	}

	public boolean evaluate(Object arg0) {
		Predicate judgement = new NullPredicate();
		if (predicates.size() == 1) {
			judgement = predicates.get(0);
		} else {
			if (conj == Conjunction.AND) {
				judgement = PredicateUtils.allPredicate(predicates);
			}
			else if (conj == Conjunction.OR) {
				judgement = PredicateUtils.anyPredicate(predicates);
			}
		}
		
		return judgement.evaluate(arg0);
	}

	public NotificationAction getClone() {
		// TODO Auto-generated method stub
		return null;
	}

	public void notify(Notification notification, Event event) {
			try {
				command.execute(event, this.evaluate(event));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
	}

	public void set(Element el) {
		// TODO Auto-generated method stub

	}

	public void set(NotificationAction other) {
		// TODO Auto-generated method stub

	}

	public void toXml(Element el) {
		el.setAttribute("resourceId", this.resourceId);
		
		if(this.conj == Rule.Conjunction.OR) {
			el.setAttribute("conjunction", "OR");
		} else if(this.conj == Rule.Conjunction.AND) {
			el.setAttribute("conjunction", "AND");
		}
		
		Element predicates = el.getOwnerDocument().createElement("predicates");
		el.appendChild(predicates);
		for (Condition c : this.predicates) {
			Element predicateElement = el.getOwnerDocument().createElement("predicate");
			predicateElement.setAttribute("class", c.getClass().getName());
			predicateElement.setAttribute("receiver", c.getReceiver());
			predicateElement.setAttribute("method", c.getMethod());
			predicateElement.setAttribute("operator", c.getOperator());
			Object argument = c.getArgument();
			if (argument == null) {
				argument = "";
			}
			predicateElement.setAttribute("argument", argument.toString());
			predicates.appendChild(predicateElement);
		}

	}

}
