package net.carmgate.morph.ui.renderer.mattersource;

import java.util.HashMap;
import java.util.Map;

import net.carmgate.morph.model.solid.mattersource.Asteroid;
import net.carmgate.morph.model.solid.mattersource.MatterSource;
import net.carmgate.morph.ui.renderer.Renderer;

public class MatterSourceRenderer implements Renderer<MatterSource> {

	private static final Map<Class<?>, Renderer> renderersMap = new HashMap<Class<?>, Renderer>();
	static {
		renderersMap.put(Asteroid.class, new AsteroidRenderer());
	}

	@Override
	public void render(int glMode, net.carmgate.morph.ui.renderer.Renderer.RenderStyle drawType, MatterSource sceneItem) {
		renderersMap.get(sceneItem.getClass()).render(glMode, drawType, sceneItem);
	}
}
