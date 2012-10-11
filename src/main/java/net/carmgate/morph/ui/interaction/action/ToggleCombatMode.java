package net.carmgate.morph.ui.interaction.action;

import net.carmgate.morph.model.solid.world.World;

public class ToggleCombatMode implements Runnable {

	@Override
	public void run() {
		World.combat = !World.combat;
	}

}
