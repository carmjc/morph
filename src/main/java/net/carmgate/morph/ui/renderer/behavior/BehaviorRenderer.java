package net.carmgate.morph.ui.renderer.behavior;

import java.util.HashMap;
import java.util.Map;

import net.carmgate.morph.model.behavior.Behavior;
import net.carmgate.morph.model.behavior.Emitting;
import net.carmgate.morph.ui.renderer.Renderer;

public class BehaviorRenderer<T extends Behavior<?>> implements Renderer<T> {

	private static Map<Class<? extends Behavior<?>>, BehaviorRenderer<?>> renderersMap = new HashMap<Class<? extends Behavior<?>>, BehaviorRenderer<?>>();

	static {
		renderersMap.put(Emitting.class, new EmittingRenderer());
	}

	public void render(int glMode, RenderStyle drawType, T behavior) {
		renderersMap.get(behavior.getClass()).render(glMode, drawType, (Behavior<?>) 	behavior);
	}

}
