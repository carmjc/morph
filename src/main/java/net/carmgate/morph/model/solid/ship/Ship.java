package net.carmgate.morph.model.solid.ship;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.carmgate.morph.ia.IA;
import net.carmgate.morph.model.ModelConstants;
import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.prop.PropulsorsLost;
import net.carmgate.morph.model.physics.Force;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.ship.listener.ShipEvent;
import net.carmgate.morph.model.solid.ship.listener.ShipListener;
import net.carmgate.morph.model.solid.world.World;
import net.carmgate.morph.model.user.User;
import net.carmgate.morph.ui.model.UIModel;

import org.apache.log4j.Logger;

/**
 * TODO : Il faut ajouter un centre d'inertie et modifier les calculs des forces pour gérer le vrai centre d'inertie.
 */
public abstract class Ship {

	private static final Logger LOGGER = Logger.getLogger(Ship.class);

	/** the last id affected to a ship. */
	private static int lastId = 0;

	/** The ship position in the world. */
	private Vect3D pos;
	private Vect3D posSpeed;
	private Vect3D posAccel;

	/** The ship orientation in the world. */
	private float rot;
	private float rotSpeed;
	private float rotAccel;

	/** The drag factor. The lower, the more it's dragged. */
	private float dragFactor = ModelConstants.INITIAL_DRAG_FACTOR;

	/**
	 * The list of the forces attached to the ship or a constituant of the ship.
	 * This is a force generated by the ship. On the contrary external forces are applied to the ship but not generated by it.
	 * Example: the force generated by a propulsor.
	 * Mathematically, it differs from external forces in that a rotation of the ship leads to a rotation of the force.
	 */
	private final List<Force> ownForceList = new ArrayList<Force>();

	/** The list of this ship's morphs. */
	private final Map<Integer, Morph> morphsByIds = new HashMap<Integer, Morph>();

	/** The list of this ship's morphs. */
	private final Map<Vect3D, Morph> morphsByPositionInShip = new HashMap<Vect3D, Morph>();

	/** 
	 * List of active morphs.
	 * TODO Is is still really useful ?
	 */
	private final List<Morph> activeMorphList = new ArrayList<Morph>();

	/** List of ships IAs. */
	private final List<IA> iaList = new ArrayList<IA>();

	/** List of ship's listeners. */
	private final List<ShipListener> shipListeners = new ArrayList<ShipListener>();

	/** The center of mass of the ship, in world coordinates */
	private Vect3D centerOfMass = new Vect3D(Vect3D.NULL);

	/** Timestamp of last time the ship's position was calculated. */
	private long lastUpdateTS;

	/** The id of the current ship. */
	private int id;

	/** Owner of the ship. */
	private User owner;

	public Ship(float x, float y, float z, User owner) {
		id = ++lastId;

		pos = new Vect3D(x, y, z);
		posSpeed = new Vect3D(0, 0, 0);
		posAccel = new Vect3D(0, 0, 0);
		rot = 0;
		rotSpeed = 0;
		rotAccel = 0;

		// Init lastUpdateTS
		lastUpdateTS = World.getWorld().getCurrentTS();

		// assign user
		this.owner = owner;
	}

	/**
	 * Adds the provided morph to the ship at the given position
	 * The "in ship" coordinates axis are the horizontal axis (x axis) 
	 *  and an axis from 7 o'clock to 1 o'clock (y axis)
	 * If there already is a morph at the specified location, it is removed and replaced
	 * by the provided morph.
	 * @param morph the morph to add to the ship
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z unused
	 * @return true if the morph has been properly added to the ship
	 */
	public boolean addMorph(Morph morph, float x, float y, float z) {
		return addMorph(morph, x, y, z, true);
	}

	/**
	 * Adds the provided morph to the ship at the given position
	 * The "in ship" coordinates axis are the horizontal axis (x axis) 
	 *  and an axis from 7 o'clock to 1 o'clock (y axis) 
	 * @param newMorph the morph to add to the ship
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z unused
	 * @param replace true if the morph should replace the morph at the given position if there is one already.
	 * @return true if the morph has been properly added to the ship
	 */
	public boolean addMorph(Morph newMorph, float x, float y, float z, boolean replace) {
		Morph existingMorph = findShipMorph(x, y, z);

		if (replace || existingMorph == null) {

			// if there already is a morph at this position in ship, remove it.
			if (existingMorph != null) {
				removeMorph(existingMorph);
			}

			// Attach the morph to the ship
			newMorph.setShip(this);
			// Insert it in both maps
			getMorphsByIds().put(newMorph.getId(), newMorph);
			getMorphsByPositionInShip().put(new Vect3D(x, y, z), newMorph);

			// set position in ship
			newMorph.setShipGridPos(new Vect3D(x, y, z));

			// Position and rotation in ship and world
			newMorph.setShip(this);
			newMorph.updatePosFromGridPos();

			// Recompute center of mass of the ship
			calculateCOM();

			return true;
		}

		return false;
	}

