package net.carmgate.morph.ui.renderer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.carmgate.morph.ia.IA;
import net.carmgate.morph.ia.tracker.FixedPositionTracker;
import net.carmgate.morph.model.Vect3D;
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
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

public class ShipRenderer implements Renderer<Ship> {

	public static final Logger logger = Logger.getLogger(ShipRenderer.class);
	private MorphRenderer currentMorphRenderer;
	private static Texture comTexture;
	/** This vector is used as temp vector wherever it's not necessary to keep the value long instead of instanciating a new object. */
	private static Vect3D dummyVect = new Vect3D();

	static {
		try {
			comTexture = TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("com32.png").getPath()));
		} catch (FileNotFoundException e) {
			logger.error("Texture file not found", e);
		} catch (IOException e) {
			logger.error("Texture file loading error", e);
		}
	}

	private final Map<Class<? extends Behavior<?>>, BehaviorRenderer<?>> behaviorRenderersMap = new HashMap<Class<? extends Behavior<?>>, BehaviorRenderer<?>>();
	private final Map<Class<? extends IA>, Renderer> iaRendererMap = new HashMap<Class<? extends IA>, Renderer>();
	private ForceRenderer forceRenderer;

	public ShipRenderer() {

		currentMorphRenderer = new MorphRenderer();

		// Behavior renderers map init
		behaviorRenderersMap.put(Emitting.class, new EmittingRenderer());
		behaviorRenderersMap.put(SpreadingEnergy.class, null);

		// IA renderers map init
		FixedPositionTrackerRenderer fixedPositionTrackerRenderer = new FixedPositionTrackerRenderer();
		fixedPositionTrackerRenderer.init();
		iaRendererMap.put(FixedPositionTracker.class, fixedPositionTrackerRenderer);

		// Force renderer
		forceRenderer = new ForceRenderer();
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
			currentMorphRenderer.render(glMode, renderStyle, morph);
			GL11.glRotatef(-morph.getRotInShip(), 0, 0, 1);
			GL11.glTranslatef(-morph.getPosInShip().x, -morph.getPosInShip().y, -morph.getPosInShip().z);

			// Selection names management
			if (glMode == GL11.GL_SELECT) {
				GL11.glPopName();
			}
		}

		GL11.glRotatef(-ship.rot, 0, 0, 1);
		GL11.glTranslatef(-ship.pos.x, -ship.pos.y, -ship.pos.z);

		// Show center of mass if in debug mode
		if (WorldRenderer.debugDisplay) {
			renderCOM(glMode, renderStyle, ship);
		}

		// Render ship IAs
		for (IA ia : ship.getIAList()) {
			renderIA(glMode, renderStyle, ia);
		}

		// Render morph behaviors
		for (Morph morph : ship.getMorphList()) {
			if (glMode == GL11.GL_RENDER) {
				for (Behavior<?> behavior : morph.alwaysActiveBehaviorList) {
					renderBehavior(glMode, renderStyle, behavior);
				}
				for (Behavior<?> behavior : morph.activableBehaviorList) {
					renderBehavior(glMode, renderStyle, behavior);
				}
			}
		}

		// Render ship speed
		Vect3D comInWorld = new Vect3D(ship.getCenterOfMassInShip());
		ship.transformShipToWorldCoords(comInWorld);
		forceRenderer.renderVector(glMode, renderStyle, comInWorld, ship.posSpeed);
		forceRenderer.renderVector(glMode, renderStyle, comInWorld, ship.posAccel, 100);
	}

	private void renderBehavior(int glMode, RenderStyle drawType, Behavior<?> behavior) {
		BehaviorRenderer<?> behaviorRenderer = behaviorRenderersMap.get(behavior.getClass());
		if (behaviorRenderer != null) {
			behaviorRenderer.render(glMode, drawType, behavior);
		}
	}

	/**
	 * Renders the center of mass.
	 */
	private void renderCOM(int glMode,
			net.carmgate.morph.ui.renderer.Renderer.RenderStyle renderStyle,
			Ship ship) {
		dummyVect.copy(ship.getCenterOfMassInShip());
		ship.transformShipToWorldCoords(dummyVect);
		GL11.glTranslatef(dummyVect.x, dummyVect.y, dummyVect.z);
		comTexture.bind();
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0, 0);
		GL11.glVertex2f(- comTexture.getTextureWidth() / 2, - comTexture.getTextureWidth() / 2);
		GL11.glTexCoord2f(1, 0);
		GL11.glVertex2f(comTexture.getTextureWidth() / 2, - comTexture.getTextureWidth() / 2);
		GL11.glTexCoord2f(1, 1);
		GL11.glVertex2f(comTexture.getTextureWidth() / 2, comTexture.getTextureHeight() / 2);
		GL11.glTexCoord2f(0, 1);
		GL11.glVertex2f(- comTexture.getTextureWidth() / 2, comTexture.getTextureHeight() / 2);
		GL11.glEnd();
		GL11.glTranslatef(-dummyVect.x, -dummyVect.y, -dummyVect.z);
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
