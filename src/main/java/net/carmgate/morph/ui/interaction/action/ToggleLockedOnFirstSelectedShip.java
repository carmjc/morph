package net.carmgate.morph.ui.interaction.action;

import net.carmgate.morph.model.solid.world.World;

public class ToggleLockedOnFirstSelectedShip implements Runnable {

	@Override
	public void run() {
		World.lockedOnFirstSelectedShip = !World.lockedOnFirstSelectedShip;
	}

}
