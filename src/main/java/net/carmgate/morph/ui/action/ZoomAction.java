package net.carmgate.morph.ui.action;

import net.carmgate.morph.Main;
import net.carmgate.morph.ui.renderer.WorldRenderer;

import org.apache.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class ZoomAction implements Runnable {

	private static final Logger LOGGER = Logger.getLogger(ZoomAction.class);
	private float smoothZoomScaleTarget = WorldRenderer.scale;
	private float smoothZoomScaleIncrement = 0;

	public ZoomAction() {
		// TODO Auto-generated constructor stub
	}

	public void pursue() {
		if (smoothZoomScaleIncrement != 0) {
			WorldRenderer.scale += smoothZoomScaleIncrement;
			if (Math.abs(smoothZoomScaleTarget - WorldRenderer.scale) < Math.abs(smoothZoomScaleIncrement)) {
				WorldRenderer.scale = smoothZoomScaleTarget;
				smoothZoomScaleIncrement = 0;
			}
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			GLU.gluOrtho2D(WorldRenderer.focalPoint.x - Main.WIDTH / 2 * WorldRenderer.scale,
					WorldRenderer.focalPoint.x + Main.WIDTH / 2 * WorldRenderer.scale,
					WorldRenderer.focalPoint.y + Main.HEIGHT / 2 * WorldRenderer.scale,
					WorldRenderer.focalPoint.y - Main.HEIGHT / 2 * WorldRenderer.scale);
		}
	}

	@Override
	public void run() {
		int eventKey = Keyboard.getEventKey();
		boolean eventKeyState = Keyboard.getEventKeyState();

		if (eventKey == Keyboard.KEY_EQUALS || eventKey == Keyboard.KEY_ADD && !eventKeyState) {
			// Zoom in
			smoothZoomScaleTarget = WorldRenderer.scale / 2;
			smoothZoomScaleIncrement = (smoothZoomScaleTarget - WorldRenderer.scale) / 10;

			LOGGER.debug("Zoom in");
		} else if (eventKey == Keyboard.KEY_6 || eventKey == Keyboard.KEY_MINUS && !eventKeyState) {
			// Zoom out
			smoothZoomScaleTarget = WorldRenderer.scale * 2;
			smoothZoomScaleIncrement = (smoothZoomScaleTarget - WorldRenderer.scale) / 10;
		}

	}
}
