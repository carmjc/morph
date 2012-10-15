package net.carmgate.morph.ui.interaction.action.zoom;

import net.carmgate.morph.Main;
import net.carmgate.morph.ui.renderer.WorldRenderer;

import org.apache.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

/**
 * This action should not be run directly.
 * However it handles zoom actions (zoomIn, zoomOut) and zoom smoothing
 */
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
				LOGGER.trace("Final scale: " + WorldRenderer.scale);
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
		// Does nothing
	}

	/**
	 * 
	 */
	public void zoomIn() {
		smoothZoomScaleTarget = smoothZoomScaleTarget / 2;
		LOGGER.trace("Scale target: " + smoothZoomScaleTarget);
		smoothZoomScaleIncrement = (smoothZoomScaleTarget - WorldRenderer.scale) / 15;
	}

	/**
	 * 
	 */
	public void zoomOut() {
		smoothZoomScaleTarget = smoothZoomScaleTarget * 2;
		smoothZoomScaleIncrement = (smoothZoomScaleTarget - WorldRenderer.scale) / 15;
	}
}
