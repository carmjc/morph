package net.carmgate.morph.ui.renderer;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureImpl;

public abstract class RendererUtil {

	/**
	 * Draws a textured disk.
	 * It is highly recommended to store cos and sin in constant attributes.
	 * The disk is generated from the segment [(0,0),(initialRadiusX, initialRadiusY)].
	 * @param texture the texture to use or null if there should be no texture
	 * @param nbSegments the number of segments of the 
	 * @param cos the cosinus of a segment angle.
	 * @param sin the sinus of a segment angle.
	 * @param initialRadiusX the x coordinate of the point from which will derive the disc
	 * @param initialRadiusY the y coordinate of the point from which will derive the disc
	 * @param percentageOfDisc
	 */
	public static void drawPartialCircle(Texture texture, int nbSegments, double cos, double sin,
			float initialRadiusX, float initialRadiusY, float percentageOfDisc, boolean clockwise,
			int glMode) {
		int textureWidth = 1;
		if (texture != null && glMode != GL11.GL_SELECT) {
			texture.bind();
			textureWidth = texture.getTextureWidth();
		} else {
			TextureImpl.bindNone();
		}

		double t; // temporary data holder
		double x = initialRadiusX; // radius = 1
		double y = initialRadiusY;
		double radius = Math.hypot(x, y);

		// Render the disc
		GL11.glBegin(GL11.GL_TRIANGLES);
		for (int i = 0; i < nbSegments * percentageOfDisc; i++) {
			GL11.glTexCoord2f(0.5f, 0.5f);
			GL11.glVertex2f(initialRadiusY, initialRadiusY);
			GL11.glTexCoord2d(0.5f + x / (2 * radius), 0.5f + y / (2 * radius));
			GL11.glVertex2d(textureWidth * x, textureWidth * y);

			t = x;
			if (clockwise) {
				x = cos * x - sin * y;
				y = sin * t + cos * y;
			} else {
				x = cos * x + sin * y;
				y = -sin * t + cos * y;
			}

			GL11.glTexCoord2d(0.5f + x / (2 * radius), 0.5f + y / (2 * radius));
			GL11.glVertex2d(textureWidth * x, textureWidth * y);
		}
		GL11.glEnd();
	}

	/**
	 * @param texture
	 */
	public static void drawTexturedHexagon(Texture texture, int glMode, int width) {
		int textureWidth;
		int textureHeight;
		if (texture != null && glMode != GL11.GL_SELECT) {
			texture.bind();
			textureWidth = texture.getTextureWidth();
			textureHeight = texture.getTextureHeight();
		} else {
			TextureImpl.bindNone();
			textureWidth = textureHeight = width;
		}

		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0, 0.5f);
		GL11.glVertex2f(-textureWidth / 2, 0);
		GL11.glTexCoord2f(0.25f, 0.5f - (float) (Math.sqrt(3) / 4));
		GL11.glVertex2f(-textureWidth / 4, (float) (-textureHeight * Math.sqrt(3) / 4));
		GL11.glTexCoord2f(0.75f, 0.5f - (float) (Math.sqrt(3) / 4));
		GL11.glVertex2f(textureWidth / 4, (float) (-textureHeight * Math.sqrt(3) / 4));
		GL11.glTexCoord2f(0.5f, 0.5f);
		GL11.glVertex2f(0, 0);

		GL11.glTexCoord2f(0, 0.5f);
		GL11.glVertex2f(-textureWidth / 2, 0);
		GL11.glTexCoord2f(0.25f, 0.5f + (float) (Math.sqrt(3) / 4));
		GL11.glVertex2f(-textureWidth / 4, (float) (textureHeight * Math.sqrt(3) / 4));
		GL11.glTexCoord2f(0.75f, 0.5f + (float) (Math.sqrt(3) / 4));
		GL11.glVertex2f(textureWidth / 4, (float) (textureHeight * Math.sqrt(3) / 4));
		GL11.glTexCoord2f(0.5f, 0.5f);
		GL11.glVertex2f(0, 0);

		GL11.glTexCoord2f(0.75f, 0.5f + (float) (Math.sqrt(3) / 4));
		GL11.glVertex2f(textureWidth / 4, (float) (textureHeight * Math.sqrt(3) / 4));
		GL11.glTexCoord2f(1, 0.5f);
		GL11.glVertex2f(textureWidth / 2, 0);
		GL11.glTexCoord2f(0.75f, 0.5f - (float) (Math.sqrt(3) / 4));
		GL11.glVertex2f(textureWidth / 4, (float) (-textureHeight * Math.sqrt(3) / 4));
		GL11.glTexCoord2f(0.5f, 0.5f);
		GL11.glVertex2f(0, 0);
		GL11.glEnd();
	}

	public static void drawTexturedRectangle(Texture texture, int glMode) {
		int textureWidth;
		int textureHeight;
		if (texture != null && glMode != GL11.GL_SELECT) {
			texture.bind();
			textureWidth = texture.getTextureWidth();
			textureHeight = texture.getTextureHeight();
		} else {
			TextureImpl.bindNone();
			textureWidth = textureHeight = 8;
		}

		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0, 0);
		GL11.glVertex2f(-textureWidth / 2, -textureHeight / 2);
		GL11.glTexCoord2f(1, 0);
		GL11.glVertex2f(textureWidth / 2, -textureHeight / 2);
		GL11.glTexCoord2f(1, 1);
		GL11.glVertex2f(textureWidth / 2, textureHeight / 2);
		GL11.glTexCoord2f(0, 1);
		GL11.glVertex2f(-textureWidth / 2, textureHeight / 2);
		GL11.glEnd();
	}

	private RendererUtil() {
	}

}
