package net.carmgate.morph.ui.interaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.carmgate.morph.Main;
import net.carmgate.morph.ia.IA;
import net.carmgate.morph.ia.tracker.FixedPositionTracker;
import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.solid.morph.GunMorph;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.ship.Ship;
import net.carmgate.morph.model.solid.world.World;
import net.carmgate.morph.model.user.User.FriendOrFoe;
import net.carmgate.morph.ui.MorphMouse;
import net.carmgate.morph.ui.interaction.action.ShowEvolveMenuAction;
import net.carmgate.morph.ui.interaction.action.SplittingAction;
import net.carmgate.morph.ui.interaction.action.ToggleDebugAction;
import net.carmgate.morph.ui.interaction.action.ToggleFreezeAction;
import net.carmgate.morph.ui.interaction.action.ToggleLockedOnFirstSelectedShip;
import net.carmgate.morph.ui.interaction.action.zoom.ZoomAction;
import net.carmgate.morph.ui.interaction.action.zoom.ZoomInAction;
import net.carmgate.morph.ui.interaction.action.zoom.ZoomOutAction;
import net.carmgate.morph.ui.model.UIModel;
import net.carmgate.morph.ui.model.iwmenu.IWMenuItem;
import net.carmgate.morph.ui.renderer.WorldRenderer;

