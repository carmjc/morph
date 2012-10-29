package net.carmgate.morph.ia.impl.user;

import net.carmgate.morph.ia.AI;
import net.carmgate.morph.ia.impl.morph.fight.GoToShipAndEngage;
import net.carmgate.morph.model.solid.ship.Ship;
import net.carmgate.morph.model.solid.world.World;
import net.carmgate.morph.model.user.User;

/**
 * This is a very simple cheating AI.
 * <p>It goes through the ship list of the world, picks the first non-self ship and
 * engages it</p>
 */
public class FightAnyOpponentShip implements AI<User> {

	private User user;
	private Ship currentOpponentShip;

	public FightAnyOpponentShip(User user) {
		this.user = user;
	}

	@Override
	public void compute() {
		// A very simple AI
		boolean newShip = false;
		if (currentOpponentShip == null || currentOpponentShip.getMorphsByIds().size() == 0) {
			currentOpponentShip = findOpponentShip();
			newShip = true;
		}

		// iterate over the user's ships and send them all engage the opponent ship
		for (Ship ship : World.getWorld().getShips().values()) {
			if (ship.getOwner().equals(user)) {
				GoToShipAndEngage foundAI = null;
				for (AI ai : ship.getAIList()) {
					if (ai instanceof GoToShipAndEngage) {
						foundAI = (GoToShipAndEngage) ai;
					}
				}

				// FIXME This way of getting the targetted morph is dangerous
				if (foundAI == null) {
					foundAI = new GoToShipAndEngage(ship, currentOpponentShip.getMorphsByIds().values().iterator().next());
					ship.getAIList().add(foundAI);
				} else if (newShip) {
					foundAI.setTarget(currentOpponentShip.getMorphsByIds().values().iterator().next());
				}
			}
		}
	}

	@Override
	public boolean done() {
		// We are never done
		return false;
	}

	/**
	 * @return an opponent ship.
	 */
	private Ship findOpponentShip() {
		Ship opponentShip = null;
		for (Ship ship : World.getWorld().getShips().values()) {
			if (!ship.getOwner().equals(user)) {
				opponentShip = ship;
				break;
			}
		}
		return opponentShip;
	}

}
