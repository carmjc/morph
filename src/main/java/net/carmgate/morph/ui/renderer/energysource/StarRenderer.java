package net.carmgate.morph.ui.renderer.energysource;

import java.io.FileInputStream;
import java.io.IOException;

import net.carmgate.morph.model.ModelConstants;
import net.carmgate.morph.model.solid.energysource.Star;
import net.carmgate.morph.ui.renderer.Renderer;

import org.apache.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureImpl;
import org.newdawn.slick.opengl.TextureLoader;

public class StarRenderer implements Renderer<Star> {

	private static final int nbSegments = 200;
	private static final double deltaAngle = 2 * Math.PI / nbSegments;
	private static final double cos = Math.cos(deltaAngle);
	private static final double sin = Math.sin(deltaAngle);

	private static final Logger LOGGER = Logger.getLogger(StarRenderer.class);

	private static Texture baseTexture;
	static {
		try {
			baseTexture = TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("stars/blue.png").getPath()));
		} catch (IOException e) {
			LOGGER.error("Error while loading morph textures.", e);
		}
	}

	@Override
	public void render(int glMode, net.carmgate.morph.ui.renderer.Renderer.RenderStyle drawType, Star star) {
		GL11.glTranslatef(star.getPos().x, star.getPos().y, star.getPos().z);

		float effectRadius = star.getEffectRadius();
		double dangerRadius = Math.sqrt(Math.pow(star.getEffectRadius(), 2)
				* (1 - (ModelConstants.MAX_DIFFUSED_EXCESS_ENERGY_PER_SECOND + ModelConstants.NEW_MASS_PER_SECOND
						/ ModelConstants.MASS_LOSS_TO_EXCESS_ENERGY_RATIO)
						/ star.getRadiatedEnergy()));

		// The different area zones should only be shown when the star is selected
		// Render effect radius
		double alphaLevel = 0.17d;
		TextureImpl.bindNone();
		GL11.glBegin(GL11.GL_TRIANGLES);
		double t; // temporary data holder
		double x = effectRadius; // radius = 1
		double y = 0;
		for (int i = 0; i < nbSegments; i++) {
			GL11.glColor4d(1, 1, 0, alphaLevel);
			GL11.glVertex2f(0, 0);
			GL11.glColor4d(1, 1, 0, alphaLevel * 0.3);
			GL11.glVertex2d(x, y);

			t = x;
			x = cos * x - sin * y;
			y = sin * t + cos * y;

			GL11.glVertex2d(x, y);
		}
		GL11.glEnd();

		// render limit of effect zone
		GL11.glBegin(GL11.GL_LINES);
		t = 0; // temporary data holder
		x = effectRadius; // radius = 1
		y = 0;
		for (int i = 0; i < nbSegments; i++) {
			GL11.glColor4d(1, 1, 1, 0.15);
			GL11.glVertex2d(x, y);

			t = x;
			x = cos * x - sin * y;
			y = sin * t + cos * y;

			GL11.glVertex2d(x, y);
		}
		GL11.glEnd();

		// Render danger zone
		alphaLevel = 0.2f;
		GL11.glBegin(GL11.GL_TRIANGLES);
		t = 0; // temporary data holder
		x = dangerRadius + 100; // radius = 1
		y = 0;
		for (int i = 0; i < nbSegments; i++) {
			GL11.glColor4d(1, 0, 0, alphaLevel);
			GL11.glVertex2f(0, 0);
			GL11.glColor4d(1, 0, 0, 0);
			GL11.glVertex2d(x, y);

			t = x;
			x = cos * x - sin * y;
			y = sin * t + cos * y;

			GL11.glVertex2d(x, y);
		}
		GL11.glEnd();

		// render limit of danger zone
		GL11.glBegin(GL11.GL_LINES);
		t = 0; // temporary data holder
		x = dangerRadius; // radius = 1
		y = 0;
		for (int i = 0; i < nbSegments; i++) {
			GL11.glColor4d(1, 0, 0, 0.2);
			GL11.glVertex2d(x, y);

			t = x;
			x = cos * x - sin * y;
			y = sin * t + cos * y;

			GL11.glVertex2d(x, y);
		}
		GL11.glEnd();

		// Render star
		alphaLevel = 1.0f;
		GL11.glColor4d(1f, 1f, 1f, alphaLevel);
		baseTexture.bind();
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0, 0);
		GL11.glVertex2f(-baseTexture.getTextureWidth() / 2, -baseTexture.getTextureWidth() / 2);
		GL11.glTexCoord2f(1, 0);
		GL11.glVertex2f(baseTexture.getTextureWidth() / 2, -baseTexture.getTextureWidth() / 2);
		GL11.glTexCoord2f(1, 1);
		GL11.glVertex2f(baseTexture.getTextureWidth() / 2, baseTexture.getTextureHeight() / 2);
		GL11.glTexCoord2f(0, 1);
		GL11.glVertex2f(-baseTexture.getTextureWidth() / 2, baseTexture.getTextureHeight() / 2);
		GL11.glEnd();
		alphaLevel /= 0.3f;
		GL11.glColor4d(1f, 1f, 1f, alphaLevel);

		GL11.glTranslatef(-star.getPos().x, -star.getPos().y, -star.getPos().z);
	}
}
