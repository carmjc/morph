package net.carmgate.morph.ui.action;

import net.carmgate.morph.model.World;

import org.lwjgl.input.Keyboard;

public class ToggleFreezeAction implements Runnable {

	public void run() {
		if (Keyboard.getEventKey() == Keyboard.KEY_PAUSE && Keyboard.getEventKeyState()) {
			World.freeze = !World.freeze;
		}
	}

}
