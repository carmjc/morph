package net.carmgate.morph.ui.renderer;

import java.awt.Font;

import net.carmgate.morph.Main;
import net.carmgate.morph.model.solid.world.World;
import net.carmgate.morph.ui.MorphMouse;
import net.carmgate.morph.ui.renderer.Renderer.RenderStyle;

import org.apache.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.opengl.TextureImpl;

public class UIRenderer {

	private static final Logger LOGGER = Logger.getLogger(UIRenderer.class);

	public static final TrueTypeFont font;
	static {
		Font awtFont = new Font("Helvetica", Font.PLAIN, 26);
		font = new TrueTypeFont(awtFont, true);
	}

	public void init() {
		// load a default java font
	}

	public void render(int glMode, RenderStyle drawType) {
		LOGGER.trace("Rendering interface");
		TextureImpl.bindNone();

		// Drawing console box
		GL11.glColor4f(0.7f, 0.7f, 0.9f, 1f);
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
}
