package net.carmgate.morph.ui.renderer;

import net.carmgate.morph.model.morph.Morph;
import net.carmgate.morph.model.ship.Ship;

import org.lwjgl.opengl.GL11;

public class ShipRenderer implements Renderer<Ship> {

	private MorphRenderer currentMorphRenderer;

	public ShipRenderer() {
		currentMorphRenderer = new MorphRenderer();
	}

	public void render(int glMode, RenderStyle renderStyle, Ship ship) {
		GL11.glTranslatef(ship.pos.x, ship.pos.y, ship.pos.z);
		GL11.glRotatef(ship.rot.z, 0, 0, 1);

		// Do whatever is necessary to draw the ship except sub items

		GL11.glRotatef(-ship.rot.z, 0, 0, 1);
		GL11.glTranslatef(-ship.pos.x, -ship.pos.y, -ship.pos.z);

		// Draw the morphs
		int selectionId = 0;
		for (Morph morph : ship.getMorphList()) {
			morph.update();

			// Selection names management
			if (glMode == GL11.GL_SELECT) {
				GL11.glPushName(selectionId++);
			}

			currentMorphRenderer.render(glMode, RenderStyle.NORMAL, morph);

			// Selection names management
			if (glMode == GL11.GL_SELECT) {
				GL11.glPopName();
			}
		}


	}

	public void setMorphRenderer(MorphRenderer morphRenderer) {
		this.currentMorphRenderer = morphRenderer;
	}

}
