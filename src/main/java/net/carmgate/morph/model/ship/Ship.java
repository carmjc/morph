package net.carmgate.morph.model.ship;

import java.util.ArrayList;
import java.util.List;

import net.carmgate.morph.ia.IA;
import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.World;
import net.carmgate.morph.model.morph.Morph;
import net.carmgate.morph.model.physics.Force;

import org.apache.log4j.Logger;

/**
 * TODO : Il faut ajouter un centre d'inertie et modifier les calculs des forces pour g�rer le vrai centre d'inertie.
 */
public abstract class Ship {

	private static final Logger logger = Logger.getLogger(Ship.class);

	/** The ship position in the world. */
	public Vect3D pos;
	public Vect3D posSpeed;
	public Vect3D posAccel;

	/** The ship orientation in the world. */
	public float rot;
	public float rotSpeed;
	public float rotAccel;

	/** The drag factor. The lower, the more it's dragged. */
	public float dragFactor = 0.990f;

	/** Under that speed, the ship stops completely. */
	public static final float MIN_SPEED = 0.00001f;

	/**
	 * The list of the forces attached to the ship or a constituant of the ship.
	 * This is a force generated by the ship. On the contrary external forces are applied to the ship but not generated by it.
	 * Example: the force generated by a propulsor.
	 * Mathematically, it differs from external forces in that a rotation of the ship leads to a rotation of the force.
	 */
	public List<Force> ownForceList = new ArrayList<Force>();

	/**
	 * External forces list.
	 * These forces are not generated by the ship but applied to it.
	 * Example: A contact force (collision with an other ship, or explosion)
	 */
	public List<Force> externalForceList = new ArrayList<Force>();

	/** The list of this ship's morphs. */
	private final List<Morph> morphList = new ArrayList<Morph>();

	/** The selected morph. */
	private final List<Morph> selectedMorphList = new ArrayList<Morph>();

	/** List of active morphs. */
	private final List<Morph> activeMorphList = new ArrayList<Morph>();

	/** List of ships IAs. */
	private final List<IA> iaList = new ArrayList<IA>();

	/** The center of mass of the ship, in world coordinates */
	private Vect3D centerOfMass = new Vect3D(Vect3D.NULL);

	/** Timestamp of last time the ship's position was calculated. */
	private long lastUpdateTS;

	public Ship(float x, float y, float z) {
		pos = new Vect3D(x, y, z);
		posSpeed = new Vect3D(0, 0, 0);
		posAccel = new Vect3D(0, 0, 0);
		rot = 0;
		rotSpeed = 0;
		rotAccel = 0;

		// Init lastUpdateTS
		lastUpdateTS = World.worldInstance.getCurrentTS();
	}

	public void addMorph(Morph morph) {
		morph.setShip(this);
		morphList.add(morph);
		calculateCOM();
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
			Vect3D forceVector = new Vect3D(f.vector);
			forceVector.rotate(f.target.getRotInShip() + rot); // remove the effect of morph and ship rotation. TODO : check this
			posAccel.add(forceVector);

			// the tangential element of the force generates a rotation of the ship
			// with intensity proportional to the force moment
			forceTarget.copy(f.target.getPosInWorld());
			forceTarget.substract(pos);
			rotAccel += forceTarget.prodVectOnZ(forceVector) / morphList.size() * 0.05f;

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
		for (Morph m : getMorphList()) {
			centerOfMass.add(m.getPosInShip());
		}
		centerOfMass.normalize(centerOfMass.modulus() / getMorphList().size());
	}

	public List<Morph> getActiveMorphList() {
		return activeMorphList;
	}

	public Vect3D getCenterOfMassInShip() {
		return centerOfMass;
	}

	public List<IA> getIAList() {
		return iaList;
	}

	public List<Morph> getMorphList() {
		return morphList;
	}

	/**
	 * Get the neighbours of the provided Morph.
	 * Works in 2D only for now.
	 * @param morph
	 * @return
	 */
	public List<Morph> getNeighbours(Morph morph) {
		List<Morph> neighbours = new ArrayList<Morph>();
		//  1 2
		// 3   4
		//  5 6
		neighbours.add(getShipMorph((int) morph.shipGridPos.x -1, (int) morph.shipGridPos.y + 1, (int) morph.shipGridPos.z));
		neighbours.add(getShipMorph((int) morph.shipGridPos.x, (int) morph.shipGridPos.y + 1, (int) morph.shipGridPos.z));
		neighbours.add(getShipMorph((int) morph.shipGridPos.x - 1, (int) morph.shipGridPos.y, (int) morph.shipGridPos.z));
		neighbours.add(getShipMorph((int) morph.shipGridPos.x + 1, (int) morph.shipGridPos.y, (int) morph.shipGridPos.z));
		neighbours.add(getShipMorph((int) morph.shipGridPos.x, (int) morph.shipGridPos.y - 1, (int) morph.shipGridPos.z));
		neighbours.add(getShipMorph((int) morph.shipGridPos.x + 1, (int) morph.shipGridPos.y - 1, (int) morph.shipGridPos.z));
		return neighbours;
	}

