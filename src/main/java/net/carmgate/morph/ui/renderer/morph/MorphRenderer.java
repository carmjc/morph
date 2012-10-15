package net.carmgate.morph.ui.renderer.morph;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.Behavior;
import net.carmgate.morph.model.behavior.ProgressBehavior;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.morph.Morph.MorphType;
import net.carmgate.morph.ui.model.UIModel;
import net.carmgate.morph.ui.renderer.Renderer;
import net.carmgate.morph.ui.renderer.WorldRenderer;

import org.apache.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

public class MorphRenderer implements Renderer<Morph> {

	private static final Logger LOGGER = Logger.getLogger(MorphRenderer.class);

	private static final int nbSegments = 40;
	private static final double deltaAngle = 2 * Math.PI / nbSegments;
	private static final double cos = Math.cos(deltaAngle);
	private static final double sin = Math.sin(deltaAngle);

	private static Texture energyMassTexture;

	/** The texture under the morph image. */
	private static Texture baseTexture;

	/** The texture under the morph image when morph is selected. */
	private static Texture baseSelectedTexture;

	/** The map of the morph texture. */
	private final static Map<MorphType, Texture> textures = new HashMap<Morph.MorphType, Texture>();

	// loading resources
	static {
		try {
			// load texture from PNG file
			baseTexture = TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("morphs/morph-base-32.png").getPath()));
			baseSelectedTexture = TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("morphs/morph-base-32-selected.png")
					.getPath()));

			// Normal textures
			textures.put(MorphType.PROPULSOR,
					TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("morphs/morph-overlay-prop-32.png").getPath())));
			textures.put(MorphType.STEM,
					TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("morphs/morph-overlay-stem-32.png").getPath())));
			textures.put(MorphType.GUN,
					TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("morphs/morph-overlay-gun-32.png").getPath())));

			// Energy and mass gauge texture
			energyMassTexture = TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("morphs/morph-base-32-gauge.png").getPath()));
		} catch (IOException e) {
			LOGGER.error("Error while loading morph textures.", e);
		}
	}

	public static Texture getBaseTexture() {
		return baseTexture;
	}

	public static Map<MorphType, Texture> getTextures() {
		return textures;
	}

	/**
	 * Renders a morph.
	 * The referential is center on the morph and rotated as the morph is rotated in the ship's referential or the world's referential
	 * if the morph is not attached to a ship.
	 */
	@Override
	public void render(int glMode, RenderStyle drawType, Morph morph) {
		float alphaLevel;
		if (morph.getMaxMass() != 0) {
			alphaLevel = morph.getMass() / morph.getMaxMass();
		} else {
			alphaLevel = 1;
		}
		float morphScaleFactor = 1;

		for (Behavior<?> b : morph.getAlternateBehaviorList()) {
			if (b instanceof ProgressBehavior) {
				morphScaleFactor = 0.65f;
				alphaLevel *= 0.5f;
				break;
			}
		}

		GL11.glScalef(morphScaleFactor, morphScaleFactor, morphScaleFactor);

		// make current morph selectable
		if (glMode == GL11.GL_SELECT) {
			GL11.glPushName(morph.getId());
		}

		// Shadow morphs are just basic morphs with more transparency
		if (morph.getClass().getAnnotation(MorphInfo.class).type() == MorphType.SHADOW) {
			alphaLevel *= 0.3f;
		}

		// morph texture
		if (WorldRenderer.debugDisplay) {
			float energyPercent = morph.getEnergy() / morph.getClass().getAnnotation(MorphInfo.class).maxEnergy();
			if (morph.getExcessEnergy() > 0) {
				GL11.glColor4f(1f, 1f, 1f, alphaLevel);
			} else if (energyPercent <= 0) {
				GL11.glColor4f(0.1f, 0.1f, 0.1f, alphaLevel);
			} else {
				GL11.glColor4f(1f - energyPercent, energyPercent, 0, alphaLevel);
			}
		} else if (UIModel.getUiModel().getSelectionModel().getSelectedMorphs().values().contains(morph)) {
			// selected morph color
			GL11.glColor4f(1, 0.85f, 0.85f, alphaLevel);
		} else if (UIModel.getUiModel().getSelectionModel().getSelectedShips().values().contains(morph.getShip())) {
			// selected ship color
			GL11.glColor4f(0.85f, 0.85f, 1, alphaLevel);
		} else if (morph.getShip().getActiveMorphList().contains(morph)) {
			// Active morph
			GL11.glColor4f(1, 1, 1, alphaLevel);
		} else {
			GL11.glColor4f(0.85f, 0.85f, 0.85f, alphaLevel);
		}

		GL11.glColor4f(1, 1, 1, alphaLevel);
		// sphere texture
		if (glMode != GL11.GL_SELECT && UIModel.getUiModel().getSelectionModel().getSelectedMorphs().containsValue(morph)) {
			baseSelectedTexture.bind();
		} else {
			baseTexture.bind();
		}
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

		GL11.glColor4f(1, 1, 1, 1);

		// morph texture
		Texture morphTexture = textures.get(morph.getClass().getAnnotation(MorphInfo.class).type());
		if (morphTexture != null) {
			morphTexture.bind();
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glTexCoord2f(0, 0);
			GL11.glVertex2f(-morphTexture.getTextureWidth() / 2, -morphTexture.getTextureHeight() / 2);
			GL11.glTexCoord2f(1, 0);
			GL11.glVertex2f(morphTexture.getTextureWidth() / 2, -morphTexture.getTextureHeight() / 2);
			GL11.glTexCoord2f(1, 1);
			GL11.glVertex2f(morphTexture.getTextureWidth() / 2, morphTexture.getTextureHeight() / 2);
			GL11.glTexCoord2f(0, 1);
			GL11.glVertex2f(-morphTexture.getTextureWidth() / 2, morphTexture.getTextureHeight() / 2);
			GL11.glEnd();
		}

		if (morph.getClass().getAnnotation(MorphInfo.class).type() != MorphType.SHADOW) {
			renderEnergy(glMode, drawType, morph);
			renderMass(glMode, drawType, morph);
		}

		GL11.glScalef(1 / morphScaleFactor, 1 / morphScaleFactor, 1 / morphScaleFactor);

		// make current morph selectable
		if (glMode == GL11.GL_SELECT) {
			GL11.glPopName();
		}

	}

	private void renderEnergy(int glMode, net.carmgate.morph.ui.renderer.Renderer.RenderStyle drawType, Morph morph) {
		energyMassTexture.bind();
		GL11.glBegin(GL11.GL_TRIANGLES);
		double t; // temporary data holder
		double x = -0.5; // radius = 1
		double y = 0;
		for (int i = 0; i < nbSegments * Math.min(1, morph.getEnergy() / morph.getMaxEnergy()) / 2; i++) {
			GL11.glTexCoord2f(0.5f, 0.5f);
			GL11.glVertex2f(0, 0);
			GL11.glTexCoord2d(0.5f + x, 0.5f + y);
			GL11.glVertex2d(energyMassTexture.getTextureWidth() * x, energyMassTexture.getTextureWidth() * y);

			t = x;
			x = cos * x - sin * y;
			y = sin * t + cos * y;

			GL11.glTexCoord2d(0.5f + x, 0.5f + y);
			GL11.glVertex2d(energyMassTexture.getTextureWidth() * x, energyMassTexture.getTextureWidth() * y);
		}
		GL11.glEnd();
	}

	private void renderMass(int glMode, net.carmgate.morph.ui.renderer.Renderer.RenderStyle drawType, Morph morph) {
		energyMassTexture.bind();
		GL11.glBegin(GL11.GL_TRIANGLES);
		double t; // temporary data holder
		double x = -0.5; // radius = 1
		double y = 0;
		for (int i = 0; i < nbSegments * morph.getMass() / morph.getMaxMass() / 2; i++) {
			GL11.glTexCoord2f(0.5f, 0.5f);
			GL11.glVertex2f(0, 0);
			GL11.glTexCoord2d(0.5f + x, 0.5f + y);
			GL11.glVertex2d(energyMassTexture.getTextureWidth() * x, energyMassTexture.getTextureWidth() * y);

			t = x;
			x = cos * x + sin * y;
			y = -sin * t + cos * y;

			GL11.glTexCoord2d(0.5f + x, 0.5f + y);
			GL11.glVertex2d(energyMassTexture.getTextureWidth() * x, energyMassTexture.getTextureWidth() * y);
		}
		GL11.glEnd();
	}
}
