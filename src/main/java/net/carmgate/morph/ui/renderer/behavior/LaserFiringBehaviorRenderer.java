package net.carmgate.morph.ui.renderer.behavior;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.behavior.LaserFiringBehavior;

import org.apache.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

public class LaserFiringBehaviorRenderer extends BehaviorRenderer<LaserFiringBehavior> {

	private static final int MORPH_RADIUS = 16;
	private static final double BEAM_WIDTH_CHANGE_RATE = 0.2;
	private static final Logger LOGGER = Logger.getLogger(LaserFiringBehaviorRenderer.class);
	private static Texture gunfireTexture;

	private static float currentBeamWidth = 1;
	private static float targetBeamWidth = 1;

	static {
		try {
			gunfireTexture = TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("gunfire-16.png").getPath()));
		} catch (FileNotFoundException e) {
			LOGGER.error("Failed loading gunfire texture: " + e);
		} catch (IOException e) {
			LOGGER.error("Failed loading gunfire texture: " + e);
		}
	}

	public LaserFiringBehaviorRenderer() {
	}

	@Override
	protected void renderBehavior(int glMode, RenderStyle drawType, LaserFiringBehavior behavior) {
		if (behavior.getEffectiveTarget() == null) {
			return;
		}

		// Source position
		Vect3D sourcePos = behavior.getOwner().getPosInWorld();
		// Target direction normalized to 1
		Vect3D targetDirection = new Vect3D(behavior.getOwner().getTarget().getPosInWorld());
		targetDirection.substract(sourcePos);
		targetDirection.normalize(1);
		// The source to effective target vector
		Vect3D sourceToEffTargetVect = behavior.getEffectiveTarget().getPosInWorld();
		sourceToEffTargetVect.substract(sourcePos);
		// Where the effective target is hit
		Vect3D effTargetHitPosition = new Vect3D(targetDirection);
		effTargetHitPosition.normalize(targetDirection.prodScal(sourceToEffTargetVect));
		effTargetHitPosition.add(sourcePos);

		// Animation of the beam
		if (Math.abs(currentBeamWidth - targetBeamWidth) < BEAM_WIDTH_CHANGE_RATE) {
			targetBeamWidth = 1 + (float) (Math.random() * 4);
		} else {
			if (currentBeamWidth < targetBeamWidth) {
				currentBeamWidth += BEAM_WIDTH_CHANGE_RATE;
			} else {
				currentBeamWidth -= BEAM_WIDTH_CHANGE_RATE;
			}
		}

		gunfireTexture.bind();
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(1, 0);
		GL11.glVertex2f(sourcePos.x + targetDirection.x * MORPH_RADIUS - targetDirection.y * currentBeamWidth,
				sourcePos.y + targetDirection.y * MORPH_RADIUS + targetDirection.x * currentBeamWidth);
		GL11.glTexCoord2f(1, 1);
		GL11.glVertex2f(sourcePos.x + targetDirection.x * MORPH_RADIUS + targetDirection.y * currentBeamWidth,
				sourcePos.y + targetDirection.y * MORPH_RADIUS - targetDirection.x * currentBeamWidth);
		GL11.glTexCoord2f(0, 1);
		GL11.glVertex2f(effTargetHitPosition.x - targetDirection.x * MORPH_RADIUS + targetDirection.y * currentBeamWidth,
				effTargetHitPosition.y - targetDirection.y * MORPH_RADIUS - targetDirection.x * currentBeamWidth);
		GL11.glTexCoord2f(0, 0);
		GL11.glVertex2f(effTargetHitPosition.x - targetDirection.x * MORPH_RADIUS - targetDirection.y * currentBeamWidth,
				effTargetHitPosition.y - targetDirection.y * MORPH_RADIUS + targetDirection.x * currentBeamWidth);
		GL11.glEnd();
	}
}
