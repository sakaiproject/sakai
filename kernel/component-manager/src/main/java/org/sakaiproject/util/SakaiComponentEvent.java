/**
 * Copyright (c) 2003-2009 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
