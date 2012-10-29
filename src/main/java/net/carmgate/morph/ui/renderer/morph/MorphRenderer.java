package net.carmgate.morph.ui.renderer.morph;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.carmgate.morph.model.State;
import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.Behavior;
import net.carmgate.morph.model.behavior.Progress;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.morph.impl.GunMorph;
import net.carmgate.morph.model.solid.morph.impl.MinerMorph;
import net.carmgate.morph.model.solid.morph.impl.PropulsorMorph;
import net.carmgate.morph.model.solid.morph.impl.stem.StemMorph;
import net.carmgate.morph.ui.model.UIModel;
import net.carmgate.morph.ui.renderer.Renderer;
import net.carmgate.morph.ui.renderer.RendererUtil;
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
	private static Texture ownerBgTexture;

	/** The texture under the morph image when morph is selected. */
	private static Texture baseSelectedTexture;

	/** The map of the morph texture. */
	private final static Map<Class<? extends Morph>, Texture> textures = new HashMap<Class<? extends Morph>, Texture>();

	// loading resources
	static {
		try {
			// load texture from PNG file
			baseTexture = TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("morphs/morph-base-32.png").getPath()));
			baseSelectedTexture = TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("morphs/morph-base-32-selected.png")
					.getPath()));

			// owner bg texture
			ownerBgTexture = TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("morphs/morph-base-32-owner-bg.png").getPath()));

			// Normal textures
			textures.put(PropulsorMorph.class,
					TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("morphs/morph-overlay-prop-32.png").getPath())));
			textures.put(StemMorph.class,
					TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("morphs/morph-overlay-stem-32.png").getPath())));
			textures.put(GunMorph.class,
					TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("morphs/morph-overlay-gun-32.png").getPath())));
			textures.put(MinerMorph.class,
					TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("morphs/morph-overlay-miner-32.png").getPath())));

			// Energy and mass gauge texture
			energyMassTexture = TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("morphs/morph-base-32-gauge.png").getPath()));
		} catch (IOException e) {
			LOGGER.error("Error while loading morph textures.", e);
		}
	}

	public static Texture getBaseTexture() {
		return baseTexture;
	}

	public static Map<Class<? extends Morph>, Texture> getTextures() {
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

		for (Behavior<?> b : morph.getActivationIsolatedBehaviorList()) {
			if (b instanceof Progress) {
				morphScaleFactor = 0.65f;
				alphaLevel *= 0.5f;
				break;
			}
		}

		GL11.glScalef(morphScaleFactor, morphScaleFactor, morphScaleFactor);

		// make current morph selectable
		if (glMode == GL11.GL_SELECT) {
			GL11.glPushName((int) morph.getId());
		}

		// Shadow morphs are just basic morphs with more transparency
		if (morph.getClass().getAnnotation(MorphInfo.class).virtual()) {
			alphaLevel *= 0.3f;
		}

		// Show the owner colored background
		if (glMode != GL11.GL_SELECT && !morph.getClass().getAnnotation(MorphInfo.class).virtual()) {
			Color color = morph.getShip().getOwner().getColor();
			GL11.glColor4f((float) color.getRed() / 256, (float) color.getGreen() / 256, (float) color.getBlue() / 256, alphaLevel);
			RendererUtil.drawTexturedRectangle(ownerBgTexture, glMode);
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
		} else if (morph.getState() == State.ACTIVE) {
			// Active morph
			GL11.glColor4f(1, 1, 1, alphaLevel);
		} else {
			GL11.glColor4f(0.85f, 0.85f, 0.85f, alphaLevel);
		}

		GL11.glColor4f(1, 1, 1, alphaLevel);
		// sphere texture
		if (glMode != GL11.GL_SELECT) {
			if (UIModel.getUiModel().getSelectionModel().getSelectedMorphs().containsValue(morph)) {
				RendererUtil.drawTexturedRectangle(baseSelectedTexture, glMode);
			} else {
				RendererUtil.drawTexturedRectangle(baseTexture, glMode);
			}
		} else {
			RendererUtil.drawTexturedHexagon(baseTexture, glMode, 32);
		}

		GL11.glColor4f(1, 1, 1, 1);

		// morph texture
		if (glMode != GL11.GL_SELECT) {
			Texture morphTexture = textures.get(morph.getClass());
			if (morphTexture != null) {
				morphTexture.bind();
				RendererUtil.drawTexturedRectangle(morphTexture, glMode);
			}

			if (!morph.getClass().getAnnotation(MorphInfo.class).virtual()) {
				// Render energy and mass gauge
				RendererUtil.drawPartialCircle(energyMassTexture, nbSegments, cos, sin, -0.5f, 0,
						Math.min(1, morph.getEnergy() / morph.getMaxEnergy()) / 2, true, glMode);
				RendererUtil.drawPartialCircle(energyMassTexture, nbSegments, cos, sin, -0.5f, 0,
						morph.getMass() / morph.getMaxMass() / 2, false, glMode);
			}
		}

		GL11.glScalef(1 / morphScaleFactor, 1 / morphScaleFactor, 1 / morphScaleFactor);

		// make current morph selectable
		if (glMode == GL11.GL_SELECT) {
			GL11.glPopName();
		}

	}
}
