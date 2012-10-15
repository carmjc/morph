package net.carmgate.morph.ui.renderer.behavior;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.behavior.State;
import net.carmgate.morph.model.behavior.prop.Propulsing;
import net.carmgate.morph.ui.BehaviorRendererInfo;

import org.apache.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

@BehaviorRendererInfo(preMorphRendering = true)
public class PropulsingRenderer extends BehaviorRenderer<Propulsing> {
	private static final int TRAIL_WIDTH = 4;

	private static final int NB_OF_COORD_IN_TRAIL = 100;

	private static final Logger LOGGER = Logger.getLogger(LaserFiringBehaviorRenderer.class);

	/** Trails, by porpulsor morphs' id. */
	private Map<Integer, Queue<Vect3D>> trails = new HashMap<Integer, Queue<Vect3D>>();
	private static Texture trailTexture;

	static {
		try {
			trailTexture = TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("gunfire-16.png").getPath()));
		} catch (FileNotFoundException e) {
			LOGGER.error("Failed loading gunfire texture: " + e);
		} catch (IOException e) {
			LOGGER.error("Failed loading gunfire texture: " + e);
		}
	}

	@Override
	protected void renderBehavior(int glMode, RenderStyle drawType, Propulsing behavior) {
		// Signifying it is still active
		setActive(true);

		// get the trail
		Queue<Vect3D> trail = trails.get(behavior.getOwner().getId());
		if (trail == null) {
			trail = new LinkedList<Vect3D>();
			trails.put(behavior.getOwner().getId(), trail);
		}

		// render the trail
		Vect3D previousPos = null;
		int currentCoordInTrail = 0;
		for (Vect3D pos : trail) {

			if (currentCoordInTrail > 0) {
				Vect3D direction = new Vect3D(pos);
				direction.substract(previousPos);
				direction.normalize(1);

				trailTexture.bind();
				// TextureImpl.bindNone();
				GL11.glBegin(GL11.GL_QUADS);
				GL11.glColor4f(1, 1, 1, (float) currentCoordInTrail / trail.size());
				GL11.glTexCoord2f(1, 0);
				GL11.glVertex2f(previousPos.x - direction.y * TRAIL_WIDTH,
						previousPos.y + direction.x * TRAIL_WIDTH);
				GL11.glTexCoord2f(1, 1);
				GL11.glVertex2f(previousPos.x + direction.y * TRAIL_WIDTH,
						previousPos.y - direction.x * TRAIL_WIDTH);
				GL11.glTexCoord2f(0, 1);
				GL11.glVertex2f(pos.x + direction.y * TRAIL_WIDTH,
						pos.y - direction.x * TRAIL_WIDTH);
				GL11.glTexCoord2f(0, 0);
				GL11.glVertex2f(pos.x - direction.y * TRAIL_WIDTH,
						pos.y + direction.x * TRAIL_WIDTH);
				GL11.glEnd();
			}

			previousPos = new Vect3D(pos);
			currentCoordInTrail++;
		}

		// Remove last one if the trail is full or the behavior INACTIVE
		if (trail.size() >= NB_OF_COORD_IN_TRAIL || behavior.getState() == State.INACTIVE) {
			trail.remove();
		}

		// Add a position to the trail if the behavior is ACTIVE
		if (behavior.getState() == State.ACTIVE) {
			trail.add(new Vect3D(behavior.getOwner().getPosInWorld()));
		}

		// Flag the renderer as inactive if the trail is empty
		if (trail.size() == 0) {
			setActive(false);
		}

	}
}
