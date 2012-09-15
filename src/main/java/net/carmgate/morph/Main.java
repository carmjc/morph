package net.carmgate.morph;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import net.carmgate.morph.ia.IA;
import net.carmgate.morph.ia.combat.WorldPositionFirer;
import net.carmgate.morph.ia.tracker.FixedPositionTracker;
import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.World;
import net.carmgate.morph.model.morph.Morph;
import net.carmgate.morph.model.ship.Ship;
import net.carmgate.morph.ui.MorphMouse;
import net.carmgate.morph.ui.action.ToggleCombatMode;
import net.carmgate.morph.ui.action.ToggleDebugAction;
import net.carmgate.morph.ui.action.ToggleFreezeAction;
import net.carmgate.morph.ui.renderer.InterfaceRenderer;
import net.carmgate.morph.ui.renderer.MorphRenderer;
import net.carmgate.morph.ui.renderer.Renderer.RenderStyle;
import net.carmgate.morph.ui.renderer.WorldRenderer;

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

	private final Vect3D worldOrigin = new Vect3D(0, 0, 0);// new Point3D(-196, -196, 0);
	private final Vect3D worldRotation = new Vect3D(0, 0, 0);
	private World world;

	// Renderers
	private WorldRenderer worldRenderer;
	private InterfaceRenderer interfaceRenderer;

	private Vect3D holdWorldMousePos = null;

	// Actions
	private final ToggleDebugAction toggleDebugAction = new ToggleDebugAction();
	private final ToggleCombatMode toggleCombatMode = new ToggleCombatMode();
	private final ToggleFreezeAction toggleFreezeAction = new ToggleFreezeAction();

	/**
	 * @param selectBuf
	 * @return
	 */
	private Morph getPickedMorph(IntBuffer selectBuf) {
		Ship ship = getPickedShip(selectBuf);
		int selectedMorphId = selectBuf.get(4);
		Morph morph = ship.getMorphs().get(selectedMorphId);
		return morph;
	}

	/**
	 * @param selectBuf
	 * @return
	 */
	private Ship getPickedShip(IntBuffer selectBuf) {
		int selectedShipId = selectBuf.get(3);
		Ship selectedShip = world.getShips().get(selectedShipId);
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

		LOGGER.debug("Picking at " + x + " " + y);
		if (world.getSelectionModel().getSelectedShips().size() > 0) {
			LOGGER.debug("Selected ship: " + world.getSelectionModel().getSelectedShips().values().iterator().next().pos);
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

		worldRenderer.render(GL11.GL_SELECT, null, world);

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
		GL11.glClearColor(0.2f, 0.2f, 0.2f, 0);

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
			world.getSelectionModel().clearAllSelections();
			return;
		}

		// Add the picked ship to the list of selected ships
		// We add the ship after handling morph selection to avoid it tempering
		// with morph selection.
		Ship selectedShip = getPickedShip(selectBuf);
		if (world.getSelectionModel().getSelectedShips().values().contains(selectedShip)) {

			// Add the selected morph if it wasn't
			// Remove it if it was already selected
			Morph morph = getPickedMorph(selectBuf);
			if (!world.getSelectionModel().getSelectedMorphs().values().contains(morph)) {
				world.getSelectionModel().addMorphToSelection(morph);
			} else {
				world.getSelectionModel().removeMorphFromSelection(morph);
			}
		}
		// Add the ship to the selection
		world.getSelectionModel().addShipToSelection(selectedShip);
	}

	/**
	 * draw a quad with the image on it
	 */
	public void render() {

		GL11.glTranslatef(worldOrigin.x, worldOrigin.y, worldOrigin.z);
		GL11.glRotatef(worldRotation.z, 0, 0, 1);

		// draw world
		RenderStyle renderStyle = RenderStyle.NORMAL;
		if (WorldRenderer.debugDisplay) {
			renderStyle = RenderStyle.DEBUG;
		}
		worldRenderer.render(GL11.GL_RENDER, renderStyle, world);

		GL11.glRotatef(-worldRotation.z, 0, 0, 1);
		GL11.glTranslatef(-worldOrigin.x, -worldOrigin.y, -worldOrigin.z);

		// Interface rendering
		GL11.glTranslatef(WorldRenderer.focalPoint.x, WorldRenderer.focalPoint.y, WorldRenderer.focalPoint.z);
		interfaceRenderer.render(GL11.GL_RENDER, WorldRenderer.debugDisplay ? RenderStyle.DEBUG : RenderStyle.NORMAL);
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
		interfaceRenderer = new InterfaceRenderer();
		interfaceRenderer.init();

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
				if (Mouse.getEventButton() == 1 && !Mouse.getEventButtonState() && world.getSelectionModel().getSelectedShips().size() > 0 && !World.combat) {
					// Right mouse button has been released and a ship is selected
					// Activate or deactivate the morph under mouse pointer.
					for (Morph morph : world.getSelectionModel().getSelectedMorphs().values()) {
						if (morph.getShip().toggleActiveMorph(morph)) {
							if (!morph.disabled) {
								morph.activate();
							}
						} else {
							morph.deactivate();
						}
					}

					// If no morph is selected, the right click should be understood as a moveto order.
					if (world.getSelectionModel().getSelectedMorphs().isEmpty()) {
						for (Ship selectedShip : world.getSelectionModel().getSelectedShips().values()) {
							List<IA> iaList = selectedShip.getIAList();

							// Look for existing tracker
							for (IA ia : iaList) {
								if (ia instanceof FixedPositionTracker) {
									((FixedPositionTracker) ia).setTargetPos(worldMousePos);
								}
							}

							iaList.add(new FixedPositionTracker(selectedShip, worldMousePos));
						}
					}
				}

				// Handling shoot
				if (Mouse.getEventButton() == 1 && !Mouse.getEventButtonState() && !world.getSelectionModel().getSelectedShips().isEmpty() && World.combat) {
					for (Ship selectedShip : world.getSelectionModel().getSelectedShips().values()) {
						selectedShip.getIAList().add(new WorldPositionFirer(selectedShip, worldMousePos));
					}
				}

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
			}

			if (Display.isCloseRequested()) {
				Display.destroy();
				System.exit(0);
			}
		}
	}
}