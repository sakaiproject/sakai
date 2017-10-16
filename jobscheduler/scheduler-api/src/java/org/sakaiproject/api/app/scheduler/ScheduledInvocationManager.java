/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
package org.sakaiproject.api.app.scheduler;

import org.sakaiproject.time.api.Time;

import java.time.Instant;


/**
 * Although not specified in the original API a recent restriction imposed is that you can't have
 * multiple delayed invocations of the same component and opaqueContext.
 * The original limitation on componentId and opaqueContext is 2000 characters.
 */
public interface ScheduledInvocationManager {

	/**
	 * Creates a new delayed invocation and returns the unique id of the created invocation
	 * 
	 * @param time the date and time the method will be invoked
	 * @param componentId the unique name of a bean in the bean factory which implements 
	 * command pattern DelayedInvocationCommand
	 * @param opaqueContext the key which the tool can use to uniquely identify some 
	 * entity when invoked; i.e. the context. This currently accepts empty string which is shouldn't as it can't be explicitly removed.
	 * @return unique id of a delayed invocation
	 * @deprecated The Time class shouldn't be used any more {@link #createDelayedInvocation(Instant, String, String)}
	 */
	public String createDelayedInvocation(Time time, String componentId, String opaqueContext);


	/**
	 * Creates a new delayed invocation and returns the unique id of the created invocation
	 *
	 * @param instant the date and time the method will be invoked.
	 * @param componentId the unique name of a bean in the bean factory which implements
	 * command pattern DelayedInvocationCommand
	 * @param opaqueContext the key which the tool can use to uniquely identify some
	 * entity when invoked; i.e. the context. This currently accepts empty string which is shouldn't as it can't be explicitly removed.
	 * @return unique id of a delayed invocation
	 */
	public String createDelayedInvocation(Instant instant, String componentId, String opaqueContext);

	/**
	 * Remove a future scheduled invocation by its unique id
	 * 
	 * @param uuid unique id of a delayed invocation
	 */
	public void deleteDelayedInvocation(String uuid);

	/**
	 * Remove future scheduled invocations by the component and/or context,
	 * can specify both items, just a component or just a context, or even leave both
	 * as empty strings to remove all future invocations
	 * 
	 * @param componentId the unique name of a bean in the bean factory which implements 
	 * command pattern DelayedInvocationCommand, may be empty string to match any component
	 * @param opaqueContext the key which the tool can use to uniquely identify some 
	 * entity when invoked; i.e. the context, may be empty string to match any context
	 */
	public void deleteDelayedInvocation(String componentId, String opaqueContext);

	/**
	 * Find future scheduled invocations by the component and/or context,
	 * can specify both items, just a component or just a context, or even leave both
	 * as empty strings to find all future invocations
	 * 
	 * @param componentId the unique name of a bean in the bean factory which implements 
	 * command pattern DelayedInvocationCommand, may be empty string to match any component
	 * @param opaqueContext the key which the tool can use to uniquely identify some 
	 * entity when invoked; i.e. the context, may be empty string to match any context
	 * @return an array of {@link DelayedInvocation} objects representing all scheduled
	 * invocations which match the inputs
	 */
	public DelayedInvocation[] findDelayedInvocations(String componentId, String opaqueContext);

}