	/**
	 * Adds the provided morph to the ship at the given position
	 * The "in ship" coordinates axis are the horizontal axis (x axis) 
	 *  and an axis from 7 o'clock to 1 o'clock (y axis)
	 * If there already is a morph at the specified location, it is removed and replaced
	 * by the provided morph.
	 * @param morph the morph to add to the ship
	 * @param posInShip the position in ship 
	 * @return true if the morph has been properly added to the ship
	 */
	public boolean addMorph(Morph morph, Vect3D posInShip) {
		return addMorph(morph, posInShip, true);
	}

	/**
	 * Adds the provided morph to the ship at the given position
	 * The "in ship" coordinates axis are the horizontal axis (x axis) 
	 *  and an axis from 7 o'clock to 1 o'clock (y axis) 
	 * @param newMorph the morph to add to the ship
	 * @param posInShip the coordinate in ship
	 * @param replace true if the morph should replace the morph at the given position if there is one already.
	 * @return true if the morph has been properly added to the ship
	 */
	public boolean addMorph(Morph newMorph, Vect3D posInShip, boolean replace) {
		return addMorph(newMorph, posInShip.x, posInShip.y, posInShip.z, replace);
	}

	public void addMorphs(Collection<? extends Morph> morphs) {
		for (Morph m : morphs) {
			m.setShip(this);
			getMorphsByIds().put(m.getId(), m);
			LOGGER.trace("morph added: " + m);
		}
		calculateCOM();
		LOGGER.trace("COM(" + centerOfMass + ")");
	}

	public void addShipListeners(ShipListener shipListener) {
		shipListeners.add(shipListener);
	}

	private void applyForces() {
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
			rotAccel += forceTarget.prodVectOnZ(forceVector) / getMorphsByIds().size() * 0.05f;

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
		LOGGER.debug("Calculate COM");

		centerOfMass.copy(Vect3D.NULL);
		float shipMass = 0;
		for (Morph m : getMorphsByIds().values()) {
			// Add the weighted pos in ship of the morph
			Vect3D weightedPosInShip = new Vect3D(m.getPosInShip());
			weightedPosInShip.prodScal(m.getMaxMass());
			centerOfMass.add(weightedPosInShip);

			// add the morph's mass to the ship's mass
			shipMass += m.getMaxMass();
		}
		centerOfMass.normalize(centerOfMass.modulus() / shipMass);
	}

	public Morph findShipMorph(float x, float y, float z) {
		return findShipMorph(new Vect3D(x, y, z));
	}

	/**
	 * Looks for the {@link Morph} at the specified position in the ship.
	 * If there is no morph at the given position, it returns null.
	 * @param pos the position in the ship
	 * @return null if there is no morph at the specified location.
	 */
	public Morph findShipMorph(Vect3D pos) {
		return morphsByPositionInShip.get(pos);
	}

	/**
	 * Fire a ship stopped event to all ship listeners
	 * @param shipEvent
	 */
	private void fireShipStopped(ShipEvent shipEvent) {
		for (ShipListener shipListener : shipListeners) {
			shipListener.shipStopped(shipEvent);
		}
	}

	public List<Morph> getActiveMorphList() {
		return activeMorphList;
	}

	public Vect3D getCenterOfMassInShip() {
		return centerOfMass;
	}

	private float getDragFactor() {
		return dragFactor;
	}

	public List<IA> getIAList() {
		return iaList;
	}

	public int getId() {
		return id;
	}

	public Map<Integer, Morph> getMorphsByIds() {
		return morphsByIds;
	}

	public Map<Vect3D, Morph> getMorphsByPositionInShip() {
		return morphsByPositionInShip;
	}

	/**
	 * Get the neighbors of the provided Morph.
	 * Works in 2D only for now.
	 * Should be put into MorphUtil
	 * @param morph
	 * @return
	 */
	public List<Morph> getNeighbors(Morph morph) {
		List<Morph> neighbors = new ArrayList<Morph>();
		// .1 2
		// 3...4
		// .5 6
		neighbors.add(findShipMorph((int) morph.getPosInShipGrid().x - 1, (int) morph.getPosInShipGrid().y + 1, (int) morph.getPosInShipGrid().z));
		neighbors.add(findShipMorph((int) morph.getPosInShipGrid().x, (int) morph.getPosInShipGrid().y + 1, (int) morph.getPosInShipGrid().z));
		neighbors.add(findShipMorph((int) morph.getPosInShipGrid().x - 1, (int) morph.getPosInShipGrid().y, (int) morph.getPosInShipGrid().z));
		neighbors.add(findShipMorph((int) morph.getPosInShipGrid().x + 1, (int) morph.getPosInShipGrid().y, (int) morph.getPosInShipGrid().z));
		neighbors.add(findShipMorph((int) morph.getPosInShipGrid().x, (int) morph.getPosInShipGrid().y - 1, (int) morph.getPosInShipGrid().z));
		neighbors.add(findShipMorph((int) morph.getPosInShipGrid().x + 1, (int) morph.getPosInShipGrid().y - 1, (int) morph.getPosInShipGrid().z));
		return neighbors;
	}

