package net.carmgate.morph.model.solid.ship.listener;

import net.carmgate.morph.model.solid.WorldPositionSupport;

public class ShipEvent {

	private final WorldPositionSupport ship;

	public ShipEvent(WorldPositionSupport ship) {
		this.ship = ship;
	}

	public final WorldPositionSupport getShip() {
		return ship;
	}
}
