package net.carmgate.morph.model.behavior.listener;

/**
 * <p>This interface should be implemented by classes willing to be notified of behavior events.
 * Of course, all listeners are called for any event on the behavior on which they have been registered.</p>
 * <p>The events currently handled are the following :
 * 	<ul><li>behaviorDeactivated: when a behavior is deactivated.</li></ul>
 * </p>
 * <p>The {@link BehaviorEvent} will store the behavior that is the source of the event.</p>
 */
public interface BehaviorListener {

	/**
	 * Called when a behavior is deactivated.
	 * @param behaviorEvent
	 */
	void behaviorDeactivated(BehaviorEvent behaviorEvent);
}
