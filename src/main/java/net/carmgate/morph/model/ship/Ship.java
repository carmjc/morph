package net.carmgate.morph.model.ship;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.carmgate.morph.ia.IA;
import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.World;
import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.morph.Morph;
import net.carmgate.morph.model.morph.stem.StemMorph;
import net.carmgate.morph.model.virtual.physics.Force;

import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;

/**
 * TODO : Il faut ajouter un centre d'inertie et modifier les calculs des forces pour g�rer le vrai centre d'inertie.
 */
public abstract class Ship {

	private static final class IsPartOfShipPredicate implements Predicate {

		private Ship ship;

		public IsPartOfShipPredicate(Ship ship) {
			this.ship = ship;
		}

		public boolean evaluate(Object object) {
			Morph morph = (Morph) object;
			return ship.getMorphs().containsValue(morph);
		}

	}

	private static final class IsStemMorphPredicate implements Predicate {
		public boolean evaluate(Object object) {
			return object instanceof StemMorph;
		}
	}

	private static final Logger LOGGER = Logger.getLogger(Ship.class);

	public static final float NEW_MASS_PER_SECOND = 0.1f;

	/** the last id affected to a ship. */
	private static int lastId = 0;

	/** The ship max speed. */
	public static final float MAX_SPEED = 250;
	public static final float SLOWING_DISTANCE = 1000;

	/** The ship position in the world. */
	private Vect3D pos;
	private Vect3D posSpeed;
	private Vect3D posAccel;

	/** The ship orientation in the world. */
	private float rot;
	private float rotSpeed;
	private float rotAccel;

	/** The drag factor. The lower, the more it's dragged. */
	public static final float DRAG_FACTOR = 0.990f;

	/** The rotation drag factor. The lower, the more it's dragged. */
	public static final float ROT_DRAG_FACTOR = 0.5f;

	/** Under that speed, the ship stops completely. */
	public static final float MIN_SPEED = 0.00001f;

	/**
	 * The list of the forces attached to the ship or a constituant of the ship.
	 * This is a force generated by the ship. On the contrary external forces are applied to the ship but not generated by it.
	 * Example: the force generated by a propulsor.
	 * Mathematically, it differs from external forces in that a rotation of the ship leads to a rotation of the force.
	 */
	private final List<Force> ownForceList = new ArrayList<Force>();

	/**
	 * External forces list.
	 * These forces are not generated by the ship but applied to it.
	 * Example: A contact force (collision with an other ship, or explosion)
	 */
	private final List<Force> externalForceList = new ArrayList<Force>();

	/** The list of this ship's morphs. */
	private final Map<Integer, Morph> morphs = new HashMap<Integer, Morph>();

	/** List of active morphs. */
	private final List<Morph> activeMorphList = new ArrayList<Morph>();

	/** List of ships IAs. */
	private final List<IA> iaList = new ArrayList<IA>();

	/** The list of this ship's morphs. */
	// private final Map<Integer, Morph> surroundingSelectedMorphs = new HashMap<Integer, Morph>();

	/** The center of mass of the ship, in world coordinates */
	private Vect3D centerOfMass = new Vect3D(Vect3D.NULL);

	/** Timestamp of last time the ship's position was calculated. */
	private long lastUpdateTS;

	/** The id of the current ship. */
	private int id;

