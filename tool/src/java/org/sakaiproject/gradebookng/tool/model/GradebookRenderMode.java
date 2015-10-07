package org.sakaiproject.gradebookng.tool.model;

/**
 * The set of modes that the Gradebook can render in.
 * Defaults to GRADEABLE.
 * However if permissions have been set, default switches to VIEW_ONLY and must be overriden on a per item basis for TAs.
 * Instructors do not have settable permissions so remain at GRADEABLE.
 */
public enum GradebookRenderMode {

	VIEW_ONLY,
	GRADEABLE;
	
	/**
	 * Get the default render mode
	 * @return the value
	 */
	public static GradebookRenderMode getDefault() {
		return GradebookRenderMode.GRADEABLE;
	}
}
