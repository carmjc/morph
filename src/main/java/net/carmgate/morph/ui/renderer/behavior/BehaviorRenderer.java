package net.carmgate.morph.ui.renderer.behavior;

import net.carmgate.morph.model.behavior.Behavior;
import net.carmgate.morph.ui.renderer.Renderer;

import org.lwjgl.opengl.GL11;

public abstract class BehaviorRenderer<K extends Behavior<?>> implements Renderer<Behavior<?>> {

	private boolean active;

	/**
	 * Denotes if the behavior renderer is active or not.
	 * It is used to denote the fact that the renderer might still be temporarily active after behavior deactivation.
	 * @return true if the renderer is still active.
	 */
	public boolean isActive() {
		return active;
	}

	@Override
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

	protected void setActive(boolean active) {
		this.active = active;
	}
}