	public Ship(float x, float y, float z) {
		id = ++lastId;

		pos = new Vect3D(x, y, z);
		posSpeed = new Vect3D(0, 0, 0);
		posAccel = new Vect3D(0, 0, 0);
		rot = 0;
		rotSpeed = 0;
		rotAccel = 0;

		// Init lastUpdateTS
		lastUpdateTS = World.getWorld().getCurrentTS();

		// Add a listener to the morph selection model listeners.
		// If one of the morph of this ship is selected, we might have to
		// show virtual morphs (for instance for stem morphs)
		// World.getWorld().getSelectionModel().addSelectionListener(new SelectionAdapter() {
		// /**
		// * When a new morph is selected, add the corresponding virtual morphs
		// * as needed.
		// */
		// @Override
		// public void morphSelected(SelectionEvent selectionEvent) {
		//
		// // Get the list of morph to add surrounding morphs for
		// List<Morph> selectedMorphList = new ArrayList<Morph>(World.getWorld().getSelectionModel().getSelectedMorphs().values());
		// CollectionUtils.filter(selectedMorphList, new IsStemMorphPredicate());
		// CollectionUtils.filter(selectedMorphList, new IsPartOfShipPredicate(Ship.this));
		//
		// LOGGER.debug(selectedMorphList.size());
		//
		// // Add surrounding morphs
		// if (World.getWorld().getSelectionModel().getSelectedShips().values().contains(Ship.this)
		// && !selectedMorphList.isEmpty()) {
		// int i = 0;
		//
		// for (Morph m : MorphUtil.createSurroundingMorphs(selectedMorphList, SurroundingMorph.class)) {
		// if (!getMorphs().containsValue(m)) {
		// getMorphs().put(m.getId(), m);
		// }
		// // if (!surroundingSelectedMorphs.containsValue(m)) {
		// // surroundingSelectedMorphs.put(m.getId(), m);
		// // }
		// }
		// }
		//
		// }
		// });
	}

	public void addMorph(Morph morph) {
		morph.setShip(this);
		getMorphs().put(morph.getId(), morph);
		LOGGER.trace("morph added: " + morph);
		calculateCOM();
		LOGGER.trace("COM(" + centerOfMass + ")");
	}

	public void addMorphs(Collection<? extends Morph> morphs) {
		for (Morph m : morphs) {
			m.setShip(this);
			getMorphs().put(m.getId(), m);
			LOGGER.trace("morph added: " + m);
		}
		calculateCOM();
		LOGGER.trace("COM(" + centerOfMass + ")");
	}

	public void applyForces() {
		posAccel.copy(Vect3D.NULL);
		rotAccel = 0;

		// Calculate com in world
		Vect3D comInWorld = new Vect3D(getCenterOfMassInShip());
		transformShipToWorldCoords(comInWorld);

		// Initialize forceTarget vector
		Vect3D forceTarget = new Vect3D();

		for (Force f : ownForceList) {

			// the acceleration caused by the force is applied to the ship's inertia center.
			Vect3D forceVector = new Vect3D(f.getVector());
			forceVector.rotate(f.getTarget().getRotInShip() + rot); // remove the effect of morph and ship rotation. TODO : check this
			posAccel.add(forceVector);

			// the tangential element of the force generates a rotation of the ship
			// with intensity proportional to the force moment
			forceTarget.copy(f.getTarget().getPosInWorld());
			forceTarget.substract(comInWorld);
			rotAccel += forceTarget.prodVectOnZ(forceVector) / getMorphs().size() * 0.05f;

		}

		// Then we clean the list
		ownForceList.clear();
	}

	/**
	 * Calculates the COM (center of mass).
	 * The COM vector origin is the morph with shipgrid coordinates (0,0)
	 * The current computation is an approximation and assumes that each and every morph in
	 * the ship is at full mass.
	 */
	private void calculateCOM() {
		centerOfMass.copy(Vect3D.NULL);
		for (Morph m : getMorphs().values()) {
			Vect3D weightedPosInShip = new Vect3D(m.getPosInShip());
			weightedPosInShip.normalize(m.getMass());
			centerOfMass.add(weightedPosInShip);
		}
		centerOfMass.normalize(centerOfMass.modulus() / getMorphs().size());
	}

	public List<Morph> getActiveMorphList() {
		return activeMorphList;
	}

	public Vect3D getCenterOfMassInShip() {
		return centerOfMass;
	}

	public List<Force> getExternalForceList() {
		return externalForceList;
	}

