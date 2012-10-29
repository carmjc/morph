package net.carmgate.morph.ui.renderer.ship;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.carmgate.morph.ia.AI;
import net.carmgate.morph.ia.impl.tracker.GoToPositionAI;
import net.carmgate.morph.model.State;
import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.behavior.Behavior;
import net.carmgate.morph.model.behavior.impl.morph.DiffusingEnergy;
import net.carmgate.morph.model.behavior.impl.morph.Evolving;
import net.carmgate.morph.model.behavior.impl.morph.LaserFiring;
import net.carmgate.morph.model.behavior.impl.morph.Mining;
import net.carmgate.morph.model.behavior.impl.morph.prop.Propulsing;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.ship.Ship;
import net.carmgate.morph.ui.model.UIModel;
import net.carmgate.morph.ui.renderer.ForceRenderer;
import net.carmgate.morph.ui.renderer.Renderer;
import net.carmgate.morph.ui.renderer.RendererUtil;
import net.carmgate.morph.ui.renderer.WorldRenderer;
import net.carmgate.morph.ui.renderer.behavior.BehaviorRenderer;
import net.carmgate.morph.ui.renderer.behavior.morph.LaserFiringRenderer;
import net.carmgate.morph.ui.renderer.behavior.morph.MiningRenderer;
import net.carmgate.morph.ui.renderer.behavior.morph.ProgressRenderer;
import net.carmgate.morph.ui.renderer.behavior.morph.PropulsingRenderer;
import net.carmgate.morph.ui.renderer.behavior.morph.SpreadingEnergyRenderer;
import net.carmgate.morph.ui.renderer.ia.FixedPositionTrackerRenderer;
import net.carmgate.morph.ui.renderer.morph.MorphRenderer;
import net.carmgate.morph.ui.renderer.morph.SelectedMorphRenderer;

import org.apache.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

public class ShipRenderer implements Renderer<Ship> {

	private static final Logger LOGGER = Logger.getLogger(ShipRenderer.class);

	private static final int nbSegments = 40;
	private static final double deltaAngle = 2 * Math.PI / nbSegments;
	private static final double cos = Math.cos(deltaAngle);
	private static final double sin = Math.sin(deltaAngle);

	private final MorphRenderer morphRenderer;
	private final SelectedMorphRenderer selectedMorphRenderer;
	private static Texture comTexture;
	private static Texture bagTexture;
	/** This vector is used as temp vector wherever it's not necessary to keep the value long instead of instanciating a new object. */
	private static Vect3D dummyVect = new Vect3D();

