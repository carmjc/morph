package net.carmgate.morph.ui.interaction.actions;

import net.carmgate.morph.model.solid.world.World;

public class ToggleLockedOnFirstSelectedShip implements Runnable {

	@Override
	public void run() {
		World.lockedOnFirstSelectedShip = !World.lockedOnFirstSelectedShip;
	}

}
