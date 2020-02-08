package org.sakaiproject.component.api;

/**
 * Simple notifier to bind a sender and listener of ComponentManager events
 * together for a concise style in high level methods.
 */
public class ComponentManagerNotifier {
	private ComponentManager sender;
	private ComponentManagerEventListener listener;

	/** Factory method to make a real notifier or null object. */
	public static ComponentManagerNotifier build(ComponentManager sender, ComponentManagerEventListener listener) {
		return listener == null ? new Blackhole() : new ComponentManagerNotifier(sender, listener);
	}

	private ComponentManagerNotifier() { }

	/** Create a notifier for this sender and listener. Neither may be null. */
	public ComponentManagerNotifier(ComponentManager sender, ComponentManagerEventListener listener) {
		if (sender == null || listener == null) {
			throw new IllegalArgumentException(
					"Notifier objects must be non-null. Use the factory if you need a null object.");
		}
		this.sender = sender;
		this.listener = listener;
	}

	/** Notify the listener that the sender has been created. */
	public void created() {
		listener.onCreate(sender);
	}

	/** Notify the listener that the sender is ready. */
	public void ready() {
		listener.onReady(sender);
	}

	/** Notify the listener that the sender has been closed. */
	public void closed() {
		listener.onClose(sender);
	}

	static class Blackhole extends ComponentManagerNotifier {
		public Blackhole() { }
		public void created() {	}
		public void ready() { }
		public void closed() { }
	}
}
