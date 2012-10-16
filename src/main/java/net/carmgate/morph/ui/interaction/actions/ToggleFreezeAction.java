package net.carmgate.morph.ui.interaction.actions;

import net.carmgate.morph.model.solid.world.World;

public class ToggleFreezeAction implements Runnable {

	@Override
	public void run() {
		World.freeze = !World.freeze;
	}

}
