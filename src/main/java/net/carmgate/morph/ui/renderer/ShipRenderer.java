package net.carmgate.morph.ui.renderer;

import java.util.HashMap;
import java.util.Map;

import net.carmgate.morph.ia.IA;
import net.carmgate.morph.ia.tracker.FixedPositionTracker;
import net.carmgate.morph.model.behavior.Behavior;
import net.carmgate.morph.model.behavior.Emitting;
import net.carmgate.morph.model.behavior.SpreadingEnergy;
import net.carmgate.morph.model.morph.Morph;
import net.carmgate.morph.model.ship.Ship;
import net.carmgate.morph.ui.renderer.behavior.BehaviorRenderer;
import net.carmgate.morph.ui.renderer.behavior.EmittingRenderer;
import net.carmgate.morph.ui.renderer.ia.FixedPositionTrackerRenderer;

import org.apache.log4j.Logger;
import org.lwjgl.opengl.GL11;

public class ShipRenderer implements Renderer<Ship> {

	public static final Logger logger = Logger.getLogger(ShipRenderer.class);
	private MorphRenderer currentMorphRenderer;

	private final Map<Class<? extends Behavior<?>>, BehaviorRenderer<?>> behaviorRenderersMap = new HashMap<Class<? extends Behavior<?>>, BehaviorRenderer<?>>();
	private final Map<Class<? extends IA>, Renderer> iaRendererMap = new HashMap<Class<? extends IA>, Renderer>();

	public ShipRenderer() {

		currentMorphRenderer = new MorphRenderer();

		// Behavior renderers map init
		behaviorRenderersMap.put(Emitting.class, new EmittingRenderer());
		behaviorRenderersMap.put(SpreadingEnergy.class, null);

		// IA renderers map init
		FixedPositionTrackerRenderer fixedPositionTrackerRenderer = new FixedPositionTrackerRenderer();
		fixedPositionTrackerRenderer.init();
		iaRendererMap.put(FixedPositionTracker.class, fixedPositionTrackerRenderer);
	}

	public void render(int glMode, RenderStyle renderStyle, Ship ship) {
		GL11.glTranslatef(ship.pos.x, ship.pos.y, ship.pos.z);
		GL11.glRotatef(ship.rot, 0, 0, 1);

		// Do whatever is necessary to draw the ship except sub items

		// Draw the morphs
		int selectionId = 0;
		for (Morph morph : ship.getMorphList()) {
			morph.update();

			// Selection names management
			if (glMode == GL11.GL_SELECT) {
				GL11.glPushName(selectionId++);
			}

			GL11.glTranslatef(morph.getPosInShip().x, morph.getPosInShip().y, morph.getPosInShip().z);
			GL11.glRotatef(morph.getRotInShip(), 0, 0, 1);
			currentMorphRenderer.render(glMode, RenderStyle.NORMAL, morph);
			GL11.glRotatef(-morph.getRotInShip(), 0, 0, 1);
			GL11.glTranslatef(-morph.getPosInShip().x, -morph.getPosInShip().y, -morph.getPosInShip().z);

			// Selection names management
			if (glMode == GL11.GL_SELECT) {
				GL11.glPopName();
			}
		}

		GL11.glRotatef(-ship.rot, 0, 0, 1);
		GL11.glTranslatef(-ship.pos.x, -ship.pos.y, -ship.pos.z);

		for (IA ia : ship.getIAList()) {
			renderIA(glMode, renderStyle, ia);
		}

		for (Morph morph : ship.getMorphList()) {
			// Render morph behaviors
			if (glMode == GL11.GL_RENDER) {
				for (Behavior<?> behavior : morph.alwaysActiveBehaviorList) {
					renderBehavior(glMode, renderStyle, behavior);
				}
				for (Behavior<?> behavior : morph.activableBehaviorList) {
					renderBehavior(glMode, renderStyle, behavior);
				}
			}
		}

	}

	private void renderBehavior(int glMode, RenderStyle drawType, Behavior<?> behavior) {
		BehaviorRenderer<?> behaviorRenderer = behaviorRenderersMap.get(behavior.getClass());
		if (behaviorRenderer != null) {
			behaviorRenderer.render(glMode, drawType, behavior);
		}
	}

	private void renderIA(int glMode,
			RenderStyle renderStyle,
			IA ia) {
		Renderer<IA> iaRenderer = iaRendererMap.get(ia.getClass());
		if (iaRenderer != null) {
			iaRenderer.render(glMode, renderStyle, ia);
		}
	}

	public void setMorphRenderer(MorphRenderer morphRenderer) {
		currentMorphRenderer = morphRenderer;
	}

}