	public User getOwner() {
		return owner;
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

	public void removeActiveMorph(Morph morph) {
		activeMorphList.remove(morph);
	}

	public void removeMorph(Morph morph) {
		UIModel.getUiModel().getSelectionModel().removeMorphFromSelection(morph);
		removeActiveMorph(morph);
		getMorphsByIds().remove(morph.getId());
		getMorphsByPositionInShip().remove(morph.getPosInShipGrid());
	}

	public void removeMorphs(Collection<? extends Morph> morphs) {
		for (Morph morph : morphs) {
			removeMorph(morph);
		}
	}

	public void removeShipListeners(ShipListener shipListener) {
		shipListeners.remove(shipListener);
	}

	/**
	 * For now, this is used only in the {@link PropulsorsLost} behavior.
	 * It is obviously sub-optimal.
	 */
	public void resetDragFactor() {
		dragFactor = ModelConstants.INITIAL_DRAG_FACTOR;
	}

	public void setCenterOfMassInWorld(Vect3D centerOfMass) {
		this.centerOfMass = centerOfMass;
	}

	public void setDragFactor(float dragFactor) {
		this.dragFactor = dragFactor;
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
		LOGGER.trace("posAccel: " + posAccel + ", posSpeed: " + posSpeed);

		// The drag factor is reduced to take into account the fact that we update the position since last TS and not from a full second ago.
		float reducedDragFactor = 1 - (1 - getDragFactor()) * secondsSinceLastUpdate;
		posSpeed.x = Math.abs(posSpeed.x * reducedDragFactor) > ModelConstants.MIN_SPEED ? posSpeed.x * reducedDragFactor : 0;
		posSpeed.y = Math.abs(posSpeed.y * reducedDragFactor) > ModelConstants.MIN_SPEED ? posSpeed.y * reducedDragFactor : 0;
		posSpeed.z = Math.abs(posSpeed.z * reducedDragFactor) > ModelConstants.MIN_SPEED ? posSpeed.z * reducedDragFactor : 0;

		// If this ship is stopped, fire a shipStopped event
		if (posSpeed.x == 0 && posSpeed.y == 0 && posSpeed.z == 0) {
			fireShipStopped(new ShipEvent(this));
		}

		pos.x += posSpeed.x * secondsSinceLastUpdate;
		pos.y += posSpeed.y * secondsSinceLastUpdate;
		pos.z += posSpeed.z * secondsSinceLastUpdate;

		rotSpeed += rotAccel * secondsSinceLastUpdate;

		// The drag factor is reduced to take into account the fact that we update the position since last TS and not from a full second ago.
		float reducedRotDragFactor = 1 - (1 - ModelConstants.ROT_DRAG_FACTOR) * secondsSinceLastUpdate;
		rotSpeed = Math.abs(rotSpeed * reducedRotDragFactor * 0.995f) > ModelConstants.MIN_SPEED ? rotSpeed * reducedRotDragFactor * 0.995f : 0;

		rot = (rot + rotSpeed * secondsSinceLastUpdate) % 360;

		updateMorphs();
	}

	private void updateMorphs() {
		// We duplicate the collection so as to avoid ConcurrentModificationException
		// TODO We should think about fixing this otherwise
		Collection<Morph> morphs = new ArrayList<Morph>(getMorphsByIds().values());

		// Count the number of suboptimally massive morphs
		int nbMorphsNeedingMass = 0;
		for (Morph m : morphs) {
			if (m.getMass() < m.getClass().getAnnotation(MorphInfo.class).maxMass()) {
				nbMorphsNeedingMass++;
			}
		}

		for (Morph m : morphs) {

			// Updating the mass of the ship's morph if it's evolving
			// TODO transform this in a behavior
			if (nbMorphsNeedingMass > 0) {
				float mass = m.getMass();
				m.setMass(Math.min(m.getMass() + ModelConstants.NEW_MASS_PER_SECOND / nbMorphsNeedingMass / World.getWorld().getSinceLastUpdateTS(), m
						.getClass().getAnnotation(MorphInfo.class)
						.maxMass()));
			}

			m.update();
		}
	}
}
