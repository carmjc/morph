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

		Vect3D source = behavior.getOwner().getPosInWorld();
		Vect3D direction = new Vect3D(behavior.getOwner().getTarget().getPosInWorld());
		direction.substract(source);
		direction.normalize(1);
		Vect3D target = new Vect3D(behavior.getOwner().getTarget().getPosInWorld());
		target.normalize(1);
		target.normalize(behavior.getEffectiveTarget().getPosInWorld().prodScal(target));

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
		GL11.glVertex2f(source.x + direction.x * MORPH_RADIUS - direction.y * currentBeamWidth,
				source.y + direction.y * MORPH_RADIUS + direction.x * currentBeamWidth);
		GL11.glTexCoord2f(1, 1);
		GL11.glVertex2f(source.x + direction.x * MORPH_RADIUS + direction.y * currentBeamWidth,
				source.y + direction.y * MORPH_RADIUS - direction.x * currentBeamWidth);
		GL11.glTexCoord2f(0, 1);
		GL11.glVertex2f(target.x - direction.x * MORPH_RADIUS + direction.y * currentBeamWidth,
				target.y - direction.y * MORPH_RADIUS - direction.x * currentBeamWidth);
		GL11.glTexCoord2f(0, 0);
		GL11.glVertex2f(target.x - direction.x * MORPH_RADIUS - direction.y * currentBeamWidth,
				target.y - direction.y * MORPH_RADIUS + direction.x * currentBeamWidth);
		GL11.glEnd();
	}
}
