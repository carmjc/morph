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
		Font awtFont = new Font("Tahoma", Font.PLAIN, 14);
		font = new TrueTypeFont(awtFont, true);
	}

	public void init() {
		// load a default java font
	}

	public void render(int glMode, RenderStyle drawType) {
		LOGGER.trace("Rendering interface");
		TextureImpl.bindNone();

		GL11.glScalef(WorldRenderer.scale, WorldRenderer.scale, WorldRenderer.scale);
		// Drawing console box
		GL11.glColor4f(0.7f, 0.7f, 0.9f, 1f);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2f(-Main.WIDTH / 2, Main.HEIGHT / 2);
		GL11.glVertex2f(Main.WIDTH / 2, Main.HEIGHT / 2);
		GL11.glVertex2f(Main.WIDTH / 2, Main.HEIGHT / 2 - 75);
		GL11.glVertex2f(-Main.WIDTH / 2, Main.HEIGHT / 2 - 75);
		GL11.glEnd();

		// Writing in the console box
		String modes = "Debug mode : " + WorldRenderer.debugDisplay + " - Combat mode : " + World.combat;
		font.drawString(-Main.WIDTH / 2 + 10, Main.HEIGHT / 2 - 65, modes, Color.black);
		font.drawString(-Main.WIDTH / 2 + 10, Main.HEIGHT / 2 - 50, "Mouse position : " + MorphMouse.getX() + "x" + MorphMouse.getY(), Color.black);

		// if debug mode : drawing positions in the screen
		if (drawType == RenderStyle.DEBUG) {
			font.drawString(-Main.WIDTH / 2 + 5, -Main.HEIGHT / 2 + 5, -Main.WIDTH / 2 + "x" + -Main.HEIGHT / 2, Color.white);
			font.drawString(Main.WIDTH / 2 - 65, -Main.HEIGHT / 2 + 5, Main.WIDTH / 2 + "x" + -Main.HEIGHT / 2, Color.white);
			font.drawString(-Main.WIDTH / 2 + 5, Main.HEIGHT / 2 - 99, -Main.WIDTH / 2 + "x" + Main.HEIGHT / 2, Color.white);
			font.drawString(Main.WIDTH / 2 - 60, Main.HEIGHT / 2 - 99, Main.WIDTH / 2 + "x" + Main.HEIGHT / 2, Color.white);
		}
		GL11.glScalef(1 / WorldRenderer.scale, 1 / WorldRenderer.scale, 1 / WorldRenderer.scale);
	}
}