	public List<IA> getIAList() {
		return iaList;
	}

	public int getId() {
		return id;
	}

	public Map<Integer, Morph> getMorphs() {
		return morphs;
	}

	/**
	 * Get the neighbours of the provided Morph.
	 * Works in 2D only for now.
	 * @param morph
	 * @return
	 */
	public List<Morph> getNeighbours(Morph morph) {
		List<Morph> neighbours = new ArrayList<Morph>();
		// 1 2
		// 3 4
		// 5 6
		neighbours.add(getShipMorph((int) morph.getShipGridPos().x - 1, (int) morph.getShipGridPos().y + 1, (int) morph.getShipGridPos().z));
		neighbours.add(getShipMorph((int) morph.getShipGridPos().x, (int) morph.getShipGridPos().y + 1, (int) morph.getShipGridPos().z));
		neighbours.add(getShipMorph((int) morph.getShipGridPos().x - 1, (int) morph.getShipGridPos().y, (int) morph.getShipGridPos().z));
		neighbours.add(getShipMorph((int) morph.getShipGridPos().x + 1, (int) morph.getShipGridPos().y, (int) morph.getShipGridPos().z));
		neighbours.add(getShipMorph((int) morph.getShipGridPos().x, (int) morph.getShipGridPos().y - 1, (int) morph.getShipGridPos().z));
		neighbours.add(getShipMorph((int) morph.getShipGridPos().x + 1, (int) morph.getShipGridPos().y - 1, (int) morph.getShipGridPos().z));
		return neighbours;
	}

	public List<Force> getOwnForceList() {
		return ownForceList;
	}

	public Vect3D getPos() {
		return pos;
	}

	public Vect3D getPosAccel() {
		return posAccel;
	}

	// public Map<Integer, Morph> getSurroundingSelectedMorphs() {
	// return surroundingSelectedMorphs;
	// }

	public Vect3D getPosSpeed() {
		return posSpeed;
	}

	public float getRot() {
		return rot;
	}

	public float getRotAccel() {
		return rotAccel;
	}

	public float getRotSpeed() {
		return rotSpeed;
	}

	public Morph getShipMorph(int x, int y, int z) {
		for (Morph m : getMorphs().values()) {
			if (m.getShipGridPos().x == x && m.getShipGridPos().y == y && m.getShipGridPos().z == z) {
				return m;
			}
		}

		return null;
	}

	/**
	 * Looks for the {@link Morph} at the specified position in the ship.
	 * If there is no morph at the given position, it returns null.
	 * @param pos the position in the ship
	 * @return null if there is no morph at the specified location.
	 */
	public Morph getShipMorph(Vect3D pos) {
		return getShipMorph((int) pos.x, (int) pos.y, (int) pos.z);
	}

	public void removeActiveMorph(Morph morph) {
		activeMorphList.remove(morph);
	}

	public void removeMorph(Morph morph) {
		removeActiveMorph(morph);
		getMorphs().remove(morph);
	}

	public void removeMorphs(Collection<? extends Morph> morphs) {
		for (Morph morph : morphs) {
			removeActiveMorph(morph);
			getMorphs().remove(morph.getId());
		}
	}

	public void setCenterOfMassInWorld(Vect3D centerOfMass) {
		this.centerOfMass = centerOfMass;
	}

	public void setRot(float rot) {
		this.rot = rot;
	}

	public void setRotAccel(float rotAccel) {
		this.rotAccel = rotAccel;
	}

	public void setRotSpeed(float rotSpeed) {
		this.rotSpeed = rotSpeed;
	}

	public boolean toggleActiveMorph(Morph morph) {
		if (activeMorphList.contains(morph)) {
			activeMorphList.remove(morph);
			return false;
		}

		activeMorphList.add(morph);
		return true;
	}

	@Override
	public String toString() {
		return "ship:" + pos.toString();
	}

