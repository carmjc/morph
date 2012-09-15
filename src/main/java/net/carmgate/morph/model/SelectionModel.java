package net.carmgate.morph.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.carmgate.morph.model.morph.Morph;
import net.carmgate.morph.model.selection.SelectionEvent;
import net.carmgate.morph.model.selection.SelectionListener;
import net.carmgate.morph.model.ship.Ship;

public class SelectionModel {

	/** selected morphs. */
	private Map<Integer, Morph> selectedMorphs = new HashMap<Integer, Morph>();

	/** selected ships. */
	private Map<Integer, Ship> selectedShips = new HashMap<Integer, Ship>();

	/** Selection Listeners. */
	private List<SelectionListener> selectionListeners = new ArrayList<SelectionListener>();

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
		selectionListeners.add(listener);
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
			l.morphSelected(new SelectionEvent(morph));
		}
	}

	/**
	 * @param morph
	 */
	private void fireMorphSelected(Morph morph) {
		for (SelectionListener l : selectionListeners) {
			l.morphSelected(new SelectionEvent(morph));
		}
	}

	/**
	 * @param ship
	 */
	private void fireShipSelected(Ship ship) {
		for (SelectionListener l : selectionListeners) {
			l.shipSelected(new SelectionEvent(ship));
		}
	}

	public Map<Integer, Morph> getSelectedMorphs() {
		return selectedMorphs;
	}

	public Map<Integer, Ship> getSelectedShips() {
		return selectedShips;
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

}
