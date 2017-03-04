package org.sakaiproject.component.app.podcasts;

public class Utilities {
	
	private Utilities() {
	};
	
	/**
	 * Checks that the value isn't null.
	 * @param arg argument to check.
	 * @param name name of variable used in exception message.
	 * @throws IllegalStateException if the supplied argument is null.
	 */
	public static void checkSet(Object arg, String name) {
		if (arg == null) {
			throw new IllegalStateException("The variable "+ name+ " hasn't been set.");
		}
	}
}
