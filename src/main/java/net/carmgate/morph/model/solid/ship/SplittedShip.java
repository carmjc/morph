package net.carmgate.morph.model.solid.ship;

import java.util.Collection;

import net.carmgate.morph.model.behavior.listener.BehaviorEvent;
import net.carmgate.morph.model.behavior.listener.BehaviorListener;
import net.carmgate.morph.model.behavior.prop.PropulsorsLost;
import net.carmgate.morph.model.solid.morph.Morph;

import org.apache.log4j.Logger;

public class SplittedShip extends Ship {

	private static final Logger LOGGER = Logger.getLogger(SplittedShip.class);

	public SplittedShip(Ship ship, Collection<Morph> selectedMorphs) {
		super(ship.getPos().x, ship.getPos().y, ship.getPos().z, ship.getOwner());
		setPosSpeed(ship.getPosSpeed());
		// setPosAccel(ship.getPosAccel());
		setRot(ship.getRot());
		setRotSpeed(ship.getRotSpeed());
		setRotAccel(ship.getRotAccel());
		addMorphs(selectedMorphs);

		final Morph anyMorph = getMorphsByIds().values().iterator().next();
		final PropulsorsLost behavior = new PropulsorsLost(anyMorph);
		anyMorph.getAlternateBehaviorList().add(behavior);
		behavior.addBehaviorListener(new BehaviorListener() {
			@Override
			public void behaviorDeactivated(BehaviorEvent behaviorEvent) {
				anyMorph.getAlternateBehaviorList().remove(behavior);
				LOGGER.debug("deactivated");
			}
		});
	}
}
