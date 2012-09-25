package net.carmgate.morph.ui.renderer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.carmgate.morph.ia.IA;
import net.carmgate.morph.ia.tracker.FixedPositionTracker;
import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.behavior.Behavior;
import net.carmgate.morph.model.behavior.SpreadingEnergy;
import net.carmgate.morph.model.behavior.old.Emitting;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.ship.Ship;
import net.carmgate.morph.ui.renderer.behavior.BehaviorRenderer;
import net.carmgate.morph.ui.renderer.behavior.EmittingRenderer;
import net.carmgate.morph.ui.renderer.ia.FixedPositionTrackerRenderer;

import org.apache.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

public class ShipRenderer implements Renderer<Ship> {

	private static final Logger LOGGER = Logger.getLogger(ShipRenderer.class);
	private MorphRenderer currentMorphRenderer;
	private static Texture comTexture;
	/** This vector is used as temp vector wherever it's not necessary to keep the value long instead of instanciating a new object. */
	private static Vect3D dummyVect = new Vect3D();

	static {
		try {
			comTexture = TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("com32.png").getPath()));
		} catch (FileNotFoundException e) {
			LOGGER.error("Texture file not found", e);
		} catch (IOException e) {
			LOGGER.error("Texture file loading error", e);
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
		// Selection names management
		if (glMode == GL11.GL_SELECT) {
			GL11.glPushName(ship.getId());
		}

		GL11.glTranslatef(ship.getPos().x, ship.getPos().y, ship.getPos().z);
		GL11.glRotatef(ship.getRot(), 0, 0, 1);

		// Do whatever is necessary to draw the ship except sub items

		// Draw the morphs
		// TODO We clone the list to avoid ConcurrentModificationExceptions. We should try to improve this.
		List<Morph> shipMorphs = new ArrayList<Morph>(ship.getMorphsByIds().values());
		for (Morph morph : shipMorphs) {
			GL11.glTranslatef(morph.getPosInShip().x, morph.getPosInShip().y, morph.getPosInShip().z);
			GL11.glRotatef(morph.getRotInShip(), 0, 0, 1);
			currentMorphRenderer.render(glMode, renderStyle, morph);
			GL11.glRotatef(-morph.getRotInShip(), 0, 0, 1);
			GL11.glTranslatef(-morph.getPosInShip().x, -morph.getPosInShip().y, -morph.getPosInShip().z);

		}

		GL11.glRotatef(-ship.getRot(), 0, 0, 1);
		GL11.glTranslatef(-ship.getPos().x, -ship.getPos().y, -ship.getPos().z);

		renderShipCenterOfMass(glMode, renderStyle, ship);
		renderShipIAs(glMode, renderStyle, ship);
		renderShipMorphBehaviors(glMode, renderStyle, ship);
		renderShipSpeed(glMode, renderStyle, ship);

		// Selection names management
		if (glMode == GL11.GL_SELECT) {
			GL11.glPopName();
		}
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
		GL11.glVertex2f(-comTexture.getTextureWidth() / 2, -comTexture.getTextureWidth() / 2);
		GL11.glTexCoord2f(1, 0);
		GL11.glVertex2f(comTexture.getTextureWidth() / 2, -comTexture.getTextureWidth() / 2);
		GL11.glTexCoord2f(1, 1);
		GL11.glVertex2f(comTexture.getTextureWidth() / 2, comTexture.getTextureHeight() / 2);
		GL11.glTexCoord2f(0, 1);
		GL11.glVertex2f(-comTexture.getTextureWidth() / 2, comTexture.getTextureHeight() / 2);
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

	/**
	 * @param glMode
	 * @param renderStyle
	 * @param ship
	 */
	private void renderShipCenterOfMass(int glMode, RenderStyle renderStyle, Ship ship) {
		if (WorldRenderer.debugDisplay) {
			renderCOM(glMode, renderStyle, ship);
		}
	}

	/**
	 * @param glMode
	 * @param renderStyle
	 * @param ship
	 */
	private void renderShipIAs(int glMode, RenderStyle renderStyle, Ship ship) {
		for (IA ia : ship.getIAList()) {
			renderIA(glMode, renderStyle, ia);
		}
	}

	/**
	 * Render morph behavior as needed
	 * @param glMode
	 * @param renderStyle
	 * @param ship
	 */
	private void renderShipMorphBehaviors(int glMode, RenderStyle renderStyle, Ship ship) {
		for (Morph morph : ship.getMorphsByIds().values()) {
			if (glMode == GL11.GL_RENDER) {
				for (Behavior<?> behavior : morph.getAlwaysActiveBehaviorList()) {
					renderBehavior(glMode, renderStyle, behavior);
				}
				for (Behavior<?> behavior : morph.getActivableBehaviorList()) {
					renderBehavior(glMode, renderStyle, behavior);
				}
			}
		}
	}

	/**
	 * Renders ship speed vector
	 * @param glMode
	 * @param renderStyle
	 * @param ship
	 */
	private void renderShipSpeed(int glMode, RenderStyle renderStyle, Ship ship) {
		// Render ship speed
		Vect3D comInWorld = new Vect3D(ship.getCenterOfMassInShip());
		ship.transformShipToWorldCoords(comInWorld);

		if (renderStyle == RenderStyle.DEBUG) {
			forceRenderer.renderVector(glMode, renderStyle, comInWorld, ship.getPosSpeed(), new float[] { 1.0f, 0.5f, 0.5f });
			forceRenderer.renderVector(glMode, renderStyle, comInWorld, ship.getPosAccel(), 100, new float[] { 0.5f, 1.0f, 0.5f });
		}
	}

	public void setMorphRenderer(MorphRenderer morphRenderer) {
		currentMorphRenderer = morphRenderer;
	}

}
