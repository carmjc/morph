package net.carmgate.morph.ui.renderer.behavior;

import org.lwjgl.opengl.GL11;

import net.carmgate.morph.model.behavior.Behavior;
import net.carmgate.morph.ui.renderer.Renderer;

public abstract class BehaviorRenderer<K extends Behavior<?>> implements Renderer<Behavior<?>> {

	public final void render(int glMode, RenderStyle drawType, Behavior<?> sceneItem) {
		renderBehavior(glMode, drawType, (K) sceneItem);
	}

	/**
	 * Renders the behavior of the morph
	 * @param glMode {@link GL11#GL_RENDER} or {@link GL11#GL_SELECT}. 
	 * @param drawType Normal or Debug ?
	 * @param sceneItem The scene item to render.
	 * @see {@link Renderer#render(int, net.carmgate.morph.ui.renderer.Renderer.RenderStyle, Object)}.
	 */
	protected abstract void renderBehavior(int glMode, RenderStyle drawType, K sceneItem);
}