	public List<Morph> getSelectedMorphList() {
		return selectedMorphList;
	}

	public Morph getShipMorph(int x, int y, int z) {
		for (Morph m : morphList) {
			if (m.shipGridPos.x == x && m.shipGridPos.y == y && m.shipGridPos.z == z) {
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

	public void setCenterOfMassInWorld(Vect3D centerOfMass) {
		this.centerOfMass = centerOfMass;
	}

	public void setSelectedMorph(int index) {
		selectedMorphList.clear();
		if (index >= 0 && index < morphList.size()) {
			selectedMorphList.add(morphList.get(index));
		}
	}

	public boolean toggleActiveMorph(Morph morph) {
		if (activeMorphList.contains(morph)) {
			activeMorphList.remove(morph);
			return false;
		}

		activeMorphList.add(morph);
		return true;
	}

	public void toggleSelectedMorph(int index) {
		Morph selectedMorph = morphList.get(index);
		if (selectedMorphList.contains(selectedMorph)) {
			selectedMorphList.remove(selectedMorph);
		} else {
			selectedMorphList.add(selectedMorph);
		}
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
		float secondsSinceLastUpdate = ((float) World.worldInstance.getCurrentTS() - lastUpdateTS) / 1000;
		lastUpdateTS = World.worldInstance.getCurrentTS();
		if (secondsSinceLastUpdate == 0f) {
			return;
		}

		applyForces();

		logger.debug(posAccel.modulus());

		posSpeed.x += posAccel.x * secondsSinceLastUpdate;
		posSpeed.y += posAccel.y * secondsSinceLastUpdate;
		posSpeed.z += posAccel.z * secondsSinceLastUpdate;

		// The drag factor is reduced to take into account the fact that we update the position since last TS and not from a full second ago.
		float reducedDragFactor = 1 - (1 - dragFactor) * secondsSinceLastUpdate;
		posSpeed.x = Math.abs(posSpeed.x * reducedDragFactor) > MIN_SPEED ? posSpeed.x * reducedDragFactor : 0;
		posSpeed.y = Math.abs(posSpeed.y * reducedDragFactor) > MIN_SPEED ? posSpeed.y * reducedDragFactor : 0;
		posSpeed.z = Math.abs(posSpeed.z * reducedDragFactor) > MIN_SPEED ? posSpeed.z * reducedDragFactor : 0;

		pos.x += posSpeed.x * secondsSinceLastUpdate;
		pos.y += posSpeed.y * secondsSinceLastUpdate;
		pos.z += posSpeed.z * secondsSinceLastUpdate;

		rotSpeed += rotAccel * secondsSinceLastUpdate;

		rotSpeed = Math.abs(rotSpeed * reducedDragFactor) > MIN_SPEED ? rotSpeed * reducedDragFactor : 0;

		rot = (rot + rotSpeed * secondsSinceLastUpdate) % 360;

		updateMorphs();
	}

	private void updateMorphs() {
		for (Morph m : morphList) {
			// Position of the morph in the referential centered on the ship (the central one has coords (0, 0).
//			m.getPosInShip().x = m.shipGridPos.x * World.GRID_SIZE + m.shipGridPos.y * World.GRID_SIZE / 2;
//			m.getPosInShip().y = (float) (m.shipGridPos.y * World.GRID_SIZE * Math.sqrt(3)/2);

			// Adding the rotation around the center of the ship
//			m.getPosInShip().rotate(rot);

			// Adding the position of the ship's inertia center
//			m.getPosInShip().add(pos);

			// Disabling if necessary
			if (m.mass < m.disableMass && !m.disabled) {
				logger.debug("Disabling morph");
				m.disabled = true;
			}

			// Reenable the morph if possible
			if (m.mass >= m.reenableMass && m.disabled && m.energy > 0) {
				m.disabled = false;
			}

			// Regaining mass if disabled
			if (m.disabled && m.mass < m.maxMass) {
				m.mass += 0.1;
			}
		}
	}
}
