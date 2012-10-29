package net.carmgate.morph.ui.renderer.behavior.morph;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.behavior.impl.morph.Mining;
import net.carmgate.morph.model.solid.mattersource.MatterSource;
import net.carmgate.morph.model.solid.world.World;
import net.carmgate.morph.ui.BehaviorRendererInfo;
import net.carmgate.morph.ui.renderer.behavior.BehaviorRenderer;

import org.apache.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

@BehaviorRendererInfo(preRendering = true)
public class MiningRenderer extends BehaviorRenderer<Mining> {

	private static final int MORPH_RADIUS = 16;
	private static final Logger LOGGER = Logger.getLogger(MiningRenderer.class);

	private static Texture gunfireTexture;
	private static float beamWidth = 2;

	static {
		try {
			gunfireTexture = TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("gunfire-16.png").getPath()));
		} catch (FileNotFoundException e) {
			LOGGER.error("Failed loading gunfire texture: " + e);
		} catch (IOException e) {
			LOGGER.error("Failed loading gunfire texture: " + e);
		}
	}

	@Override
	protected void renderBehavior(int glMode, RenderStyle drawType, Mining behavior) {

		// Source position
		Vect3D sourcePos = behavior.getOwner().getPosInWorld();
		// Target direction normalized to 1
		Vect3D targetDirection = new Vect3D(behavior.getOwner().getTarget().getPos());
		targetDirection.substract(sourcePos);
		Vect3D targetDirectionNormalized = new Vect3D(targetDirection);
		targetDirectionNormalized.normalize(1);
		// The source to effective target vector
		// sourceToEffTargetVect.substract(sourcePos);
		// Where the effective target is hit
		Vect3D effTargetHitPosition = new Vect3D(targetDirection);
		// effTargetHitPosition.normalize(targetDirection.prodScal(sourceToEffTargetVect));
		effTargetHitPosition.add(sourcePos);

		gunfireTexture.bind();
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(1, 0);
		GL11.glVertex2f(sourcePos.x + targetDirectionNormalized.x * MORPH_RADIUS - targetDirectionNormalized.y * beamWidth,
				sourcePos.y + targetDirectionNormalized.y * MORPH_RADIUS + targetDirectionNormalized.x * beamWidth);
		GL11.glTexCoord2f(1, 1);
		GL11.glVertex2f(sourcePos.x + targetDirectionNormalized.x * MORPH_RADIUS + targetDirectionNormalized.y * beamWidth,
				sourcePos.y + targetDirectionNormalized.y * MORPH_RADIUS - targetDirectionNormalized.x * beamWidth);
		GL11.glTexCoord2f(0, 1);
		GL11.glVertex2f(effTargetHitPosition.x + targetDirectionNormalized.y * beamWidth,
				effTargetHitPosition.y - targetDirectionNormalized.x * beamWidth);
		GL11.glTexCoord2f(0, 0);
		GL11.glVertex2f(effTargetHitPosition.x - targetDirectionNormalized.y * beamWidth,
				effTargetHitPosition.y + targetDirectionNormalized.x * beamWidth);
		GL11.glEnd();

		// generating rock particles
		MatterSource matterSource = behavior.getOwner().getTarget();
		Vect3D direction = new Vect3D(behavior.getOwner().getPosInWorld()).substract(matterSource.getPos());
		Vect3D normalizedDirection = new Vect3D(direction);
		normalizedDirection.normalize(1);
		float ySpeed = (float) (Math.random() * 50 + 50);
		long life1 = (long) (20 / ySpeed * 50);
		World.getWorld().getParticleEngine().addParticle((long) (life1 * direction.modulus()),
				new Vect3D(matterSource.getPos().x + (float) (Math.random() * 8) - 4, matterSource.getPos().y + (float) (Math.random() * 8) - 4, 0),
				new Vect3D(normalizedDirection.x * ySpeed, normalizedDirection.y * ySpeed, 0),
				2000, true);

	}
}
