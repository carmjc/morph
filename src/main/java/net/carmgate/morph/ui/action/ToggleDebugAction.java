package net.carmgate.morph.ui.action;

import net.carmgate.morph.ui.renderer.WorldRenderer;

import org.apache.log4j.Logger;
import org.lwjgl.input.Keyboard;

/**
 * Toggle debug mode.
 */
public class ToggleDebugAction implements Runnable {

	// Logger
	private static Logger log = Logger.getLogger(ToggleDebugAction.class);

	public void run() {
		if (Keyboard.getEventKey() == Keyboard.KEY_D && !Keyboard.getEventKeyState()) {
			if (WorldRenderer.debugDisplay) {
				WorldRenderer.debugDisplay = false;
				log.info("Graphical debug: Off");
			} else {
				WorldRenderer.debugDisplay = true;
				log.info("Graphical debug: On");
			}
		}
	}

}
