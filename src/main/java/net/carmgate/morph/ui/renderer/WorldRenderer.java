package net.carmgate.morph.ui.renderer;

import net.carmgate.morph.Main;
import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.physics.Force;
import net.carmgate.morph.model.solid.energysource.EnergySource;
import net.carmgate.morph.model.solid.mattersource.MatterSource;
import net.carmgate.morph.model.solid.ship.Ship;
import net.carmgate.morph.model.solid.world.World;
import net.carmgate.morph.model.solid.world.WorldArea;
import net.carmgate.morph.ui.MorphMouse;
import net.carmgate.morph.ui.model.UIModel;
import net.carmgate.morph.ui.renderer.energysource.EnergySourceRenderer;
import net.carmgate.morph.ui.renderer.mattersource.MatterSourceRenderer;
import net.carmgate.morph.ui.renderer.ship.SelectedShipRenderer;
import net.carmgate.morph.ui.renderer.ship.ShipRenderer;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.TextureImpl;

public class WorldRenderer implements Renderer<World> {

	public static boolean selectRendering = false;
	public static boolean debugDisplay = false;
	public static float scale = 1;
	public static final Vect3D focalPoint = new Vect3D(0, 0, 0);

	// Renderers
	private ShipRenderer shipRenderer = new ShipRenderer();
	private SelectedShipRenderer selectedShipRenderer = new SelectedShipRenderer();
	private ForceRenderer currentForceRenderer = new ForceRenderer();
	private WorldAreaRenderer currentWorldAreaRenderer = new WorldAreaRenderer();
	private EnergySourceRenderer energySourceRenderer = new EnergySourceRenderer();
	private MatterSourceRenderer matterSourceRenderer = new MatterSourceRenderer();
	private ParticleRenderer particleRenderer = new ParticleRenderer();

	/**
	 * Renders the world.
	 * In normal mode, renders the world areas and the ships.
	 * In debug mode, renders the mouse pointer and the forces.
	 */
	@Override
	public void render(int glMode, RenderStyle drawType, World world) {

		// glMode = GL11.GL_SELECT;

		if (glMode != GL11.GL_SELECT) {
			particleRenderer.render(glMode, drawType, world.getParticleEngine(), true);
		}

		if (glMode == GL11.GL_RENDER) {
			TextureImpl.bindNone();
			renderWorldAreas(glMode, drawType, world);
		}

		// Do whatever is necessary to draw the behaviors in the background
		for (Ship s : World.getWorld().getShips().values()) {
			shipRenderer.renderShipMorphBehaviors(glMode, drawType, s, true);
		}

		if (glMode == GL11.GL_SELECT) {
			renderShips(glMode, drawType, world);
			renderMatterSources(glMode, drawType, world);
			renderEnergySources(glMode, drawType, world);
		} else {
			renderEnergySources(glMode, drawType, world);
			renderMatterSources(glMode, drawType, world);
			renderShips(glMode, drawType, world);
		}

		if (debugDisplay && glMode == GL11.GL_RENDER) {
			TextureImpl.bindNone();
			renderForces(glMode, drawType, world);
			world.getForceList().clear();

			// Show the pointer
			renderPointer(glMode, drawType);
		}

		if (glMode != GL11.GL_SELECT) {
			particleRenderer.render(glMode, drawType, world.getParticleEngine(), false);
		}

	}

	private void renderEnergySources(int glMode, net.carmgate.morph.ui.renderer.Renderer.RenderStyle drawType, World world) {
		for (EnergySource e : world.getEnergySources().values()) {
			energySourceRenderer.render(glMode, drawType, e);
		}
	}

	/**
	 * Renders forces.
	 * @param glMode {@link GL11#GL_RENDER} or {@link GL11#GL_SELECT}
	 * @param drawType Normal or Debug mode
	 * @param world the world whose ships should be rendered.
	 */
	private void renderForces(int glMode, net.carmgate.morph.ui.renderer.Renderer.RenderStyle drawType, World world) {
		for (Force f : world.getForceList()) {
			currentForceRenderer.render(glMode, drawType, f);
		}
	}

	private void renderMatterSources(int glMode, net.carmgate.morph.ui.renderer.Renderer.RenderStyle drawType, World world) {
		for (MatterSource e : world.getMatterSources().values()) {
			matterSourceRenderer.render(glMode, drawType, e);
		}
	}

	/**
	 * Renders the mouse pointer.
	 * @param glMode {@link GL11#GL_RENDER} or {@link GL11#GL_SELECT}
	 * @param drawType Normal or Debug mode
	 * @param world the world whose ships should be rendered.
	 */
	private void renderPointer(int glMode, net.carmgate.morph.ui.renderer.Renderer.RenderStyle drawType) {
		TextureImpl.bindNone();
		GL11.glTranslatef(MorphMouse.getX(), MorphMouse.getY(), 0);
		GL11.glColor3f(1f, 1f, 1f);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2f(-5, -5);
		GL11.glVertex2f(-5, 5);
		GL11.glVertex2f(5, 5);
		GL11.glVertex2f(5, -5);
		GL11.glEnd();
		GL11.glTranslatef(-MorphMouse.getX(), -MorphMouse.getY(), 0);
	}

	/**
	 * Cycles through the ships to render them.
	 * @param glMode {@link GL11#GL_RENDER} or {@link GL11#GL_SELECT}
	 * @param drawType Normal or Debug mode
	 * @param world the world whose ships should be rendered.
	 */
	private void renderShips(int glMode, RenderStyle drawType, World world) {
		GL11.glPushName(Main.PickingContext.SHIP.ordinal());

		for (Ship ship : world.getShips().values()) {
			shipRenderer.render(glMode, drawType, ship);
			if (glMode != GL11.GL_SELECT && UIModel.getUiModel().getSelectionModel().getSelectedShips().containsValue(ship)) {
				selectedShipRenderer.render(glMode, drawType, ship);
			}
		}

		GL11.glPopName();
	}

	/**
	 * Render the world area
	 * @param glMode {@link GL11#GL_RENDER} or {@link GL11#GL_SELECT}
	 * @param drawType Normal or Debug mode
	 * @param world the world whose ships should be rendered.
	 */
	private void renderWorldAreas(int glMode, net.carmgate.morph.ui.renderer.Renderer.RenderStyle drawType, World world) {
		for (WorldArea wa : world.getWorldAreas().values()) {
			currentWorldAreaRenderer.render(glMode, drawType, wa);
		}
	}

	public void setForceRenderer(ForceRenderer forceRenderer) {
		currentForceRenderer = forceRenderer;
	}

	public void setShipRenderer(ShipRenderer shipRenderer) {
		shipRenderer = shipRenderer;
	}

	public void setWorldAreaRenderer(WorldAreaRenderer worldAreaRenderer) {
		currentWorldAreaRenderer = worldAreaRenderer;
	}

}
