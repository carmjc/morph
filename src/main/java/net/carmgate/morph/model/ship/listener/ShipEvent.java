package net.carmgate.morph.model.ship.listener;

import net.carmgate.morph.model.ship.Ship;

public class ShipEvent {

	private final Ship ship;

	public ShipEvent(Ship ship) {
		this.ship = ship;
	}

	public Ship getShip() {
		return ship;
	}
}
