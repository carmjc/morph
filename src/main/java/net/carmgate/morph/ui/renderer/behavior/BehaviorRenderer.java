package net.carmgate.morph.ui.renderer.behavior;

import net.carmgate.morph.model.behavior.Behavior;
import net.carmgate.morph.ui.BehaviorRendererInfo;
import net.carmgate.morph.ui.renderer.Renderer;

import org.lwjgl.opengl.GL11;

@BehaviorRendererInfo
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
	public final void render(int glMode, RenderStyle renderStyle, Behavior<?> behavior) {
		renderBehavior(glMode, renderStyle, (K) behavior);
	}

	/**
	 * Renders a behavior 
	 * @param glMode the mode {@link GL11#GL_SELECT} or {@link GL11#GL_RENDER} 
	 * @param renderStyle a {@link RenderStyle} reference
	 * @param behavior the behavior to render
	 * @param preMorphRendering true if the renderer is called before all morphs rendering
	 */
	public final void render(int glMode, RenderStyle renderStyle, Behavior<?> behavior, boolean preMorphRendering) {
		if (preMorphRendering == getClass().getAnnotation(BehaviorRendererInfo.class).preMorphRendering()) {
			render(glMode, renderStyle, behavior);
		}
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
