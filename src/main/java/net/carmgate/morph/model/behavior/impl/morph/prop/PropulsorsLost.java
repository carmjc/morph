package net.carmgate.morph.model.behavior.impl.morph.prop;

import net.carmgate.morph.model.ModelConstants;
import net.carmgate.morph.model.State;
import net.carmgate.morph.model.behavior.NoActivationBehavior;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.ship.listener.ShipEvent;
import net.carmgate.morph.model.solid.ship.listener.ShipListener;

import org.apache.log4j.Logger;

/**
 * This behavior adds heavy drag on the ship.
 * <p>This behavior is intended to be used when a ship has lost all its propulsors to prevent
 * the ship being sent in a distant galaxy in case of energy drain.</p>
 * <p>This might be abandonned if we find a way to take energy drain into account in the tracker IAs 
 */
public class PropulsorsLost extends NoActivationBehavior<Morph> {

	private static final Logger LOGGER = Logger.getLogger(PropulsorsLost.class);

	public PropulsorsLost(Morph owner) {
		super(owner, State.ACTIVE);
	}

	@Override
	protected boolean activate() {
		getOwner().getShip().setDragFactor(ModelConstants.SLOW_DOWN_FACTOR);

		// Add a listener so that, when the ship stops, the behavior is removed from the ship.
		getOwner().getShip().addShipListener(new ShipListener() {
			@Override
			public void shipStopped(ShipEvent event) {
				// FIXME This test should not be necessary
				if (event.getShip() == getOwner().getShip()) {
					PropulsorsLost.this.tryToDeactivate();
				}
			}
		});

		LOGGER.debug("PropulsorsLost activated.");

		return true;
	}

	@Override
	protected boolean deactivate(boolean forced) {
		// reset drag factor to the original value before being tempered with by this behavior.
		getOwner().getShip().resetDragFactor();

		// Removing the behavior from the parent morph once it's done its job.
		// TODO Careful with this, if the propulsor is removed before it's executed, there might be an issue
		// TODO Maybe the getActivationIsolatedBehaviorList() method should not be accessible like that.
		getOwner().getActivationIsolatedBehaviorList().remove(this);

		LOGGER.debug("Propulsor lost deactivated.");

		return true;
	}

	@Override
	protected void execute() {
		// does nothing
	}

}
