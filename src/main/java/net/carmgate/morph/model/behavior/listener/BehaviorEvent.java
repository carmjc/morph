package net.carmgate.morph.model.behavior.listener;

import net.carmgate.morph.model.behavior.Behavior;

/**
 * <p>Event passed to Behavior listeners when behavior listeners are to be notified.</p>
 */
public class BehaviorEvent {
	private Behavior<?> source;

	/**
	 * Creates a new instance with the provided source.
	 * @param source the behavior source of the event.
	 */
	public BehaviorEvent(Behavior<?> source) {
		this.source = source;
	}

	/**
	 * @return The behavior source of the event.
	 */
	public final Behavior<?> getSource() {
		return source;
	}
}
