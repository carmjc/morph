package net.carmgate.morph.ui.renderer;

import org.lwjgl.opengl.GL11;

/**
 * Classes inheriting this interface allow to draw a member object in the gl scene.
 */
public interface Renderer<T> {

	public static enum RenderStyle {
		NORMAL,
		DEBUG // TODO Take this renderstyle into account instead of WorldRenderer.debugDisplay
	}

	/**
	 * Draw the member object in the scene.
	 * @param glMode {@link GL11#GL_RENDER} or {@link GL11#GL_SELECT}
	 * @param drawType Normal or Debug ?
	 * @param sceneItem the scene item to render.
	 */
	void render(int glMode, RenderStyle drawType, T sceneItem);
}
