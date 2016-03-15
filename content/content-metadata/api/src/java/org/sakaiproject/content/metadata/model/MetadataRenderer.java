/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.content.metadata.model;

/**
 * @author Colin Hebert
 */
public interface MetadataRenderer
{
	/**
	 * Specifies the Velocity template used in the rendering part when the user is editing the metadata configuration.
	 *
	 * @return a path to a Velocity template
	 */
	String getMetadataTypeEditTemplate();

	/**
	 * Specifies the Velocity template used in the rendering part when the user is viewing the metadata configuration.
	 *
	 * @return a path to a Velocity template
	 */
	String getMetadataTypeDisplayTemplate();

	/**
	 * Specifies the Velocity template used in the rendering part when the user is editing the metadata of a resource.
	 *
	 * @return a path to a Velocity template
	 */
	String getMetadataValueEditTemplate();

	/**
	 * Specifies the Velocity template used in the rendering part when the user is viewing the metadata of a resource.
	 *
	 * @return a path to a Velocity template
	 */
	String getMetadataValueDisplayTemplate();
}
