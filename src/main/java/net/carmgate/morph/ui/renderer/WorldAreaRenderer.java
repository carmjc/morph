package net.carmgate.morph.ui.renderer;

import net.carmgate.morph.model.WorldArea;

import org.lwjgl.opengl.GL11;

public class WorldAreaRenderer implements Renderer<WorldArea> {

	public void render(int glMode, RenderStyle drawType, WorldArea worldArea) {
		float x = worldArea.pos.x * WorldArea.WORLDAREA_SIZE;
		float y = worldArea.pos.y * WorldArea.WORLDAREA_SIZE;
		float z = worldArea.pos.z * WorldArea.WORLDAREA_SIZE;

		GL11.glTranslatef(x, y, z);

		// World area color
		float massColor = 0.2f + worldArea.mass / 10;
		GL11.glColor4f(massColor, massColor, massColor, 0.5f);

		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2f(0, 0);
		GL11.glVertex2f(WorldArea.WORLDAREA_SIZE, 0);
		GL11.glVertex2f(WorldArea.WORLDAREA_SIZE, WorldArea.WORLDAREA_SIZE);
		GL11.glVertex2f(0, WorldArea.WORLDAREA_SIZE);
		GL11.glEnd();

		GL11.glTranslatef(-x, -y, -z);
	}

}
