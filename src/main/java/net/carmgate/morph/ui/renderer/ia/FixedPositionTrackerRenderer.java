package net.carmgate.morph.ui.renderer.ia;

import java.io.FileInputStream;
import java.io.IOException;

import net.carmgate.morph.ia.tracker.FixedPositionTracker;
import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.ui.renderer.ForceRenderer;
import net.carmgate.morph.ui.renderer.Renderer;

import org.apache.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

public class FixedPositionTrackerRenderer implements Renderer<FixedPositionTracker> {

	static private final Logger log = Logger.getLogger(FixedPositionTrackerRenderer.class);

	/** The texture under the morph image. */
	private static Texture texture;
	private ForceRenderer forceRenderer;

	public void init() {
		try {
			texture = TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("target.png").getPath()));
		} catch (IOException e) {
			log.error("Could not load textures", e);
		}

		forceRenderer = new ForceRenderer();
	}

	public void render(int glMode,
			net.carmgate.morph.ui.renderer.Renderer.RenderStyle drawType,
			FixedPositionTracker tracker) {
		Vect3D targetPos = tracker.getTargetPos();

		GL11.glTranslatef(targetPos.x, targetPos.y, targetPos.z);
		texture.bind();
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0, 0);
		GL11.glVertex2f(- texture.getTextureWidth() / 2, - texture.getTextureWidth() / 2);
		GL11.glTexCoord2f(1, 0);
		GL11.glVertex2f(texture.getTextureWidth() / 2, - texture.getTextureWidth() / 2);
		GL11.glTexCoord2f(1, 1);
		GL11.glVertex2f(texture.getTextureWidth() / 2, texture.getTextureHeight() / 2);
		GL11.glTexCoord2f(0, 1);
		GL11.glVertex2f(- texture.getTextureWidth() / 2, texture.getTextureHeight() / 2);
		GL11.glEnd();
		GL11.glTranslatef(-targetPos.x, -targetPos.y, -targetPos.z);

	}

}
