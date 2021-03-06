package net.carmgate.morph.ui.interaction;

import java.nio.IntBuffer;

import net.carmgate.morph.Main;
import net.carmgate.morph.Main.PickingContext;
import net.carmgate.morph.model.solid.mattersource.MatterSource;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.ship.Ship;
import net.carmgate.morph.model.solid.world.World;
import net.carmgate.morph.ui.model.UIModel;
import net.carmgate.morph.ui.model.iwmenu.IWMenuItem;
import net.carmgate.morph.ui.renderer.Renderer.RenderStyle;
import net.carmgate.morph.ui.renderer.RendererHolder;
import net.carmgate.morph.ui.renderer.WorldRenderer;

import org.apache.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class PickingHandler {
	private static final Logger LOGGER = Logger.getLogger(PickingHandler.class);

	// name stack management
	private static final int NAME_STACK_LEVEL_IN_WORLD_MENU_ITEMS = 1;
	private static final int NAME_STACK_LEVEL_MORPHS = 2;
	private static final int NAME_STACK_LEVEL_SELECT_BUFFER_STACK_DEPTH = 0;
	private static final int NAME_STACK_LEVEL_SHIPS = 1;

	public PickingHandler() {
	}

	/**
	 * @param selectBuf
	 * @return the picked in-world menu item.
	 */
	private IWMenuItem getPickedIWMenuItem(IntBuffer selectBuf) {
		IWMenuItem menuItem = UIModel.getUiModel().getCurrentIWMenu().getMenuItems().get((long) selectBuf.get(3 + NAME_STACK_LEVEL_IN_WORLD_MENU_ITEMS));
		return menuItem;
	}

	/**
	 * @param selectBuf
	 * @return the picked in-world menu item.
	 */
	private MatterSource getPickedMatterSource(IntBuffer selectBuf) {
		MatterSource mSource = World.getWorld().getMatterSources().get((long) selectBuf.get(3 + NAME_STACK_LEVEL_IN_WORLD_MENU_ITEMS));
		return mSource;
	}

	/**
	 * @param selectBuf
	 * @return the picked morph
	 */
	private Morph getPickedMorph(IntBuffer selectBuf) {
		Ship ship = getPickedShip(selectBuf);
		Morph morph = ship.getMorphsByIds().get((long) selectBuf.get(3 + NAME_STACK_LEVEL_MORPHS));
		return morph;
	}

	/**
	 * @param selectBuf
	 * @return the picked ship
	 */
	private Ship getPickedShip(IntBuffer selectBuf) {
		Ship selectedShip = World.getWorld().getShips().get((long) selectBuf.get(3 + NAME_STACK_LEVEL_SHIPS));
		return selectedShip;
	}

	/**
	 * @param selectBuf
	 * @return
	 */
	private String getSelectBufferDebugString(IntBuffer selectBuf) {
		int[] dst = new int[selectBuf.remaining()];
		selectBuf.get(dst);
		String dstStr = "[";
		int index = 0;
		for (int i : dst) {
			dstStr += (index != 0 ? ", " : "") + i;
			index++;
		}
		dstStr += "]";
		return dstStr;
	}

	/**
	 * @param x
	 * @param y
	 * @param selectBuf
	 * @return
	 */
	private int glPick(int x, int y, IntBuffer selectBuf) {
		GL11.glSelectBuffer(selectBuf);
		LOGGER.trace("Picking at " + x + " " + y);

		// get viewport
		IntBuffer viewport = BufferUtils.createIntBuffer(16);
		GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);
		GL11.glClear(GL11.GL_VIEWPORT);

		GL11.glRenderMode(GL11.GL_SELECT);

		GL11.glInitNames();

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();

		float pickMatrixX = x; // SCALE_FACTOR;
		float pickMatrixY = y; // SCALE_FACTOR;
		GLU.gluPickMatrix(pickMatrixX, pickMatrixY, 0.002f, 0.002f, viewport);
		GLU.gluOrtho2D(0, Main.WIDTH, 0, Main.HEIGHT);

		RendererHolder.iwuiRenderer.render(GL11.GL_SELECT, WorldRenderer.debugDisplay ? RenderStyle.DEBUG : RenderStyle.NORMAL, World.getWorld());
		RendererHolder.worldRenderer.render(GL11.GL_SELECT, null, World.getWorld());

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPopMatrix();
		GL11.glFlush();

		int hits = GL11.glRenderMode(GL11.GL_RENDER);
		return hits;
	}

	/**
	 * Pick an object at world coordinates x and y.
	 * If multi-level object is selected, the result may vary slightly on the context:
	 * <ul><li>if a ship is clicked on</li>
	 *  <ul><li>if no ship is selected or if the clicked ship is not the currently selected one, the clicked
	 *  	ship is returned.</li>
	 *  <li>if the clicked ship is already selected, the clicked morph will be returned.</li></ul>
	 * </ul>
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @return the picked object.
	 */
	public Object pick(int x, int y) {
		return pick(x, y, null);
	}

	/**
	 * Pick an object at world coordinates x and y.
	 * If multi-level object is selected (for instance ship/morph), 
	 * the result may vary slightly depending on the context if <b>null</b> is given as the <b>kind</b> parameter :
	 * <ul><li>if a ship is clicked on</li>
	 *  <ul><li>if no ship is selected or if the clicked ship is not the currently selected one, the clicked
	 *  	ship is returned.</li>
	 *  <li>if the clicked ship is already selected, the clicked morph will be returned.</li></ul>
	 * </ul>
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param kind the kind of object we want when a click happens on a multi level object.
	 * @return the picked object.
	 */
	public Object pick(int x, int y, Class<?> kind) {
		Object pickedObject = null;

		IntBuffer selectBuf = BufferUtils.createIntBuffer(512);
		int hits = glPick(x, y, selectBuf);
		LOGGER.debug("pick hits: " + hits + "- selectBuf: " + getSelectBufferDebugString(selectBuf));

		// if there was no solid model hit, we need to deselect everything
		if (hits == 0 || selectBuf.get(NAME_STACK_LEVEL_SELECT_BUFFER_STACK_DEPTH) == 0) {
			return null;
		}

		// do not allow ship/morph selection if there is an active in-world menu
		if (selectBuf.get(NAME_STACK_LEVEL_SELECT_BUFFER_STACK_DEPTH) > 0
				&& selectBuf.get(3) == PickingContext.SHIP.ordinal()) {
			// Add the picked ship to the list of selected ships
			// We add the ship after handling morph selection to avoid it tempering
			// with morph selection.
			Ship selectedShip = getPickedShip(selectBuf);
			pickedObject = selectedShip;
			if (Morph.class.equals(kind) || UIModel.getUiModel().getSelectionModel().getSelectedShips().values().contains(selectedShip)) {
				pickedObject = getPickedMorph(selectBuf);
			}

			return pickedObject;
		}

		// pick in-world menu items
		LOGGER.trace("Menu item: " + selectBuf.get(3 + NAME_STACK_LEVEL_IN_WORLD_MENU_ITEMS));
		if (selectBuf.get(NAME_STACK_LEVEL_SELECT_BUFFER_STACK_DEPTH) > 0
				&& selectBuf.get(3) == PickingContext.IW_MENU.ordinal()) {
			return getPickedIWMenuItem(selectBuf);
		}

		// pick asteroids
		if (selectBuf.get(NAME_STACK_LEVEL_SELECT_BUFFER_STACK_DEPTH) > 0
				&& selectBuf.get(3) == PickingContext.ASTEROID.ordinal()) {
			return getPickedMatterSource(selectBuf);
		}
		return pickedObject;
	}

}
