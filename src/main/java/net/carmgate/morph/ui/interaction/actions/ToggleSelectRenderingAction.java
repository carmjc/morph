package net.carmgate.morph.ui.interaction.actions;

import net.carmgate.morph.ui.renderer.WorldRenderer;

import org.apache.log4j.Logger;

/**
 * Toggle debug mode.
 */
public class ToggleSelectRenderingAction implements Runnable {

	// Logger
	private static Logger LOGGER = Logger.getLogger(ToggleSelectRenderingAction.class);

	@Override
	public void run() {
		if (WorldRenderer.selectRendering) {
			WorldRenderer.selectRendering = false;
			LOGGER.trace("Select Rendering: Off");
		} else {
			WorldRenderer.selectRendering = true;
			LOGGER.trace("Select Rendering: On");
		}
	}

}
