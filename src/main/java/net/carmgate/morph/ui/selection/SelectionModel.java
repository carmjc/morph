package net.carmgate.morph.ui.selection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.ship.Ship;

import org.apache.log4j.Logger;

public class SelectionModel {

	private static final Logger LOGGER = Logger.getLogger(SelectionModel.class);

	/** selected morphs. */
	private Map<Integer, Morph> selectedMorphs = new HashMap<Integer, Morph>();

	/** selected ships. */
	private Map<Integer, Ship> selectedShips = new HashMap<Integer, Ship>();

	/** Selection Listeners. */
	private List<SelectionListener> selectionListeners = new ArrayList<SelectionListener>();
	/** Selection Listeners. */
	private List<SelectionListener> selectionListenersDuringLocks = new ArrayList<SelectionListener>();

	private static int selectionListenersLock = 0;

	public SelectionModel() {
	}

	/**
	 * Add a morph to the selected morph collection
	 * @param morph
	 */
	public void addMorphToSelection(Morph morph) {
		getSelectedMorphs().put(morph.getId(), morph);
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
			selectionListenersDuringLocks.add(listener);
		}
	}

	/**
	 * Add a ship to the selected ship collection
	 * @param ship
	 */
	public void addShipToSelection(Ship ship) {
		getSelectedShips().put(ship.getId(), ship);
		fireShipSelected(ship);
	}

	/**
	 * Clears all selections (ships, morphs, etc.).
	 */
	public void clearAllSelections() {
		getSelectedMorphs().clear();
		getSelectedShips().clear();
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

	public Map<Integer, Morph> getSelectedMorphs() {
		return selectedMorphs;
	}

	public Map<Integer, Ship> getSelectedShips() {
		return selectedShips;
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
		for (Morph m : getSelectedMorphs().values()) {
			getSelectedMorphs().remove(m.getId());
			fireMorphDeselected(m);
		}
	}

	/**
	 * Removes a morph from the collection of the selected morphs
	 * @param morph the morph to "deselect"
	 */
	public void removeMorphFromSelection(Morph morph) {
		getSelectedMorphs().remove(morph.getId());
		fireMorphDeselected(morph);
	}

	/**
	 * Removes a selection listener
	 * @param listener
	 */
	public void removeSelectionListener(SelectionListener listener) {
		selectionListeners.remove(listener);
	}

	/**
	 * Decrements a counter telling how many loops are iterating over the selectionListeners list.
	 * This is used to avoid concurrent modification exceptions
	 */
	private void unlockSelectionListeners() {
		selectionListenersLock--;
		if (selectionListenersLock == 0) {
			selectionListeners.addAll(selectionListenersDuringLocks);
			selectionListenersDuringLocks.clear();
		}
	}

}
