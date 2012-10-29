package net.carmgate.morph.model.solid.world;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.carmgate.morph.ia.AI;
import net.carmgate.morph.ia.impl.user.FightAnyOpponentShip;
import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.physics.Force;
import net.carmgate.morph.model.solid.energysource.EnergySource;
import net.carmgate.morph.model.solid.energysource.impl.Star;
import net.carmgate.morph.model.solid.mattersource.MatterSource;
import net.carmgate.morph.model.solid.mattersource.impl.Asteroid;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.particle.ParticleEngine;
import net.carmgate.morph.model.solid.ship.Ship;
import net.carmgate.morph.model.solid.ship.test.EnemyTestShip1;
import net.carmgate.morph.model.solid.ship.test.EnemyTestShip2;
import net.carmgate.morph.model.solid.ship.test.TestShip;
import net.carmgate.morph.model.user.User;
import net.carmgate.morph.model.user.User.FriendOrFoe;
import net.carmgate.morph.model.user.User.UserType;
import net.carmgate.morph.model.user.UserFactory;

import org.apache.log4j.Logger;

/**
 * The world has a list of world areas, a ships list, a forces list and a currently selected ship (for now).
 * @author Carm
 */
public class World {

	private static final Logger LOGGER = Logger.getLogger(World.class);

	/** the ratio between world x coordinates and ship grid x coordinates. **/
	public static final int GRID_SIZE = 32;

	/** World singleton instance. */
	private static World worldInstance;

	/** number of millis since game start. */
	private long currentTS = 0;
	/** number of millis since last update. */
	private long sinceLastUpdateTS = 0;

	/** timestamp of game start. */
	private long gameStartMsec = new Date().getTime();

	public static boolean freeze = false;
	public static boolean lockedOnFirstSelectedShip = false;

	public static World getWorld() {
		if (worldInstance == null) {
			worldInstance = new World();
		}
		return worldInstance;
	}

	/** the list of all energy sources in the game. */
	private final Map<Long, EnergySource> energySources = new HashMap<Long, EnergySource>();

	/** the list of all matter sources in the game. */
	private final Map<Long, MatterSource> matterSources = new HashMap<Long, MatterSource>();

	/** the list of all ships in game. */
	private final Map<Long, Ship> ships = new HashMap<Long, Ship>();

	/** the list of opponent AIs. */
	private final List<AI> opponentAIs = new ArrayList<AI>();

	/** A list of forces to show. */
	private final List<Force> forceList = new ArrayList<Force>();

	/** Particle engine. */
	private final ParticleEngine particleEngine = new ParticleEngine();

	private World() {
		// privatized constructor
	}

	/**
	 * @return number of millis since game start.
	 */
	public final long getCurrentTS() {
		return currentTS;
	}

	public final Map<Long, EnergySource> getEnergySources() {
		return energySources;
	}

	public final List<Force> getForceList() {
		return forceList;
	}

	public final Map<Long, MatterSource> getMatterSources() {
		return matterSources;
	}

	/**
	 * @return This world's particle engine.
	 */
	public final ParticleEngine getParticleEngine() {
		return particleEngine;
	}

	public final Map<Long, Ship> getShips() {
		return ships;
	}

	/**
	 * @return the number of millis since last update.
	 */
	public final long getSinceLastUpdateTS() {
		return sinceLastUpdateTS;
	}

	public final void init() {
		// Create users
		UserFactory.addUser(new User(UserType.HUMAN, "Me", FriendOrFoe.SELF, new Color(0, 0, 0)));
		UserFactory.addUser(new User(UserType.AI, "Nemesis", FriendOrFoe.FOE, new Color(150, 50, 50)));
		// UserFactory.addUser(new User(UserType.GOD, "God", FriendOrFoe.FRIEND, new Color(255, 255, 255)));

		// Assign opponent AIs
		opponentAIs.add(new FightAnyOpponentShip(UserFactory.findUser("Nemesis")));

		// Create a star
		EnergySource star = new Star(1000, -400, 0, 5.2f, 3000);
		getEnergySources().put(star.getId(), star);

		// Create an asteroid
		MatterSource asteroid = new Asteroid(-800, 400, 0, 1000);
		getMatterSources().put(asteroid.getId(), asteroid);

		// Create a ship for me
		Ship ship = new TestShip(0, 0, 0, UserFactory.findUser("Me"));
		getShips().put(ship.getId(), ship);

		// Create a ship for the ennemy
		ship = new EnemyTestShip1(280, -300, 0, UserFactory.findUser("Nemesis"));
		// getShips().put(ship.getId(), ship);

		// Create another ship for the ennemy
		ship = new EnemyTestShip2(250, -220, 0, UserFactory.findUser("Nemesis"));
		getShips().put(ship.getId(), ship);
	}

	/**
	 * Update the world.
	 */
	public final void update() {
		// update the number of millis since game start
		long oldTS = currentTS;
		currentTS = new Date().getTime() - gameStartMsec;
		sinceLastUpdateTS = currentTS - oldTS;

		if (World.freeze) {
			gameStartMsec += sinceLastUpdateTS;
			currentTS -= sinceLastUpdateTS;
			return;
		}

		// update ships
		List<Ship> shipsToRemove = null;
		for (Ship ship : getShips().values()) {

			// Remove dead morphs
			// FIXME it should use a morph property. The morph should be responsible for its death status
			List<Morph> morphsToRemove = null;
			for (Morph m : ship.getMorphsByIds().values()) {
				if (m.getMass() <= 0 && !m.getClass().getAnnotation(MorphInfo.class).virtual()) {
					if (morphsToRemove == null) {
						morphsToRemove = new ArrayList<Morph>();
					}
					morphsToRemove.add(m);
				}
			}
			// really remove dead morphs
			ship.removeMorphs(morphsToRemove);

			// Remove dead ships
			if (ship.getMorphsByIds().size() == 0) {
				if (shipsToRemove == null) {
					shipsToRemove = new ArrayList<Ship>();
				}
				shipsToRemove.add(ship);
			} else {
				// FIXME Once energy management behaviors have been created, this should be put
				// before death management
				ship.update();
			}
		}
		// really remove dead ships
		if (shipsToRemove != null) {
			getShips().values().removeAll(shipsToRemove);
		}

		LOGGER.trace(getShips());

		// update particles
		particleEngine.update();

		// Remove empty matter sources
		for (Iterator<MatterSource> i = matterSources.values().iterator(); i.hasNext();) {
			MatterSource m = i.next();
			if (m.getMass() <= 0) {
				i.remove();
			}
		}

		// udpate ship IAs
		// TODO This code should evolve. AIs might target non-ship elements
		for (Ship ship : World.getWorld().getShips().values()) {
			List<AI<Ship>> iasToRemove = new ArrayList<AI<Ship>>();
			ship.getAIList().lock();
			for (AI<Ship> ia : ship.getAIList()) {
				if (ia != null) {
					if (ia.done()) {
						iasToRemove.add(ia);
						LOGGER.trace("Removing Ai: " + ia.getClass().getName());
					} else {
						ia.compute();
					}
				}
			}
			ship.getAIList().unlock();
			for (AI<Ship> ia : iasToRemove) {
				ship.getAIList().remove(ia);
			}
			iasToRemove.clear();
		}

		// update opponent AIs
		for (AI<User> ai : opponentAIs) {
			ai.compute();
		}
	}
}
