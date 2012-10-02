package net.carmgate.morph;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import net.carmgate.morph.ia.IA;
import net.carmgate.morph.ia.tracker.FixedPositionTracker;
import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.ship.Ship;
import net.carmgate.morph.model.solid.world.World;
import net.carmgate.morph.ui.MorphMouse;
import net.carmgate.morph.ui.action.ShowEvolveMenuAction;
import net.carmgate.morph.ui.action.ToggleCombatMode;
import net.carmgate.morph.ui.action.ToggleDebugAction;
import net.carmgate.morph.ui.action.ToggleFreezeAction;
import net.carmgate.morph.ui.model.UIModel;
import net.carmgate.morph.ui.model.iwmenu.IWMenuItem;
import net.carmgate.morph.ui.renderer.IWUIRenderer;
import net.carmgate.morph.ui.renderer.Renderer.RenderStyle;
import net.carmgate.morph.ui.renderer.UIRenderer;
import net.carmgate.morph.ui.renderer.WorldRenderer;
import net.carmgate.morph.ui.renderer.morph.MorphRenderer;

import org.apache.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class Main {

	private static final int NAME_STACK_LEVEL_SELECT_BUFFER_STACK_DEPTH = 0;
	private static final int NAME_STACK_LEVEL_IN_WORLD_MENU_ITEMS = 0;
	private static final int NAME_STACK_LEVEL_SHIPS = 1;
	private static final int NAME_STACK_LEVEL_MORPHS = 2;

	/**
	 * The distance (in pixels) the mouse should be dragged to trigger a world translation following the mouse pointer.
	 */
	private static final int MIN_MOVE_FOR_DRAG = 5;

	private static Logger LOGGER = Logger.getLogger(Main.class);

	// public static final float SCALE_FACTOR = 1f;
	public static final int HEIGHT = 768;
	public static final int WIDTH = 1024;

	/**
	 * Main Class
	 */
	public static void main(String[] argv) {
		Main sample = new Main();
		sample.start();
	}

	private World world;

	// Renderers
	private WorldRenderer worldRenderer;
	private UIRenderer uiRenderer;
	private IWUIRenderer uiInWorldRenderer;

	private Vect3D holdWorldMousePos = null;

	// Actions
	private final ToggleDebugAction toggleDebugAction = new ToggleDebugAction();
	private final ToggleCombatMode toggleCombatMode = new ToggleCombatMode();
	private final ToggleFreezeAction toggleFreezeAction = new ToggleFreezeAction();
	private final ShowEvolveMenuAction showEvolveMenuAction = new ShowEvolveMenuAction();

	/**
	 * @param selectBuf
	 * @return the picked in-world menu item.
	 */
	private IWMenuItem getPickedIWMenuItem(IntBuffer selectBuf) {
		IWMenuItem menuItem = UIModel.getUiModel().getCurrentIWMenu().getMenuItems().get(selectBuf.get(3 + NAME_STACK_LEVEL_IN_WORLD_MENU_ITEMS));
		return menuItem;
	}

	/**
	 * @param selectBuf
	 * @return the picked morph
	 */
	private Morph getPickedMorph(IntBuffer selectBuf) {
		Ship ship = getPickedShip(selectBuf);
		Morph morph = ship.getMorphsByIds().get(selectBuf.get(3 + NAME_STACK_LEVEL_MORPHS));
		return morph;
	}

	/**
	 * @param selectBuf
	 * @return the picked ship
	 */
	private Ship getPickedShip(IntBuffer selectBuf) {
		Ship selectedShip = world.getShips().get(selectBuf.get(3 + NAME_STACK_LEVEL_SHIPS));
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
		if (UIModel.getUiModel().getSelectionModel().getSelectedShips().size() > 0) {
			LOGGER.trace("Selected ship: " + UIModel.getUiModel().getSelectionModel().getSelectedShips().values().iterator().next().getPos());
		}

		// get viewport
		IntBuffer viewport = BufferUtils.createIntBuffer(16);
		GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);
		GL11.glClear(GL11.GL_VIEWPORT);

		GL11.glRenderMode(GL11.GL_SELECT);

		GL11.glInitNames();

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		// GL11.glScalef(SCALE_FACTOR, SCALE_FACTOR, SCALE_FACTOR);
		float pickMatrixX = x; // SCALE_FACTOR;
		float pickMatrixY = y; // SCALE_FACTOR;
		GLU.gluPickMatrix(pickMatrixX, pickMatrixY, 5.0f, 5.0f, viewport);
		GLU.gluOrtho2D(0, WIDTH, 0, HEIGHT);

		// name stack level for the ui elements
		// the name stack for ships and morphs is handled in the ShipRenderer and the MorphRenderer
		// make current morph selectable
		GL11.glPushName(0);
		uiInWorldRenderer.render(GL11.GL_SELECT, WorldRenderer.debugDisplay ? RenderStyle.DEBUG : RenderStyle.NORMAL);
		worldRenderer.render(GL11.GL_SELECT, null, world);
		// pop name stack level for ui elements
		GL11.glPopName();

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPopMatrix();
		GL11.glFlush();

		int hits = GL11.glRenderMode(GL11.GL_RENDER);
		return hits;
	}

	/**
	 * Initialise the GL display
	 *
	 * @param width The width of the display
	 * @param height The height of the display
	 */
	private void initGL(int width, int height) {
		try {
			Display.setDisplayMode(new DisplayMode(width, height));
			Display.create();
			Display.setTitle("Morph");
			Display.setVSyncEnabled(true);
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}

		// set clear color - Wont be needed once we have a background
		GL11.glClearColor(0, 0, 0, 0);

		// enable alpha blending
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GLU.gluOrtho2D(WorldRenderer.focalPoint.x - width * WorldRenderer.scale / 2,
				WorldRenderer.focalPoint.x + width * WorldRenderer.scale / 2,
				WorldRenderer.focalPoint.y + height * WorldRenderer.scale / 2,
				WorldRenderer.focalPoint.y - height * WorldRenderer.scale / 2);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
	}

	public void pick(int x, int y) {

		IntBuffer selectBuf = BufferUtils.createIntBuffer(512);
		int hits = glPick(x, y, selectBuf);
		LOGGER.debug("pick hits: " + hits + "- selectBuf: " + getSelectBufferDebugString(selectBuf));

		// if there was no hit, we need to deselect everything
		if (hits == 0) {
			UIModel.getUiModel().getSelectionModel().clearAllSelections();
			UIModel.getUiModel().setCurrentInWorldMenu(null);
			return;
		}

		// do not allow ship/morph selection if there is an active in-world menu
		if (UIModel.getUiModel().getCurrentIWMenu() == null
				&& selectBuf.get(NAME_STACK_LEVEL_SELECT_BUFFER_STACK_DEPTH) > NAME_STACK_LEVEL_SHIPS
				&& selectBuf.get(3 + NAME_STACK_LEVEL_SHIPS) > 0) {
			// Add the picked ship to the list of selected ships
			// We add the ship after handling morph selection to avoid it tempering
			// with morph selection.
			Ship selectedShip = getPickedShip(selectBuf);
			if (UIModel.getUiModel().getSelectionModel().getSelectedShips().values().contains(selectedShip)) {

				Morph morph = getPickedMorph(selectBuf);
				// if LCONTROL is down, add/remove to/from selection
				// else, just replace the selection by the currently selected morph.
				if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) == true) {
					// Add the selected morph if it wasn't
					// Remove it if it was already selected
					if (!UIModel.getUiModel().getSelectionModel().getSelectedMorphs().values().contains(morph)) {
						UIModel.getUiModel().getSelectionModel().addMorphToSelection(morph);
					} else {
						UIModel.getUiModel().getSelectionModel().removeMorphFromSelection(morph);
					}
				} else {
					UIModel.getUiModel().getSelectionModel().removeAllMorphsFromSelection();
					UIModel.getUiModel().getSelectionModel().addMorphToSelection(morph);
				}
			}
			// Add the ship to the selection
			UIModel.getUiModel().getSelectionModel().addShipToSelection(selectedShip);
		}

		// pick in-world menu items
		LOGGER.trace("Menu item: " + selectBuf.get(3 + NAME_STACK_LEVEL_IN_WORLD_MENU_ITEMS));
		if (selectBuf.get(NAME_STACK_LEVEL_SELECT_BUFFER_STACK_DEPTH) > NAME_STACK_LEVEL_IN_WORLD_MENU_ITEMS
				&& selectBuf.get(3 + NAME_STACK_LEVEL_IN_WORLD_MENU_ITEMS) > 0) {
			UIModel.getUiModel().getSelectionModel().addIWMenuItemToSelection(getPickedIWMenuItem(selectBuf));
		}
	}

	/**
	 * draw a quad with the image on it
	 */
	public void render() {

		GL11.glNormal3i(0, 0, -1);

		// draw world
		RenderStyle renderStyle = RenderStyle.NORMAL;
		if (WorldRenderer.debugDisplay) {
			renderStyle = RenderStyle.DEBUG;
		}
		// go down into the stack to leave a stack level for the interface
		worldRenderer.render(GL11.GL_RENDER, renderStyle, world);
		uiInWorldRenderer.render(GL11.GL_RENDER, WorldRenderer.debugDisplay ? RenderStyle.DEBUG : RenderStyle.NORMAL);

		// Interface rendering
		GL11.glTranslatef(WorldRenderer.focalPoint.x, WorldRenderer.focalPoint.y, WorldRenderer.focalPoint.z);
		uiRenderer.render(GL11.GL_RENDER, WorldRenderer.debugDisplay ? RenderStyle.DEBUG : RenderStyle.NORMAL);
		GL11.glTranslatef(-WorldRenderer.focalPoint.x, -WorldRenderer.focalPoint.y, -WorldRenderer.focalPoint.z);

		// move world
		world.update();

		// udpate IAs
		for (Ship ship : world.getShips().values()) {
			List<IA> iasToRemove = new ArrayList<IA>();
			for (IA ia : ship.getIAList()) {
				if (ia != null) {
					if (ia.done()) {
						iasToRemove.add(ia);
					} else {
						ia.compute();
					}
				}
			}
			for (IA ia : iasToRemove) {
				ship.getIAList().remove(ia);
			}
			iasToRemove.clear();
		}
	}

	/**
	 * Start the application
	 */
	public void start() {
		initGL(WIDTH, HEIGHT);
		MorphRenderer.init();

		// Initializes the world and its renderer
		world = World.getWorld();
		world.init();
		worldRenderer = new WorldRenderer();
		uiRenderer = new UIRenderer();
		uiRenderer.init();
		uiInWorldRenderer = new IWUIRenderer();

		// Rendering loop
		while (true) {
			// Renders everything
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			render();

			// updates display and sets frame rate
			Display.update();
			Display.sync(50);

			// Get mouse position in world coordinates
			Vect3D worldMousePos = new Vect3D(MorphMouse.getX(), MorphMouse.getY(), 0);
			// Get mouse position in pixels on the display.
			// We will use this later to detect interaction with the 2D part of the GUI
			Vect3D mousePos = new Vect3D(Mouse.getX(), Mouse.getY(), 0);

			// Handling world moving around by drag and dropping the world.
			// This portion of code is meant to allow the engine to show the world while it's being dragged.
			if (holdWorldMousePos != null) {
				if (Math.abs(holdWorldMousePos.x - MorphMouse.getX()) > MIN_MOVE_FOR_DRAG
						|| Math.abs(holdWorldMousePos.y - MorphMouse.getY()) > MIN_MOVE_FOR_DRAG) {
					WorldRenderer.focalPoint.add(holdWorldMousePos);
					WorldRenderer.focalPoint.substract(worldMousePos);
					GL11.glMatrixMode(GL11.GL_PROJECTION);
					GL11.glLoadIdentity();
					GLU.gluOrtho2D(WorldRenderer.focalPoint.x - WIDTH * WorldRenderer.scale / 2,
							WorldRenderer.focalPoint.x + WIDTH * WorldRenderer.scale / 2,
							WorldRenderer.focalPoint.y + HEIGHT * WorldRenderer.scale / 2,
							WorldRenderer.focalPoint.y - HEIGHT * WorldRenderer.scale / 2);
					holdWorldMousePos.x = MorphMouse.getX();
					holdWorldMousePos.y = MorphMouse.getY();

				}
			}

			// If a mouse event has fired, Mouse.next() returns true.
			if (Mouse.next()) {

				// Event button == 0 : Left button related event
				if (Mouse.getEventButton() == 0) {
					// if event button state is false, the button is being released
					if (!Mouse.getEventButtonState()) {
						if (Math.abs(holdWorldMousePos.x - MorphMouse.getX()) > MIN_MOVE_FOR_DRAG
								|| Math.abs(holdWorldMousePos.y - MorphMouse.getY()) > MIN_MOVE_FOR_DRAG) {
							WorldRenderer.focalPoint.add(holdWorldMousePos);
							WorldRenderer.focalPoint.substract(worldMousePos);
							GL11.glMatrixMode(GL11.GL_PROJECTION);
							GL11.glLoadIdentity();
							GLU.gluOrtho2D(WorldRenderer.focalPoint.x - WIDTH * WorldRenderer.scale / 2,
									WorldRenderer.focalPoint.x + WIDTH * WorldRenderer.scale / 2,
									WorldRenderer.focalPoint.y + HEIGHT * WorldRenderer.scale / 2,
									WorldRenderer.focalPoint.y - HEIGHT * WorldRenderer.scale / 2);
						} else {
							pick(MorphMouse.getX(), MorphMouse.getY());
						}
						holdWorldMousePos = null;
					} else {
						// the mouse left button is being pressed
						holdWorldMousePos = worldMousePos;
					}
				}

				// Event button == 1 : Right button related event
				if (Mouse.getEventButton() == 1 && !Mouse.getEventButtonState() && UIModel.getUiModel().getSelectionModel().getSelectedShips().size() > 0
						&& !World.combat) {

					LOGGER.debug("Number of selected morphs: " + UIModel.getUiModel().getSelectionModel().getSelectedMorphs().size());
					for (Ship selectedShip : UIModel.getUiModel().getSelectionModel().getSelectedShips().values()) {
						List<IA> iaList = selectedShip.getIAList();

						// Look for existing tracker
						// If we find one, update it's target
						boolean foundATracker = false;
						for (IA ia : iaList) {
							if (ia instanceof FixedPositionTracker) {
								((FixedPositionTracker) ia).setTargetPos(worldMousePos);
								foundATracker = true;
							}
						}

						// If we found no tracker, create a new one and add it to this ship's
						// IA list
						if (!foundATracker) {
							iaList.add(new FixedPositionTracker(selectedShip, worldMousePos));
						}
					}
				}

				// Commented because it should be reworked
				// Handling shoot
				// if (Mouse.getEventButton() == 1 && !Mouse.getEventButtonState() && !UIModel.getUiModel().getSelectionModel().getSelectedShips().isEmpty() &&
				// World.combat) {
				// for (Ship selectedShip : UIModel.getUiModel().getSelectionModel().getSelectedShips().values()) {
				// selectedShip.getIAList().add(new WorldPositionFirer(selectedShip, worldMousePos));
				// }
				// }

				// TODO Should be reworked
				// int dWheel = Mouse.getDWheel();
				// if (dWheel != 0) {
				// float scale = (float) (Math.pow(1 + Math.pow(4, -5 + Math.abs(dWheel / 120)), Math.signum(dWheel)));
				// GL11.glScalef(scale, scale, scale);
				// }
			}

			if (Keyboard.next()) {
				toggleDebugAction.run();
				toggleCombatMode.run();
				toggleFreezeAction.run();
				showEvolveMenuAction.run();
			}

			if (Display.isCloseRequested()) {
				Display.destroy();
				System.exit(0);
			}
		}
	}
}