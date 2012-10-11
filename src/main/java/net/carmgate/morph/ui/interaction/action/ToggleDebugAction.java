package net.carmgate.morph.ui.interaction.action;

import net.carmgate.morph.ui.renderer.WorldRenderer;

import org.apache.log4j.Logger;

/**
 * Toggle debug mode.
 */
public class ToggleDebugAction implements Runnable {

	// Logger
	private static Logger LOGGER = Logger.getLogger(ToggleDebugAction.class);

	@Override
	public void run() {
		if (WorldRenderer.debugDisplay) {
			WorldRenderer.debugDisplay = false;
			LOGGER.info("Graphical debug: Off");
		} else {
			WorldRenderer.debugDisplay = true;
			LOGGER.info("Graphical debug: On");
		}
	}

}
