package net.carmgate.morph.model.solid.ship.listener;

/**
 * Ship listener.
 * Use a ship listener to be notified of events happening on ships.
 * Whatever the ship, all listeners are notified when there is an event on any ship.
 */
public interface ShipListener {

	/**
	 * Triggered when a ship's speed falls to zero from a non-zero value.
	 * The ship speed is understood as the com speed.
	 * It means that this event might be triggered if the com speed falls to zero
	 * even if the ship is still rotating.
	 * @param event
	 */
	void shipStopped(ShipEvent event);
}
