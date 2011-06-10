package net.carmgate.morph.ui.renderer;

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
	 */
	void render(int glMode, RenderStyle drawType, T sceneItem);
}
