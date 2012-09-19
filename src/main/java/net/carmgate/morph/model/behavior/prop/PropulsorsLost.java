package net.carmgate.morph.model.behavior.prop;

import net.carmgate.morph.model.behavior.NoActivationBehavior;
import net.carmgate.morph.model.behavior.State;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.ship.listener.ShipEvent;
import net.carmgate.morph.model.solid.ship.listener.ShipListener;

import org.apache.log4j.Logger;

public class PropulsorsLost extends NoActivationBehavior<Morph> {

	private static final float SLOW_DOWN_FACTOR = 0.1f;
	private static final Logger LOGGER = Logger.getLogger(PropulsorsLost.class);

	public PropulsorsLost(Morph owner) {
		super(owner, State.ACTIVE);
	}

	@Override
	protected boolean activate() {
		getOwner().getShip().setDragFactor(SLOW_DOWN_FACTOR);

		// Add a listener so that, when the ship stops, the behavior is removed from the ship.
		getOwner().getShip().addShipListeners(new ShipListener() {
			public void shipStopped(ShipEvent event) {
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
		// TODO Maybe the getAlternateBehaviorList() method should not be accessible like that.
		getOwner().getAlternateBehaviorList().remove(this);

		LOGGER.debug("Propulsor lost deactivated.");

		return true;
	}

	@Override
	protected void execute() {
		// does nothing
	}

}
