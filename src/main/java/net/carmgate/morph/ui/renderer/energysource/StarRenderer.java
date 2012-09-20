package net.carmgate.morph.ui.renderer.energysource;

import java.io.FileInputStream;
import java.io.IOException;

import net.carmgate.morph.model.solid.energysource.Star;
import net.carmgate.morph.ui.renderer.Renderer;

import org.apache.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

public class StarRenderer implements Renderer<Star> {

	private static final Logger LOGGER = Logger.getLogger(StarRenderer.class);

	private static Texture baseTexture;
	static {
		try {
			baseTexture = TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("stars/blue.png").getPath()));
		} catch (IOException e) {
			LOGGER.error("Error while loading morph textures.", e);
		}
	}

	public void render(int glMode, net.carmgate.morph.ui.renderer.Renderer.RenderStyle drawType, Star star) {
		GL11.glTranslatef(star.getPos().x, star.getPos().y, star.getPos().z);

		float alphaLevel = 1.0f;
		GL11.glColor4f(1f, 1f, 1f, alphaLevel);
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
		GL11.glColor4f(1f, 1f, 1f, alphaLevel);

		GL11.glTranslatef(-star.getPos().x, -star.getPos().y, -star.getPos().z);
	}

}
