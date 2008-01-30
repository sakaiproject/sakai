package org.sakaiproject.scorm.ui.console.components;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.util.lang.PropertyResolver;
import org.sakaiproject.wicket.markup.html.repeater.data.table.Action;

public class AttemptNumberAction extends Action {

	private static final long serialVersionUID = 1L;

	public AttemptNumberAction(String propertyExpression, Class<?> pageClass, String[] paramPropertyExpressions) {
		super(propertyExpression, pageClass, paramPropertyExpressions);
	}
	
	public Component newLink(String id, Object bean) {
		String number = String.valueOf(PropertyResolver.getValue(labelPropertyExpression, bean));
		long numberOfAttempts = 0;
		
		numberOfAttempts = Long.parseLong(number);
		
		PageParameters params = buildPageParameters(paramPropertyExpressions, bean);
		
		return new AttemptNumberPanel(id, numberOfAttempts, pageClass, params);
	}
	
}
