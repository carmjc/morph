package net.carmgate.morph.ui.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.ship.Ship;
import net.carmgate.morph.ui.model.iwmenu.IWMenuItem;
import net.carmgate.morph.util.collections.ModifiableList;
import net.carmgate.morph.util.collections.ModifiableMap;

import org.apache.log4j.Logger;

public class SelectionModel {

	private static final Logger LOGGER = Logger.getLogger(SelectionModel.class);

	/** selected morphs. */
	private final ModifiableMap<Integer, Morph> selectedMorphs = new ModifiableMap<Integer, Morph>(new HashMap<Integer, Morph>());

	/** selected ships. */
	private final ModifiableMap<Integer, Ship> selectedShips = new ModifiableMap<Integer, Ship>(new HashMap<Integer, Ship>());

	/** selected in-world menu items. */
	private final ModifiableMap<Integer, IWMenuItem> selectedIWMenuItems = new ModifiableMap<Integer, IWMenuItem>(new HashMap<Integer, IWMenuItem>());

	/** Selection Listeners. */
	private ModifiableList<SelectionListener> selectionListeners = new ModifiableList<SelectionListener>(new ArrayList<SelectionListener>());

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

	public void addMorphToSelection(Morph morph) {
		if (morph.isSelectable()) {
			selectedMorphs.put(morph.getId(), morph);
			fireMorphSelected(morph);
		}
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
		selectedShips.put(ship.getId(), ship);
		fireShipSelected(ship);
	}

	/**
	 * Clears all selections (ships, morphs, etc.).
	 */
	public void clearAllSelections() {
		removeAllMorphsFromSelection();
		removeAllShipsFromSelection();
		removeAllIWMenuItemsFromSelection();
	}

	private void fireIWMenuItemDeselected(IWMenuItem inWorldMenuItem) {
		selectionListeners.lock();
		for (SelectionListener l : selectionListeners) {
			l.iwMenuItemDeselected(new SelectionEvent(inWorldMenuItem));
		}
		selectionListeners.unlock();
	}

	private void fireIWMenuItemSelected(IWMenuItem inWorldMenuItem) {
		selectionListeners.lock();
		for (SelectionListener l : selectionListeners) {
			l.iwMenuItemSelected(new SelectionEvent(inWorldMenuItem));
		}
		selectionListeners.unlock();
	}

	/**
	 * @param morph
	 */
	private void fireMorphDeselected(Morph morph) {
		selectionListeners.lock();
		for (SelectionListener l : selectionListeners) {
			l.morphDeselected(new SelectionEvent(morph));
		}
		selectionListeners.unlock();
	}

	/**
	 * @param morph
	 */
	private void fireMorphSelected(Morph morph) {
		selectionListeners.lock();
		for (SelectionListener l : selectionListeners) {
			l.morphSelected(new SelectionEvent(morph));
			LOGGER.trace("morph selected: " + morph);
		}
		selectionListeners.unlock();
	}

	/**
	 * @param ship
	 */
	private void fireShipDeselected(Ship ship) {
		selectionListeners.lock();
		for (SelectionListener l : selectionListeners) {
			l.shipDeselected(new SelectionEvent(ship));
		}
		selectionListeners.unlock();
	}

	/**
	 * @param ship
	 */
	private void fireShipSelected(Ship ship) {
		selectionListeners.lock();
		for (SelectionListener l : selectionListeners) {
			l.shipSelected(new SelectionEvent(ship));
		}
		selectionListeners.unlock();
	}

	public Map<Integer, IWMenuItem> getSelectedIWMenuItems() {
		return Collections.unmodifiableMap(selectedIWMenuItems.getMap());
	}

	public Map<Integer, Morph> getSelectedMorphs() {
		return Collections.unmodifiableMap(selectedMorphs.getMap());
	}

	public Map<Integer, Ship> getSelectedShips() {
		return Collections.unmodifiableMap(selectedShips.getMap());
	}

	/**
	 * Removes a morph from the collection of the selected morphs
	 * @param morph the morph to "deselect"
	 */
	public void removeAllIWMenuItemsFromSelection() {
		for (IWMenuItem menuItem : selectedIWMenuItems.values()) {
			selectedIWMenuItems.remove(menuItem.getId());
			fireIWMenuItemDeselected(menuItem);
		}
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
	public void removeAllShipsFromSelection() {
		for (Ship s : selectedShips.values()) {
			selectedShips.remove(s.getId());
			fireShipDeselected(s);
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
		selectionListeners.remove(listener);
	}

}
