package net.carmgate.morph.ui.action;

import net.carmgate.morph.model.solid.world.World;

import org.lwjgl.input.Keyboard;

public class ToggleCombatMode implements Runnable {

	public void run() {
		if (Keyboard.getEventKey() == Keyboard.KEY_A && Keyboard.getEventKeyState()) {
			World.combat = !World.combat;
		}
	}

}
