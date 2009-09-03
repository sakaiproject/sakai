package org.sakaiproject.util;

import org.springframework.context.ApplicationEvent;

/**
 * Sakai specific events. Listening to these events means you have to bind to spring.
 * @author buckett
 *
 */
public class SakaiComponentEvent extends ApplicationEvent {

	public static enum Type {
		/**
		 * Event when the component manager has been started.
		 */
		STARTED,
		/**
		 * Event for just before the component manager is asked to stop.
		 */
		STOPPING
	};

	private Type type;

	public SakaiComponentEvent(Object source, Type type) {
		super(source);
		this.type = type;
	}

	public Type getType() {
		return type;
	}

}
