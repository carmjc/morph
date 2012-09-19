package net.carmgate.morph.model.solid.ship.listener;

import net.carmgate.morph.model.solid.ship.Ship;

public class ShipEvent {

	private final Ship ship;

	public ShipEvent(Ship ship) {
		this.ship = ship;
	}

	public Ship getShip() {
		return ship;
	}
}