	static {
		try {
			comTexture = TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("com16.png").getPath()));
			bagTexture = TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("particle-8.png").getPath()));
		} catch (FileNotFoundException e) {
			LOGGER.error("Texture file not found", e);
		} catch (IOException e) {
			LOGGER.error("Texture file loading error", e);
		}
	}

	private final Map<Class<? extends Behavior<?>>, BehaviorRenderer<?>> behaviorRenderersMap = new HashMap<Class<? extends Behavior<?>>, BehaviorRenderer<?>>();
	private final Map<Class<? extends AI<Ship>>, Renderer> iaRendererMap = new HashMap<Class<? extends AI<Ship>>, Renderer>();
	private ForceRenderer forceRenderer;

	public ShipRenderer() {

		morphRenderer = new MorphRenderer();
		selectedMorphRenderer = new SelectedMorphRenderer();

		// Behavior renderers map init
		behaviorRenderersMap.put(DiffusingEnergy.class, null);
		behaviorRenderersMap.put(Evolving.class, new ProgressRenderer());
		behaviorRenderersMap.put(LaserFiring.class, new LaserFiringRenderer());
		behaviorRenderersMap.put(Propulsing.class, new PropulsingRenderer());
		behaviorRenderersMap.put(DiffusingEnergy.class, new SpreadingEnergyRenderer());
		behaviorRenderersMap.put(Mining.class, new MiningRenderer());

		// IA renderers map init
		FixedPositionTrackerRenderer fixedPositionTrackerRenderer = new FixedPositionTrackerRenderer();
		fixedPositionTrackerRenderer.init();
		iaRendererMap.put(GoToPositionAI.class, fixedPositionTrackerRenderer);

		// Force renderer
		forceRenderer = new ForceRenderer();
	}

	@Override
	public void render(int glMode, RenderStyle renderStyle, Ship ship) {
		// Selection names management
		if (glMode == GL11.GL_SELECT) {
			GL11.glPushName((int) ship.getId());
		}

		GL11.glTranslatef(ship.getPos().x, ship.getPos().y, ship.getPos().z);
		GL11.glTranslatef(ship.getComInShip().x, ship.getComInShip().y, ship.getComInShip().z);

		if (glMode != GL11.GL_SELECT) {
			// Draw the ship matter bag
			if (ship.getStoredMass() > 0) {
				GL11.glColor4f(1f, 1f, 1f, 0.8f);
				RendererUtil.drawPartialCircle(bagTexture, nbSegments, cos, sin, (float) Math.sqrt(ship.getStoredMass()) / 8, 0, 1, true, glMode);
			}
		}

		GL11.glRotatef(ship.getRot(), 0, 0, 1);
		GL11.glTranslatef(-ship.getComInShip().x, -ship.getComInShip().y, -ship.getComInShip().z);

		// Draw the morphs
		// TODO We clone the list to avoid ConcurrentModificationExceptions. We should try to improve this.
		List<Morph> shipMorphs = new ArrayList<Morph>(ship.getMorphsByIds().values());
		for (Morph morph : shipMorphs) {
			GL11.glTranslatef(morph.getPosInShip().x, morph.getPosInShip().y, morph.getPosInShip().z);
			if (glMode != GL11.GL_SELECT) {
				GL11.glRotatef(morph.getRotInShip(), 0, 0, 1);
			}
			morphRenderer.render(glMode, renderStyle, morph);
			if (glMode != GL11.GL_SELECT) {
				GL11.glRotatef(-morph.getRotInShip(), 0, 0, 1);
			}
			GL11.glTranslatef(-morph.getPosInShip().x, -morph.getPosInShip().y, -morph.getPosInShip().z);

		}

		GL11.glTranslatef(ship.getComInShip().x, ship.getComInShip().y, ship.getComInShip().z);
		GL11.glRotatef(-ship.getRot(), 0, 0, 1);
		GL11.glTranslatef(-ship.getComInShip().x, -ship.getComInShip().y, -ship.getComInShip().z);
		GL11.glTranslatef(-ship.getPos().x, -ship.getPos().y, -ship.getPos().z);

		renderShipMorphBehaviors(glMode, renderStyle, ship, false);
		renderShipCom(glMode, renderStyle, ship);
		renderShipIAs(glMode, renderStyle, ship);
		renderShipSpeed(glMode, renderStyle, ship);

		if (glMode != GL11.GL_SELECT && WorldRenderer.debugDisplay) {
			for (Morph morph : UIModel.getUiModel().getSelectionModel().getSelectedMorphs().values()) {
				GL11.glTranslatef(morph.getPosInWorld().x, morph.getPosInWorld().y, morph.getPosInWorld().z);
				selectedMorphRenderer.render(glMode, renderStyle, morph);
				GL11.glTranslatef(-morph.getPosInWorld().x, -morph.getPosInWorld().y, -morph.getPosInWorld().z);
			}
		}

		// Selection names management
		if (glMode == GL11.GL_SELECT) {
			GL11.glPopName();
		}
	}

	private void renderBehavior(int glMode, RenderStyle drawType, Behavior<?> behavior, boolean preMorphRendering) {
		BehaviorRenderer<?> behaviorRenderer = behaviorRenderersMap.get(behavior.getClass());
		if (behavior.getState() == State.ACTIVE || behaviorRenderer != null && (behaviorRenderer.isActive() || behaviorRenderer.isActive(behavior))) {
			if (behaviorRenderer != null) {
				behaviorRenderer.render(glMode, drawType, behavior, preMorphRendering);
			}
		}
	}

	private void renderIA(int glMode,
			RenderStyle renderStyle,
			AI<Ship> ia) {
		Renderer<AI<Ship>> iaRenderer = iaRendererMap.get(ia.getClass());
		if (iaRenderer != null) {
			iaRenderer.render(glMode, renderStyle, ia);
		}
	}

	/**
	 * @param glMode
	 * @param renderStyle
	 * @param ship
	 */
	private void renderShipCom(int glMode, RenderStyle renderStyle, Ship ship) {
		if (WorldRenderer.debugDisplay) {
			dummyVect.copy(ship.getComInShip());
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
	}

	/**
	 * @param glMode
	 * @param renderStyle
	 * @param ship
	 */
	private void renderShipIAs(int glMode, RenderStyle renderStyle, Ship ship) {
		for (AI<Ship> ia : ship.getAIList()) {
			renderIA(glMode, renderStyle, ia);
		}
	}

	/**
	 * Render morph active behaviors as needed
	 * @param glMode
	 * @param renderStyle
	 * @param ship
	 */
	public void renderShipMorphBehaviors(int glMode, RenderStyle renderStyle, Ship ship, boolean preMorphRendering) {
		for (Morph morph : ship.getMorphsByIds().values()) {
			if (glMode == GL11.GL_RENDER) {
				for (Behavior<?> behavior : morph.getAlwaysActiveBehaviorList()) {
					renderBehavior(glMode, renderStyle, behavior, preMorphRendering);
				}
				for (Behavior<?> behavior : morph.getActivationLinkedBehaviorList()) {
					renderBehavior(glMode, renderStyle, behavior, preMorphRendering);
				}
				for (Behavior<?> behavior : morph.getActivationIsolatedBehaviorList()) {
					renderBehavior(glMode, renderStyle, behavior, preMorphRendering);
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
		Vect3D comInWorld = new Vect3D(ship.getComInShip());
		ship.transformShipToWorldCoords(comInWorld);

		if (renderStyle == RenderStyle.DEBUG) {
			// Speed in red
			forceRenderer.renderVector(glMode, renderStyle, comInWorld, ship.getPosSpeed(), new float[] { 1.0f, 0.5f, 0.5f });
			// Accel in green
			forceRenderer.renderVector(glMode, renderStyle, comInWorld, ship.getPosAccel(), new float[] { 0.5f, 1.0f, 0.5f });
			// // Desired speed in blue
			// GoToPositionAI ai;
			// if (ship.getAIList().size() > 0) {
			// ai = (GoToPositionAI) ship.getAIList().iterator().next();
			// forceRenderer.renderVector(glMode, renderStyle, comInWorld, ai.desiredVelocity, new float[] {
			// 0.5f, 0.5f, 1.0f });
			// }
		}
	}

}
