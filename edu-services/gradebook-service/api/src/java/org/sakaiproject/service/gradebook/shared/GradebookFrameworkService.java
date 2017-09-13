/**
 * Copyright (c) 2003-2015 The Apereo Foundation
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
package org.sakaiproject.service.gradebook.shared;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This service is meant to be used by the framework which manages the Gradebook
 * application. The service provides various administrative functions which
 * aren't handled by the application itself, including creating and removing
 * gradebooks, and managing system-wide grading schemes.
 *
 * Because these aren't operations taken by the application, responsibility
 * for security is left completely up to the client. THERE ARE NO AUTHORIZATION
 * CHECKS.
 *
 * In other words, DO NOT provide open web service access to this service's methods!
 */
public interface GradebookFrameworkService {
	/**
	 * Creates a new gradebook with the given UID and name
	 *
	 * @param uid
	 *            The UID used to specify a gradebook and its associated data.
	 *            It is the caller's responsibility to ensure that this is
	 *            unique within gradebook storage.
	 *
	 * @param name
	 *            The name of the gradebook, to be used for logging and other
	 *            conveniences by the application. This should be the name of
	 *            the site or the course. It is only used for convenience, and
	 *            does not need to be unique.
	 */
	public void addGradebook(String uid, String name);

	/**
	 * Deletes the gradebook with the given UID, along with all its associated
	 * data.
	 */
	public void deleteGradebook(String uid) throws GradebookNotFoundException;

	/**
	 * Checks to see whether a gradebook with the given uid exists.
	 *
	 * @param gradebookUid
	 *            The gradebook UID to check
	 * @return Whether the gradebook exists
	 */
	public boolean isGradebookDefined(String gradebookUid);

	/**
     * @param gradingScaleDefinitions
     *	A collection of GradingScaleDefinition beans.
	 */
	public void setAvailableGradingScales(Collection gradingScaleDefinitions);

	/**
     * @param uid
     *	The UID of the grading scale to use as the default for new gradebooks.
	 */
	public void setDefaultGradingScale(String uid);

	/**
	 *	Get all of the available Grading Scales in the system.
	 *	@return List of GradingScale
	 */
	public List getAvailableGradingScales();
	
	/**
	 *	Get all of the available Grading Scales in the system, as shared DTOs.
	 *	@return List of GradingScaleDefinition
	 */
	public List<GradingScaleDefinition> getAvailableGradingScaleDefinitions();

	/**
	 * Adds a new grade scale to an existing gradebook.
	 *
	 * @param scaleUuid
	 *   The uuid of the scale we want to be added to the gradebook
	 * @param gradebookUid
	 *   The gradebook with GradeMappings where we will add the grading scale.
	 *
	 */
	public void saveGradeMappingToGradebook(String scaleUuid, String gradebookUid);

	/**
	 * Update a grademapping with new values.
	 *
	 * @param gradeMappingId id of GradeMapping to update
	 * @param gradeMap the updated map of grades
	 *
	 */
	public void updateGradeMapping(Long gradeMappingId, Map<String, Double> gradeMap);

}