import org.apache.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class KeyboardAndMouseHandler {

	private static final Logger LOGGER = Logger.getLogger(KeyboardAndMouseHandler.class);

	// Mouse
	private Vect3D holdMousePos = null;

	// Actions
	private final Map<Command, Runnable> keyboardMapping = new HashMap<Command, Runnable>();
	private final ZoomAction zoomAction = new ZoomAction();

	// These renderers are used for picking
	private PickingHandler pickingHandler;

	/** true if the user is currently dragging. */
	private boolean isDragging = false;

	/**
	 * The distance (in pixels) the mouse should be dragged to trigger a world translation following the mouse pointer.
	 */
	private static final int MIN_MOVE_FOR_DRAG = 5;

	public KeyboardAndMouseHandler() {
		pickingHandler = new PickingHandler();

		initKeyboardMapping();
	}

	private void initKeyboardMapping() {
		keyboardMapping.put(new Command(Keyboard.KEY_D, null), new ToggleDebugAction());
		keyboardMapping.put(new Command(Keyboard.KEY_E, null), new ShowEvolveMenuAction());
		keyboardMapping.put(new Command(Keyboard.KEY_L, null), new ToggleLockedOnFirstSelectedShip());
		keyboardMapping.put(new Command(Keyboard.KEY_S, new Integer[] { Keyboard.KEY_LCONTROL }), new SplittingAction());
		keyboardMapping.put(new Command(Keyboard.KEY_6, null), new ZoomInAction(zoomAction));
		keyboardMapping.put(new Command(Keyboard.KEY_EQUALS, null), new ZoomOutAction(zoomAction));
		keyboardMapping.put(new Command(Keyboard.KEY_PAUSE, null), new ToggleFreezeAction());
	}

	/**
	 * 
	 */
	private void processKeyboard() {
		List<Integer> modifiers = new ArrayList<Integer>();

		// Only process release key events
		if (Keyboard.next() && Keyboard.getEventKeyState()) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
				modifiers.add(Keyboard.KEY_LCONTROL);
			}

			Command command;
			if (modifiers.size() == 0) {
				command = new Command(Keyboard.getEventKey(), null);
			} else {
				command = new Command(Keyboard.getEventKey(), modifiers.toArray(new Integer[0]));
			}

			Runnable action = keyboardMapping.get(command);
			if (action != null) {
				action.run();
			}
		}
	}

	/**
	 * @param main TODO
	 * 
	 */
	public void processKeyboardAndMouse() {
		// Get mouse position in world coordinates
		Vect3D worldMousePos = new Vect3D(MorphMouse.getX(), MorphMouse.getY(), 0);

		processMouse(worldMousePos);
		processKeyboard();

		// If a zoom has been demanded, we must call pursue to smooth it.
		zoomAction.pursue();
	}

	/**
	 * @param worldMousePos
	 */
	private void processMouse(Vect3D worldMousePos) {
		if (!isDragging && World.lockedOnFirstSelectedShip
				&& UIModel.getUiModel().getSelectionModel().getSelectedShips().values().size() > 0) {
			WorldRenderer.focalPoint.copy(UIModel.getUiModel().getSelectionModel().getSelectedShips().values().iterator().next().getPos());
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			GLU.gluOrtho2D(WorldRenderer.focalPoint.x - Main.WIDTH / 2 * WorldRenderer.scale,
					WorldRenderer.focalPoint.x + Main.WIDTH / 2 * WorldRenderer.scale,
					WorldRenderer.focalPoint.y + Main.HEIGHT / 2 * WorldRenderer.scale,
					WorldRenderer.focalPoint.y - Main.HEIGHT / 2 * WorldRenderer.scale);
		}

		// Handling world moving around by drag and dropping the world.
		// This portion of code is meant to allow the engine to show the world properly
		// while it's being dragged.
		if (holdMousePos != null) {
			if (isDragging || Math.abs(holdMousePos.x - MorphMouse.getX()) > KeyboardAndMouseHandler.MIN_MOVE_FOR_DRAG
					|| Math.abs(holdMousePos.y - MorphMouse.getY()) > KeyboardAndMouseHandler.MIN_MOVE_FOR_DRAG) {
				WorldRenderer.focalPoint.add(holdMousePos);
				WorldRenderer.focalPoint.substract(worldMousePos);
				GL11.glMatrixMode(GL11.GL_PROJECTION);
				GL11.glLoadIdentity();
				GLU.gluOrtho2D(WorldRenderer.focalPoint.x - Main.WIDTH / 2 * WorldRenderer.scale,
						WorldRenderer.focalPoint.x + Main.WIDTH / 2 * WorldRenderer.scale,
						WorldRenderer.focalPoint.y + Main.HEIGHT / 2 * WorldRenderer.scale,
						WorldRenderer.focalPoint.y - Main.HEIGHT / 2 * WorldRenderer.scale);
				holdMousePos.x = MorphMouse.getX();
				holdMousePos.y = MorphMouse.getY();

				// we are dragging the world
				isDragging = true;
			}
		}

		// If a mouse event has fired, Mouse.next() returns true.
		if (Mouse.next()) {

			// Event button == 0 : Left button related event
			if (Mouse.getEventButton() == 0) {
				// if event button state is false, the button is being released
				if (!Mouse.getEventButtonState()) {
					if (isDragging || Math.abs(holdMousePos.x - MorphMouse.getX()) > KeyboardAndMouseHandler.MIN_MOVE_FOR_DRAG
							|| Math.abs(holdMousePos.y - MorphMouse.getY()) > KeyboardAndMouseHandler.MIN_MOVE_FOR_DRAG) {
						WorldRenderer.focalPoint.add(holdMousePos);
						WorldRenderer.focalPoint.substract(worldMousePos);
						GL11.glMatrixMode(GL11.GL_PROJECTION);
						GL11.glLoadIdentity();
						GLU.gluOrtho2D(WorldRenderer.focalPoint.x - Main.WIDTH / 2 * WorldRenderer.scale,
								WorldRenderer.focalPoint.x + Main.WIDTH / 2 * WorldRenderer.scale,
								WorldRenderer.focalPoint.y + Main.HEIGHT / 2 * WorldRenderer.scale,
								WorldRenderer.focalPoint.y - Main.HEIGHT / 2 * WorldRenderer.scale);
						isDragging = false;
					} else {
						select(pickingHandler.pick(MorphMouse.getX(), MorphMouse.getY()));
					}
					holdMousePos = null;
				} else {
					// the mouse left button is being pressed
					holdMousePos = worldMousePos;
				}
			}

			// Event button == 1 : Right button related event
			if (Mouse.getEventButton() == 1 && !Mouse.getEventButtonState() && UIModel.getUiModel().getSelectionModel().getSelectedShips().size() > 0) {

				Object pickedObject = pickingHandler.pick(MorphMouse.getX(), MorphMouse.getY(), Morph.class);
				if (pickedObject instanceof Morph && FriendOrFoe.FOE.equals(((Morph) pickedObject).getShip().getOwner().getFriendOrFoe())) {
					Morph morph = (Morph) pickedObject;
					LOGGER.trace("Start firing");

					for (Ship s : UIModel.getUiModel().getSelectionModel().getSelectedShips().values()) {
						for (Morph m : s.getMorphsByIds().values()) {
							if (m instanceof GunMorph) {
								((GunMorph) m).setTarget(morph);
								m.tryToActivate();
							}
						}
					}
				} else {
					LOGGER.trace("Number of selected morphs: " + UIModel.getUiModel().getSelectionModel().getSelectedMorphs().size());
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
						// AI list
						if (!foundATracker) {
							iaList.add(new FixedPositionTracker(selectedShip, worldMousePos));
						}
					}
				}
			}

			// TODO Should be reworked
			int dWheel = Mouse.getDWheel();
			if (dWheel > 0) {
				zoomAction.zoomIn();
			} else if (dWheel < 0) {
				zoomAction.zoomOut();
			}
		}
	}

	public void select(Object pickedObject) {

		if (pickedObject == null) {
			UIModel.getUiModel().getSelectionModel().clearAllSelections();
			UIModel.getUiModel().setCurrentInWorldMenu(null);
			return;
		}

		if (pickedObject instanceof Morph) {
			Morph morph = (Morph) pickedObject;
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

		if (pickedObject instanceof Ship) {
			Ship selectedShip = (Ship) pickedObject;

			// Replace the ship in the selection if the owner is "Me" (the player)
			// TODO #80
			if (UIModel.getUiModel().getSelectionModel().getSelectedShips().size() > 0) {
				UIModel.getUiModel().getSelectionModel().removeAllShipsFromSelection();
			}

			if (FriendOrFoe.SELF.equals(selectedShip.getOwner().getFriendOrFoe())) {
				UIModel.getUiModel().getSelectionModel().addShipToSelection(selectedShip);
			}
		}

		// pick in-world menu items
		if (pickedObject instanceof IWMenuItem) {
			IWMenuItem menuItem = (IWMenuItem) pickedObject;

			UIModel.getUiModel().getSelectionModel().addIWMenuItemToSelection(menuItem);
		}
	}
}
