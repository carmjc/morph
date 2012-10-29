package net.carmgate.morph;

import net.carmgate.morph.model.solid.world.World;
import net.carmgate.morph.ui.interaction.KeyboardAndMouseHandler;
import net.carmgate.morph.ui.renderer.Renderer.RenderStyle;
import net.carmgate.morph.ui.renderer.RendererHolder;
import net.carmgate.morph.ui.renderer.WorldRenderer;

import org.apache.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class Main {

	/** 
	 * Picking contexts.
	 */
	public enum PickingContext {
		IW_MENU(1),
		SHIP(2),
		ASTEROID(1);

		private final int depth;

		private PickingContext(int depth) {
			this.depth = depth;
		}

		public final int getDepth() {
			return depth;
		}
	}

	@SuppressWarnings("unused")
	private static Logger LOGGER = Logger.getLogger(Main.class);

	// public static final float SCALE_FACTOR = 1f;
	public static final int HEIGHT = 768;
	public static final int WIDTH = 1280;

	/**
	 * Main Class
	 */
	public static void main(String[] argv) {
		Main sample = new Main();
		sample.start();
	}

	// Interactions handler
	private KeyboardAndMouseHandler keyboardAndMouseHandler;

	/**
	 * Initialise the GL display
	 *
	 * @param width The width of the display
	 * @param height The height of the display
	 */
	private void initGL(int width, int height) {
		try {
			// Display.setFullscreen(true);
			DisplayMode[] availableDisplayModes = Display.getAvailableDisplayModes();
			DisplayMode displayMode = null;
			for (DisplayMode mode : availableDisplayModes) {
				if (mode.getBitsPerPixel() == 32 && mode.getHeight() == height && mode.getWidth() == width) {
					displayMode = mode;
					break;
				}
			}
			if (displayMode == null) {
				throw new RuntimeException("There is no matching mode (depth=32, width=" + width + ", height=" + height + ")");
			}
			Display.setDisplayMode(displayMode);
			Display.create();
			Display.setTitle("Morph");
			Display.setVSyncEnabled(true);
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}

		// set clear color - Wont be needed once we have a background
		GL11.glClearColor(0, 0, 0, 0);
		GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
		GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);

		// enable alpha blending
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		// Tests
		// GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GLU.gluOrtho2D(WorldRenderer.focalPoint.x - width / 2 * WorldRenderer.scale,
				WorldRenderer.focalPoint.x + width / 2 * WorldRenderer.scale,
				WorldRenderer.focalPoint.y + height / 2 * WorldRenderer.scale,
				WorldRenderer.focalPoint.y - height / 2 * WorldRenderer.scale);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
	}

	/**
	 * draw a quad with the image on it
	 */
	public void render() {

		// draw world
		int renderType = GL11.GL_RENDER;
		if (WorldRenderer.selectRendering) {
			renderType = GL11.GL_SELECT;
		}
		RendererHolder.worldRenderer.render(renderType, WorldRenderer.debugDisplay ? RenderStyle.DEBUG : RenderStyle.NORMAL, World.getWorld());
		RendererHolder.iwuiRenderer.render(renderType, WorldRenderer.debugDisplay ? RenderStyle.DEBUG : RenderStyle.NORMAL, World.getWorld());

		// Interface rendering
		GL11.glTranslatef(WorldRenderer.focalPoint.x, WorldRenderer.focalPoint.y, WorldRenderer.focalPoint.z);
		RendererHolder.uiRenderer.render(renderType, WorldRenderer.debugDisplay ? RenderStyle.DEBUG : RenderStyle.NORMAL);
		GL11.glTranslatef(-WorldRenderer.focalPoint.x, -WorldRenderer.focalPoint.y, -WorldRenderer.focalPoint.z);

		// update the world
		World.getWorld().update();

	}

	/**
	 * Start the application
	 */
	public void start() {
		initGL(WIDTH, HEIGHT);

		// Initializes the world
		World.getWorld().init();
		keyboardAndMouseHandler = new KeyboardAndMouseHandler();

		// Rendering loop
		while (true) {
			// Renders everything
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			render();

			// updates display and sets frame rate
			Display.update();
			Display.sync(80);

			keyboardAndMouseHandler.processKeyboardAndMouse();

			if (Display.isCloseRequested()) {
				Display.destroy();
				System.exit(0);
			}
		}
	}
}