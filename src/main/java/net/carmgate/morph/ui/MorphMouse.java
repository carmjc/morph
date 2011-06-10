package net.carmgate.morph.ui;

import net.carmgate.morph.Main;
import net.carmgate.morph.ui.renderer.WorldRenderer;

public class MorphMouse {

	public static int getX() {
		return (int) ((org.lwjgl.input.Mouse.getX() - Main.WIDTH / 2) * WorldRenderer.scale + WorldRenderer.focalPoint.x) ;
	}

	public static int getY() {
		return (int) ((org.lwjgl.input.Mouse.getY() - Main.HEIGHT / 2) * WorldRenderer.scale + WorldRenderer.focalPoint.y) ;
	}

}
