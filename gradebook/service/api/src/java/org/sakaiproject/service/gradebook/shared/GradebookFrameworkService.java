package org.sakaiproject.service.gradebook.shared;

import java.util.Collection;

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

}
