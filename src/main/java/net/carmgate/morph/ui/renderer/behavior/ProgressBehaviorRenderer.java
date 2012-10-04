package net.carmgate.morph.ui.renderer.behavior;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.behavior.Behavior;
import net.carmgate.morph.model.behavior.ProgressBehavior;

import org.apache.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

public class ProgressBehaviorRenderer extends BehaviorRenderer<Behavior<?>> {

	private static final Logger LOGGER = Logger.getLogger(ProgressBehaviorRenderer.class);

	private static final int nbSegments = 40;
	private static final double deltaAngle = 2 * Math.PI / nbSegments;
	private static final double cos = Math.cos(deltaAngle);
	private static final double sin = Math.sin(deltaAngle);

	private static Texture texture;

	static {
		try {
			texture = TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("ui/circular-progress-64.png").getPath()));
			// texture = TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("ui/circular-menu-fake.png").getPath()));
		} catch (FileNotFoundException e) {
			LOGGER.error("Failed loading texture.", e);
		} catch (IOException e) {
			LOGGER.error("Failed loading texture.", e);
		}
	}

	@Override
	protected void renderBehavior(int glMode, RenderStyle drawType, Behavior<?> behavior) {
		// if this behavior does not implement ProgressBehavior,
		// we have no way to render the progress
		if (!(behavior instanceof ProgressBehavior)) {
			return;
		}

		ProgressBehavior progressBehavior = (ProgressBehavior) behavior;

		Vect3D posInWorld = behavior.getOwner().getPosInWorld();
		GL11.glTranslatef(posInWorld.x, posInWorld.y, posInWorld.z);

		float alphaLevel = 1f;
		GL11.glColor4f(1f, 1f, 1f, alphaLevel);
		texture.bind();
		GL11.glBegin(GL11.GL_TRIANGLES);
		double t; // temporary data holder
		double x = 0.5; // radius = 1
		double y = 0;
		for (int i = 0; i < nbSegments * progressBehavior.getProgress(); i++) {
			GL11.glTexCoord2f(0.5f, 0.5f);
			GL11.glVertex2f(0, 0);
			GL11.glTexCoord2d(0.5f + x, 0.5f + y);
			GL11.glVertex2d(texture.getTextureWidth() * x, texture.getTextureWidth() * y);

			t = x;
			x = cos * x - sin * y;
			y = sin * t + cos * y;

			GL11.glTexCoord2d(0.5f + x, 0.5f + y);
			GL11.glVertex2d(texture.getTextureWidth() * x, texture.getTextureWidth() * y);
		}
		GL11.glEnd();
		alphaLevel /= 0.3f;
		GL11.glColor4f(1f, 1f, 1f, alphaLevel);

		GL11.glTranslatef(-posInWorld.x, -posInWorld.y, -posInWorld.z);
	}
}
