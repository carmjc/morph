package net.carmgate.morph.ui.renderer;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.morph.Morph.MorphType;
import net.carmgate.morph.model.solid.world.World;

import org.apache.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

public class MorphRenderer implements Renderer<Morph> {

	private static final Logger LOGGER = Logger.getLogger(MorphRenderer.class);

	/** Morph scale factor. */
	private static final float MORPH_SCALE_FACTOR = 0.97f;

	/** The texture under the morph image. */
	private static Texture baseTexture;

	/** The map of the morph texture. */
	private static Map<MorphType, Texture> textures = new HashMap<Morph.MorphType, Texture>();
	private static Map<MorphType, Texture> debugTextures = new HashMap<Morph.MorphType, Texture>();

	// loading resources
	public static void init() {

		try {
			// load texture from PNG file
			baseTexture = TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("new-morphs/neutral.png").getPath()));

			// Normal textures
			textures.put(MorphType.BASIC,
					TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("new-morphs/neutral.png").getPath())));
			textures.put(MorphType.SHADOW,
					TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("new-morphs/neutral.png").getPath())));
			textures.put(MorphType.EMITTER,
					TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("new-morphs/firer.png").getPath())));
			textures.put(MorphType.PROPULSOR,
					TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("new-morphs/propulsor.png").getPath())));
			textures.put(MorphType.SHIELD,
					TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("new-morphs/shield.png").getPath())));
			textures.put(MorphType.STEM_MORPH,
					TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("new-morphs/stem.png").getPath())));

			// Debug textures
			debugTextures.put(MorphType.BASIC,
					TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("new-morphs/neutral.png").getPath())));
			debugTextures.put(MorphType.SHADOW,
					TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("new-morphs/neutral.png").getPath())));
			debugTextures.put(MorphType.EMITTER,
					TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("new-morphs/firer.png").getPath())));
			debugTextures.put(MorphType.PROPULSOR,
					TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("new-morphs/propulsor.png").getPath())));
			debugTextures.put(MorphType.SHIELD,
					TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("new-morphs/shield.png").getPath())));
			debugTextures.put(MorphType.STEM_MORPH,
					TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("new-morphs/stem.png").getPath())));
		} catch (IOException e) {
			LOGGER.error("Error while loading morph textures.", e);
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
		float alphaLevel = 1f;
		GL11.glScalef(MORPH_SCALE_FACTOR, MORPH_SCALE_FACTOR, MORPH_SCALE_FACTOR);

		// make current morph selectable
		if (glMode == GL11.GL_SELECT) {
			GL11.glPushName(morph.getId());
		}

		// Shadow morphs are just basic morphs with half transparency
		if (morph.getClass().getAnnotation(MorphInfo.class).type() == MorphType.SHADOW) {
			alphaLevel *= 0.5f;
		}

		// sphere texture
		alphaLevel *= 0.3f;
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

		// morph texture
		if (WorldRenderer.debugDisplay) {
			float energyPercent = morph.getEnergy() / morph.getClass().getAnnotation(MorphInfo.class).maxEnergy();
			if (energyPercent <= 0) {
				GL11.glColor4f(0.1f, 0.1f, 0.1f, alphaLevel);
			} else {
				GL11.glColor4f(1f - energyPercent, energyPercent, 0, alphaLevel);
			}
		} else if (World.getWorld().getSelectionModel().getSelectedMorphs().values().contains(morph)) {
			// selected morph color
			GL11.glColor4f(1, 0.85f, 0.85f, alphaLevel);
		} else if (World.getWorld().getSelectionModel().getSelectedShips().values().contains(morph.getShip())) {
			// selected ship color
			GL11.glColor4f(0.85f, 0.85f, 1, alphaLevel);
		} else if (morph.getShip().getActiveMorphList().contains(morph)) {
			// Active morph
			GL11.glColor4f(1, 1, 1, alphaLevel);
		} else {
			GL11.glColor4f(0.85f, 0.85f, 0.85f, alphaLevel);
		}

		// if (World.getWorld().getSelectedShip() == morph.getShip()) {
		// GL11.glColor3f(0.7f, 0.7f, 0.7f);
		// }

		GL11.glScalef(1 / MORPH_SCALE_FACTOR, 1 / MORPH_SCALE_FACTOR, 1 / MORPH_SCALE_FACTOR);
		float size = MORPH_SCALE_FACTOR * morph.getMass() / morph.getClass().getAnnotation(MorphInfo.class).maxMass();
		if (size == Float.POSITIVE_INFINITY || size == 0 || Float.isNaN(size)) {
			size = 0.01f;
		}
		LOGGER.trace(morph.getClass() + " current mass: " + morph.getMass() + " - size: " + size);
		GL11.glScalef(size, size, size);

		// morph texture
		Texture morphTexture = textures.get(morph.getClass().getAnnotation(MorphInfo.class).type());
		if (drawType == RenderStyle.DEBUG) {
			morphTexture = debugTextures.get(morph.getClass().getAnnotation(MorphInfo.class).type());
		}
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

		GL11.glColor4f(1, 1, 1, 1);
		GL11.glScalef(1 / size, 1 / size, 1 / size);

		// Selection names management
		if (glMode == GL11.GL_SELECT) {
			GL11.glPopName();
		}
	}
}
