package net.carmgate.morph.ui.interaction.actions;

import java.util.Map;

import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.ship.Ship;
import net.carmgate.morph.model.solid.ship.SplittedShip;
import net.carmgate.morph.model.solid.world.World;
import net.carmgate.morph.ui.model.UIModel;

import org.apache.log4j.Logger;

public class SplittingAction implements Runnable {

	private static final Logger LOGGER = Logger.getLogger(SplittingAction.class);

	/**
	 * When splitting, it is most probable that there will be no morph at position (0,0)
	 * Morphs will be shifted from the (0,0) as much as the original morphs were in the original ship.
	 * It will be easier to work with ships whose morphs are reshifted properly. 
	 * @param s the ship whose morphs should be recentered
	 */
	private void recenterShipMorphs(Ship s) {
		if (s.getMorphsByPosInShipGrid().get(new Vect3D(0, 0, 0)) == null) {
			Morph anyMorph = s.getMorphsByPosInShipGrid().values().iterator().next();
			Vect3D translationInWorld = anyMorph.getPosInWorld();
			Vect3D translationInShipGrid = anyMorph.getPosInShipGrid();

			// Translate ship
			s.setPos(translationInWorld);

			// translate all morphs in ship grid
			for (Morph m : s.getMorphsByPosInShipGrid().values()) {
				m.setPosInShipGrid(new Vect3D(
						m.getPosInShipGrid().x - translationInShipGrid.x,
						m.getPosInShipGrid().y - translationInShipGrid.y,
						m.getPosInShipGrid().z - translationInShipGrid.z));
				m.updatePosFromGridPos();
			}
		}
	}

	@Override
	public void run() {
		Map<Integer, Ship> selectedShips = UIModel.getUiModel().getSelectionModel().getSelectedShips();
		Map<Integer, Morph> selectedMorphs = UIModel.getUiModel().getSelectionModel().getSelectedMorphs();

		if (selectedShips.size() == 1 && selectedMorphs.size() > 0) {
			Ship selectedShip = selectedShips.values().iterator().next();

			// Remove the morphs from the source ship
			selectedShip.removeMorphs(selectedMorphs.values());

			// Create a new ship and add the selected morphs to it
			Ship newShip = new SplittedShip(selectedShip, selectedMorphs.values());

			// Replace a morph at (0,0) in ship coordinates for each ship
			recenterShipMorphs(newShip);
			recenterShipMorphs(selectedShip);

			// Refresh the ships
			selectedShip.computeCOM();
			newShip.computeCOM();
			for (Morph m : newShip.getMorphsByIds().values()) {
				selectedShip.computeRadiusIncremental(m, false);
				newShip.computeRadiusIncremental(m, true);
			}

			World.getWorld().getShips().put(newShip.getId(), newShip);
			UIModel.getUiModel().getSelectionModel().removeAllShipsFromSelection();
			UIModel.getUiModel().getSelectionModel().addShipToSelection(newShip);
		}
	}

}
