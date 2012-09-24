package net.carmgate.morph.ui.renderer;

import java.awt.Font;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.carmgate.morph.Main;
import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.morph.Morph.MorphType;
import net.carmgate.morph.model.solid.morph.prop.PropulsorMorph;
import net.carmgate.morph.model.solid.morph.stem.StemMorph;
import net.carmgate.morph.model.solid.world.World;
import net.carmgate.morph.ui.MorphMouse;
import net.carmgate.morph.ui.UIModel;
import net.carmgate.morph.ui.UIModel.UIState;
import net.carmgate.morph.ui.renderer.Renderer.RenderStyle;

import org.apache.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

public class UIRenderer {

	private static final Logger LOGGER = Logger.getLogger(UIRenderer.class);
	private static Texture[] baseTextures = new Texture[6];
	static {
		try {
			for (int i = 0; i < 6; i++) {
				baseTextures[i] = TextureLoader.getTexture("PNG",
						new FileInputStream(ClassLoader.getSystemResource("ui/circular-menu-" + i + ".png").getPath()));
			}
		} catch (FileNotFoundException e) {
			LOGGER.error("Problem loading textures.", e);
		} catch (IOException e) {
			LOGGER.error("Problem loading textures.", e);
		}

	}

	private TrueTypeFont font;

	public void init() {
		// load a default java font
		Font awtFont = new Font("Arial", Font.PLAIN, 28);
		font = new TrueTypeFont(awtFont, true);
	}

	public void render(int glMode, RenderStyle drawType) {
		LOGGER.trace("Rendering interface");
		GL11.glDisable(GL11.GL_TEXTURE_2D);

		renderConsole(drawType);
		if (UIModel.getUiModel().getUiState() == UIState.EVOLVING) {
			renderEvolvingContextualMenu(glMode, drawType);
		}
	}

	/**
	 * @param drawType
	 */
	private void renderConsole(RenderStyle drawType) {
		// Drawing console box
		GL11.glColor4f(0.7f, 0.7f, 0.9f, 0.9f);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2f(-Main.WIDTH, Main.HEIGHT);
		GL11.glVertex2f(Main.WIDTH, Main.HEIGHT);
		GL11.glVertex2f(Main.WIDTH, Main.HEIGHT - 150);
		GL11.glVertex2f(-Main.WIDTH, Main.HEIGHT - 150);
		GL11.glEnd();

		// Writing in the console box
		String modes = "Debug mode : " + WorldRenderer.debugDisplay + " - Combat mode : " + World.combat;
		font.drawString(-Main.WIDTH + 20, Main.HEIGHT - 130, modes, Color.black);
		font.drawString(-Main.WIDTH + 20, Main.HEIGHT - 100, "Mouse position : " + MorphMouse.getX() + "x" + MorphMouse.getY(), Color.black);

		// if debug mode : drawing positions in the screen
		if (drawType == RenderStyle.DEBUG) {
			font.drawString(-Main.WIDTH + 10, -Main.HEIGHT + 10, -Main.WIDTH + "x" + -Main.HEIGHT, Color.white);
			font.drawString(Main.WIDTH - 140, -Main.HEIGHT + 10, Main.WIDTH + "x" + -Main.HEIGHT, Color.white);
			font.drawString(-Main.WIDTH + 10, Main.HEIGHT - 190, -Main.WIDTH + "x" + Main.HEIGHT, Color.white);
			font.drawString(Main.WIDTH - 140, Main.HEIGHT - 190, Main.WIDTH + "x" + Main.HEIGHT, Color.white);
		}
	}

	/**
	 * Renders the contextual menu associated with a morph.
	 * @param glMode
	 * @param drawType
	 */
	private void renderEvolvingContextualMenu(int glMode, RenderStyle drawType) {
		Map<Integer, Morph> selectedMorphs = World.getWorld().getSelectionModel().getSelectedMorphs();
		if (selectedMorphs.size() > 0) {
			Morph m = selectedMorphs.values().iterator().next();
			// TODO investigate why it is necessary to add 5 to the morph coordinates
			// to get the proper position of the contextual menu.
			GL11.glTranslatef(m.getPosInWorld().x + 5, m.getPosInWorld().y + 5, m.getPosInWorld().z);

			List<Class<? extends Morph>> possibleMorphEvol = new ArrayList<Class<? extends Morph>>();
			if (m.getClass().getAnnotation(MorphInfo.class).type() == MorphType.BASIC) {
				possibleMorphEvol.add(PropulsorMorph.class);
				possibleMorphEvol.add(StemMorph.class);
			}

			int i = 0;
			int textureRatio = 3;
			for (Class<? extends Morph> morphClass : possibleMorphEvol) {
				Texture baseTexture = baseTextures[i++];

				GL11.glColor4f(1f, 1f, 1f, 0.8f);
				baseTexture.bind();
				GL11.glBegin(GL11.GL_QUADS);
				GL11.glTexCoord2f(0, 0);
				GL11.glVertex2f(-baseTexture.getTextureWidth() / textureRatio, -baseTexture.getTextureWidth() / textureRatio);
				GL11.glTexCoord2f(1, 0);
				GL11.glVertex2f(baseTexture.getTextureWidth() / textureRatio, -baseTexture.getTextureWidth() / textureRatio);
				GL11.glTexCoord2f(1, 1);
				GL11.glVertex2f(baseTexture.getTextureWidth() / textureRatio, baseTexture.getTextureHeight() / textureRatio);
				GL11.glTexCoord2f(0, 1);
				GL11.glVertex2f(-baseTexture.getTextureWidth() / textureRatio, baseTexture.getTextureHeight() / textureRatio);
				GL11.glEnd();
				GL11.glColor4f(1f, 1f, 1f, 1f);
			}

			for (; i < 6; i++) {
				Texture baseTexture = baseTextures[i];

				GL11.glColor4f(1f, 1f, 1f, 0.1f);
				baseTexture.bind();
				GL11.glBegin(GL11.GL_QUADS);
				GL11.glTexCoord2f(0, 0);
				GL11.glVertex2f(-baseTexture.getTextureWidth() / textureRatio, -baseTexture.getTextureWidth() / textureRatio);
				GL11.glTexCoord2f(1, 0);
				GL11.glVertex2f(baseTexture.getTextureWidth() / textureRatio, -baseTexture.getTextureWidth() / textureRatio);
				GL11.glTexCoord2f(1, 1);
				GL11.glVertex2f(baseTexture.getTextureWidth() / textureRatio, baseTexture.getTextureHeight() / textureRatio);
				GL11.glTexCoord2f(0, 1);
				GL11.glVertex2f(-baseTexture.getTextureWidth() / textureRatio, baseTexture.getTextureHeight() / textureRatio);
				GL11.glEnd();
				GL11.glColor4f(1f, 1f, 1f, 1f);
			}

			GL11.glTranslatef(-m.getPosInWorld().x - 5, -m.getPosInWorld().y - 5, -m.getPosInWorld().z);
		}
	}
}
