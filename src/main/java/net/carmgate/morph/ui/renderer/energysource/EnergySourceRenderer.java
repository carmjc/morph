package net.carmgate.morph.ui.renderer.energysource;

import java.util.HashMap;
import java.util.Map;

import net.carmgate.morph.model.solid.energysource.EnergySource;
import net.carmgate.morph.model.solid.energysource.impl.Star;
import net.carmgate.morph.ui.renderer.Renderer;

public class EnergySourceRenderer implements Renderer<EnergySource> {

	private static final Map<Class<?>, Renderer> renderersMap = new HashMap<Class<?>, Renderer>();
	static {
		renderersMap.put(Star.class, new StarRenderer());
	}

	public void render(int glMode, net.carmgate.morph.ui.renderer.Renderer.RenderStyle drawType, EnergySource sceneItem) {
		renderersMap.get(sceneItem.getClass()).render(glMode, drawType, sceneItem);
	}
}
