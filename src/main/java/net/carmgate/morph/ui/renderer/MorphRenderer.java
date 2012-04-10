package net.carmgate.morph.ui.renderer;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.carmgate.morph.model.World;
import net.carmgate.morph.model.morph.Morph;
import net.carmgate.morph.model.morph.Morph.MorphType;

import org.apache.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

public class MorphRenderer implements Renderer<Morph> {

	private static final Logger logger = Logger.getLogger(MorphRenderer.class);

	/** Morph scale factor. */
	private static final float MORPH_SCALE_FACTOR = 1f;

	/** The texture under the morph image. */
	public static Texture baseTexture;

	/** The map of the morph texture. */
	private static Map<MorphType, Texture> textures;

	// loading resources
	public static void init() {
		textures = new HashMap<Morph.MorphType, Texture>();

		try {
			// load texture from PNG file
			baseTexture = TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("morph64.png").getPath()));
			textures.put(MorphType.BASIC, null);
			textures.put(MorphType.EMITTER, TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("morphs/emitter-0.png").getPath())));
			textures.put(MorphType.PROPULSOR, TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("morphs/propulsor-0.png").getPath())));
			textures.put(MorphType.SHIELD, TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("morphs/shield-0.png").getPath())));
			textures.put(MorphType.SPREADER, TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("morphs/spreader-0.png").getPath())));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public MorphRenderer() {
	}

	/**
	 * Renders a morph.
	 * The referential is center on the morph and rotated as the morph is rotated in the ship's referential or the world's referential
	 * if the morph is not attached to a ship.
	 */
	public void render(int glMode, RenderStyle drawType, Morph morph) {

		float size = MORPH_SCALE_FACTOR * morph.mass / morph.maxMass;
		GL11.glScalef(size, size, size);

		// sphere texture
		if (WorldRenderer.debugDisplay) {
			float energyPercent = morph.energy / morph.getMaxEnergy();
			if (energyPercent <= 0) {
				GL11.glColor3f(0.1f, 0.1f, 0.1f);
			} else {
				GL11.glColor3f(1f - energyPercent, energyPercent, 0);
			}
		} else if (morph.getShip().getSelectedMorphList().contains(morph)) {
			logger.debug("posInShip: " + morph.getShip().pos);
			logger.debug("posInShip: " + morph.getPosInShip());
			logger.debug("shipRot: " + morph.getShip().rot);
			GL11.glColor3f(1, 0.7f, 0.7f);
		} else if (World.getWorld().getSelectedShip() == morph.getShip()) {
			GL11.glColor3f(0.7f, 0.7f, 1);
		} else if (morph.getShip().getActiveMorphList().contains(morph)) {
			GL11.glColor3f(1, 1, 1);
		} else {
			GL11.glColor3f(0.7f, 0.7f, 0.7f);
		}

		baseTexture.bind();
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0, 0);
		GL11.glVertex2f(- baseTexture.getTextureWidth() / 2, - baseTexture.getTextureWidth() / 2);
		GL11.glTexCoord2f(1, 0);
		GL11.glVertex2f(baseTexture.getTextureWidth() / 2, - baseTexture.getTextureWidth() / 2);
		GL11.glTexCoord2f(1, 1);
		GL11.glVertex2f(baseTexture.getTextureWidth() / 2, baseTexture.getTextureHeight() / 2);
		GL11.glTexCoord2f(0, 1);
		GL11.glVertex2f(- baseTexture.getTextureWidth() / 2, baseTexture.getTextureHeight() / 2);
		GL11.glEnd();

		if (World.getWorld().getSelectedShip() == morph.getShip()) {
			GL11.glColor3f(0.7f, 0.7f, 0.7f);
		}

		// morph texture
		Texture morphTexture = textures.get(morph.getType());
		if (morphTexture != null) {
			morphTexture.bind();
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glTexCoord2f(0, 0);
			GL11.glVertex2f(-morphTexture.getTextureWidth() / 2, -morphTexture.getTextureWidth() / 2);
			GL11.glTexCoord2f(1, 0);
			GL11.glVertex2f(morphTexture.getTextureWidth() / 2, -morphTexture.getTextureWidth() / 2);
			GL11.glTexCoord2f(1, 1);
			GL11.glVertex2f(morphTexture.getTextureWidth() / 2, morphTexture.getTextureHeight() / 2);
			GL11.glTexCoord2f(0, 1);
			GL11.glVertex2f(-morphTexture.getTextureWidth() / 2, morphTexture.getTextureHeight() / 2);
			GL11.glEnd();
		}

		GL11.glScalef(1 / size, 1 / size, 1 / size);

	}

}
