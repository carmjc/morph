package net.carmgate.morph.ui.renderer;

import java.util.HashMap;
import java.util.Map;

import net.carmgate.morph.model.behavior.Behavior;
import net.carmgate.morph.model.behavior.Emitting;
import net.carmgate.morph.model.behavior.SpreadingEnergy;
import net.carmgate.morph.model.morph.Morph;
import net.carmgate.morph.model.ship.Ship;
import net.carmgate.morph.ui.renderer.behavior.BehaviorRenderer;
import net.carmgate.morph.ui.renderer.behavior.EmittingRenderer;

import org.lwjgl.opengl.GL11;

public class ShipRenderer implements Renderer<Ship> {

	private MorphRenderer currentMorphRenderer;

	private final Map<Class<? extends Behavior<?>>, BehaviorRenderer<?>> renderersMap = new HashMap<Class<? extends Behavior<?>>, BehaviorRenderer<?>>();

	public ShipRenderer() {
		currentMorphRenderer = new MorphRenderer();

		// Behavior renderers map init
		renderersMap.put(Emitting.class, new EmittingRenderer());
		renderersMap.put(SpreadingEnergy.class, null);
	}

	public void render(int glMode, RenderStyle renderStyle, Ship ship) {
		GL11.glTranslatef(ship.pos.x, ship.pos.y, ship.pos.z);
		GL11.glRotatef(ship.rot.z, 0, 0, 1);

		// Do whatever is necessary to draw the ship except sub items

		GL11.glRotatef(-ship.rot.z, 0, 0, 1);
		GL11.glTranslatef(-ship.pos.x, -ship.pos.y, -ship.pos.z);

		// Draw the morphs
		int selectionId = 0;
		for (Morph morph : ship.getMorphList()) {
			morph.update();

			// Selection names management
			if (glMode == GL11.GL_SELECT) {
				GL11.glPushName(selectionId++);
			}

			currentMorphRenderer.render(glMode, RenderStyle.NORMAL, morph);

			// Selection names management
			if (glMode == GL11.GL_SELECT) {
				GL11.glPopName();
			}
		}

		for (Morph morph : ship.getMorphList()) {
			// Render morph behaviors
			if (glMode == GL11.GL_RENDER) {
				for (Behavior<?> behavior : morph.alwaysActiveSpecificBehaviorList) {
					renderBehavior(glMode, renderStyle, behavior);
				}
				for (Behavior<?> behavior : morph.activableSpecificBehaviorList) {
					renderBehavior(glMode, renderStyle, behavior);
				}
			}
		}

	}

	private void renderBehavior(int glMode, RenderStyle drawType, Behavior<?> behavior) {
		BehaviorRenderer<?> behaviorRenderer = renderersMap.get(behavior.getClass());
		if (behaviorRenderer != null) {
			behaviorRenderer.render(glMode, drawType, behavior);
		}
	}

	public void setMorphRenderer(MorphRenderer morphRenderer) {
		this.currentMorphRenderer = morphRenderer;
	}

}
