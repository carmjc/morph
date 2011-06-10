package net.carmgate.morph.ui.renderer;

import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.World;
import net.carmgate.morph.model.WorldArea;
import net.carmgate.morph.model.physics.Force;
import net.carmgate.morph.model.ship.Ship;
import net.carmgate.morph.ui.MorphMouse;

import org.lwjgl.opengl.GL11;

public class WorldRenderer implements Renderer<World> {

	private int selectionId = 0;
	public static boolean debugDisplay = false;
	public static float scale = 3;
	public static final Vect3D focalPoint = new Vect3D(0, 0, 0);

	// Renderers
	private ShipRenderer currentShipRenderer;
	private ForceRenderer currentForceRenderer;
	private WorldAreaRenderer currentWorldAreaRenderer;

	public WorldRenderer() {
		currentShipRenderer = new ShipRenderer();
		currentForceRenderer = new ForceRenderer();
		currentWorldAreaRenderer = new WorldAreaRenderer();
	}

	/**
	 * @param glMode
	 * @param drawType this value is not used
	 */
	public void render(int glMode, RenderStyle drawType, World world) {

		if (glMode == GL11.GL_RENDER) {
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			renderWorldAreas(glMode, drawType, world);
		}

		selectionId = 0;
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		renderShips(glMode, drawType, world);

		if (debugDisplay && glMode == GL11.GL_RENDER) {
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			renderForces(glMode, drawType, world);
			world.getForceList().clear();

			// Show the pointer
			renderPointer(glMode, drawType);
		}
	}

	private void renderForces(int glMode, net.carmgate.morph.ui.renderer.Renderer.RenderStyle drawType, World world) {
		for (Force f : world.getForceList()) {
			currentForceRenderer.render(glMode, drawType, f);
		}
	}

	private void renderPointer(int glMode, net.carmgate.morph.ui.renderer.Renderer.RenderStyle drawType) {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glTranslatef(MorphMouse.getX(), MorphMouse.getY(), 0);
		GL11.glColor3f(1f, 1f, 1f);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2f(-10, -10);
		GL11.glVertex2f(-10, 10);
		GL11.glVertex2f(10, 10);
		GL11.glVertex2f(10, -10);
		GL11.glEnd();
		GL11.glTranslatef(-MorphMouse.getX(), -MorphMouse.getY(), 0);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	private void renderShips(int glMode, RenderStyle drawType, World world) {
		for (Ship ship : world.getShipList()) {

			// Selection names management
			if (glMode == GL11.GL_SELECT) {
				GL11.glPushName(selectionId++);
			}

			currentShipRenderer.render(glMode, RenderStyle.NORMAL, ship);

			// Selection names management
			if (glMode == GL11.GL_SELECT) {
				GL11.glPopName();
			}
		}
	}

	private void renderWorldAreas(int glMode, net.carmgate.morph.ui.renderer.Renderer.RenderStyle drawType, World world) {
		for (WorldArea wa : world.getWorldAreas().values()) {
			currentWorldAreaRenderer.render(glMode, drawType, wa);
		}
	}

	public void setForceRenderer(ForceRenderer forceRenderer) {
		this.currentForceRenderer = forceRenderer;
	}

	public void setShipRenderer(ShipRenderer shipRenderer) {
		this.currentShipRenderer = shipRenderer;
	}

	public void setWorldAreaRenderer(WorldAreaRenderer worldAreaRenderer) {
		this.currentWorldAreaRenderer = worldAreaRenderer;
	}

}
