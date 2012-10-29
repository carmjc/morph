package net.carmgate.morph.ui.renderer.mattersource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.carmgate.morph.Main;
import net.carmgate.morph.model.solid.mattersource.MatterSource;
import net.carmgate.morph.model.solid.world.World;
import net.carmgate.morph.ui.renderer.Renderer;
import net.carmgate.morph.ui.renderer.RendererUtil;

import org.apache.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

public class AsteroidRenderer implements Renderer<MatterSource> {

	private static final Logger LOGGER = Logger.getLogger(AsteroidRenderer.class);

	private static Texture baseTexture;
	static {
		try {
			baseTexture = TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("asteroids/asteroid1-64.png").getPath()));
		} catch (FileNotFoundException e) {
			LOGGER.error("Error while loading asteroid textures.", e);
		} catch (IOException e) {
			LOGGER.error("Error while loading asteroid textures.", e);
		}
	}

	@Override
	public void render(int glMode, net.carmgate.morph.ui.renderer.Renderer.RenderStyle drawType, MatterSource asteroid) {
		GL11.glTranslatef(asteroid.getPos().x, asteroid.getPos().y, asteroid.getPos().z);
		GL11.glRotatef(asteroid.getRotationSpeed() * World.getWorld().getCurrentTS() / 1000 % 360, 0, 0, 1);

		float scale = 0.2f + 0.8f * asteroid.getMass() / asteroid.getInitialMass();
		GL11.glScalef(scale, scale, scale);

		if (glMode != GL11.GL_SELECT) {
			RendererUtil.drawTexturedRectangle(baseTexture, glMode);
		} else {
			GL11.glPushName(Main.PickingContext.ASTEROID.ordinal());
			GL11.glPushName((int) asteroid.getId()); // FIXME replace this by using two names instead of one.
			RendererUtil.drawTexturedHexagon(baseTexture, glMode, 64);
			GL11.glPopName();
			GL11.glPopName();
		}

		GL11.glScalef(1f / scale, 1f / scale, 1f / scale);

		GL11.glRotatef(-asteroid.getRotationSpeed() * World.getWorld().getCurrentTS() / 1000 % 360, 0, 0, 1);
		GL11.glTranslatef(-asteroid.getPos().x, -asteroid.getPos().y, -asteroid.getPos().z);
	}
}
