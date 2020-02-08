package org.sakaiproject.component.api;

/**
 * Listener interface for lifecycle events of a ComponentManager.
 */
public interface ComponentManagerEventListener {
	/**
	 * Event: This component manager has been created, but may not yet be ready for
	 * use.
	 */
	public void onCreate(ComponentManager manager);

	/** Event: This component manager is now ready for use. */
	public void onReady(ComponentManager manager);

	/** Event: This component manager has closed and should be used. */
	public void onClose(ComponentManager manager);
}