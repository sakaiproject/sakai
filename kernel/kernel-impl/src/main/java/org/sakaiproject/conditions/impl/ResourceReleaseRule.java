/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/conditionalrelease/tags/sakai_2-4-1/impl/src/java/org/sakaiproject/conditions/impl/ResourceReleaseRule.java $
 * $Id: ResourceReleaseRule.java 44304 2007-12-17 04:35:22Z zach.thomas@txstate.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.conditions.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.conditions.api.Condition;
import org.sakaiproject.conditions.api.Rule;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.GroupAwareEdit;
import org.sakaiproject.event.api.Obsoletable;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.event.api.NotificationAction;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;

/**
 * @author zach
 *
 */
public class ResourceReleaseRule implements Rule, Obsoletable {
	private static final String SATISFIES_RULE = "resource.satisfies.rule";
	private static final long LENGTH_OF_A_DAY = 86400000; // length of a day in milliseconds
	private String resourceId;
	private List<Condition> predicates;
	private Conjunction conj;
		
	// we need a no-arg constructor so BaseNotificationService can instantiate these things with Class.forName(className).newInstance();
	public ResourceReleaseRule() {
		
	}
	
	public ResourceReleaseRule(String resourceId, List<Condition> predicates, Conjunction conj) {
		this.resourceId = resourceId;
		this.predicates = predicates;
		this.conj = conj;
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
	 */
	public boolean evaluate(Object arg0) {
		Predicate judgement = null;
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

	/* (non-Javadoc)
	 * @see org.sakaiproject.event.api.NotificationAction#getClone()
	 */
	public NotificationAction getClone() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.event.api.NotificationAction#notify(org.sakaiproject.event.api.Notification, org.sakaiproject.event.api.Event)
	 */
	public void notify(Notification notification, Event event) {
		SecurityService.pushAdvisor(new SecurityAdvisor() {
			public SecurityAdvice isAllowed(String userId, String function,
					String reference) {
				return SecurityAdvice.ALLOWED;
			}
		});
		
		if (this.isObsolete()) return;
		
		if (("gradebook.updateItemScore").equals(event.getEvent())) {
			AssignmentGrading grading = produceAssignmentGradingFromEvent(event);
			boolean shouldBeAvailable = this.evaluate(grading);
			// update access control list on ContentHostingService here
			try {
				GroupAwareEdit resource = null;
				if (ContentHostingService.isCollection(this.resourceId)) {
					resource = ContentHostingService.editCollection(this.resourceId);
				} else {
					resource = ContentHostingService.editResource(this.resourceId);
				}
				ResourceProperties resourceProps = resource.getProperties();
				// since we're following a per-user event now, the global rule property should be removed
				resourceProps.removeProperty(SATISFIES_RULE);
				List<String> prop = resourceProps.getPropertyList(ContentHostingService.CONDITIONAL_ACCESS_LIST);
				if (prop == null) prop = new ArrayList<String>();
				Set<String> acl = new TreeSet<String>(prop);
				if ((shouldBeAvailable && acl.contains(grading.getUserId()))
					|| (!shouldBeAvailable && !acl.contains(grading.getUserId()))) {
					 // no change to the ACL necessary, but we still have to commit the change to SATISFIES_RULE
					if (ContentHostingService.isCollection(this.resourceId)) {
						ContentHostingService.commitCollection((ContentCollectionEdit)resource);
					} else {
						ContentHostingService.commitResource((ContentResourceEdit)resource, NotificationService.NOTI_NONE);
					}
					SecurityService.popAdvisor();
					return;
				}
				if (!shouldBeAvailable && acl.contains(grading.getUserId())) {
					// remove user from access list
					acl.remove(grading.getUserId());
					// time to re-populate the property list, start by tearing it down
					// we only have to do it this way because props does not have a removePropertyFromList method
					resourceProps.removeProperty(ContentHostingService.CONDITIONAL_ACCESS_LIST);
					for (String id : acl) {
						resourceProps.addPropertyToList(ContentHostingService.CONDITIONAL_ACCESS_LIST, id);
					}
				} else if (shouldBeAvailable && !acl.contains(grading.getUserId()))  {
					// add user to access list
					resourceProps.addPropertyToList(ContentHostingService.CONDITIONAL_ACCESS_LIST, grading.getUserId());
				}
				
				if (ContentHostingService.isCollection(this.resourceId)) {
					ContentHostingService.commitCollection((ContentCollectionEdit)resource);
				} else {
					ContentHostingService.commitResource((ContentResourceEdit)resource, NotificationService.NOTI_NONE);
				}
			} catch (PermissionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IdUnusedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InUseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OverQuotaException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServerOverloadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				SecurityService.popAdvisor();
			}
		} else if ("gradebook.updateAssignment".equals(event.getEvent()) || ("cond+gradebook.updateAssignment").equals(event.getEvent())) {
			// this availability applies to the whole Resource, not on a per-user basis
			// TODO set the resource availability
			AssignmentUpdate update = produceAssignmentUpdateFromEvent(event);
			boolean shouldBeAvailable = this.evaluate(update);
			try {
				GroupAwareEdit resource = null;
				if (ContentHostingService.isCollection(this.resourceId)) {
					resource = ContentHostingService.editCollection(this.resourceId);
				} else {
					resource = ContentHostingService.editResource(this.resourceId);
				}
				ResourceProperties resourceProps = resource.getProperties();
				resourceProps.addProperty(SATISFIES_RULE, new Boolean(shouldBeAvailable).toString());
				if (ContentHostingService.isCollection(this.resourceId)) {
					ContentHostingService.commitCollection((ContentCollectionEdit)resource);
				} else {
					ContentHostingService.commitResource((ContentResourceEdit)resource, NotificationService.NOTI_NONE);
				}
			} catch (PermissionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IdUnusedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InUseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OverQuotaException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServerOverloadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				SecurityService.popAdvisor();
			}
			
		} else if (("cond+gradebook.updateItemScore").equals(event.getEvent())) {
			// this event means the Rule has just been added
			// and we need to look at any scores that may have been recorded already
			try {
				String[] assignmentRefParts = event.getResource().split("/");
				String[] resourceRefParts = this.resourceId.split("/");
				String authzRef = "/site/" + resourceRefParts[2];
				AuthzGroup group = AuthzGroupService.getAuthzGroup(authzRef);
				Set<Member> members = group.getMembers();
				
				// build access control list up from scratch using site members
				Set<String> acl = new HashSet<String>();
				for (Member member : members) {
					boolean shouldBeAvailable = false;
					if (member.getRole().equals(group.getMaintainRole())) {
						shouldBeAvailable = true;
					} else {
						AssignmentGrading grading = produceAssignmentGrading(assignmentRefParts[2],assignmentRefParts[3],member.getUserId());
						shouldBeAvailable = this.evaluate(grading);
					}
					if (shouldBeAvailable) acl.add(member.getUserId());
				}
				
				// update state on this resource
				GroupAwareEdit resource = null;
				if (ContentHostingService.isCollection(this.resourceId)) {
					resource = ContentHostingService.editCollection(this.resourceId);
				} else {
					resource = ContentHostingService.editResource(this.resourceId);
				}
				ResourceProperties resourceProps = resource.getProperties();
				// since we're following a per-user event now, the global rule property should be removed
				resourceProps.removeProperty(SATISFIES_RULE);
				resourceProps.removeProperty(ContentHostingService.CONDITIONAL_ACCESS_LIST);
				for (String id : acl) {
					resourceProps.addPropertyToList(ContentHostingService.CONDITIONAL_ACCESS_LIST, id);
				}
				
				if (ContentHostingService.isCollection(this.resourceId)) {
					ContentHostingService.commitCollection((ContentCollectionEdit)resource);
				} else {
					ContentHostingService.commitResource((ContentResourceEdit)resource, NotificationService.NOTI_NONE);
				}
				
				
			} catch (GroupNotDefinedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IdUnusedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PermissionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InUseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OverQuotaException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServerOverloadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				SecurityService.popAdvisor();
			}
			
			
		} 

	}

	public boolean isObsolete() {
		SecurityService.pushAdvisor(new SecurityAdvisor() {
			public SecurityAdvice isAllowed(String userId, String function,
					String reference) {
				return SecurityAdvice.ALLOWED;
			}
		});
		try {
			ContentHostingService.getProperties(this.resourceId);
			return false;
		} catch (PermissionException e1) {
			return true;
		} catch (IdUnusedException e1) {
			return true;
		} finally {
			SecurityService.popAdvisor();
		}
	}
	

	private AssignmentUpdate produceAssignmentUpdateFromEvent(Event event) {
		AssignmentUpdate rv = new AssignmentUpdate();
		String[] assignmentRefParts = event.getResource().split("/");
		return rv;
	}

	private Date addADay(Date date) {
		return (date == null) ? null : new java.util.Date(date.getTime() + LENGTH_OF_A_DAY);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.event.api.NotificationAction#set(org.w3c.dom.Element)
	 */
	public void set(Element el) {

		// setup for predicates
		predicates = new Vector();

		this.resourceId = el.getAttribute("resourceId");
		
		String conjunction = el.getAttribute("conjunction");
		if("OR".equals(conjunction)) {
			this.conj = Rule.Conjunction.OR;
		} else if("AND".equals(conjunction)) {
			this.conj = Rule.Conjunction.AND;
		}

		// the children (predicates)
		NodeList children = el.getChildNodes();
		final int length = children.getLength();
		for (int i = 0; i < length; i++)
		{
			Node child = children.item(i);
			if (child.getNodeType() != Node.ELEMENT_NODE) continue;
			Element element = (Element) child;

			// look for properties
			if (element.getTagName().equals("predicates"))
			{
				// re-create properties
				predicates = reconstitutePredicates(element);
			}
		}
		
		

	}

	private List<Condition> reconstitutePredicates(Element element) {
		List<Condition> rv = new ArrayList<Condition>();
		try {
			Condition aPredicate = null;
			NodeList children = element.getChildNodes();
			final int length = children.getLength();
			for (int i = 0; i < length; i++) {
				Node child = children.item(i);
				if (child.getNodeType() != Node.ELEMENT_NODE) continue;
				Element predicate = (Element) child;

				// look for properties
				if (predicate.getTagName().equals("predicate")) {
					String className = predicate.getAttribute("class");
					aPredicate = (Condition) Class.forName(className).newInstance();
					((BooleanExpression) aPredicate).setReceiver(predicate.getAttribute("receiver"));
					((BooleanExpression) aPredicate).setMethod(predicate.getAttribute("method"));
					((BooleanExpression) aPredicate).setOperator(predicate.getAttribute("operator"));
					((BooleanExpression) aPredicate).setArgument(predicate.getAttribute("argument"));
					rv.add(aPredicate);
				}
			}
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rv;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.event.api.NotificationAction#set(org.sakaiproject.event.api.NotificationAction)
	 */
	public void set(NotificationAction other) {
		ResourceReleaseRule eOther = (ResourceReleaseRule) other;
		resourceId = eOther.resourceId;

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.event.api.NotificationAction#toXml(org.w3c.dom.Element)
	 */
	public void toXml(Element el) {
		el.setAttribute("resourceId", this.resourceId);
		
		if(this.conj == Rule.Conjunction.OR) {
			el.setAttribute("conjunction", "OR");
		} else if(this.conj == Rule.Conjunction.AND) {
			el.setAttribute("conjunction", "AND");
		}
		
		Element predicates = el.getOwnerDocument().createElement("predicates");
		el.appendChild(predicates);
		for (Predicate p : this.predicates) {
			Element predicateElement = el.getOwnerDocument().createElement("predicate");
			predicateElement.setAttribute("class", p.getClass().getName());
			predicateElement.setAttribute("receiver", ((BooleanExpression)p).getReceiver());
			predicateElement.setAttribute("method", ((BooleanExpression)p).getMethod());
			predicateElement.setAttribute("operator", ((BooleanExpression)p).getOperator());
			Object argument = ((BooleanExpression)p).getArgument();
			if (argument == null) {
				argument = "";
			}
			predicateElement.setAttribute("argument", argument.toString());
			predicates.appendChild(predicateElement);
		}

	}
	
	private AssignmentGrading produceAssignmentGradingFromEvent(Event event) {
		AssignmentGrading rv = new AssignmentGrading();
		String[] assignmentRefParts = event.getResource().split("/");
		// a score may be null after a grading event
		try {
			rv.setScore(new Double(assignmentRefParts[5]));
		} catch (NumberFormatException e) {
			rv.setScore(null);
		}
		rv.setUserId(assignmentRefParts[4]);
		
		return rv;
	}
	
	private AssignmentGrading produceAssignmentGrading(String gradebookId, String assignmentName, String userId) {
		AssignmentGrading rv = new AssignmentGrading();
		rv.setUserId(userId);
		
		return rv;
	}

	private boolean isAssignmentPastDue(Object assignment) {
		// TODO stub method
		return false;
	}

}
