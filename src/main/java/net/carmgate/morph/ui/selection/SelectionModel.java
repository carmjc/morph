package net.carmgate.morph.ui.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.ship.Ship;
import net.carmgate.morph.ui.model.menu.IWMenuItem;

import org.apache.log4j.Logger;

public class SelectionModel {

	private static final Logger LOGGER = Logger.getLogger(SelectionModel.class);

	/** selected morphs. */
	private final Map<Integer, Morph> selectedMorphs = new HashMap<Integer, Morph>();

	/** selected ships. */
	private final Map<Integer, Ship> selectedShips = new HashMap<Integer, Ship>();

	/** selected in-world menu items. */
	private final Map<Integer, IWMenuItem> selectedIWMenuItems = new HashMap<Integer, IWMenuItem>();

	/** Selection Listeners. */
	private List<SelectionListener> selectionListeners = new ArrayList<SelectionListener>();

	/** Selection Listeners during locks. */
	private List<SelectionListener> selectionListenersAddedDuringLock = new ArrayList<SelectionListener>();
	private List<SelectionListener> selectionListenersRemovedDuringLock = new ArrayList<SelectionListener>();

	private static int selectionListenersLock = 0;

	public SelectionModel() {
	}

	/**
	 * Add an {@link IWMenuItem} to the selected in-world menu items.
	 * @param iwMenuItem
	 */
	public void addIWMenuItemToSelection(IWMenuItem iwMenuItem) {
		selectedIWMenuItems.put(iwMenuItem.getId(), iwMenuItem);
		fireIWMenuItemSelected(iwMenuItem);
	}

	/**
	 * Add a morph to the selected morph collection
	 * @param morph
	 */
	public void addMorphToSelection(Morph morph) {
		selectedMorphs.put(morph.getId(), morph);
		fireMorphSelected(morph);
	}

	/**
	 * Adds a selection listener
	 * @param listener
	 */
	public void addSelectionListener(SelectionListener listener) {
		if (selectionListenersLock == 0) {
			selectionListeners.add(listener);
		} else {
			selectionListenersAddedDuringLock.add(listener);
		}
	}

	/**
	 * Add a ship to the selected ship collection
	 * @param ship
	 */
	public void addShipToSelection(Ship ship) {
		selectedShips.put(ship.getId(), ship);
		fireShipSelected(ship);
	}

	/**
	 * Clears all selections (ships, morphs, etc.).
	 */
	public void clearAllSelections() {
		selectedMorphs.clear();
		selectedShips.clear();
	}

	private void fireIWMenuItemSelected(IWMenuItem inWorldMenuItem) {
		lockSelectionListeners();
		for (SelectionListener l : selectionListeners) {
			l.inWorldMenuItemSelected(new SelectionEvent(inWorldMenuItem));
		}
		unlockSelectionListeners();
	}

	/**
	 * @param morph
	 */
	private void fireMorphDeselected(Morph morph) {
		for (SelectionListener l : selectionListeners) {
			l.morphDeselected(new SelectionEvent(morph));
		}
	}

	/**
	 * @param morph
	 */
	private void fireMorphSelected(Morph morph) {
		lockSelectionListeners();
		for (SelectionListener l : selectionListeners) {
			l.morphSelected(new SelectionEvent(morph));
			LOGGER.trace("morph selected: " + morph);
		}
		unlockSelectionListeners();
	}

	/**
	 * @param ship
	 */
	private void fireShipSelected(Ship ship) {
		lockSelectionListeners();
		for (SelectionListener l : selectionListeners) {
			l.shipSelected(new SelectionEvent(ship));
		}
		unlockSelectionListeners();
	}

	public Map<Integer, IWMenuItem> getSelectedIWMenuItems() {
		return Collections.unmodifiableMap(selectedIWMenuItems);
	}

	public Map<Integer, Morph> getSelectedMorphs() {
		return Collections.unmodifiableMap(selectedMorphs);
	}

	public Map<Integer, Ship> getSelectedShips() {
		return Collections.unmodifiableMap(selectedShips);
	}

	/**
	 * Increments a counter telling how many loops are iterating over the selectionListeners list.
	 * This is used to avoid concurrent modification exceptions
	 */
	private void lockSelectionListeners() {
		selectionListenersLock++;
	}

	/**
	 * Removes a morph from the collection of the selected morphs
	 * @param morph the morph to "deselect"
	 */
	public void removeAllMorphsFromSelection() {
		for (Morph m : selectedMorphs.values()) {
			selectedMorphs.remove(m.getId());
			fireMorphDeselected(m);
		}
	}

	/**
	 * Removes a morph from the collection of the selected morphs
	 * @param morph the morph to "deselect"
	 */
	public void removeMorphFromSelection(Morph morph) {
		selectedMorphs.remove(morph.getId());
		fireMorphDeselected(morph);
	}

	/**
	 * Removes a selection listener
	 * @param listener
	 */
	public void removeSelectionListener(SelectionListener listener) {
		if (selectionListenersLock == 0) {
			selectionListeners.remove(listener);
		} else {
			selectionListenersRemovedDuringLock.add(listener);
		}
	}

	/**
	 * Decrements a counter telling how many loops are iterating over the selectionListeners list.
	 * This is used to avoid concurrent modification exceptions
	 */
	private void unlockSelectionListeners() {
		selectionListenersLock--;
		if (selectionListenersLock == 0) {
			selectionListeners.addAll(selectionListenersAddedDuringLock);
			selectionListenersAddedDuringLock.clear();
			selectionListeners.removeAll(selectionListenersRemovedDuringLock);
			selectionListenersRemovedDuringLock.clear();
		}
	}

}
