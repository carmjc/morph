package net.carmgate.morph;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import net.carmgate.morph.ia.IA;
import net.carmgate.morph.ia.tracker.FixedPositionTracker;
import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.World;
import net.carmgate.morph.model.behavior.Emitting;
import net.carmgate.morph.model.morph.EmitterMorph;
import net.carmgate.morph.model.morph.Morph;
import net.carmgate.morph.model.ship.Ship;
import net.carmgate.morph.ui.MorphMouse;
import net.carmgate.morph.ui.renderer.MorphRenderer;
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

	private static Logger log = Logger.getLogger(Main.class);

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

	private WorldRenderer worldDecorator;

	private final List<IA> trackerList = new ArrayList<IA>();
	private Vect3D holdMousePos = null;
	private Vect3D holdWorldMousePos = null;

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
				WorldRenderer.focalPoint.y - height * WorldRenderer.scale / 2,
				WorldRenderer.focalPoint.y + height * WorldRenderer.scale / 2);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
	}

	public void pick(int x, int y) {

		log.debug("Picking at " + x + " " + y);
		if (World.getWorld().getSelectedShip() != null) {
			log.debug("Selected ship: " + World.getWorld().getSelectedShip().pos);
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

		worldDecorator.render(GL11.GL_SELECT, null, world);

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
		//		worldOrigin.x += 0.3;
		//		worldOrigin.y += 0.3;
		//		worldRotation.z += 0;

		GL11.glTranslatef(worldOrigin.x, worldOrigin.y, worldOrigin.z);
		GL11.glRotatef(worldRotation.z, 0, 0, 1);

		// draw world
		worldDecorator.render(GL11.GL_RENDER, null, world);

		// move world
		world.update();

		// udpate tracker
		List<IA> iasToRemove = new ArrayList<IA>();
		for (IA track : trackerList) {
			if (track != null) {
				if (track.done()) {
					iasToRemove.add(track);
				} else {
					track.compute();
				}
			}
		}
		for (IA track : iasToRemove) {
			trackerList.remove(track);
		}
		iasToRemove.clear();

		GL11.glRotatef(-worldRotation.z, 0, 0, 1);
		GL11.glTranslatef(-worldOrigin.x, -worldOrigin.y, -worldOrigin.z);
	}

	/**
	 * Start the application
	 */
	public void start() {
		initGL(WIDTH, HEIGHT);
		MorphRenderer.init();

		world = World.getWorld();
		world.init();
		worldDecorator = new WorldRenderer();

		while (true) {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			render();

			Display.update();
			Display.sync(200);

			Vect3D worldMousePos = new Vect3D(MorphMouse.getX(), MorphMouse.getY(), 0);
			Vect3D mousePos = new Vect3D(Mouse.getX(), Mouse.getY(), 0);
			if (holdWorldMousePos != null) {
				if (holdWorldMousePos.x != MorphMouse.getX() || holdWorldMousePos.y != MorphMouse.getY()) {
					WorldRenderer.focalPoint.add(holdWorldMousePos);
					WorldRenderer.focalPoint.substract(worldMousePos);
					GL11.glMatrixMode(GL11.GL_PROJECTION);
					GL11.glLoadIdentity();
					GLU.gluOrtho2D(WorldRenderer.focalPoint.x - WIDTH * WorldRenderer.scale / 2,
							WorldRenderer.focalPoint.x + WIDTH * WorldRenderer.scale / 2,
							WorldRenderer.focalPoint.y - HEIGHT * WorldRenderer.scale / 2,
							WorldRenderer.focalPoint.y + HEIGHT * WorldRenderer.scale / 2);
					holdWorldMousePos.x = MorphMouse.getX();
					holdWorldMousePos.y = MorphMouse.getY();

				}
			}

			if (Mouse.next()) {

				if (Mouse.getEventButton() == 0) {
					if (!Mouse.getEventButtonState()) {
						if (holdMousePos.x != Mouse.getX() || holdMousePos.y != Mouse.getY()) {
							WorldRenderer.focalPoint.add(holdWorldMousePos);
							WorldRenderer.focalPoint.substract(worldMousePos);
							GL11.glMatrixMode(GL11.GL_PROJECTION);
							GL11.glLoadIdentity();
							GLU.gluOrtho2D(WorldRenderer.focalPoint.x - WIDTH * WorldRenderer.scale / 2,
									WorldRenderer.focalPoint.x + WIDTH * WorldRenderer.scale / 2,
									WorldRenderer.focalPoint.y - HEIGHT * WorldRenderer.scale / 2,
									WorldRenderer.focalPoint.y + HEIGHT * WorldRenderer.scale / 2);
						} else {
							pick(MorphMouse.getX(),MorphMouse.getY());
						}
						holdWorldMousePos = null;
						holdMousePos = null;
					} else {
						holdWorldMousePos = worldMousePos;
						holdMousePos = mousePos;
					}
				}

				if (Mouse.getEventButton() == 1 && !Mouse.getEventButtonState() && world.getSelectedShip() != null) {
					for (Morph morph : world.getSelectedShip().getSelectedMorphList()) {
						if (morph.ship.toggleActiveMorph(morph)) {
							if (!morph.disabled) {
								morph.activate();
							}
						} else {
							morph.deactivate();
						}
					}

					if (world.getSelectedShip().getSelectedMorphList().isEmpty()) {
						trackerList.add(new FixedPositionTracker(world.getSelectedShip(), worldMousePos));
					}
				}

				if (Mouse.getEventButton() == 2 && !Mouse.getEventButtonState() && world.getSelectedShip() != null) {
					for (Morph morph : world.getSelectedShip().getMorphList()) {
						if (morph instanceof EmitterMorph) {
							System.out.println("activation");
							Emitting emitting = (Emitting) morph.activableSpecificBehaviorList.get(0);
							emitting.target = new Vect3D(worldMousePos);
							emitting.tryToActivate();
						}
					}
				}

				//				int dWheel = Mouse.getDWheel();
				//				if (dWheel != 0) {
				//					float scale = (float) (Math.pow(1 + Math.pow(4, -5 + Math.abs(dWheel / 120)), Math.signum(dWheel)));
				//					GL11.glScalef(scale, scale, scale);
				//				}
			}

			if (Keyboard.next()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_D && Keyboard.getEventKeyState()) {
					if (WorldRenderer.debugDisplay) {
						WorldRenderer.debugDisplay = false;
						log.info("Graphical debug: Off");
					} else {
						WorldRenderer.debugDisplay = true;
						log.info("Graphical debug: On");
					}
				}
			}

			if (Display.isCloseRequested()) {
				Display.destroy();
				System.exit(0);
			}
		}
	}
}