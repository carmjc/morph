package net.carmgate.morph.ui.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.carmgate.morph.model.solid.WorldPositionSupport;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.ship.Ship;
import net.carmgate.morph.ui.model.iwmenu.IWMenuItem;
import net.carmgate.morph.util.collections.ModifiableIterable;
import net.carmgate.morph.util.collections.ModifiableMap;

import org.apache.log4j.Logger;

public class SelectionModel {

	private static final Logger LOGGER = Logger.getLogger(SelectionModel.class);

	/** selected morphs. */
	private final ModifiableMap<Long, Morph> selectedMorphs = new ModifiableMap<Long, Morph>(new HashMap<Long, Morph>());

	/** selected ships. */
	private final ModifiableMap<Long, Ship> selectedShips = new ModifiableMap<Long, Ship>(new HashMap<Long, Ship>());

	/** selected in-world menu items. */
	private final ModifiableMap<Long, IWMenuItem> selectedIWMenuItems = new ModifiableMap<Long, IWMenuItem>(new HashMap<Long, IWMenuItem>());

	/** Selection Listeners. */
	private ModifiableIterable<SelectionListener> selectionListeners = new ModifiableIterable<SelectionListener>(new ArrayList<SelectionListener>());

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
	private void fireShipDeselected(WorldPositionSupport ship) {
		selectionListeners.lock();
		for (SelectionListener l : selectionListeners) {
			l.shipDeselected(new SelectionEvent(ship));
		}
		selectionListeners.unlock();
	}

	/**
	 * @param ship
	 */
	private void fireShipSelected(WorldPositionSupport ship) {
		selectionListeners.lock();
		for (SelectionListener l : selectionListeners) {
			l.shipSelected(new SelectionEvent(ship));
		}
		selectionListeners.unlock();
	}

	public Map<Long, IWMenuItem> getSelectedIWMenuItems() {
		return Collections.unmodifiableMap(selectedIWMenuItems.getMap());
	}

	public Map<Long, Morph> getSelectedMorphs() {
		return Collections.unmodifiableMap(new HashMap<Long, Morph>(selectedMorphs.getMap()));
	}

	public Map<Long, Ship> getSelectedShips() {
		return Collections.unmodifiableMap(selectedShips.getMap());
	}

	/**
	 * Removes a morph from the collection of the selected morphs
	 * @param morph the morph to "deselect"
	 */
	public void removeAllIWMenuItemsFromSelection() {
		selectedIWMenuItems.lock();
		for (IWMenuItem menuItem : selectedIWMenuItems.values()) {
			selectedIWMenuItems.remove(menuItem.getId());
			fireIWMenuItemDeselected(menuItem);
		}
		selectedIWMenuItems.unlock();
	}

	/**
	 * Removes a morph from the collection of the selected morphs
	 * @param morph the morph to "deselect"
	 */
	public void removeAllMorphsFromSelection() {
		selectedMorphs.lock();
		for (Morph m : selectedMorphs.values()) {
			selectedMorphs.remove(m.getId());
			fireMorphDeselected(m);
		}
		selectedMorphs.unlock();
	}

	/**
	 * Removes a morph from the collection of the selected morphs
	 * @param morph the morph to "deselect"
	 */
	public void removeAllShipsFromSelection() {
		selectedShips.lock();
		for (Ship s : selectedShips.values()) {
			selectedShips.remove(s.getId());
			fireShipDeselected(s);
		}
		selectedShips.unlock();
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
