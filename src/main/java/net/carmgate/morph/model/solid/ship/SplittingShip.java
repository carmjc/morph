package net.carmgate.morph.model.solid.ship;

import java.util.Collection;

import net.carmgate.morph.model.behavior.impl.morph.prop.PropulsorsLost;
import net.carmgate.morph.model.behavior.listener.BehaviorEvent;
import net.carmgate.morph.model.behavior.listener.BehaviorListener;
import net.carmgate.morph.model.solid.morph.Morph;

import org.apache.log4j.Logger;

/**
 * When splitting a ship in two part, we need a new ship to hold the morphs separated from the
 * original ship.
 */
public class SplittingShip extends Ship {

	private static final Logger LOGGER = Logger.getLogger(SplittingShip.class);

	/**
	 * Use this constructor to create a new ship containing the provided morphs.
	 * <p>The new ship is initialized with the provided morph and those morphs are removed from the
	 * original ship.</p>
	 * @param ship the source ship.
	 * @param selectedMorphs the morphs that should be removed from the source ship
	 * and added to the new ship.
	 */
	public SplittingShip(Ship ship, Collection<Morph> selectedMorphs) {
		super(ship.getPos().x, ship.getPos().y, ship.getPos().z, ship.getOwner());

		// Copy movement properties on the new ship.
		// It is not necessary to set accel as well since accel is reset each cycle.
		setPosSpeed(ship.getPosSpeed());
		setRot(ship.getRot());
		setRotSpeed(ship.getRotSpeed());
		setRotAccel(ship.getRotAccel());
		addMorphs(selectedMorphs);

		// FIXME rework this once behaviors are handled by ships.
		final Morph anyMorph = getMorphsByIds().values().iterator().next();
		final PropulsorsLost behavior = new PropulsorsLost(anyMorph);
		anyMorph.getActivationIsolatedBehaviorList().add(behavior);
		behavior.addBehaviorListener(new BehaviorListener() {
			@Override
			public void behaviorDeactivated(BehaviorEvent behaviorEvent) {
				anyMorph.getActivationIsolatedBehaviorList().remove(behavior);
				LOGGER.trace("deactivated");
			}
		});
	}
}
