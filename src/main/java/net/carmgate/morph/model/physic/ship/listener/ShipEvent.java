package net.carmgate.morph.model.physic.ship.listener;

import net.carmgate.morph.model.physic.ship.Ship;

public class ShipEvent {

	private final Ship ship;

	public ShipEvent(Ship ship) {
		this.ship = ship;
	}

	public Ship getShip() {
		return ship;
	}
}
