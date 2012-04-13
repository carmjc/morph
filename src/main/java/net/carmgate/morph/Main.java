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

	private static Logger logger = Logger.getLogger(Main.class);

	//	public static final float SCALE_FACTOR = 1f;
	public static final int HEIGHT = 768;
	public static final int WIDTH = 1024;

	/**
	 * Main Class
	 */
	public static void main(String[] argv) {
		Main sample = new Main();
		sample.start();
	}
	private final Vect3D worldOrigin = new Vect3D(0, 0, 0);//new Point3D(-196, -196, 0);
	private final Vect3D worldRotation = new Vect3D(0, 0, 0);
	private World world;

	// Renderers
	private WorldRenderer worldRenderer;
	private InterfaceRenderer interfaceRenderer;

	private Vect3D holdWorldMousePos = null;

	// Actions
	private final ToggleDebugAction toggleDebugAction = new ToggleDebugAction();
	private final ToggleCombatMode toggleCombatMode = new ToggleCombatMode();

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

		// enable texturing
		//		GL11.glEnable(GL11.GL_TEXTURE_2D);
		// It seems it's not needed, but I do not understand why ...

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

		logger.debug("Picking at " + x + " " + y);
		if (World.getWorld().getSelectedShip() != null) {
			logger.debug("Selected ship: " + World.getWorld().getSelectedShip().pos);
		}

		// get viewport
		IntBuffer viewport = BufferUtils.createIntBuffer(16);
		GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);

		IntBuffer selectBuf = BufferUtils.createIntBuffer(512);
		GL11.glSelectBuffer(selectBuf);
		GL11.glRenderMode(GL11.GL_SELECT);

		GL11.glInitNames();
		GL11.glPushName(-1);

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		//		GL11.glScalef(SCALE_FACTOR, SCALE_FACTOR, SCALE_FACTOR);
		float pickMatrixX = x; // SCALE_FACTOR;
		float pickMatrixY = y; // SCALE_FACTOR;
		GLU.gluPickMatrix(pickMatrixX, pickMatrixY, 6.0f, 6.0f, viewport);
		GLU.gluOrtho2D(0, WIDTH, 0, HEIGHT);

		worldRenderer.render(GL11.GL_SELECT, null, world);

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPopMatrix();
		GL11.glFlush();

		int hits = GL11.glRenderMode(GL11.GL_RENDER);

		if (hits == 0) {
			if (world.getSelectedShip() != null) {
				world.getSelectedShip().setSelectedMorph(-1);
			}
			world.setSelectedShip(-1);
			return;
		}

		int j = 0;
		Ship lastSelectedShip = world.getSelectedShip();
		int index = selectBuf.get(j + 4);
		world.setSelectedShip(index);
		if (lastSelectedShip != null && lastSelectedShip == world.getSelectedShip()) {
			world.getSelectedShip().toggleSelectedMorph(selectBuf.get(j + 5));
		}
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
		for (Ship ship : world.getShipList()) {
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
			Vect3D mousePos = new Vect3D(Mouse.getX(), Mouse.getY(), 0);

			// Handling world moving around by drag and dropping the world.
			// This portion of code is meant to allow the engine to show the world while it's being dragged.
			if (holdWorldMousePos != null) {
				if (Math.abs(holdWorldMousePos.x - MorphMouse.getX()) > MIN_MOVE_FOR_DRAG || Math.abs(holdWorldMousePos.y - MorphMouse.getY()) > MIN_MOVE_FOR_DRAG) {
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
						if (Math.abs(holdWorldMousePos.x - MorphMouse.getX()) > MIN_MOVE_FOR_DRAG || Math.abs(holdWorldMousePos.y - MorphMouse.getY()) > MIN_MOVE_FOR_DRAG) {
							WorldRenderer.focalPoint.add(holdWorldMousePos);
							WorldRenderer.focalPoint.substract(worldMousePos);
							GL11.glMatrixMode(GL11.GL_PROJECTION);
							GL11.glLoadIdentity();
							GLU.gluOrtho2D(WorldRenderer.focalPoint.x - WIDTH * WorldRenderer.scale / 2,
									WorldRenderer.focalPoint.x + WIDTH * WorldRenderer.scale / 2,
									WorldRenderer.focalPoint.y + HEIGHT * WorldRenderer.scale / 2,
									WorldRenderer.focalPoint.y - HEIGHT * WorldRenderer.scale / 2);
						} else {
							pick(MorphMouse.getX(),MorphMouse.getY());
						}
						holdWorldMousePos = null;
					} else {
						// the mouse left button is being pressed
						holdWorldMousePos = worldMousePos;
					}
				}

				// Event button == 0 : Right button related event
				if (Mouse.getEventButton() == 1 && !Mouse.getEventButtonState() && world.getSelectedShip() != null && !World.combat) {
					// Right mouse button has been released and a ship is selected
					// Activate or deactivate the morph under mouse pointer.
					for (Morph morph : world.getSelectedShip().getSelectedMorphList()) {
						if (morph.getShip().toggleActiveMorph(morph)) {
							if (!morph.disabled) {
								morph.activate();
							}
						} else {
							morph.deactivate();
						}
					}

					// If no morph is selected, the right click should be understood as a moveto order.
					if (world.getSelectedShip().getSelectedMorphList().isEmpty()) {
						world.getSelectedShip().getIAList().add(new FixedPositionTracker(world.getSelectedShip(), worldMousePos));
					}
				}

				// Handling shoot
				if (Mouse.getEventButton() == 1 && !Mouse.getEventButtonState() && world.getSelectedShip() != null && World.combat) {
					world.getSelectedShip().getIAList().add(new WorldPositionFirer(world.getSelectedShip(), worldMousePos));
				}

				//				int dWheel = Mouse.getDWheel();
				//				if (dWheel != 0) {
				//					float scale = (float) (Math.pow(1 + Math.pow(4, -5 + Math.abs(dWheel / 120)), Math.signum(dWheel)));
				//					GL11.glScalef(scale, scale, scale);
				//				}
			}

			if (Keyboard.next()) {
				toggleDebugAction.run();
				toggleCombatMode.run();
			}

			if (Display.isCloseRequested()) {
				Display.destroy();
				System.exit(0);
			}
		}
	}
}