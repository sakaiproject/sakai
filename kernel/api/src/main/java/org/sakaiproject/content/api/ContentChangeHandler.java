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
package org.sakaiproject.content.api;

/**
 * This interface is for copying resources when
 * ContentCopyImpl.copyResource(ContentCopyContext, String) is not enough.
 * For example, a resource type may need to copy data stored outside
 * CONTENT_RESOURCE.
 *
 * To use this service, you need to register your ContentChangeHandler
 * with a particular resource type in the ResourceTypeRegistry,
 * e.g.,
 * <pre>
 * {@code
 * registry.register(resourceType, contentChangeHandler);
 * }
 * </pre>
 *
 * Then, when a resource is being copied,
 * ContentCopyImpl.copyResource(ContentCopyContext, String)
 * will call your handler's copy(ContentResource) method, where any
 * copying of data that is specific to that resource type can occur.
 *
 * @author Nick Wilson
 *
 */
public interface ContentChangeHandler {

	/**
	 * Copies a resource
	 */
	public void copy(ContentResource resource);
}