	/**
	 * Transforms the provided vector from ship referential coordinates to world referential coordinates.
	 * @param coords the coordinates in ship referential
	 */
	public void transformShipToWorldCoords(Vect3D coords) {
		coords.rotate(rot);
		coords.add(pos);
	}

	/**
	 * Transforms the provided vector from world referential coordinates to ship referential coordinates.
	 * @param coords the coordinates in world referential
	 */
	public void transformWorldToShipCoords(Vect3D coords) {
		coords.substract(pos);
		coords.rotate(-rot);
	}

	public void update() {
		// timestamp of last update
		float secondsSinceLastUpdate = ((float) World.getWorld().getCurrentTS() - lastUpdateTS) / 1000;
		lastUpdateTS = World.getWorld().getCurrentTS();
		if (secondsSinceLastUpdate == 0f) {
			return;
		}

		applyForces();

		posSpeed.x += posAccel.x * secondsSinceLastUpdate;
		posSpeed.y += posAccel.y * secondsSinceLastUpdate;
		posSpeed.z += posAccel.z * secondsSinceLastUpdate;

		// The drag factor is reduced to take into account the fact that we update the position since last TS and not from a full second ago.
		float reducedDragFactor = 1 - (1 - DRAG_FACTOR) * secondsSinceLastUpdate;
		posSpeed.x = Math.abs(posSpeed.x * reducedDragFactor) > MIN_SPEED ? posSpeed.x * reducedDragFactor : 0;
		posSpeed.y = Math.abs(posSpeed.y * reducedDragFactor) > MIN_SPEED ? posSpeed.y * reducedDragFactor : 0;
		posSpeed.z = Math.abs(posSpeed.z * reducedDragFactor) > MIN_SPEED ? posSpeed.z * reducedDragFactor : 0;

		pos.x += posSpeed.x * secondsSinceLastUpdate;
		pos.y += posSpeed.y * secondsSinceLastUpdate;
		pos.z += posSpeed.z * secondsSinceLastUpdate;

		rotSpeed += rotAccel * secondsSinceLastUpdate;

		// The drag factor is reduced to take into account the fact that we update the position since last TS and not from a full second ago.
		float reducedRotDragFactor = 1 - (1 - ROT_DRAG_FACTOR) * secondsSinceLastUpdate;
		rotSpeed = Math.abs(rotSpeed * reducedRotDragFactor * 0.995f) > MIN_SPEED ? rotSpeed * reducedRotDragFactor * 0.995f : 0;

		rot = (rot + rotSpeed * secondsSinceLastUpdate) % 360;

		updateMorphs();
	}

	private void updateMorphs() {
		// Count the number of suboptimally massive morphs
		int nbMorphsNeedingMass = 0;
		for (Morph m : getMorphs().values()) {
			if (m.getMass() < m.getClass().getAnnotation(MorphInfo.class).maxMass()) {
				nbMorphsNeedingMass++;
			}
		}

		for (Morph m : getMorphs().values()) {

			// Updating the mass of the ship's morph if it's evolving
			if (nbMorphsNeedingMass > 0) {
				m.setMass(Math.min(m.getMass() + NEW_MASS_PER_SECOND / nbMorphsNeedingMass, m.getClass().getAnnotation(MorphInfo.class).maxMass()));
			}

			// Disabling if necessary (not enough mass)
			if (m.getMass() < m.getClass().getAnnotation(MorphInfo.class).disableMass() && !m.isDisabled()) {
				LOGGER.trace("Disabling morph");
				m.disable();
			}

			// Reenable the morph if possible
			if (m.getMass() >= m.getClass().getAnnotation(MorphInfo.class).reEnableMass() && m.isDisabled() && m.getEnergy() > 0) {
				m.enable();
			}

			// Regaining mass if disabled
			if (m.isDisabled() && m.getMass() < m.getClass().getAnnotation(MorphInfo.class).maxMass()) {
				m.setMass(m.getMass() + 0.1f);
			}
		}
	}
}
