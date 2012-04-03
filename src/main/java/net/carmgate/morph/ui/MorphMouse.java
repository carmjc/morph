package net.carmgate.morph.ui;

import net.carmgate.morph.Main;
import net.carmgate.morph.ui.renderer.WorldRenderer;

/**
 * Allows mouse manipulation in world coordinates.
 * @author Carm
 */
public class MorphMouse {

	/**
	 * @return mouse X position in world coordinates.
	 */
	public static int getX() {
		return (int) ((org.lwjgl.input.Mouse.getX() - Main.WIDTH / 2) * WorldRenderer.scale + WorldRenderer.focalPoint.x) ;
	}

	/**
	 * @return mouse Y position in world coordinates.
	 */
	public static int getY() {
		return - (int) ((org.lwjgl.input.Mouse.getY() - Main.HEIGHT / 2) * WorldRenderer.scale + WorldRenderer.focalPoint.y) ;
	}

}
