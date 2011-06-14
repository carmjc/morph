package net.carmgate.morph.ui.renderer.behavior;

import net.carmgate.morph.model.behavior.Behavior;
import net.carmgate.morph.ui.renderer.Renderer;

public abstract class BehaviorRenderer<K extends Behavior<?>> implements Renderer<Behavior<?>> {

	public final void render(int glMode, RenderStyle drawType, Behavior<?> sceneItem) {
		renderBehavior(glMode, drawType, (K) sceneItem);
	}

	protected abstract void renderBehavior(int glMode, RenderStyle drawType, K sceneItem);
}
